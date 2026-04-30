package com.CyberSecurity.dto.user;

import com.CyberSecurity.model.Organization;
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
    private Long id;
    private String name;
    private String lastname;
    private String organization;

    public CreatedUserDTO(User registered) {
        id = registered.getId();
        name = registered.getName();
        lastname = registered.getLastname();
        Organization org = registered.getOrganization();
        if (org != null)
            organization = registered.getOrganization().getName();
        else
            organization = "";
    }
}
