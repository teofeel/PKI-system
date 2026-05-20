package com.CyberSecurity.dto.certficate;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CertificateDropdownListDTO {
    private Long id;
    private String commonName;
}
