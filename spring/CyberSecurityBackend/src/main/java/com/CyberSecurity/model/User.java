package com.CyberSecurity.model;

import com.CyberSecurity.dto.authentication.RegisterCAUserDTO;
import com.CyberSecurity.dto.authentication.RegisterDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String lastname;

    @ManyToOne
    private Organization organization;

    @Column(columnDefinition = "TEXT")
    private String publicKey;

    @Column(columnDefinition = "TEXT")
    private String secretPayload;

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

    public User(RegisterDTO registrationData) {
        name = registrationData.getName();
        lastname = registrationData.getLastname();
        publicKey = registrationData.getPublicKey();
    }

    public User(RegisterCAUserDTO registrationData) {
        name = registrationData.getName();
        lastname = registrationData.getLastname();
    }
}
