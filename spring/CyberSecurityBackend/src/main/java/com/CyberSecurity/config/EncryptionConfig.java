package com.CyberSecurity.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.HexFormat;

@Configuration
public class EncryptionConfig {
    @Value("${ROOT_KEY}")
    private String rootKey;

    public byte[] getRootKey() {
        return HexFormat.of().parseHex(rootKey);
    }
}
