package com.CyberSecurity.service;

import com.CyberSecurity.KeyPairGeneratorService;
import com.CyberSecurity.certificate.CertificateGenerator;
import com.CyberSecurity.certificate.data.Issuer;
import com.CyberSecurity.certificate.data.Subject;
import com.CyberSecurity.certificate.dto.CertificateRequestDTO;
import com.CyberSecurity.dto.encryption.EncryptionResult;
import com.CyberSecurity.enums.CertificateType;
import com.CyberSecurity.mapper.CertificateMapper;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.Certificate;
import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.repository.AccountRepository;
import com.CyberSecurity.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateService {
    private final KeyPairGeneratorService keyPairGeneratorService;
    private final OrganizationService organizationService;
    private final EncryptionService encryptionService;
    private final AccountRepository accountRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateGenerator certificateGenerator;
    private final CertificateMapper certificateMapper;

    public X509Certificate issueRootCertificate() {
        KeyPair rootKeyPair = keyPairGeneratorService.generateKeyPair();

        X500Name rootName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, "Root CA")
                .addRDN(BCStyle.O, "MyOrg")
                .addRDN(BCStyle.C, "RS")
                .build();

        Subject subject = new Subject(rootName, rootKeyPair.getPublic());
        Issuer issuer = new Issuer(rootName, rootKeyPair.getPrivate());

        Date validFrom = new Date();
        Date validTo = new Date(validFrom.getTime() + 10L * 365 * 24 * 60 * 60 * 1000);

        X509Certificate cert = CertificateGenerator.generateCertificate(
                subject, issuer, validFrom, validTo,
                new BigInteger(64, new SecureRandom()).toString()
        );

        try{
            this.createAndSaveCertificate(cert, rootKeyPair.getPrivate());
        }catch(Exception e){
            return null;
        }

        return cert;
    }

    public X509Certificate issueChildCertificate(
            CertificateRequestDTO dto,
            X509Certificate parentCert,
            java.security.PrivateKey parentPrivateKey
    ) {
        KeyPair subjectKeyPair = keyPairGeneratorService.generateKeyPair();

        X500Name issuerName = X500Name.getInstance(
                parentCert.getSubjectX500Principal().getEncoded()
        );
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
                UUID.randomUUID().toString().replace("-", "").substring(0, 16)
        );

        try{
            this.createAndSaveCertificate(cert, subjectKeyPair.getPrivate());
        }catch(Exception e){
            return null;
        }

        return cert;
    }

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
        return certificateRepository.findAll();
    }

    public List<Certificate> fetchByIssuer(Long issuerID){
        return certificateRepository.findCertificateByIssuerId(issuerID);
    }


    public Certificate createAndSaveCertificate(X509Certificate cert, PrivateKey privateKey) throws Exception {
        Certificate certificate = certificateMapper.toCertificate(cert);

        encryptAndSetPrivateKey(certificate, privateKey);
        certificateRepository.save(certificate);

        return certificate;
    }
}