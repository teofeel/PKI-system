package com.CyberSecurity.security;

import com.CyberSecurity.config.EncryptionConfig;
import com.CyberSecurity.dto.encryption.EncryptionResult;
import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.model.KmsKey;
import com.CyberSecurity.repository.KmsRepository;
import com.CyberSecurity.service.EncryptionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class KmsInitializer implements ApplicationRunner {
    @Autowired
    private EncryptionConfig encryptionConfig;
    @Autowired
    private KmsRepository kmsRepository;
    @Autowired
    private EncryptionService encryptionService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if(kmsRepository.count() == 0){
            System.out.println("Generating KMS key....");

            byte[] key = encryptionService.generateRandomBytes(32);
            EncryptionResult res = encryptionService.encrypt(key, encryptionConfig.getRootKey(), null, null);

            KmsKey kmsKey = new KmsKey();
            kmsKey.setEncryptedData(new EncryptedData(res.getText(), res.getIv(), res.getTag()));

            kmsRepository.save(kmsKey);
        }else {
            System.out.println("KMS key already exists");
        }
    }
}
