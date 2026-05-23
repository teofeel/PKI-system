package com.CyberSecurity.dto.certficate;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CsrUploadRequestDTO {
    @NotBlank(message = "Parent CA is required")
    private String parentCAId;

    @NotBlank(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer duration;

    @NotNull(message = "Valid from date is required")
    @FutureOrPresent(message = "Valid from must not be in the past")
    private Date validFrom;

    @NotNull(message = "Valid to date is required")
    @Future(message = "Valid to must be in the future")
    private Date validTo;
}
