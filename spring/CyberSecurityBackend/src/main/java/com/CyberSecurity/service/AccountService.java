package com.CyberSecurity.service;

import com.CyberSecurity.dto.authentication.LoginResponseDTO;
import com.CyberSecurity.dto.authentication.RegisterCAUserDTO;
import com.CyberSecurity.dto.authentication.RegisterDTO;
import com.CyberSecurity.enums.AccountStatus;
import com.CyberSecurity.enums.AccountType;
import com.CyberSecurity.exceptions.AccountNotVerifiedException;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.SetPasswordToken;
import com.CyberSecurity.model.User;
import com.CyberSecurity.repository.AccountRepository;
import com.CyberSecurity.repository.OrganizationRepository;
import com.CyberSecurity.repository.SetPasswordTokenRepository;
import com.CyberSecurity.utils.EmailUtils;
import com.CyberSecurity.utils.JWTService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class AccountService {
    @Autowired
    private JWTService jwtService;
    @Autowired
    private  AccountRepository accountRepository;
    @Autowired
    private  OrganizationRepository organizationRepository;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private  SetPasswordTokenRepository sptRepository;
    @Autowired
    private  EmailUtils emailUtils;

    public LoginResponseDTO login(Account account){
        if (account.getAccountStatus() == AccountStatus.UNVERIFIED)
            throw new AccountNotVerifiedException("Account has not been verified");
        String token = jwtService.generateToken(account);
        return LoginResponseDTO.builder().accessToken(token).build();
    }

    public User register(RegisterDTO registrationData) {
        if (accountRepository.findAccountByEmail(registrationData.getEmail()) == null) {
            Account newAccount = new Account(registrationData.getEmail(), passwordEncoder.encode(registrationData.getPassword()), registrationData.getAccountType());
            User newUser = new User(registrationData);
            newUser.setOrganization(organizationRepository.findByName(registrationData.getOrganization()));
            newUser.setAccount(newAccount);
            newAccount.setUser(newUser);
            accountRepository.save(newAccount);
            emailUtils.sendVerificationEmail(newAccount);
            return newUser;
        }
        return null;
    }

    public User registerCAUser(RegisterCAUserDTO registrationData) {
        if (accountRepository.findAccountByEmail(registrationData.getEmail()) == null) {
            Account newAccount = new Account(registrationData.getEmail(), registrationData.getAccountType());
            User newUser = new User(registrationData);
            newUser.setOrganization(organizationRepository.findByName(registrationData.getOrganization()));
            newUser.setAccount(newAccount);
            newAccount.setUser(newUser);
            accountRepository.save(newAccount);

            SetPasswordToken token = new SetPasswordToken(newAccount);
            sptRepository.save(token);
            emailUtils.sendSetPasswordEmail(token);
            return newUser;
        }
        return null;
    }

    public boolean verifyAccountToken(String token){
        Account acc = accountRepository.findAccountByVerification(token);
        if (acc != null) {
            long now = new Date().getTime();
            if (now - acc.getLastPasswordResetTime().getTime() < 600000)
                acc.setAccountStatus(AccountStatus.VERIFIED);
            acc.setVerification(null);
            accountRepository.save(acc);
            return true;
        }
        return false;
    }

    public boolean verifySetPasswordToken(String token){
        SetPasswordToken spt = sptRepository.findByToken(token);
        return spt != null;
    }

    public boolean setPassword(String token, String password){
        SetPasswordToken spt = sptRepository.findByToken(token);
        if (spt == null)
            return false;
        Account acc = spt.getAccount();
        acc.setPassword(passwordEncoder.encode(password));
        acc.setAccountStatus(AccountStatus.VERIFIED);
        acc.setVerification(null);
        accountRepository.save(acc);
        sptRepository.delete(spt);
        return true;
    }
}
