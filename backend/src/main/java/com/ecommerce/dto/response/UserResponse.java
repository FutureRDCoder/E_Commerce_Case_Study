package com.ecommerce.dto.response;

import com.ecommerce.model.Role;

public class UserResponse {

    private Long id;
    private String name;
    private String username;
    private String email;
    private Role role;
    private Long tenantId;
    private String tenantName;
    private String tenantSlug;

    public UserResponse() {}

    public UserResponse(Long id, String name, String username, String email, Role role, Long tenantId, String tenantName, String tenantSlug) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.role = role;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.tenantSlug = tenantSlug;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getTenantSlug() { return tenantSlug; }
    public void setTenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private String username;
        private String email;
        private Role role;
        private Long tenantId;
        private String tenantName;
        private String tenantSlug;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder tenantId(Long tenantId) { this.tenantId = tenantId; return this; }
        public Builder tenantName(String tenantName) { this.tenantName = tenantName; return this; }
        public Builder tenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; return this; }

        public UserResponse build() {
            return new UserResponse(id, name, username, email, role, tenantId, tenantName, tenantSlug);
        }
    }
}
