package com.CyberSecurity.service;

import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.model.SystemConfiguration;
import com.CyberSecurity.repository.SystemConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemConfigurationService {
    private final SystemConfigurationRepository systemConfigurationRepository;
    private final EncryptionService encryptionService;
    private final KmsService kmsService;

    public byte[] getUnwrappedSystemDataKey() throws Exception {
        SystemConfiguration config = systemConfigurationRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException(
                        "Critical Security Error: System configuration record (ID=1) has not been initialized"
                ));

        byte[] liveKmsKey = kmsService.getCurrentKmsKey();

        try{
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

    public void encryptSystemDek(byte[] rawSystemDek, byte[] liveKmsKey) throws Exception {
        EncryptedData realKeyData = kmsService.encryptDek(rawSystemDek, liveKmsKey);
        SystemConfiguration config = new SystemConfiguration(1L, realKeyData);
        systemConfigurationRepository.save(config);
    }
}
