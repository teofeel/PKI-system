package com.CyberSecurity.service;

import com.CyberSecurity.dto.authentication.RegisterDTO;
import com.CyberSecurity.enums.AccountType;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.SetPasswordToken;
import com.CyberSecurity.model.User;
import com.CyberSecurity.repository.AccountRepository;
import com.CyberSecurity.repository.OrganizationRepository;
import com.CyberSecurity.repository.SetPasswordTokenRepository;
import com.CyberSecurity.repository.UserRepository;
import com.CyberSecurity.utils.EmailUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class AccountService implements UserDetailsService {
    @Autowired
    private  AccountRepository accountRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  OrganizationRepository organizationRepository;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private  SetPasswordTokenRepository sptRepository;
    @Autowired
    private  EmailUtils emailUtils;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    public User register(RegisterDTO registrationData) {
        if (accountRepository.findAccountByEmail(registrationData.getEmail()) == null) {
            Account newAccount = new Account(registrationData.getEmail(), passwordEncoder.encode(registrationData.getPassword()), registrationData.getAccountType());
            User newUser = new User(registrationData);
            newUser.setOrganization(organizationRepository.findByName(registrationData.getOrganization()));
            newUser.setAccount(newAccount);
            newAccount.setUser(newUser);

            accountRepository.save(newAccount);

            if (registrationData.getAccountType() == AccountType.CA_USER) {
                SetPasswordToken token = new SetPasswordToken(newAccount);
                sptRepository.save(token);
                //emailUtils.sendSetPasswordEmail(token);
            } else {
                emailUtils.sendVerificationEmail(newAccount);
            }
            return newUser;
        }
        return null;
    }
}
