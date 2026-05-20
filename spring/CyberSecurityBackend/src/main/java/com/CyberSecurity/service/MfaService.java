package com.CyberSecurity.service;

import com.CyberSecurity.config.EncryptionConfig;
import com.CyberSecurity.dto.encryption.EncryptionResult;
import com.CyberSecurity.dto.mfa.MfaCode;
import com.CyberSecurity.dto.mfa.MfaQrCode;
import com.CyberSecurity.enums.AccountType;
import com.CyberSecurity.enums.MfaVerifyStatus;
import com.CyberSecurity.exceptions.AccountNotFoundException;
import com.CyberSecurity.exceptions.InvalidCodeException;
import com.CyberSecurity.model.Account;
import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.model.KmsKey;
import com.CyberSecurity.model.SystemConfiguration;
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
    private OrganizationService organizationService;
    @Autowired
    private SystemConfigurationService systemConfigurationService;

    private final DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final ZxingPngQrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

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

    public MfaQrCode generateCode(Account acc) throws Exception {
        byte[] kekBytes;

        if(acc.getAccountType().equals(AccountType.ADMIN)) kekBytes = systemConfigurationService.getUnwrappedSystemDataKey();
        else kekBytes = organizationService.getUnwrappedOrganizationDataKey(acc.getUser().getOrganization());

        String secret = secretGenerator.generate();
        EncryptedData encryptedData = getSecret2FA(secret, kekBytes);
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

    public void verify(Account acc, MfaCode code, MfaVerifyStatus status) throws Exception {
        byte[] kekBytes;

        if(acc.getAccountType().equals(AccountType.ADMIN)) kekBytes = systemConfigurationService.getUnwrappedSystemDataKey();
        else kekBytes = organizationService.getUnwrappedOrganizationDataKey(acc.getUser().getOrganization());

        EncryptedData encryptedSecret2Fa = acc.getSecret2FA();
        byte[] decryptedSecret2FA = encryptionService.decrypt(
                encryptedSecret2Fa.getEncryptedText(),
                encryptedSecret2Fa.getIv(),
                encryptedSecret2Fa.getTag(),
                kekBytes,
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
