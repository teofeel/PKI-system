package com.CyberSecurity.controller;

import com.CyberSecurity.certificate.dto.CertificateRequestDTO;
import com.CyberSecurity.certificate.dto.ParentCAResponseDTO;
import com.CyberSecurity.enums.AccountType;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.User;
import com.CyberSecurity.service.CertificateService;
import com.CyberSecurity.service.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.print.attribute.standard.Media;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class CertificateController {
    private final CertificateService certificateService;

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER')")
    @PostMapping(value = "/certificates", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> issueCertificate(@Valid @RequestBody CertificateRequestDTO dto){
        try{
            X509Certificate cert = certificateService.issueCertificate(dto);
            String encoded = Base64.getEncoder().encodeToString(cert.getEncoded());
            return ResponseEntity.ok(encoded);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER')")
    @GetMapping(value = "/get-parent-ca", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ParentCAResponseDTO>> getAllParentCA
            (@AuthenticationPrincipal Account account){
        if (account == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        AccountType type = account.getAccountType();
        Long orgId = account.getUser().getOrganization().getId();
        List<ParentCAResponseDTO> parentCAs = certificateService.getAllParentCA(type, orgId);
        return new ResponseEntity<>(parentCAs, HttpStatus.OK);
    }
}
