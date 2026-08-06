package com.ecommerce.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Name is required.")
    @Size(min = 2, max = 100,
            message = "Name must be between 2 and 100 characters.")
    private String name;

    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 30,
            message = "Username must be between 3 and 30 characters.")
    @Pattern(
            regexp = "^[a-zA-Z0-9._-]+$",
            message = "Username may contain only letters, numbers, dots, underscores, and hyphens."
    )
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid email address.")
    @Size(max = 254,
            message = "Email cannot exceed 254 characters.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 128,
            message = "Password must be between 8 and 128 characters.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit."
    )
    private String password;

    @Size(max = 100,
            message = "Tenant slug cannot exceed 100 characters.")
    @Pattern(
            regexp = "^[a-z0-9-]+$",
            message = "Tenant slug may contain only lowercase letters, numbers and hyphens."
    )
    private String tenantSlug;

    public RegisterRequest() {}

    public RegisterRequest(String name, String username, String email, String password, String tenantSlug) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.tenantSlug = tenantSlug;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTenantSlug() { return tenantSlug; }
    public void setTenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private String username;
        private String email;
        private String password;
        private String tenantSlug;

        public Builder name(String name) { this.name = name; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder tenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; return this; }

        public RegisterRequest build() {
            return new RegisterRequest(name, username, email, password, tenantSlug);
        }
    }
}
