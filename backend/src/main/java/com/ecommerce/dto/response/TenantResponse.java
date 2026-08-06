package com.ecommerce.dto.response;

public class TenantResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;

    public TenantResponse() {}

    public TenantResponse(Long id, String name, String slug, String description, String logoUrl) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.logoUrl = logoUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
        private Long id;
        private String name;
        private String slug;
        private String description;
        private String logoUrl;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder slug(String slug) { this.slug = slug; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder logoUrl(String logoUrl) { this.logoUrl = logoUrl; return this; }

        public TenantResponse build() {
            return new TenantResponse(id, name, slug, description, logoUrl);
        }
    }
}
