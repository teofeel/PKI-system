package com.CyberSecurity.model;

import com.CyberSecurity.enums.AccountStatus;
import com.CyberSecurity.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Account implements UserDetails {
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

    @Column(unique = true)
    private String verification;

    @Column
    private Timestamp lastPasswordResetTime;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    private User user;

    public Account(String email, String password, AccountType accountType) {
        this.email = email;
        this.password = password;
        this.accountType = accountType;
        this.accountStatus = AccountStatus.UNVERIFIED;
        this.verification = UUID.randomUUID().toString();
        this.lastPasswordResetTime = new Timestamp(new Date().getTime());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(this.accountType);
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
