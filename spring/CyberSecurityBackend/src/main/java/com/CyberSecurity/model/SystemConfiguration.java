package com.CyberSecurity.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "system_configuration")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SystemConfiguration {
    @Id
    Long id = 1L;

    @Embedded
    private EncryptedData systemDataKey;
}
