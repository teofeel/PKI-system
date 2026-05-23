package com.CyberSecurity.config;

import com.CyberSecurity.dto.encryption.EncryptionResult;
import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.model.KmsKey;
import com.CyberSecurity.model.SystemConfiguration;
import com.CyberSecurity.repository.KmsRepository;
import com.CyberSecurity.repository.SystemConfigurationRepository;
import com.CyberSecurity.service.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class DatabaseInitializer {

    @Autowired
    private EncryptionConfig encryptionConfig;

    @Bean
    CommandLineRunner initSystemConfig(
            SystemConfigurationRepository repository,
            KmsRepository kmsRepository,
            EncryptionService encryptionService) {

        return args -> {
            try {
                Optional<KmsKey> kmsRootOpt = kmsRepository.findFirstBy();

                if (kmsRootOpt.isEmpty()) {
                    return;
                }

                KmsKey kmsRoot = kmsRootOpt.get();
                byte[] envKeyBytes = encryptionConfig.getRootKey();

                byte[] liveKmsKey = encryptionService.decrypt(
                        kmsRoot.getEncryptedData().getEncryptedText(),
                        kmsRoot.getEncryptedData().getIv(),
                        kmsRoot.getEncryptedData().getTag(),
                        envKeyBytes,
                        null,
                        null
                );

                try {
                    if (!repository.existsById(1L)) {
                        byte[] rawSystemDataKey = encryptionService.generateRandomBytes(32);

                        EncryptionResult result = encryptionService.encrypt(
                                rawSystemDataKey,
                                liveKmsKey,
                                null,
                                null
                        );

                        EncryptedData realKeyData = new EncryptedData(
                                result.getText(),
                                result.getIv(),
                                result.getTag()
                        );

                        SystemConfiguration config = new SystemConfiguration(1L, realKeyData);
                        repository.save(config);
                    }
                } finally {
                    if (liveKmsKey != null) {
                        java.util.Arrays.fill(liveKmsKey, (byte) 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}