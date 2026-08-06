package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TenantRequest {

    @NotBlank(message = "Tenant name is required.")
    @Size(min = 2, max = 100,
            message = "Tenant name must be between 2 and 100 characters.")
    private String name;

    @NotBlank(message = "Tenant slug is required.")
    @Size(min = 2, max = 50,
            message = "Tenant slug must be between 2 and 50 characters.")
    @Pattern(
            regexp = "^[a-z0-9-]+$",
            message = "Tenant slug may contain only lowercase letters, numbers and hyphens."
    )
    private String slug;

    @Size(max = 1000,
            message = "Description cannot exceed 1000 characters.")
    private String description;

    @Size(max = 500,
            message = "Logo URL cannot exceed 500 characters.")
    @Pattern(
            regexp = "^(https?://).+",
            message = "Logo URL must start with http:// or https://"
    )
    private String logoUrl;

    public TenantRequest() {}

    public TenantRequest(String name, String slug, String description, String logoUrl) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.logoUrl = logoUrl;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private String slug;
        private String description;
        private String logoUrl;

        public Builder name(String name) { this.name = name; return this; }
        public Builder slug(String slug) { this.slug = slug; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder logoUrl(String logoUrl) { this.logoUrl = logoUrl; return this; }

        public TenantRequest build() {
            return new TenantRequest(name, slug, description, logoUrl);
        }
    }
}
