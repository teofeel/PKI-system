package com.CyberSecurity.exceptions;

public class CertificateIssuingException extends RuntimeException {
    public CertificateIssuingException(String message) {
        super(message);
    }
}