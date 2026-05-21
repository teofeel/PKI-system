package com.CyberSecurity.dto.certficate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

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
}
