package com.CyberSecurity.utils;

import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
    private static final int MAX_FILENAME_LENGTH = 255;
    private static final String FILENAME_PATTERN = "[^a-zA-Z0-9.\\-_]";

    public String sanitizeFilename(String filename) {
        if(filename == null || filename.isBlank()){
            return "certificate";
        }
        String sanitized = filename.replaceAll(FILENAME_PATTERN, "_");

        if (sanitized.contains("..") || sanitized.contains("/") || sanitized.contains("\\")) {
            throw new SecurityException("Invalid filename");
        }

        sanitized = sanitized.replaceAll("^\\.+", "");

        if (sanitized.length() > MAX_FILENAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_FILENAME_LENGTH);
        }

        if (sanitized.isEmpty()) {
            sanitized = "certificate";
        }

        return sanitized;
    }
}
