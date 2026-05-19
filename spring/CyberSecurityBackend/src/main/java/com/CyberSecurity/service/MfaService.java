package com.CyberSecurity.service;

import com.CyberSecurity.config.EncryptionConfig;
import com.CyberSecurity.dto.encryption.EncryptionResult;
import com.CyberSecurity.dto.mfa.MfaCode;
import com.CyberSecurity.dto.mfa.MfaQrCode;
import com.CyberSecurity.enums.MfaVerifyStatus;
import com.CyberSecurity.exceptions.AccountNotFoundException;
import com.CyberSecurity.exceptions.InvalidCodeException;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.model.KmsKey;
import com.CyberSecurity.repository.AccountRepository;
import com.CyberSecurity.repository.KmsRepository;
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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Service
public class MfaService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private EncryptionService encryptionService;
    @Autowired
    private KmsRepository kmsRepository;
    @Autowired
    private EncryptionConfig encryptionConfig;

    private final DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final ZxingPngQrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

    private Account checkAndExtractAccount(Long accountID) throws AccountNotFoundException{
        Optional<Account> accountOpt = accountRepository.findById(accountID);
        if (accountOpt.isEmpty()) throw new AccountNotFoundException("Account doesn't exist");

        return accountOpt.get();
    }

    private byte[] getActiveMasterKek() throws Exception {
        KmsKey activeKmsKey = kmsRepository.findFirstBy()
                .orElseThrow(() -> new IllegalStateException("No active Master Key found"));

        byte[] envKeyBytes = encryptionConfig.getRootKey();
        EncryptedData data = activeKmsKey.getEncryptedData();

        return encryptionService.decrypt(
                data.getEncryptedText(),
                data.getIv(),
                data.getTag(),
                envKeyBytes,
                null,
                null
        );
    }

    private EncryptedData getSecret2FA(String secret, byte[] rawPlaintextDek) throws Exception {
        EncryptionResult encryptedSecretResult = encryptionService.encrypt(
                secret.getBytes(StandardCharsets.UTF_8),
                rawPlaintextDek,
                null,
                null
        );
        return new EncryptedData(
                encryptedSecretResult.getText(),
                encryptedSecretResult.getIv(),
                encryptedSecretResult.getTag()
        );
    }

    private EncryptedData getDek(byte[] rawPlaintextDek) throws Exception {
        byte[] masterKekBytes = this.getActiveMasterKek();
        EncryptionResult encryptedDekResult = encryptionService.encrypt(
                rawPlaintextDek,
                masterKekBytes,
                null,
                null
        );

        return new EncryptedData(
                encryptedDekResult.getText(),
                encryptedDekResult.getIv(),
                encryptedDekResult.getTag()
        );
    }

    public MfaQrCode generateCode(Long accountID) throws QrGenerationException, AccountNotFoundException, Exception {
        Account acc = this.checkAndExtractAccount(accountID);
        byte[] masterKekBytes = this.getActiveMasterKek();

        String secret = secretGenerator.generate();
        EncryptedData encryptedData = getSecret2FA(secret, masterKekBytes);
        acc.setSecret2FA(encryptedData);

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

    public void verify(Long accountID, MfaCode code, MfaVerifyStatus status) throws AccountNotFoundException, InvalidCodeException, Exception {
        Account acc = this.checkAndExtractAccount(accountID);
        byte[] masterKekBytes = this.getActiveMasterKek();

        EncryptedData encryptedSecret2Fa = acc.getSecret2FA();
        byte[] decryptedSecret2FA = encryptionService.decrypt(
                encryptedSecret2Fa.getEncryptedText(),
                encryptedSecret2Fa.getIv(),
                encryptedSecret2Fa.getTag(),
                masterKekBytes,
                null, null
        );

        String secret2FA = new String(decryptedSecret2FA, StandardCharsets.UTF_8);

        boolean valid = codeVerifier.isValidCode(secret2FA, code.getCode());
        if(!valid) throw new InvalidCodeException("Verification code is invalid");

        if(status.equals(MfaVerifyStatus.SETUP_VERIFY)){
            acc.setUsing2FA(true);
            accountRepository.save(acc);
        }else if (status.equals(MfaVerifyStatus.REMOVE)){
            acc.setUsing2FA(false);
            acc.setSecret2FA(null);

            accountRepository.save(acc);
        }
    }
}
