package com.CyberSecurity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class EncryptedData {
    @Column(nullable = false, length = 4096)
    private String encryptedText;

    // initialization vector, aes requires it for encryption operations
    // cannot decrypt without it
    @Column(nullable = false, length=32)
    private String iv;

    // tag to check if someone modified encrypted key manually in db
    @Column(nullable = false, length=32)
    private String tag;
}
