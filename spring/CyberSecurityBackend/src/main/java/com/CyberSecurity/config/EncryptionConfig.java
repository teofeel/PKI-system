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
    @Autowired
    private Environment env;


    public byte[] getRootKey() throws IllegalAccessException {
        String key = env.getProperty("ROOT_KEY");

        if (key == null) {
            throw new IllegalAccessException("ROOT KEY must exist before starting application");
        }
        
        return HexFormat.of().parseHex(key);
    }
}
