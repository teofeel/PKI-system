package com.CyberSecurity.service;

import com.CyberSecurity.config.EncryptionConfig;
import com.CyberSecurity.dto.encryption.EncryptionResult;
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

    public EncryptionResult generateTenantDataKey() throws Exception{

        byte[] dataKey = encryptionService.generateRandomBytes(32);

        KmsKey kmsRoot = kmsRepository.findFirstBy()
                    .orElseThrow(() -> new IllegalStateException("KMS Root Key not found"));

        byte[] decryptedKmsKey = encryptionService.decrypt(
                kmsRoot.getEncryptedData().getEncryptedText(),
                kmsRoot.getEncryptedData().getIv(),
                kmsRoot.getEncryptedData().getTag(),
                encryptionConfig.getRootKey(),
                null,
                null
        );

        return encryptionService.encrypt(dataKey, decryptedKmsKey, null, null);
    }
}
