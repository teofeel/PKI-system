package com.CyberSecurity.service;

import com.CyberSecurity.KeyPairGeneratorService;
import com.CyberSecurity.dto.certficate.*;
import com.CyberSecurity.exceptions.OrganizationNotFoundException;
import com.CyberSecurity.model.*;
import com.CyberSecurity.model.Certificate;
import com.CyberSecurity.repository.UserRepository;
import com.CyberSecurity.utils.CertificateGenerator;
import com.CyberSecurity.dto.encryption.EncryptionResult;
import com.CyberSecurity.enums.CertificateType;
import com.CyberSecurity.exceptions.CertificateIssuingException;
import com.CyberSecurity.mapper.CertificateMapper;
import com.CyberSecurity.repository.AccountRepository;
import com.CyberSecurity.enums.AccountType;
import com.CyberSecurity.exceptions.CertificateNotFoundException;
import com.CyberSecurity.repository.CertificateRepository;
import com.CyberSecurity.repository.OrganizationRepository;
import com.CyberSecurity.utils.SecurityUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import java.util.*;


@Service
@RequiredArgsConstructor
public class CertificateService {
    private final KeyPairGeneratorService keyPairGeneratorService;
    private final OrganizationService organizationService;
    private final EncryptionService encryptionService;
    private final AccountRepository accountRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateMapper certificateMapper;
    private final OrganizationRepository organizationRepository;
    private final UserRepository usersRepository;
    private final SecurityUtil securityUtil;
    private final CertificateGenerator certificateGenerator;

    @Transactional
    public X509Certificate issueCertificate(CertificateRequestDTO dto) throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
        if (dto.getValidFrom().after(dto.getValidTo()))     //dto date is not valid
            throw new IllegalArgumentException("ValidTo must be after ValidFrom");

        boolean valid = true;
        if (!dto.getCertificateType().equals(CertificateType.ROOT))
            valid = validate(dto);
        X509Certificate cert = null;
        if (valid) {
            if (dto.getParentCAId() == null && dto.getCertificateType() == CertificateType.ROOT) {
                cert = issueRootCertificate(dto);
            } else {
                Optional<Certificate> parentCAOpt = certificateRepository.findById(Long.parseLong(dto.getParentCAId()));
                if (parentCAOpt.isEmpty())
                    throw new CertificateNotFoundException("Certificate with this id doesn't exist");
                Certificate parentCA = parentCAOpt.get();
                if (CertificateType.INTERMEDIATE.equals(dto.getCertificateType())) {
                    if (CertificateType.INTERMEDIATE.equals(parentCA.getType()) ){
                        if (parentCA.getPathLength() > 0) {
                            cert = issueChildCertificate(dto, parentCA, parentCA.getPathLength() - 1);
                        } else {
                            throw new CertificateIssuingException("Parent certificate can't issue any more intermediate certificates");
                        }
                    } else if (CertificateType.ROOT.equals(parentCA.getType())) {
                        if (dto.getPathLen() == null){
                            cert = issueChildCertificate(dto, parentCA, 0);
                        } else {
                            cert = issueChildCertificate(dto, parentCA, dto.getPathLen());
                        }
                    } else {
                        throw new CertificateIssuingException("Parent Type Is Not Valid");
                    }
                } else {
                    cert = issueChildCertificate(dto, parentCA, null);
                }

            }
        }
        return cert;
    }

    @Transactional
    public boolean validate(CertificateRequestDTO dto) throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
        Optional<Certificate> parentCAOpt = certificateRepository.findById(Long.parseLong(dto.getParentCAId()));

        if (parentCAOpt.isEmpty())
            throw new CertificateNotFoundException("Certificate with this id doesn't exist");   //parentCA doesnt exist

        LocalDate parentValidFrom = parentCAOpt.get().getValidFrom();
        LocalDate parentValidTo = parentCAOpt.get().getValidUntil();

        LocalDate dtoValidFrom = dto.getValidFrom().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate dtoValidTo = dto.getValidTo().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (parentValidTo.isBefore(LocalDate.now()))     //parentCA expired
            throw new DateTimeException("ParentCA expired");

        if (!((dtoValidFrom.isAfter(parentValidFrom) || dtoValidFrom.isEqual(parentValidFrom))
                && dtoValidFrom.isBefore(parentValidTo)     //parentCA wont be valid
                && ((dtoValidTo.isBefore(parentValidTo) || dtoValidTo.isEqual(parentValidTo)))))
            throw new DateTimeException("ParentCA wont be valid");

        verifyDigitalSignature(parentCAOpt.get());
        return true;
    }

    public void verifyDigitalSignature(Certificate current) throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
        while (current != null){
            X509Certificate currentX509 = certificateMapper.toX509(current.getEncoded());  //DIGITAL SIGNATURE
            if (current.getIssuer() == null) {
                currentX509.verify(currentX509.getPublicKey());
                break;
            } else {
                X509Certificate parentX509 = certificateMapper.toX509(current.getIssuer().getEncoded());
                currentX509.verify(parentX509.getPublicKey());
                current = current.getIssuer();
            }
        }
    }

    @Transactional
    public X509Certificate issueRootCertificate(CertificateRequestDTO dto) {
        Organization org = organizationRepository.findByName(dto.getOrganization());
        if(org == null){
            throw new EntityNotFoundException("Organization not found");
        }
      
        KeyPair rootKeyPair = keyPairGeneratorService.generateKeyPair();

        X500Name rootName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, dto.getCommonName())
                .addRDN(BCStyle.O, dto.getOrganization())
                .addRDN(BCStyle.C, dto.getCountry())
                .build();

        Subject subject = new Subject(rootName, rootKeyPair.getPublic());
        Issuer issuer = new Issuer(rootName, rootKeyPair.getPrivate());

        Date validFrom = dto.getValidFrom();
        Date validTo = dto.getValidTo();
        
        X509Certificate cert = CertificateGenerator.generateCertificate(
                subject, issuer, validFrom, validTo,
                new BigInteger(64, new SecureRandom()).toString(), dto.getCertificateType(), null
        );

        try{
            this.createAndSaveCertificate(cert, org, rootKeyPair.getPrivate(), dto.getUserId(), null, null);
        }catch(Exception e){
            e.printStackTrace();
            throw new CertificateIssuingException("Failed to issue certificate");
        }

        return cert;
    }

    @Transactional
    public X509Certificate issueChildCertificate(
            CertificateRequestDTO dto,
            Certificate parentCert,
            Integer pathLen
    ) {
        Organization org = organizationRepository.findByName(dto.getOrganization());
        if(org == null) {
            throw new EntityNotFoundException("Organization not found");
        }

        KeyPair subjectKeyPair = keyPairGeneratorService.generateKeyPair();
        PrivateKey parentPrivateKey;
        try {
            parentPrivateKey = getDecryptedPrivateKey(parentCert);
        }catch (Exception e){
            throw new CertificateIssuingException("Failed to decrypt private key");
        }

        X500Name issuerName = X500Name.getInstance(parentCert.getSubjectNameEncoded());
        Issuer issuer = new Issuer(issuerName, parentPrivateKey);

        X500Name subjectName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, dto.getCommonName())
                .addRDN(BCStyle.SURNAME, dto.getSurname())
                .addRDN(BCStyle.GIVENNAME, dto.getGivenName())
                .addRDN(BCStyle.O, dto.getOrganization())
                .addRDN(BCStyle.OU, dto.getOrganizationUnit())
                .addRDN(BCStyle.C, dto.getCountry())
                .addRDN(BCStyle.E, dto.getEmail())
                .build();

        Subject subject = new Subject(subjectName, subjectKeyPair.getPublic());

        X509Certificate cert = CertificateGenerator.generateCertificate(
                subject, issuer,
                dto.getValidFrom(), dto.getValidTo(),
                new BigInteger(64, new SecureRandom()).toString(),
                dto.getCertificateType(), pathLen
        );


        try{
            this.createAndSaveCertificate(cert, org, subjectKeyPair.getPrivate(), dto.getUserId(), parentCert, pathLen);
        }catch(Exception e){
            throw new CertificateIssuingException("Failed to issue certificate");
        }

        return cert;
    }

    @Transactional
    public PrivateKey getDecryptedPrivateKey(Certificate cert) throws Exception {
        byte[] dataKeyBytes = organizationService.getUnwrappedOrganizationDataKey(cert.getOrganization());

        EncryptedData encryptedData = cert.getEncryptedPrivateKey();
        byte[] decryptedKeyBytes = encryptionService.decrypt(
                encryptedData.getEncryptedText(),
                encryptedData.getIv(),
                encryptedData.getTag(),
                dataKeyBytes,
                null,
                null
        );

        java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(decryptedKeyBytes);
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);

    }

    @Transactional
    public void encryptAndSetPrivateKey(Certificate cert, PrivateKey privateKey) throws Exception {
        byte[] privateKeyBytes = privateKey.getEncoded();
        byte[] dataKeyBytes = organizationService.getUnwrappedOrganizationDataKey(cert.getOrganization());

        EncryptionResult res = encryptionService.encrypt(privateKeyBytes, dataKeyBytes, null, null);

        cert.setEncryptedPrivateKey(
                new EncryptedData(res.getText(), res.getIv(), res.getTag())
        );
    }


    public List<Certificate> fetchByAccount(Long accountID) throws AccountNotFoundException{
        Optional<Account> accOptional = accountRepository.findById(accountID);
        if(accOptional.isEmpty()){
            throw new AccountNotFoundException("ID not found");
        }

        return certificateRepository.findCertificateByOwner(accOptional.get());
    }

    public List<Certificate> fetchByAdmin() {
        return certificateRepository.findCertificateByType(CertificateType.ROOT);
    }

    public List<Certificate> fetchByIssuer(Long issuerID){
        return certificateRepository.findCertificateByIssuerId(issuerID);
    }

    @Transactional
    public Certificate createAndSaveCertificate(X509Certificate cert, Organization org, PrivateKey privateKey,
                                                Long userId, Certificate issuer, Integer pathLen) throws Exception {
        Optional<User> userOptional = usersRepository.findById(userId);
        if (userOptional.isEmpty()) return null;
        User user = userOptional.get();
        Account owner = user.getAccount();
        Certificate certificate = certificateMapper.toCertificate(cert, org, issuer, pathLen);

        certificate.setOwner(owner);

        if(privateKey != null){
            encryptAndSetPrivateKey(certificate, privateKey);
        }

        certificateRepository.save(certificate);

        return certificate;
    }


    public List<ParentCAResponseDTO> getAllParentCA(AccountType type, Long orgId){
        List<Certificate> parentCA;
        if (AccountType.ADMIN.equals(type))
            parentCA = certificateRepository.findParentCAForAdmin();
        else
            parentCA = certificateRepository.findParentCAForCAUser(orgId);
        List<ParentCAResponseDTO> dto = new ArrayList<>();
        for (Certificate c : parentCA){
            dto.add(ParentCAResponseDTO
                    .builder()
                    .id(c.getId())
                    .commonName(c.getCommonName())
                    .build());
        }
        return dto;
    }

    public DownloadCertNoPkDTO exportCertAsPem(Long accID, Long certID)  throws AccessDeniedException{
        Certificate cert = certificateRepository.findById(certID)
                .orElseThrow(() -> new CertificateNotFoundException("Certificate not found"));

        if (!cert.getOwner().getId().equals(accID))
            throw new AccessDeniedException("You don't have permission to download this cert");

        byte[] certEncoded = cert.getEncoded();
        if( certEncoded == null || certEncoded.length == 0 )
            throw new IllegalStateException("Certificate data is empty");

        String pemString = convertCertToPem(certEncoded);
        return DownloadCertNoPkDTO.builder()
                .cert(pemString.getBytes(StandardCharsets.UTF_8))
                .filename(securityUtil.sanitizeFilename(cert.getCommonName())+".pem")
                .mediaType(MediaType.parseMediaType("application/x-pem-file"))
                .build();
    }

    public DownloadCertNoPkDTO exportCertAsCerBytes(Long accID, Long certID) throws AccessDeniedException{
        Certificate cert = certificateRepository.findById(certID)
                .orElseThrow(() -> new CertificateNotFoundException("Certificate not found"));

        if (!cert.getOwner().getId().equals(accID))
            throw new AccessDeniedException("You don't have permission to download this cert");

        byte[] certEncoded = cert.getEncoded();
        if( certEncoded == null || certEncoded.length == 0 )
            throw new IllegalStateException("Certificate data is empty");

        return DownloadCertNoPkDTO.builder()
                .cert(certEncoded)
                .filename(securityUtil.sanitizeFilename(cert.getCommonName())+".cer")
                .mediaType(MediaType.APPLICATION_OCTET_STREAM)
                .build();
    }

    private String convertCertToPem(byte[] certEncoded) {
        Base64.Encoder encoder = Base64.getMimeEncoder(64, new byte[]{'\n'});
        String base64Encoded = encoder.encodeToString(certEncoded);

        return "-----BEGIN CERTIFICATE-----\n" +
                base64Encoded +
                "\n-----END CERTIFICATE-----\n";
    }

    @Transactional
    public byte[] exportToPkcs12(Long id, String keystorePassword) throws Exception {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found with ID: " + id));

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate x509Cert = (X509Certificate) certFactory.generateCertificate(
                new ByteArrayInputStream(certificate.getEncoded())
        );

        byte[] dataKeyBytes = organizationService.getUnwrappedOrganizationDataKey(certificate.getOrganization());

        EncryptedData encryptedData = certificate.getEncryptedPrivateKey();
        byte[] decryptedKeyBytes = encryptionService.decrypt(
                encryptedData.getEncryptedText(),
                encryptedData.getIv(),
                encryptedData.getTag(),
                dataKeyBytes,
                null,
                null
        );
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decryptedKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(x509Cert.getPublicKey().getAlgorithm());
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);

        X509Certificate[] chain = new X509Certificate[]{x509Cert};
        char[] passwordArr = keystorePassword.toCharArray();

        keyStore.setKeyEntry(certificate.getCommonName(), privateKey, passwordArr, chain);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        keyStore.store(outputStream, passwordArr);

        return outputStream.toByteArray();
    }


    public List<CertificateDropdownListDTO> getAllCA(){

        List<Certificate> certs = certificateRepository.findAllIntermediate();
        return certs
                .stream()
                .map((cert) -> certificateMapper.toCertListDTO(cert))
                .toList();
    }

    @Transactional
    public AutogeneratedCertificateResponseDTO issueAutogenerateEECertificate(AutoGenerateRequestDTO data)
            throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException, Exception{

        CertificateRequestDTO certDto = certificateMapper.toCertReqDTO(data);

        if(!this.validate(certDto)) throw new CertificateException("Certificate request is not valid");

        Certificate parentCA = certificateRepository.findById(Long.parseLong(certDto.getParentCAId()))
                .orElseThrow(() -> new CertificateNotFoundException("Certificate with this id doesn't exist"));

        Organization org = organizationRepository.findByName(certDto.getOrganization());
        if (org == null) throw new OrganizationNotFoundException("Organization not found");

        X500Name subjectName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, certDto.getCommonName())
                .addRDN(BCStyle.SURNAME, certDto.getSurname())
                .addRDN(BCStyle.GIVENNAME, certDto.getGivenName())
                .addRDN(BCStyle.O, certDto.getOrganization())
                .addRDN(BCStyle.OU, certDto.getOrganizationUnit())
                .addRDN(BCStyle.C, certDto.getCountry())
                .addRDN(BCStyle.E, certDto.getEmail())
                .build();


        KeyPair subjectKeyPair = keyPairGeneratorService.generateKeyPair();

        X509Certificate x509Cert = this.getX509Cert(parentCA, subjectKeyPair.getPublic(), subjectName,
                certDto.getValidFrom(), certDto.getValidTo(), certDto);


        Certificate cert = this.createAndSaveCertificate(x509Cert, org, null, certDto.getUserId(), parentCA, null);

        java.security.cert.Certificate parentX509 = certificateMapper.toX509(parentCA.getEncoded());
        java.security.cert.Certificate[] chain = new java.security.cert.Certificate[] { x509Cert, parentX509 };

        byte[] certBytes = packToBundle(subjectKeyPair.getPrivate(), chain, data.getExportFormat(), data.getKeystorePassword(), certDto.getCommonName());

        return AutogeneratedCertificateResponseDTO.builder()
                .certificate(certBytes)
                .id(cert.getId())
                .build();
    }

    private byte[] packToBundle(
            PrivateKey privateKey,
            java.security.cert.Certificate[] certChain,
            String format,
            String keystorePass,
            String alias
    ){
        try{
            String keyStoreType = "pkcs12".equalsIgnoreCase(format) ? "PKCS12" : "JKS";
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);

            keyStore.load(null, null);

            char[] passwordChars = keystorePass.toCharArray();

            keyStore.setKeyEntry(alias, privateKey, passwordChars, certChain);

            try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
                keyStore.store(bos, passwordChars);
                return bos.toByteArray();
            }

        }catch(Exception e){
            throw new CertificateIssuingException("Failed to generate secure bundle");
        }
    }

    @Transactional
    public Long issueCsrEECertificate(CsrUploadRequestDTO data, byte[] fileBytes)
        throws Exception {
        Certificate parentCA = certificateRepository.findById(Long.parseLong(data.getParentCAId()))
                .orElseThrow(() -> new CertificateNotFoundException("Certificate with this id doesn't exist"));

        PKCS10CertificationRequest csr = this.convertPemToCert(new String(fileBytes, StandardCharsets.UTF_8));
        JcaPKCS10CertificationRequest jcsCsr = new JcaPKCS10CertificationRequest(csr);

        X500Name subjectName = jcsCsr.getSubject();

        CertificateRequestDTO certDto = certificateMapper.toCertificateRequestDTO(subjectName, data);
        if(!this.validate(certDto)) throw new CertificateException("Certificate request is not valid");

        X509Certificate x509Cert = this.getX509Cert(parentCA, jcsCsr.getPublicKey(), subjectName, data.getValidFrom(), data.getValidTo(), certDto);

        Organization org = organizationRepository.findByName(certificateGenerator.getRDNValue(subjectName, BCStyle.O));
        if (org == null) throw new OrganizationNotFoundException("Organization not found");

        Certificate cert = this.createAndSaveCertificate(x509Cert, org, null, certDto.getUserId(), parentCA, null);

        return cert.getId();
    }

    @Transactional
    public X509Certificate getX509Cert(
            Certificate parentCA,
            PublicKey subjectPublicKey,
            X500Name subjectName,
            Date validFrom, Date validTo,
            CertificateRequestDTO certDto
    ) throws Exception {
        PrivateKey parentPrivateKey = this.getDecryptedPrivateKey(parentCA);

        X500Name issuerName = X500Name.getInstance(parentCA.getSubjectNameEncoded());
        Issuer issuer = new Issuer(issuerName, parentPrivateKey);

        Subject subject = new Subject(subjectName, subjectPublicKey);

        X509Certificate x509Cert = CertificateGenerator.generateCertificate(
                subject, issuer,
                validFrom, validTo,
                new BigInteger(64, new SecureRandom()).toString(),
                certDto.getCertificateType(), null
        );

        if (x509Cert == null) throw new CertificateException("Certificate couldn't be generated");

        return x509Cert;
    }


    private PKCS10CertificationRequest convertPemToCert(String pemContent)
            throws Exception {
        try(PEMParser pemParser = new PEMParser(new StringReader(pemContent))) {
            Object obj = pemParser.readObject();

            if(obj instanceof PKCS10CertificationRequest) {
                return (PKCS10CertificationRequest) obj;
            }else {
                throw new CertificateException("Provided PEM does not contain a valid CSR");
            }
        }
    }
}