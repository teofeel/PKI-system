package com.CyberSecurity.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.Base64;
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

        byte[] decodedKey = Base64.getDecoder().decode(key);

        if (decodedKey.length != 32) {
            throw new IllegalStateException("ROOT KEY must be 32 characters long, or 256 bits");
        }

        return decodedKey;
    }
}
