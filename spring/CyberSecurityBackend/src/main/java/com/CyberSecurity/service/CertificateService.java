package com.CyberSecurity.service;

import com.CyberSecurity.KeyPairGeneratorService;
import com.CyberSecurity.dto.certficate.*;
import com.CyberSecurity.enums.CertificateApprovalStatus;
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
import java.security.spec.X509EncodedKeySpec;
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
            Certificate savedCert = this.createAndSaveCertificate(cert, org, rootKeyPair.getPrivate(), dto.getUserId(), null, null);
            if (savedCert != null) {
                savedCert.setApprovalStatus(CertificateApprovalStatus.APPROVED);
                savedCert.setApprovedAt(java.time.LocalDateTime.now());
                certificateRepository.save(savedCert);
            }
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
            Certificate savedCert = this.createAndSaveCertificate(cert, org, subjectKeyPair.getPrivate(), dto.getUserId(), parentCert, pathLen);
            if (savedCert != null) {
                savedCert.setApprovalStatus(CertificateApprovalStatus.APPROVED);
                savedCert.setApprovedAt(java.time.LocalDateTime.now());
                certificateRepository.save(savedCert);
            }
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

        validateCertificateApproved(cert);

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

        validateCertificateApproved(cert);

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

    public X509Certificate[] generateCertificateChain(Certificate leafCertificate) throws CertificateException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        List<X509Certificate> certChainList = new ArrayList<>();
        Certificate current = leafCertificate;

        while (current != null) {
            X509Certificate x509 = (X509Certificate) certFactory.generateCertificate(
                    new ByteArrayInputStream(current.getEncoded())
            );
            certChainList.add(x509);
            if (!CertificateType.ROOT.equals(current.getType())) {
                current = current.getIssuer();
            }
            else break;
        }
        return certChainList.toArray(new X509Certificate[0]);
    }

    private byte[] getDecryptedKeyBytes(Certificate certificate) throws Exception {
        byte[] dataKeyBytes = organizationService.getUnwrappedOrganizationDataKey(certificate.getOrganization());
        EncryptedData encryptedData = certificate.getEncryptedPrivateKey();

        return encryptionService.decrypt(
                encryptedData.getEncryptedText(),
                encryptedData.getIv(),
                encryptedData.getTag(),
                dataKeyBytes,
                null,
                null
        );
    }

    @Transactional
    public byte[] exportToPkcs12(Long id, ExportKeystoreDTO dto) throws Exception {
        Certificate leafCertificate = certificateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found with ID: " + id));

        validateCertificateApproved(leafCertificate);

        X509Certificate[] chain = generateCertificateChain(leafCertificate);
        byte[] decryptedKeyBytes = getDecryptedKeyBytes(leafCertificate);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decryptedKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(chain[0].getPublicKey().getAlgorithm());
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        if (!dto.isPkcs12()){
            keyStore = KeyStore.getInstance("JKS");
        }
        keyStore.load(null, null);

        char[] passwordArr = dto.getPassword().toCharArray();
        String alias = leafCertificate.getCommonName();
        if (dto.getAlias() != null && !dto.getAlias().trim().isEmpty())
            alias = dto.getAlias().trim();
        keyStore.setKeyEntry(alias, privateKey, passwordArr, chain);

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

    public void validateCertificateApproved(Certificate cert) {
        if (cert.getApprovalStatus() != CertificateApprovalStatus.APPROVED) {
            throw new IllegalStateException("Certificate is not approved");
        }
    }
}