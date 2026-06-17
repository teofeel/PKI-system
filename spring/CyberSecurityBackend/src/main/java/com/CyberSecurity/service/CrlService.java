package com.CyberSecurity.service;

import com.CyberSecurity.enums.CertificateType;
import com.CyberSecurity.mapper.CertificateMapper;
import com.CyberSecurity.model.Certificate;
import com.CyberSecurity.repository.CertificateRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrlService {
    private final CertificateRepository certificateRepository;
    private final CertificateService certificateService;
    private final CertificateMapper certificateMapper;

    @Transactional
    public byte[] getCrl() {
        try {
            Certificate rootCa = certificateRepository
                    .findCertificateByType(CertificateType.ROOT)
                    .stream()
                    .filter(c -> !c.isRevoked())
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("No active Root CA found"));

            X509Certificate x509Ca = certificateMapper.toX509(rootCa.getEncoded());

            PrivateKey caPrivateKey = certificateService.getDecryptedPrivateKey(rootCa);

            X500Name caName = new X500Name(x509Ca.getSubjectX500Principal().getName());

            X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(caName, new Date());
            crlBuilder.setNextUpdate(Date.from(Instant.now().plus(24, ChronoUnit.HOURS)));

            List<Certificate> revoked = certificateRepository.findAllByIsRevoked(true);
            for (Certificate cert : revoked) {
                X509Certificate x509 = certificateMapper.toX509(cert.getEncoded());
                crlBuilder.addCRLEntry(
                        new BigInteger(cert.getSerialNumber()),
                        Date.from(cert.getRevokedAt().toInstant(ZoneOffset.UTC)),
                        cert.getRevocationReason().getCode()
                );
            }

            ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                    .setProvider("BC")
                    .build(caPrivateKey);

            byte[] crlBytes = crlBuilder.build(signer).getEncoded();

            return crlBytes;
        } catch (OperatorCreationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
