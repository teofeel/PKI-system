package com.CyberSecurity.service;

import com.CyberSecurity.config.EncryptionConfig;
import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.model.KmsKey;
import com.CyberSecurity.model.Organization;
import com.CyberSecurity.model.SystemConfiguration;
import com.CyberSecurity.repository.KmsRepository;
import com.CyberSecurity.repository.SystemConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigurationService {
    @Autowired
    private SystemConfigurationRepository systemConfigurationRepository;
    @Autowired
    private KmsRepository kmsRepository;
    @Autowired
    private EncryptionService encryptionService;
    @Autowired
    private EncryptionConfig encryptionConfig;

    public byte[] getUnwrappedSystemDataKey() throws Exception{
        SystemConfiguration config = systemConfigurationRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException(
                        "Critical Security Error: System configuration record (ID=1) has not been initialized"
                ));

        KmsKey kmsRoot = kmsRepository.findFirstBy().orElseThrow(() -> {
            throw new IllegalStateException("KMS Root Key not found");
        });

        byte[] envKeyBytes = encryptionConfig.getRootKey();
        EncryptedData masterKeyData = kmsRoot.getEncryptedData();

        byte[] liveKmsKey = null;
        try{
            liveKmsKey = encryptionService.decrypt(
                    kmsRoot.getEncryptedData().getEncryptedText(),
                    kmsRoot.getEncryptedData().getIv(),
                    kmsRoot.getEncryptedData().getTag(),
                    envKeyBytes,
                    null,
                    null
            );

            EncryptedData systemDekData = config.getSystemDataKey();
            return encryptionService.decrypt(
                    systemDekData.getEncryptedText(),
                    systemDekData.getIv(),
                    systemDekData.getTag(),
                    liveKmsKey,
                    null,
                    null
            );
        }finally {
            if (liveKmsKey != null) {
                java.util.Arrays.fill(liveKmsKey, (byte) 0);
            }
        }
    }
}
