package com.CyberSecurity.controller;
import com.CyberSecurity.dto.certficate.*;
import com.CyberSecurity.exceptions.CertificateIssuingException;
import com.CyberSecurity.exceptions.CertificateNotFoundException;
import com.CyberSecurity.exceptions.OrganizationNotFoundException;
import com.CyberSecurity.mapper.CertificateMapper;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.Certificate;
import com.CyberSecurity.enums.AccountType;
import com.CyberSecurity.service.CertificateService;
import com.CyberSecurity.utils.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.login.AccountNotFoundException;
import java.nio.file.AccessDeniedException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class CertificateController {
    private final CertificateService certificateService;
    private final CertificateMapper certificateMapper;
    private final SecurityUtil securityUtil;

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER')")
    @PostMapping(value = "/certificates", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> issueCertificate(@Valid @RequestBody CertificateRequestDTO dto){
        try{
            X509Certificate cert = certificateService.issueCertificate(dto);
            if (cert == null){
                return ResponseEntity.badRequest().body("Certificate not created");
            }
            String encoded = Base64.getEncoder().encodeToString(cert.getEncoded());
            return ResponseEntity.ok(encoded);
        } catch (CertificateIssuingException e) {
            return ResponseEntity.badRequest().body("Request is not valid");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'USER')")
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/certificates/admin")
    public ResponseEntity<List<GetCertificateListDTO>> fetchByAdmin(){
        List<Certificate> certificates = certificateService.fetchByAdmin();
        List<GetCertificateListDTO> certificateDTOs = certificateMapper.toGetCertificateListDTO(certificates);
        return  ResponseEntity.ok(certificateDTOs);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER')")
    @GetMapping(value = "/certificates/{id}/children")
    public ResponseEntity<List<GetCertificateListDTO>> fetchChildren(@PathVariable long id) {
        List<Certificate> certificates = certificateService.fetchByIssuer(id);
        List<GetCertificateListDTO> certificateDTOs = certificateMapper.toGetCertificateListDTO(certificates);
        return ResponseEntity.ok(certificateDTOs);
    }
    
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

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'USER')")
    @GetMapping(value = "/certificates/{id}/export-no-pk")
    public ResponseEntity<byte[]> exportNoPK(
            @AuthenticationPrincipal Account acc,
            @PathVariable long id,
            @RequestParam(value = "format", defaultValue = "pem") String format
    ){
        try{
            DownloadCertNoPkDTO data;

            if("pem".equalsIgnoreCase(format)){
                data = certificateService.exportCertAsPem(acc.getId(), id);

            }else if("cer".equalsIgnoreCase(format)) {
                data = certificateService.exportCertAsCerBytes(acc.getId(), id);
            }else
                throw new IllegalStateException();

            ContentDisposition contentDisposition = ContentDisposition.attachment()
                    .filename(data.getFilename())
                    .build();

            return ResponseEntity.ok()
                    .contentType(data.getMediaType())
                    .headers(headers -> {
                        headers.setContentDisposition(contentDisposition);
                        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
                    })
                    .body(data.getCert());
        }catch(CertificateNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(AccessDeniedException e){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch(IllegalStateException | IllegalArgumentException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'USER')")
    @GetMapping(value = "/certificates/ca")
    public ResponseEntity<Collection<CertificateDropdownListDTO>> getAllCa
            (@AuthenticationPrincipal Account account){
        if (account == null)
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        try{
            List<CertificateDropdownListDTO> dto = certificateService.getAllCA();
            return new ResponseEntity<>(dto, HttpStatus.OK);

        }catch(OrganizationNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'USER')")
    @PostMapping(value = "/certificates/ee/autogenerate")
    public ResponseEntity<byte[]> createEECert(
            @AuthenticationPrincipal Account acc,
            @Valid @RequestBody AutoGenerateRequestDTO data

    ){
        try{
            if(data == null) throw new IllegalStateException("Certificate data is missing");

            AutogeneratedCertificateResponseDTO cert = certificateService.issueAutogenerateEECertificate(data);
            MediaType mediaType;
            String fileExtension;

            if("pkcs12".equalsIgnoreCase(data.getExportFormat())){
                mediaType = MediaType.parseMediaType("application/x-pkcs12");
                fileExtension = ".p12";
            }else if("jks".equalsIgnoreCase(data.getExportFormat())) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
                fileExtension = ".jks";
            }else
                throw new IllegalStateException("Unsupported export format");

            String finalFilename = securityUtil.sanitizeFilename(data.getCommonName()) + fileExtension;
            ContentDisposition contentDisposition = ContentDisposition.attachment()
                    .filename(finalFilename)
                    .build();

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .headers(headers -> {
                        headers.setContentDisposition(contentDisposition);
                        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
                        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition, X-Certificate-Id");
                        headers.add("X-Certificate-Id", cert.getId().toString());
                    })
                    .body(cert.getCertificate());

        } catch(OrganizationNotFoundException | CertificateNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException | IllegalStateException | CertificateException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CA_USER', 'USER')")
    @PostMapping(value = "/certificates/ee/csr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> uploadCertificate(
            @AuthenticationPrincipal Account acc,
            @RequestPart("data") CsrUploadRequestDTO data,
            @RequestPart("file") MultipartFile file
            ){
        try{
            if(data == null || file.isEmpty()) throw new IllegalArgumentException("Data is missing");

            Long certId = certificateService.issueCsrEECertificate(data, file.getBytes());

            return new ResponseEntity<Long>(certId, HttpStatus.OK);

        }catch(OrganizationNotFoundException | CertificateNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(IllegalArgumentException | IllegalStateException | CertificateException e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
