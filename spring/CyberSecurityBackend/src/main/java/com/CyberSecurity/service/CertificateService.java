package com.CyberSecurity.service;

import com.CyberSecurity.KeyPairGeneratorService;
import com.CyberSecurity.certificate.CertificateGenerator;
import com.CyberSecurity.certificate.data.Issuer;
import com.CyberSecurity.certificate.data.Subject;
import com.CyberSecurity.certificate.dto.CertificateRequestDTO;
import com.CyberSecurity.config.EncryptionConfig;
import com.CyberSecurity.model.Certificate;
import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.model.KmsKey;
import com.CyberSecurity.repository.KmsRepository;
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
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateService {
    private final KeyPairGeneratorService keyPairGeneratorService;
    private final OrganizationService organizationService;
    private final EncryptionService encryptionService;
    private final KmsRepository kmsRepository;
    private final EncryptionConfig encryptionConfig;
    private final DataKeyService dataKeyService;

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

        return CertificateGenerator.generateCertificate(
                subject, issuer, validFrom, validTo,
                new BigInteger(64, new SecureRandom()).toString()
        );
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

        return CertificateGenerator.generateCertificate(
                subject, issuer,
                dto.getValidFrom(), dto.getValidTo(),
                UUID.randomUUID().toString().replace("-", "").substring(0, 16)
        );
    }

    public byte[] getUnwrappedDek(Certificate cert) throws Exception {
        KmsKey kmsRoot = kmsRepository.findFirstBy().orElseThrow(() -> {
            throw new IllegalStateException("KMS Root Key not found");
        });

        byte[] liveKmsKey = encryptionService.decrypt(
                kmsRoot.getEncryptedData().getEncryptedText(),
                kmsRoot.getEncryptedData().getIv(),
                kmsRoot.getEncryptedData().getTag(),
                encryptionConfig.getRootKey(),
                null,
                null
        );

        return encryptionService.decrypt(
                cert.getDataKey().getEncryptedText(),
                cert.getDataKey().getIv(),
                cert.getDataKey().getTag(),
                liveKmsKey,
                null,
                null
        );
    }

    public void encryptAndSetPrivateKey(Certificate cert, PrivateKey privateKey) throws Exception {
        byte[] privateKeyBytes = privateKey.getEncoded();
        byte[] rawDek = encryptionService.generateRandomBytes(32);

        try{
            EncryptedData res = encryptionService.encrypt(privateKeyBytes, rawDek, null, null);
            EncryptedData dekRes = dataKeyService.generateTenantDataKey(rawDek);

            cert.setDataKey(dekRes);
            cert.setEncryptedPrivateKey(res);
        }finally{
            Arrays.fill(rawDek, (byte) 0);
            Arrays.fill(privateKeyBytes, (byte) 0);
        }
    }

}