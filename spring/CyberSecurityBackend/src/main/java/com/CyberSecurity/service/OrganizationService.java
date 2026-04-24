package com.CyberSecurity.service;

import com.CyberSecurity.config.EncryptionConfig;
import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.model.KmsKey;
import com.CyberSecurity.model.Organization;
import com.CyberSecurity.repository.KmsRepository;
import com.CyberSecurity.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                encryptionConfig.getRootKey()
        );

        return encryptionService.decrypt(
                org.getDataKey().getEncryptedText(),
                org.getDataKey().getIv(),
                org.getDataKey().getTag(),
                liveKmsKey
        );

    }
}
