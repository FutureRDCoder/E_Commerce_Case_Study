package com.ecommerce.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "products",
        indexes = {

                @Index(
                        name = "idx_product_tenant",
                        columnList = "tenant_id"
                ),

                @Index(
                        name = "idx_product_category",
                        columnList = "category"
                ),

                @Index(
                        name = "idx_product_name",
                        columnList = "name"
                )
        }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer availableQuantity;

    private String imageUrl;

    public Product() {}

    public Product(Long id, Tenant tenant, String name, String description, BigDecimal price, String category, Integer availableQuantity, String imageUrl) {
        this.id = id;
        this.tenant = tenant;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.availableQuantity = availableQuantity;
        this.imageUrl = imageUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Tenant tenant;
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private Integer availableQuantity;
        private String imageUrl;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder tenant(Tenant tenant) { this.tenant = tenant; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder availableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }

        public Product build() {
            return new Product(id, tenant, name, description, price, category, availableQuantity, imageUrl);
        }
    }
}
