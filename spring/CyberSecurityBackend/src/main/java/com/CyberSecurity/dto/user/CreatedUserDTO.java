package com.CyberSecurity.dto.user;

import com.CyberSecurity.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatedUserDTO {
    private Long Id;
    private String name;
    private String lastname;
    private String organization;

    public CreatedUserDTO(User registered) {
        Id = registered.getId();
        name = registered.getName();
        lastname = registered.getLastname();
        organization = registered.getOrganization().getName();
    }
}
