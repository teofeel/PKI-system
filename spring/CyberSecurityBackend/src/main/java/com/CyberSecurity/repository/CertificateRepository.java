package com.CyberSecurity.repository;

import com.CyberSecurity.enums.CertificateType;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    List<Certificate> findCertificateByOwner(Account account);

    List<Certificate> findCertificateByIssuerId(Long issuerID);

    List<Certificate> findCertificateByType(CertificateType type);
}
