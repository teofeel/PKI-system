package com.CyberSecurity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class KmsKey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 2048)
    private String encryptedKey;

    // initialization vector, aes requires it for encryption operations
    // cannot decrypt without it
    @Column(nullable = false)
    private String iv;

    // tag to check if someone modified encrypted key manually in db
    @Column(nullable = false)
    private String tag;

    private LocalDateTime creationDate;

    @PrePersist
    protected void onCreate() {
        creationDate = LocalDateTime.now();
    }
}
