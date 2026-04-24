package com.CyberSecurity.service;

import com.CyberSecurity.config.EncryptionConfig;
import com.CyberSecurity.dto.encription.EncriptionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.HexFormat;

@Service
public class EncryptionService {
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;


    @Autowired
    private EncryptionConfig encryptionConfig;


    public byte[] generateRandomBytes(int length){
        byte[] key = new byte[length];

        new SecureRandom().nextBytes(key);
        return key;
    }

    public EncriptionResult encryptWithRootKey(byte[] value) throws Exception{
        byte[] keyBytes = encryptionConfig.getRootKey();
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        byte[] iv = generateRandomBytes(GCM_IV_LENGTH);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH*8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        byte[] encryptedBytes = cipher.doFinal(value);

        int ciphertextLength = encryptedBytes.length - GCM_TAG_LENGTH;
        byte[] ciphertext = new byte[ciphertextLength];
        byte[] tag = new byte[GCM_TAG_LENGTH];
        
        return new EncriptionResult(
                HexFormat.of().formatHex(ciphertext),
                HexFormat.of().formatHex(iv),
                HexFormat.of().formatHex(tag)
        );
    }


}
