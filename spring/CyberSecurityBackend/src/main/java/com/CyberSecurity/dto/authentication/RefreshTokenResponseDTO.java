package com.CyberSecurity.dto.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class RefreshTokenResponseDTO {
    private String accessToken;
    private String refreshToken;
}
