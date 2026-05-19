package com.CyberSecurity.dto.authentication;

import com.CyberSecurity.enums.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String lastname;
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    private String email;
    @Size(min = 8)
    private String password;
    private AccountType accountType;
    private String organization;
    private String publicKey;
}
