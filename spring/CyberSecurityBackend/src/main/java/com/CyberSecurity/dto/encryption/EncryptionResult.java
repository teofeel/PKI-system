package com.CyberSecurity.dto.encryption;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EncryptionResult {
    private String text;
    private String iv;
    private String tag;
}
