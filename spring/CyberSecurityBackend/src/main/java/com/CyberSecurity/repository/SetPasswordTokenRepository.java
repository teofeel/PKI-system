package com.CyberSecurity.repository;

import com.CyberSecurity.model.SetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetPasswordTokenRepository extends JpaRepository<SetPasswordToken, Long> {
    SetPasswordToken findByToken(String token);
}
