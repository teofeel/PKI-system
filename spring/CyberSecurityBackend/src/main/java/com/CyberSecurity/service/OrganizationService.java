package com.CyberSecurity.service;

import com.CyberSecurity.dto.OrganizationDTO;
import com.CyberSecurity.model.Organization;
import com.CyberSecurity.repository.KmsRepository;
import com.CyberSecurity.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final KmsRepository kmsRepository;

    public List<Organization> getOrganizations(){
        return organizationRepository.findAll();
    }
    public void saveOrganization(OrganizationDTO organizationDTO) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey orgKey = keyGen.generateKey();
    }

}
