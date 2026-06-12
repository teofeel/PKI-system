package com.CyberSecurity.utils;

import com.CyberSecurity.model.KmsKey;
import com.CyberSecurity.service.EncryptionService;
import com.CyberSecurity.service.KmsService;
import com.CyberSecurity.service.OrganizationService;
import com.CyberSecurity.service.SystemConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KmsRotationService {
    private final EncryptionService encryptionService;
    private final SystemConfigurationService systemConfigService;
    private final OrganizationService organizationService;
    private final KmsService kmsService;

    @Scheduled(cron = "0 0 0 1 1,7 *")
    @Transactional
    public void rotateKMS() throws Exception {
        byte[] newKmsKey = encryptionService.generateRandomBytes(32);
        KmsKey newKms = kmsService.generateKmsKey(newKmsKey);
        System.out.println("Starting Master Key Rotation...");

        byte[] decryptedSystemDek = systemConfigService.getUnwrappedSystemDataKey();
        systemConfigService.encryptSystemDek(decryptedSystemDek, newKmsKey);

        System.out.println("Successfully Rewrapped System DEK");

        organizationService.rewrapAllOrganizationDataKeys(newKmsKey);

        kmsService.save(newKms);

        System.out.println("Master Key Rotation Successfully Finished!");
    }
}
