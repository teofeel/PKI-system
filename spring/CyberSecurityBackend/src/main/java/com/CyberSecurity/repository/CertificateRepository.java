package com.CyberSecurity.repository;

import com.CyberSecurity.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    @Query("SELECT c FROM Certificate c WHERE " +
            "c.type != 'END_ENTITY' AND " +
            "c.isRevoked = false AND " +
            "c.validUntil > CURRENT_DATE ")
    List<Certificate> findParentCAForAdmin();

    @Query("SELECT c FROM Certificate c WHERE " +
            "c.type = 'INTERMEDIATE' AND " +
            "c.isRevoked = false AND " +
            "c.validUntil > CURRENT_DATE AND " +
            "c.organization.id = :orgId")
    List<Certificate> findParentCAForCAUser(@Param("orgId") Long orgId);

}
