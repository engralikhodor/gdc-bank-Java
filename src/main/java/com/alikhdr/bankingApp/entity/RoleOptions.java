package com.alikhdr.bankingApp.entity;

import org.springframework.security.core.GrantedAuthority; // Import GrantedAuthority

public enum RoleOptions implements GrantedAuthority // Implement GrantedAuthority
{
    CUSTOMER("ROLE_CUSTOMER", "Standard customer role with basic banking access."),
    EMPLOYEE("ROLE_EMPLOYEE", "Internal bank employee with specific operational access."),
    ADMIN("ROLE_ADMIN", "Administrator role with full system access.");

    private final String authority;
    private final String description;

    RoleOptions(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    public String getDescription() {
        return description;
    }
}
