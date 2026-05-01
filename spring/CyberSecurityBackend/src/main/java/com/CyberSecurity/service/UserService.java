package com.CyberSecurity.service;

import com.CyberSecurity.dto.user.CreatedUserDTO;
import com.CyberSecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<CreatedUserDTO> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(CreatedUserDTO::new)
                .toList();
    }
}
