package com.CyberSecurity.dto.encription;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EncriptionResult {
    private String text;
    private String iv;
    private String tag;
}
