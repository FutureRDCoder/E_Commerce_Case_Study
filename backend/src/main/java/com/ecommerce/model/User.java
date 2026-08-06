package com.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_tenant",
                        columnList = "tenant_id")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    @JsonIgnore
    private String password;

    @Column(unique = true)
    private String keycloakUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    public User() {}

    public User(Long id, String name, String username, String email, String password, String keycloakUserId, Role role, Tenant tenant) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.keycloakUserId = keycloakUserId;
        this.role = role;
        this.tenant = tenant;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getKeycloakUserId() { return keycloakUserId; }
    public void setKeycloakUserId(String keycloakUserId) { this.keycloakUserId = keycloakUserId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private String username;
        private String email;
        private String password;
        private String keycloakUserId;
        private Role role;
        private Tenant tenant;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder keycloakUserId(String keycloakUserId) { this.keycloakUserId = keycloakUserId; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder tenant(Tenant tenant) { this.tenant = tenant; return this; }

        public User build() {
            return new User(id, name, username, email, password, keycloakUserId, role, tenant);
        }
    }
}
