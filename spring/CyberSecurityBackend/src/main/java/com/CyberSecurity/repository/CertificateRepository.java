package com.CyberSecurity.repository;

import com.CyberSecurity.enums.CertificateApprovalStatus;
import com.CyberSecurity.enums.CertificateType;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    @Query("SELECT c FROM Certificate c WHERE c.owner = :account AND c.approvalStatus = 'APPROVED'")
    List<Certificate> findCertificateByOwner(@Param("account") Account account);

    @Query("SELECT c FROM Certificate c WHERE c.issuer.id = :issuerID AND c.approvalStatus = 'APPROVED'")
    List<Certificate> findCertificateByIssuerId(@Param("issuerID") Long issuerID);

    @Query("SELECT c FROM Certificate c WHERE c.type = :type AND c.approvalStatus = 'APPROVED'")
    List<Certificate> findCertificateByType(@Param("type") CertificateType type);

    @Query("SELECT c FROM Certificate c WHERE " +
            "c.type != 'END_ENTITY' AND " +
            "c.isRevoked = false AND " +
            "c.validUntil > CURRENT_DATE AND " +
            "c.approvalStatus = 'APPROVED'")
    List<Certificate> findParentCAForAdmin();

    @Query("SELECT c FROM Certificate c WHERE " +
            "c.type = 'INTERMEDIATE' AND " +
            "c.isRevoked = false AND " +
            "c.validUntil > CURRENT_DATE AND " +
            "c.organization.id = :orgId AND " +
            "c.approvalStatus = 'APPROVED'")
    List<Certificate> findParentCAForCAUser(@Param("orgId") Long orgId);

    @Query("SELECT c FROM Certificate c WHERE " +
            "c.type = 'INTERMEDIATE' AND " +
            "c.isRevoked = false AND " +
            "c.validUntil > CURRENT_DATE AND " +
            "c.approvalStatus = 'APPROVED'")
    List<Certificate> findAllIntermediate();

    @Query("SELECT c FROM Certificate c WHERE " +
            "c.approvalStatus = :status " +
            "ORDER BY c.approvedAt ASC")
    List<Certificate> findByApprovalStatus(@Param("status") CertificateApprovalStatus status);

    @Query("SELECT c FROM Certificate c WHERE " +
            "c.approvalStatus = :status AND " +
            "c.organization.id = :orgId " +
            "ORDER BY c.approvedAt ASC")
    List<Certificate> findByApprovalStatusAndOrganization(
            @Param("status") CertificateApprovalStatus status,
            @Param("orgId") Long orgId);

    @Query("SELECT c FROM Certificate c WHERE " +
            "c.approvalStatus = 'PENDING' AND " +
            "c.organization.id = :orgId " +
            "ORDER BY c.id ASC")
    List<Certificate> findPendingByOrganization(@Param("orgId") Long orgId);

    @Query("SELECT c FROM Certificate c WHERE " +
            "c.approvalStatus = 'REJECTED' AND " +
            "c.organization.id = :orgId " +
            "ORDER BY c.approvedAt DESC")
    List<Certificate> findRejectedByOrganization(@Param("orgId") Long orgId);

    @Query("SELECT c FROM Certificate c WHERE c.issuer.id = :issuerId AND c.approvalStatus = :status")
    List<Certificate> findByIssuerAndStatus(@Param("issuerId") Long issuerId, @Param("status") CertificateApprovalStatus status);

}
