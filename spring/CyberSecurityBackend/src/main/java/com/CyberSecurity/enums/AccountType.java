package com.CyberSecurity.enums;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

public enum AccountType implements GrantedAuthority {
    ADMIN,
    CA_USER,
    USER;

    @Override
    public @Nullable String getAuthority() {
        return "";
    }
}
