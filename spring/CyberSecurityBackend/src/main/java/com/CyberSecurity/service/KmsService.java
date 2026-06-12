package com.CyberSecurity.service;

import com.CyberSecurity.config.EncryptionConfig;
import com.CyberSecurity.dto.encryption.EncryptionResult;
import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.model.KmsKey;
import com.CyberSecurity.repository.KmsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KmsService {
    private final EncryptionService encryptionService;
    private final EncryptionConfig encryptionConfig;
    private final KmsRepository kmsRepository;

    public KmsKey generateKmsKey(byte[] key) throws Exception {
        EncryptionResult res = encryptionService.encrypt(
                key,
                encryptionConfig.getRootKey(),
                null,
                null
        );

        KmsKey kmsKey = new KmsKey();
        kmsKey.setEncryptedData(new EncryptedData(res.getText(), res.getIv(), res.getTag()));

        return kmsKey;
    }

    public byte[] getCurrentKmsKey() throws Exception {
        Optional<KmsKey> kmsRootOpt = kmsRepository.findFirstBy();

        if (kmsRootOpt.isEmpty()) {
            return null;
        }

        KmsKey kmsRoot = kmsRootOpt.get();
        byte[] envKeyBytes = encryptionConfig.getRootKey();

        return encryptionService.decrypt(
                kmsRoot.getEncryptedData().getEncryptedText(),
                kmsRoot.getEncryptedData().getIv(),
                kmsRoot.getEncryptedData().getTag(),
                envKeyBytes,
                null,
                null
        );
    }

    public EncryptedData encryptDek(byte[] rawDek, byte[] liveKmsKey) throws Exception{
        EncryptionResult result = encryptionService.encrypt(
                rawDek,
                liveKmsKey,
                null,
                null
        );

        return new EncryptedData(
                result.getText(),
                result.getIv(),
                result.getTag()
        );
    }

    public void save(KmsKey kmsKey) throws Exception {
        kmsRepository.deleteAll();
        kmsRepository.save(kmsKey);
    }


}
