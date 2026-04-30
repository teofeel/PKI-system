package com.CyberSecurity.controller;

import com.CyberSecurity.certificate.dto.CertificateRequestDTO;
import com.CyberSecurity.dto.certficate.GetCertificateListDTO;
import com.CyberSecurity.mapper.CertificateMapper;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.Certificate;
import com.CyberSecurity.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class CertificateController {
    private final CertificateService certificateService;
    private final CertificateMapper certificateMapper;

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
    }
}
