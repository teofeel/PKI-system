package com.CyberSecurity.model;

import com.CyberSecurity.enums.AccountStatus;
import com.CyberSecurity.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Column
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private User user;

    public Account(String email, String password, AccountType accountType) {
        this.email = email;
        this.password = password;
        this.accountType = accountType;
        this.accountStatus = AccountStatus.UNVERIFIED;
    }
}
