package com.CyberSecurity.certificateGenerator.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParentCAResponseDTO {
    private Long id;
    private String commonName;
}
