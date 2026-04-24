package com.CyberSecurity.service;

import com.CyberSecurity.config.EncryptionConfig;
import com.CyberSecurity.dto.encription.EncriptionResult;
import com.CyberSecurity.model.KmsKey;
import com.CyberSecurity.repository.KmsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataKeyService {
    @Autowired
    private EncryptionConfig encryptionConfig;
    @Autowired
    private EncryptionService encryptionService;
    @Autowired
    private KmsRepository kmsRepository;

    public EncriptionResult generateTenantDataKey() throws Exception{

        byte[] dataKey = encryptionService.generateRandomBytes(32);

        KmsKey kmsRoot = kmsRepository.findFirstBy()
                    .orElseThrow(() -> new IllegalStateException("KMS Root Key not found"));

        byte[] decryptedKmsKey = encryptionService.decrypt(
                kmsRoot.getEncryptedData().getEncryptedKey(),
                kmsRoot.getEncryptedData().getIv(),
                kmsRoot.getEncryptedData().getTag(),
                encryptionConfig.getRootKey()
        );

        return encryptionService.encrypt(dataKey, decryptedKmsKey);
    }
}
