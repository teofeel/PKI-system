package com.CyberSecurity.mapper;


import com.CyberSecurity.dto.certficate.GetCertificateListDTO;
import com.CyberSecurity.model.Certificate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class CertificateMapper {

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

    public List<GetCertificateListDTO> toGetCertificateListDTO(List<Certificate> certificateList){
        return certificateList.stream().map(this::toGetCertificateListDTO).toList();
    }
}
