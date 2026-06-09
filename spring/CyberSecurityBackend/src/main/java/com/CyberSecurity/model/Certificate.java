package com.CyberSecurity.model;

import com.CyberSecurity.enums.CertificateApprovalStatus;
import com.CyberSecurity.enums.CertificateType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateType type;

    @Column(nullable = false)
    private String issuerName;

    @Column(nullable = false)
    private String commonName;

    @Column(nullable = false)
    private String organizationName;

    @Column(nullable = false)
    private String signatureAlgorithm;

    @Column(nullable = false)
    private LocalDate validFrom;

    @Column(nullable = false)
    private LocalDate validUntil;

    @Column(nullable = false)
    private boolean isRevoked = false;

    @Column
    private String revocationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_id")
    private Certificate issuer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column
    private Integer pathLength;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "encryptedText", column = @Column(name = "encrypted_private_key", length = 4096)),
            @AttributeOverride(name = "iv", column = @Column(name = "private_key_iv")),
            @AttributeOverride(name = "tag", column = @Column(name = "private_key_tag"))
    })
    private EncryptedData encryptedPrivateKey;

    @Column(nullable = false)
    private byte[] subjectNameEncoded;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "account_id")
    private Account owner;

    @Column(nullable = false)
    private byte[] encoded;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificateApprovalStatus approvalStatus;

    @Column
    private String approvedBy;

    @Column
    private LocalDateTime approvedAt;

    @Column(length = 1024)
    private String rejectionReason;
}
