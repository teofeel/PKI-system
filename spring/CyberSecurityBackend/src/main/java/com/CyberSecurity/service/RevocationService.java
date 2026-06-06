package com.CyberSecurity.service;

import com.CyberSecurity.enums.RevocationReason;
import com.CyberSecurity.model.Certificate;
import com.CyberSecurity.repository.CertificateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RevocationService {
    @Autowired
    private CertificateRepository certRepository;

    public void revoke(Long certId, RevocationReason reason) {
        Certificate cert = certRepository.findById(certId)
                .orElseThrow(() -> new EntityNotFoundException("Certificate not found"));

        if (!cert.isRevoked()) {
            cert.setRevoked(true);
            cert.setRevocationReason(reason);
            cert.setRevokedAt(LocalDateTime.now());
            certRepository.save(cert);
        }

        //recursively for all children
        List<Certificate> children = certRepository.findCertificateByIssuerId(certId);
        for (Certificate child: children){
            revoke(child.getId(), reason);
        }
    }
}
