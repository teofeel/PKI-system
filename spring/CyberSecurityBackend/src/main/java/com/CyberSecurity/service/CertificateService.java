package com.CyberSecurity.service;

import com.CyberSecurity.KeyPairGeneratorService;
import com.CyberSecurity.certificateGenerator.CertificateGenerator;
import com.CyberSecurity.certificateGenerator.data.Issuer;
import com.CyberSecurity.certificateGenerator.data.Subject;
import com.CyberSecurity.certificateGenerator.dto.CertificateRequestDTO;
import com.CyberSecurity.certificateGenerator.dto.ParentCAResponseDTO;
import com.CyberSecurity.dto.encryption.EncryptionResult;
import com.CyberSecurity.enums.CertificateType;
import com.CyberSecurity.exceptions.CertificateIssuingException;
import com.CyberSecurity.mapper.CertificateMapper;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.repository.AccountRepository;
import com.CyberSecurity.enums.AccountType;
import com.CyberSecurity.exceptions.CertificateNotFoundException;
import com.CyberSecurity.model.Certificate;
import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.model.Organization;
import com.CyberSecurity.repository.CertificateRepository;
import com.CyberSecurity.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.stereotype.Service;
import javax.security.auth.login.AccountNotFoundException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
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
                cert = issueChildCertificate(dto, parentCAOpt.get());
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

        //Date validFrom = new Date();
        //Date validTo = new Date(validFrom.getTime() + 10L * 365 * 24 * 60 * 60 * 1000);

        Date validFrom = dto.getValidFrom();
        Date validTo = dto.getValidTo();
        
        X509Certificate cert = CertificateGenerator.generateCertificate(
                subject, issuer, validFrom, validTo,
                new BigInteger(64, new SecureRandom()).toString(), dto.getCertificateType()
        );

        try{
            this.createAndSaveCertificate(cert, org, rootKeyPair.getPrivate(), dto.getUserId(), null);
        }catch(Exception e){
            e.printStackTrace();
            throw new CertificateIssuingException("Failed to issue certificate");
        }

        return cert;
    }

    @Transactional
    public X509Certificate issueChildCertificate(
            CertificateRequestDTO dto,
            Certificate parentCert
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
                dto.getCertificateType()
        );


        try{
            this.createAndSaveCertificate(cert, org, subjectKeyPair.getPrivate(), dto.getUserId(), parentCert);
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
    public Certificate createAndSaveCertificate(X509Certificate cert, Organization org, PrivateKey privateKey, Long userId, Certificate issuer) throws Exception {
        Optional<Account> owner = accountRepository.findById(userId);
        Certificate certificate = certificateMapper.toCertificate(cert, org, issuer);

        if (owner.isPresent()) certificate.setOwner(owner.get());

        encryptAndSetPrivateKey(certificate, privateKey);
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

}