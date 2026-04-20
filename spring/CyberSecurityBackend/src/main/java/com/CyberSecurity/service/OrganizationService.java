package com.CyberSecurity.service;

import com.CyberSecurity.model.Organization;
import com.CyberSecurity.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    public List<Organization> getOrganizations(){
        return organizationRepository.findAll();
    }
}
