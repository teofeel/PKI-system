package com.CyberSecurity.controller;

import com.CyberSecurity.dto.OrganizationDTO;
import com.CyberSecurity.model.Organization;
import com.CyberSecurity.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value ="/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrganizationController {
    private final OrganizationService organizationService;

    @GetMapping(value = "/organizations")
    ResponseEntity<List<Organization>> getOrganizations(){
        List<Organization> organizations = organizationService.getOrganizations();
        return new ResponseEntity<>(organizations, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/save-organization")
    ResponseEntity<Void> saveOrganization(@RequestBody OrganizationDTO dto) throws Exception {
        organizationService.saveOrganization(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
