package com.CyberSecurity.dto.certficate;

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
