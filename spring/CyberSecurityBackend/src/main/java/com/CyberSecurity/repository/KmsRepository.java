package com.CyberSecurity.repository;

import com.CyberSecurity.model.KmsKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface KmsRepository extends JpaRepository<KmsKey, String> {
    Optional<KmsKey> findFirstBy();
}
