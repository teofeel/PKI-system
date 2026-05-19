package com.CyberSecurity.dto.certficate;

import com.CyberSecurity.enums.CertificateType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;

@Data
public class CertificateRequestDTO {

    @NotBlank(message = "Common name is required")
    private String commonName;

    @NotBlank(message = "Surname is required")
    private String surname;

    @NotBlank(message = "Given name is required")
    private String givenName;

    @NotBlank(message = "Organization is required")
    private String organization;

    @NotBlank(message = "Organization unit is required")
    private String organizationUnit;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Valid from date is required")
    @FutureOrPresent(message = "Valid from must not be in the past")
    private Date validFrom;

    @NotNull(message = "Valid to date is required")
    @Future(message = "Valid to must be in the future")
    private Date validTo;

    private String parentCAId;

    private Integer pathLen;

    @NotNull(message = "Certificate type is required")
    private CertificateType certificateType;
}