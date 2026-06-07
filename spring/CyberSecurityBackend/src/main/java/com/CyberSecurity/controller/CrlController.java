package com.CyberSecurity.controller;
import com.CyberSecurity.service.CrlService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class CrlController {
    private final CrlService crlService;

    @GetMapping(value = "/crl", produces = "application/pkix-crl")
    public ResponseEntity<byte[]> getCrl() {
        try {
            byte[] crlBytes = crlService.getCrl();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=revoked.crl")
                    .body(crlBytes);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
