package com.CyberSecurity.mapper;

import com.CyberSecurity.dto.user.UserDTO;
import com.CyberSecurity.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .name(user.getName())
                .lastname(user.getLastname())
                .organization(user.getOrganization().getName())
                .build();
    }
}
