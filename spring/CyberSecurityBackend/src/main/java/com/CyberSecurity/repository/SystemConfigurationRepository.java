package com.CyberSecurity.repository;

import com.CyberSecurity.model.KmsKey;
import com.CyberSecurity.model.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Long> {
}
