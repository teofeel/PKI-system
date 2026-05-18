package com.CyberSecurity.service;

import com.CyberSecurity.dto.mfa.MfaCode;
import com.CyberSecurity.dto.mfa.MfaQrCode;
import com.CyberSecurity.enums.MfaVerifyStatus;
import com.CyberSecurity.exceptions.AccountNotFoundException;
import com.CyberSecurity.exceptions.InvalidCodeException;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.repository.AccountRepository;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Service
public class MfaService {
    @Autowired
    private AccountRepository accountRepository;
    private final DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final ZxingPngQrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

    private Account checkAndExtractAccount(Long accountID) throws AccountNotFoundException{
        Optional<Account> accountOpt = accountRepository.findById(accountID);
        if (accountOpt.isEmpty()) throw new AccountNotFoundException("Account doesn't exist");

        return accountOpt.get();
    }

    public MfaQrCode generateCode(Long accountID) throws QrGenerationException, AccountNotFoundException {
        Account acc = this.checkAndExtractAccount(accountID);

        String secret = secretGenerator.generate();
        acc.setSecret2FA(secret);
        accountRepository.save(acc);

        QrData qrData = new QrData.Builder()
                .label(acc.getEmail())
                .secret(secret)
                .issuer("PKI IB SIIT")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        byte[] qrCodeBytes = qrGenerator.generate(qrData);
        String base64QrCode = Base64.getEncoder().encodeToString(qrCodeBytes);

        return new MfaQrCode(base64QrCode);
    }

    public void verify(Long accountID, MfaCode code, MfaVerifyStatus status) throws AccountNotFoundException, InvalidCodeException {
        Account acc = this.checkAndExtractAccount(accountID);

        boolean valid = codeVerifier.isValidCode(acc.getSecret2FA(), code.getCode());
        if(!valid) throw new InvalidCodeException("Verification code is invalid");

        if(status.equals(MfaVerifyStatus.SETUP_VERIFY)){
            acc.setUsing2FA(true);
            accountRepository.save(acc);
        }else if (status.equals(MfaVerifyStatus.REMOVE)){
            acc.setUsing2FA(false);
            acc.setSecret2FA("");

            accountRepository.save(acc);
        }
    }
}
