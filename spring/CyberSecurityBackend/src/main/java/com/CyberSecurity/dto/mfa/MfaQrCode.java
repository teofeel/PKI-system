package com.CyberSecurity.dto.mfa;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MfaQrCode {
    private String qrCode;
}
