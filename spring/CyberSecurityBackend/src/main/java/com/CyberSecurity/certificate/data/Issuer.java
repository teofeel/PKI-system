package com.CyberSecurity.certificate.data;

import lombok.*;
import org.bouncycastle.asn1.x500.X500Name;

import java.security.PrivateKey;
import java.security.PublicKey;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Issuer {
    private X500Name x500Name;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public Issuer(X500Name rootName, PrivateKey aPrivate) {
        this.x500Name = rootName;
        this.privateKey = aPrivate;
    }
}
