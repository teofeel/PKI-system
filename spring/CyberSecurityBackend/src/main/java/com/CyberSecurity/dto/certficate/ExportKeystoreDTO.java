package com.CyberSecurity.dto.certficate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExportKeystoreDTO {
    boolean pkcs12;
    String alias;
    String password;
}
