package com.CyberSecurity.service;

import com.CyberSecurity.config.EncryptionConfig;
import com.CyberSecurity.dto.encription.EncriptionResult;
import com.CyberSecurity.model.KmsKey;
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
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;


    @Autowired
    private EncryptionConfig encryptionConfig;


    public byte[] generateRandomBytes(int length){
        byte[] key = new byte[length];
        new SecureRandom().nextBytes(key);
        return key;
    }

    private EncriptionResult splitAndFormat(byte[] encryptedBytes, byte[] iv){
        int ciphertextLength = encryptedBytes.length - GCM_TAG_LENGTH;
        byte[] ciphertext = new byte[ciphertextLength];
        byte[] tag = new byte[GCM_TAG_LENGTH];

        System.arraycopy(encryptedBytes, 0, ciphertext, 0, ciphertextLength);
        System.arraycopy(encryptedBytes, ciphertextLength, tag, 0, GCM_TAG_LENGTH);

        return new EncriptionResult(
                HexFormat.of().formatHex(ciphertext),
                HexFormat.of().formatHex(iv),
                HexFormat.of().formatHex(tag)
        );
    }

    public byte[] decrypt(String hexCipherText, String hexIv, String hexTag, byte[] rootKey) throws Exception {
        byte[] ciphertext = HexFormat.of().parseHex(hexCipherText);
        byte[] iv = HexFormat.of().parseHex(hexIv);
        byte[] tag = HexFormat.of().parseHex(hexTag);

        SecretKey secretKey = new SecretKeySpec(rootKey, AES_ALGORITHM);

        byte[] combined = new byte[ciphertext.length + tag.length];
        System.arraycopy(ciphertext, 0, combined, 0, ciphertext.length);
        System.arraycopy(tag, 0, combined, ciphertext.length, tag.length);

        Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH*8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        return cipher.doFinal(combined);
    }

    public EncriptionResult encrypt(byte[] data, byte[] key) throws Exception{
        SecretKey secretKey = new SecretKeySpec(key, AES_ALGORITHM);
        byte[] iv = generateRandomBytes(GCM_IV_LENGTH);

        Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH*8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        byte[] encryptedBytes = cipher.doFinal(data);

        return this.splitAndFormat(encryptedBytes, iv);
    }

}
