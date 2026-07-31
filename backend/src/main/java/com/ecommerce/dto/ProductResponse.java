package com.ecommerce.dto;

public class ProductResponse {

    private Long id;
    private Long tenantId;
    private String tenantName;
    private String tenantSlug;
    private String name;
    private String description;
    private Double price;
    private String category;
    private Integer availableQuantity;
    private String imageUrl;
    private Boolean isFavourite;

    public ProductResponse() {}

    public ProductResponse(Long id, Long tenantId, String tenantName, String tenantSlug, String name, String description, Double price, String category, Integer availableQuantity, String imageUrl, Boolean isFavourite) {
        this.id = id;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.tenantSlug = tenantSlug;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.availableQuantity = availableQuantity;
        this.imageUrl = imageUrl;
        this.isFavourite = isFavourite;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getTenantSlug() { return tenantSlug; }
    public void setTenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getIsFavourite() { return isFavourite; }
    public void setIsFavourite(Boolean favourite) { isFavourite = favourite; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long tenantId;
        private String tenantName;
        private String tenantSlug;
        private String name;
        private String description;
        private Double price;
        private String category;
        private Integer availableQuantity;
        private String imageUrl;
        private Boolean isFavourite;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder tenantId(Long tenantId) { this.tenantId = tenantId; return this; }
        public Builder tenantName(String tenantName) { this.tenantName = tenantName; return this; }
        public Builder tenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder price(Double price) { this.price = price; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder availableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public Builder isFavourite(Boolean isFavourite) { this.isFavourite = isFavourite; return this; }

        public ProductResponse build() {
            return new ProductResponse(id, tenantId, tenantName, tenantSlug, name, description, price, category, availableQuantity, imageUrl, isFavourite);
        }
    }
}
