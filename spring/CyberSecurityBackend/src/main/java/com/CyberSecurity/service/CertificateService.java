package com.CyberSecurity.service;

import com.CyberSecurity.KeyPairGeneratorService;
import com.CyberSecurity.certificate.CertificateGenerator;
import com.CyberSecurity.certificate.data.Issuer;
import com.CyberSecurity.certificate.data.Subject;
import com.CyberSecurity.certificate.dto.CertificateRequestDTO;
import com.CyberSecurity.certificate.dto.ParentCAResponseDTO;
import com.CyberSecurity.dto.encryption.EncryptionResult;
import com.CyberSecurity.enums.AccountType;
import com.CyberSecurity.exceptions.CertificateNotFoundException;
import com.CyberSecurity.model.Certificate;
import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CertificateService {
    private final KeyPairGeneratorService keyPairGeneratorService;
    private final OrganizationService organizationService;
    private final EncryptionService encryptionService;
    private final CertificateRepository certificateRepository;

    public X509Certificate issueCertificate(CertificateRequestDTO dto){
        X509Certificate cert = null;
        if (dto.getParentCAId() == null){
            cert = issueRootCertificate();
        }else{
            Optional<Certificate> parentCAOpt = certificateRepository.findById(Long.parseLong(dto.getParentCAId()));
            if (parentCAOpt.isEmpty()) throw new CertificateNotFoundException("Certificate with this id doesn't exist");
            cert = issueChildCertificate(dto, parentCAOpt.get());
        }
        return cert;
    }

    private X509Certificate issueRootCertificate() {
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

        return CertificateGenerator.generateCertificate(
                subject, issuer, validFrom, validTo,
                new BigInteger(64, new SecureRandom()).toString()
        );
    }

    private X509Certificate issueChildCertificate(
            CertificateRequestDTO dto,
            Certificate parentCert
    ) {
        KeyPair subjectKeyPair = keyPairGeneratorService.generateKeyPair();
        PrivateKey parentPrivateKey;
        try {
            parentPrivateKey = getDecryptedPrivateKey(parentCert);
        }catch (Exception e){
            return null;
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

        return CertificateGenerator.generateCertificate(
                subject, issuer,
                dto.getValidFrom(), dto.getValidTo(),
                UUID.randomUUID().toString().replace("-", "").substring(0, 16)
        );
    }

    private PrivateKey getDecryptedPrivateKey(Certificate cert) throws Exception {
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

    public void encryptAndSetPrivateKey(Certificate cert, PrivateKey privateKey) throws Exception {
        byte[] privateKeyBytes = privateKey.getEncoded();
        byte[] dataKeyBytes = organizationService.getUnwrappedOrganizationDataKey(cert.getOrganization());

        EncryptionResult res = encryptionService.encrypt(privateKeyBytes, dataKeyBytes, null, null);

        cert.setEncryptedPrivateKey(
                new EncryptedData(res.getText(), res.getIv(), res.getTag())
        );
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