package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

public class TenantRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    private String description;

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
