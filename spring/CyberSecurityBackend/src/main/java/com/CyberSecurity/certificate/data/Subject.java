package com.CyberSecurity.certificate.data;

import lombok.*;
import org.bouncycastle.asn1.x500.X500Name;

import java.security.PublicKey;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Subject {
    private X500Name x500Name;
    private PublicKey publicKey;


}
