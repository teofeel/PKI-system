package com.CyberSecurity.service;

import com.CyberSecurity.model.Account;
import com.CyberSecurity.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetails implements UserDetailsService {
    @Autowired
    private AccountRepository accountRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account acc = accountRepository.findAccountByEmail(username);
        if (acc == null)
            throw new UsernameNotFoundException(String.format("No user found with email '%s'.", username));
        else
            return acc;
    }
}
