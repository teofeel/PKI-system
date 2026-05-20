package com.CyberSecurity.model;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SetPasswordToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String token;

    @Column
    private Timestamp createdAt;

    @ManyToOne
    private Account account;

    public SetPasswordToken(Account account){
        this.token = UUID.randomUUID().toString();
        this.createdAt = new Timestamp(new Date().getTime());
        this.account = account;
    }
}
