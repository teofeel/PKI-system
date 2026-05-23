package com.CyberSecurity.repository;

import com.CyberSecurity.model.KmsKey;
import org.springframework.stereotype.Repository;
import com.CyberSecurity.model.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Long> {
}
