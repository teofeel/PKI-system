package com.CyberSecurity.controller;

import com.CyberSecurity.certificate.dto.CertificateRequestDTO;
import com.CyberSecurity.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class CertificateController {
    private final CertificateService certificateService;

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER')")
    @PostMapping(value = "/certificates", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> issueCertificate(@Valid @RequestBody CertificateRequestDTO dto){
        X509Certificate cert = null;
        try {
        if (dto.getParentCAId() == null)
            cert = certificateService.issueRootCertificate();
        else{
            // TODO: preuzeti odgovarajuci CA iz baze za issuera i proslijediti privatni kljuc
        }
        String encoded = Base64.getEncoder().encodeToString(cert.getEncoded());
        return ResponseEntity.ok(encoded);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

}
