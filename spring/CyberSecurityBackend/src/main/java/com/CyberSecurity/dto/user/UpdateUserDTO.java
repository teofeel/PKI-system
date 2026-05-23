package com.CyberSecurity.dto.user;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserDTO {
    private String firstname;
    private String lastname;
}
