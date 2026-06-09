package com.CyberSecurity.dto.certficate;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApproveRejectRequestDTO {
    @NotBlank(message = "Certificate ID is required")
    private Long certificateId;

    private String rejectionReason;
}
