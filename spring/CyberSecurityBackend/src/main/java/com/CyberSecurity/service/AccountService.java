package com.CyberSecurity.service;

import com.CyberSecurity.dto.authentication.*;
import com.CyberSecurity.enums.AccountStatus;
import com.CyberSecurity.exceptions.AccountNotVerifiedException;
import com.CyberSecurity.exceptions.RefreshTokenException;
import com.CyberSecurity.dto.account.AccountDTO;
import com.CyberSecurity.dto.account.GetAccountDetailsDTO;
import com.CyberSecurity.enums.AccountType;
import com.CyberSecurity.exceptions.AccountNotFoundException;
import com.CyberSecurity.mapper.AccountMapper;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.RefreshToken;
import com.CyberSecurity.model.SetPasswordToken;
import com.CyberSecurity.model.User;
import com.CyberSecurity.repository.AccountRepository;
import com.CyberSecurity.repository.OrganizationRepository;
import com.CyberSecurity.repository.RefreshTokenRepository;
import com.CyberSecurity.repository.SetPasswordTokenRepository;
import com.CyberSecurity.utils.EmailUtils;
import com.CyberSecurity.utils.JWTService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class AccountService {
    @Autowired
    private JWTService jwtService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SetPasswordTokenRepository sptRepository;
    @Autowired
    private EmailUtils emailUtils;
    @Autowired
    private AccountMapper accountMapper;



    public LoginResponseDTO login(Account account){
        if (account.getAccountStatus() == AccountStatus.UNVERIFIED)
            throw new AccountNotVerifiedException("Account has not been verified");
        String refreshToken = jwtService.generateRefreshToken(account);
        String accessToken;
        if(account.isUsing2FA()) accessToken = jwtService.generateShortToken(account);
        else accessToken = jwtService.generateToken(account);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .using2FA(account.isUsing2FA())
                .build();
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

    public RefreshTokenResponseDTO refreshToken(String token){
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        if (refreshTokenOpt.isEmpty())
            throw new RefreshTokenException("Refresh token is missing");
        RefreshToken refreshToken = refreshTokenOpt.get();

        if (refreshToken.getExpiryDate().before(new Date())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RefreshTokenException("The refresh token has expired");
        }

        Account account = refreshToken.getAccount();
        return RefreshTokenResponseDTO.builder()
                .accessToken(jwtService.generateAccessToken(account))
                .refreshToken(jwtService.generateRefreshToken(account))
                .build();
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

    @Transactional
    public void logout(RefreshTokenRequestDTO requestDTO){
        refreshTokenRepository.deleteByToken(requestDTO.getRefreshToken());
    }
    
    public GetAccountDetailsDTO getAccount(Long accountID){
        Optional<Account> accountOpt = accountRepository.findById(accountID);

        if (accountOpt.isEmpty()) throw new AccountNotFoundException("Account doesn't exist");

        return accountMapper.toAccountDetailsDTO(accountOpt.get());
    }

}
