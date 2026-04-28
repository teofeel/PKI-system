package com.CyberSecurity.service;

import com.CyberSecurity.config.EncryptionConfig;
import com.CyberSecurity.dto.OrganizationDTO;
import com.CyberSecurity.dto.encryption.EncryptionResult;
import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.model.KmsKey;
import com.CyberSecurity.model.Organization;
import com.CyberSecurity.repository.KmsRepository;
import com.CyberSecurity.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final KmsRepository kmsRepository;
    private final EncryptionService encryptionService;
    private final EncryptionConfig encryptionConfig;

    public List<Organization> getOrganizations(){
        return organizationRepository.findAll();
    }

    public EncryptedData getOrganizationDataKey(Long id) throws Exception{
        Optional<Organization> org = organizationRepository.findById(id);

        if(org.isEmpty()){
            throw new Exception("Organization doesn't exist");
        }

        Organization orgRes = org.get();

        return orgRes.getDataKey();
    }

    public byte[] getUnwrappedOrganizationDataKey(Organization org) throws Exception{
        KmsKey kmsRoot = kmsRepository.findFirstBy().orElseThrow(() -> {
            throw new IllegalStateException("KMS Root Key not found");
        });

        byte[] liveKmsKey = encryptionService.decrypt(
                kmsRoot.getEncryptedData().getEncryptedText(),
                kmsRoot.getEncryptedData().getIv(),
                kmsRoot.getEncryptedData().getTag(),
                encryptionConfig.getRootKey(),
                null,
                null
        );

        return encryptionService.decrypt(
                org.getDataKey().getEncryptedText(),
                org.getDataKey().getIv(),
                org.getDataKey().getTag(),
                liveKmsKey,
                null,
                null
        );

    }

    public void saveOrganization(OrganizationDTO organizationDTO) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        byte[] orgKey = keyGen.generateKey().getEncoded();

        KmsKey kmsRoot = kmsRepository.findFirstBy()
                .orElseThrow(() -> new IllegalStateException("KMS Root Key not found"));

        byte[] liveKmsKey = encryptionService.decrypt(
                kmsRoot.getEncryptedData().getEncryptedText(),
                kmsRoot.getEncryptedData().getIv(),
                kmsRoot.getEncryptedData().getTag(),
                encryptionConfig.getRootKey(),
                null,
                null
        );

        EncryptionResult result = encryptionService.encrypt(
                orgKey,
                liveKmsKey,
                null,
                null
        );

        Organization org = new Organization();
        org.setName(organizationDTO.getName());
        org.setDataKey(toEntity(result));

        organizationRepository.save(org);
    }

    public EncryptedData toEntity(EncryptionResult r) {
        EncryptedData e = new EncryptedData();
        e.setEncryptedText(r.getText());
        e.setIv(r.getIv());
        e.setTag(r.getTag());
        return e;
    }
}