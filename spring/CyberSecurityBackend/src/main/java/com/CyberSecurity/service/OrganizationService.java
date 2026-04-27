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

}
