package com.CyberSecurity.mapper;


import com.CyberSecurity.utils.CertificateGenerator;
import com.CyberSecurity.dto.certficate.GetCertificateListDTO;
import com.CyberSecurity.model.Certificate;
import com.CyberSecurity.model.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CertificateMapper {
    private final CertificateGenerator certificateGenerator;

    public GetCertificateListDTO toGetCertificateListDTO(Certificate certificate) {
        String status;

        if (certificate.isRevoked()) {
            status = "REVOKED";
        } else if (LocalDate.now().isAfter(certificate.getValidUntil())) {
            status = "EXPIRED";
        } else {
            status = "VALID";
        }

        return GetCertificateListDTO.builder()
                .id(certificate.getId())
                .commonName(certificate.getCommonName())
                .type(certificate.getType())
                .validFrom(certificate.getValidFrom())
                .validUntil(certificate.getValidUntil())
                .status(status)
                .build();
    }

    public Certificate toCertificate(X509Certificate cert, Organization org, Certificate issuer, Integer pathLen) throws Exception {
        String commonName = certificateGenerator.x500NameExtractor(cert.getSubjectX500Principal().getName(), "CN");
        String issuerName = cert.getIssuerX500Principal().getName();

        return Certificate.builder()
                .serialNumber(cert.getSerialNumber().toString())
                .commonName(commonName)
                .issuerName(issuerName)
                .organizationName(certificateGenerator.x500NameExtractor(cert.getSubjectX500Principal().getName(), "O"))
                .organization(org)
                .signatureAlgorithm(cert.getSigAlgName())
                .validFrom(cert.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .validUntil(cert.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .isRevoked(false)
                .issuer(issuer)
                .subjectNameEncoded(cert.getSubjectX500Principal().getEncoded())
                .encoded(cert.getEncoded())
                .type(certificateGenerator.determineCertificateType(cert))
                .pathLength(pathLen)
                .encoded(cert.getEncoded())
                .build();
    }

    public List<GetCertificateListDTO> toGetCertificateListDTO(List<Certificate> certificateList){
        return certificateList.stream().map(this::toGetCertificateListDTO).toList();
    }

    public X509Certificate toX509(byte[] encoded) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(encoded)
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid certificate");
        }
    }
}
