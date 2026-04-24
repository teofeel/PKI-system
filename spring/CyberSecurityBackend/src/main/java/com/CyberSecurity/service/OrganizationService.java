package com.CyberSecurity.service;

import com.CyberSecurity.model.EncryptedData;
import com.CyberSecurity.model.Organization;
import com.CyberSecurity.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

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
}
