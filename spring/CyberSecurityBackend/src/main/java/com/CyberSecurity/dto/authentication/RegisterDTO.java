package com.CyberSecurity.dto.authentication;

import com.CyberSecurity.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterDTO {
    private String name;
    private String lastname;
    private String email;
    private String password;
    private AccountType accountType;
    private String organization;
}
