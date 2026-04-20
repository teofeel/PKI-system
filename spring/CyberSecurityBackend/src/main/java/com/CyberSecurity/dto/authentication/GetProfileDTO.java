package com.CyberSecurity.dto.authentication;

import com.CyberSecurity.dto.account.AccountDTO;
import com.CyberSecurity.dto.user.CreatedUserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetProfileDTO {
    private CreatedUserDTO createdUserDTO;
    private AccountDTO accountDTO;
}
