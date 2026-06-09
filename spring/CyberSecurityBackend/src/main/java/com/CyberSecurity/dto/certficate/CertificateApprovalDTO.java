package com.CyberSecurity.dto.certficate;

import com.CyberSecurity.enums.CertificateApprovalStatus;
import com.CyberSecurity.enums.CertificateType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CertificateApprovalDTO {
    private Long id;

    private String commonName;

    private String organizationName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate validFrom;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate validUntil;

    private CertificateType type;

    private String status;

    private CertificateApprovalStatus approvalStatus;

    private String approvedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    private String rejectionReason;
}
