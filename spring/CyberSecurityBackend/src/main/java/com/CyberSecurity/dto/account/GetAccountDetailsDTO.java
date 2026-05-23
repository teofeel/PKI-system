package com.CyberSecurity.dto.account;

import com.CyberSecurity.dto.user.CreatedUserDTO;
import com.CyberSecurity.dto.user.UserDTO;
import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetAccountDetailsDTO {
    private String email;
    private boolean isUsing2FA;
    private UserDTO user;
}
