package com.ecommerce.dto;

import com.ecommerce.model.Role;

public class AuthResponse {

    private String token;
    private Long userId;
    private String name;
    private String username;
    private String email;
    private Role role;
    private Long tenantId;
    private String tenantSlug;
    private String tenantName;

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String name, String username, String email, Role role, Long tenantId, String tenantSlug, String tenantName) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.email = email;
        this.role = role;
        this.tenantId = tenantId;
        this.tenantSlug = tenantSlug;
        this.tenantName = tenantName;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getTenantSlug() { return tenantSlug; }
    public void setTenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token;
        private Long userId;
        private String name;
        private String username;
        private String email;
        private Role role;
        private Long tenantId;
        private String tenantSlug;
        private String tenantName;

        public Builder token(String token) { this.token = token; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder tenantId(Long tenantId) { this.tenantId = tenantId; return this; }
        public Builder tenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; return this; }
        public Builder tenantName(String tenantName) { this.tenantName = tenantName; return this; }

        public AuthResponse build() {
            return new AuthResponse(token, userId, name, username, email, role, tenantId, tenantSlug, tenantName);
        }
    }
}
