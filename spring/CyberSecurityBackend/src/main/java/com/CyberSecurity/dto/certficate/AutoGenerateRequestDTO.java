package com.CyberSecurity.dto.certficate;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutoGenerateRequestDTO {
    @NotBlank(message = "Common name is required")
    private String commonName;

    @NotBlank(message = "Given name is required")
    private String givenName;

    @NotBlank(message = "Surname is required")
    private String surname;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Organization is required")
    private String organization;

    @NotBlank(message = "Organization unit is required")
    private String organizationUnit;

    @NotBlank(message = "Country is required")
    private String country;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Valid from date is required")
    @FutureOrPresent(message = "Valid from must not be in the past")
    private Date validFrom;

    @NotNull(message = "Valid to date is required")
    @Future(message = "Valid to must be in the future")
    private Date validTo;

    private String parentCAId;

    @NotBlank(message = "Export format is required")
    private String exportFormat;

    @NotBlank(message = "Keystore password is required")
    private String keystorePassword;

    private String alias;
}
