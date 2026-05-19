package com.CyberSecurity.service;

import com.CyberSecurity.dto.user.CreatedUserDTO;
import com.CyberSecurity.dto.user.UpdateUserDTO;
import com.CyberSecurity.exceptions.AccountNotFoundException;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.User;
import com.CyberSecurity.repository.AccountRepository;
import com.CyberSecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public List<CreatedUserDTO> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(CreatedUserDTO::new)
                .toList();
    }

    public void updateUser(Long accountID, UpdateUserDTO data){
        Optional<Account> accountOpt = accountRepository.findById(accountID);
        if (accountOpt.isEmpty()) throw new AccountNotFoundException("Account doesn't exist");

        User user = accountOpt.get().getUser();
        user.setName(data.getFirstname());
        user.setLastname(data.getLastname());

        userRepository.save(user);
    }
}
