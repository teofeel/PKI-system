package com.CyberSecurity.repository;

import com.CyberSecurity.model.KmsKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KmsRepository extends JpaRepository<KmsKey, String> {
}
