package com.CyberSecurity.repository;

import com.CyberSecurity.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Organization findByName(String organization);
    Stream<Organization> streamAllBy();
}
