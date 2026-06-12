package com.CyberSecurity.config;

import com.CyberSecurity.repository.SystemConfigurationRepository;
import com.CyberSecurity.service.EncryptionService;
import com.CyberSecurity.service.KmsService;
import com.CyberSecurity.service.SystemConfigurationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseInitializer {

    @Bean
    CommandLineRunner initSystemConfig(
            SystemConfigurationRepository repository,
            EncryptionService encryptionService,
            SystemConfigurationService systemConfigurationService,
            KmsService kmsService) {

        return args -> {
            try {
                byte[] liveKmsKey = kmsService.getCurrentKmsKey();

                if (liveKmsKey == null) return;

                if (!repository.existsById(1L)) {
                    byte[] rawSystemDataKey = encryptionService.generateRandomBytes(32);

                    systemConfigurationService.encryptSystemDek(rawSystemDataKey, liveKmsKey);
                }
                java.util.Arrays.fill(liveKmsKey, (byte) 0);

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}