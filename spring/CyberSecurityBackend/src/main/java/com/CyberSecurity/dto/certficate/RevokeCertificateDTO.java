package com.CyberSecurity.dto.certficate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RevokeCertificateDTO {
    public Long id;
    public String revokeReason;
    public String revokeNote;
}
