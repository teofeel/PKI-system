package com.CyberSecurity.controller;

import com.CyberSecurity.certificate.dto.CertificateRequestDTO;

import com.CyberSecurity.dto.certficate.GetCertificateListDTO;
import com.CyberSecurity.mapper.CertificateMapper;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.Certificate;
import com.CyberSecurity.certificate.dto.ParentCAResponseDTO;
import com.CyberSecurity.enums.AccountType;
import com.CyberSecurity.model.User;

import com.CyberSecurity.service.CertificateService;
import com.CyberSecurity.service.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Base64;
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
    private final CertificateMapper certificateMapper;

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


    @GetMapping(value = "/accounts/{id}/certificates")
    public ResponseEntity<List<GetCertificateListDTO>> fetchByAccount(@PathVariable long id){
        try{
            List<Certificate> certificates = certificateService.fetchByAccount(id);
            List<GetCertificateListDTO> certificateDTOs = certificateMapper.toGetCertificateListDTO(certificates);
            return ResponseEntity.ok(certificateDTOs);
        }catch(AccountNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/certificates/admin")
    public ResponseEntity<List<GetCertificateListDTO>> fetchByAdmin(){
        List<Certificate> certificates = certificateService.fetchByAdmin();
        List<GetCertificateListDTO> certificateDTOs = certificateMapper.toGetCertificateListDTO(certificates);
        return  ResponseEntity.ok(certificateDTOs);
    }

    @GetMapping(value = "/certificates/{id}/children")
    public ResponseEntity<List<GetCertificateListDTO>> fetchChildren(@PathVariable long id){
        List<Certificate> certificates = certificateService.fetchByIssuer(id);
        List<GetCertificateListDTO> certificateDTOs = certificateMapper.toGetCertificateListDTO(certificates);
        return  ResponseEntity.ok(certificateDTOs);

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER')")
    @GetMapping(value = "/get-parent-ca", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ParentCAResponseDTO>> getAllParentCA
            (@AuthenticationPrincipal Account account){
        if (account == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        AccountType type = account.getAccountType();
        Long orgId = null;
        if (!AccountType.ADMIN.equals(type)){
            orgId = account.getUser().getOrganization().getId();
        }
        List<ParentCAResponseDTO> parentCAs = certificateService.getAllParentCA(type, orgId);
        return new ResponseEntity<>(parentCAs, HttpStatus.OK);

    }
}
