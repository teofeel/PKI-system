package com.CyberSecurity.service;

import com.CyberSecurity.config.EncryptionConfig;
import com.CyberSecurity.model.EncryptedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class EncryptionService {
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    public byte[] generateRandomBytes(int length){
        byte[] key = new byte[length];
        new SecureRandom().nextBytes(key);
        return key;
    }

    private EncryptedData splitAndFormat(byte[] encryptedBytes, byte[] iv, String algo){
        if (!algo.contains("/GCM/"))
            return new EncryptedData(
                    Base64.getEncoder().encodeToString(encryptedBytes),
                    Base64.getEncoder().encodeToString(iv),
                    null
            );

        int ciphertextLength = encryptedBytes.length - GCM_TAG_LENGTH;
        byte[] ciphertext = new byte[ciphertextLength];
        byte[] tag = new byte[GCM_TAG_LENGTH];

        System.arraycopy(encryptedBytes, 0, ciphertext, 0, ciphertextLength);
        System.arraycopy(encryptedBytes, ciphertextLength, tag, 0, GCM_TAG_LENGTH);

        return new EncryptedData(
                Base64.getEncoder().encodeToString(ciphertext),
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(tag)
        );
    }


    private AlgorithmParameterSpec getSpec(String fullAlgo, byte[] iv) {
        if (fullAlgo.contains("/GCM/")) {
            return new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        }
        return new IvParameterSpec(iv);
    }


    private Cipher initCipher(int mode, byte[] keyBytes, byte[] iv, String aad, String algo) throws Exception{
        String baseAlgo = algo.split("/")[0];

        SecretKey secretKey = new SecretKeySpec(keyBytes, baseAlgo);

        Cipher cipher = Cipher.getInstance(algo);
        AlgorithmParameterSpec spec = this.getSpec(algo, iv);
        cipher.init(mode, secretKey, spec);

        if(aad != null && !aad.isEmpty()){
            cipher.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
        }

        return cipher;
    }

    public byte[] decrypt(String base64CipherText, String base64Iv, String base64Tag, byte[] rootKey, String aad, String algo) throws Exception {
        String fullAlgo = algo!=null && !algo.isEmpty() ? algo : AES_GCM_ALGORITHM;

        byte[] ciphertext = Base64.getDecoder().decode(base64CipherText);
        byte[] iv = Base64.getDecoder().decode(base64Iv);

        byte[] combined;

        if(fullAlgo.contains("/GCM/")){
            byte[] tag = Base64.getDecoder().decode(base64Tag);
            combined = new byte[ciphertext.length + tag.length];

            System.arraycopy(ciphertext, 0, combined, 0, ciphertext.length);
            System.arraycopy(tag, 0, combined, ciphertext.length, tag.length);
        }else{
            combined = ciphertext;
        }

        Cipher cipher = this.initCipher(Cipher.DECRYPT_MODE, rootKey, iv, aad, fullAlgo);

        return cipher.doFinal(combined);
    }

    public EncryptedData encrypt(byte[] data, byte[] key, String aad, String algo) throws Exception{
        String fullAlgo = algo!=null && !algo.isEmpty() ? algo : AES_GCM_ALGORITHM;
        byte[] iv = generateRandomBytes(GCM_IV_LENGTH);

        Cipher cipher = this.initCipher(Cipher.ENCRYPT_MODE, key, iv, aad, fullAlgo);

        byte[] encryptedBytes = cipher.doFinal(data);

        return this.splitAndFormat(encryptedBytes, iv, fullAlgo);
    }

}
