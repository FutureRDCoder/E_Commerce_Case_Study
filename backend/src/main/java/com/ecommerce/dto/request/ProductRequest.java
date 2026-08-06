package com.ecommerce.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductRequest {

    @NotBlank(message = "Product name is required.")
    @Size(min = 2, max = 150,
            message = "Product name must be between 2 and 150 characters.")
    private String name;

    @Size(max = 2000,
            message = "Description cannot exceed 2000 characters.")
    private String description;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.01",
            message = "Price must be greater than zero.")
    private BigDecimal price;

    @NotBlank(message = "Category is required.")
    @Size(max = 100,
            message = "Category cannot exceed 100 characters.")
    private String category;

    @NotNull(message = "Available quantity is required.")
    @PositiveOrZero(message = "Available quantity cannot be negative.")
    private Integer availableQuantity;

    @Size(max = 500,
            message = "Image URL cannot exceed 500 characters.")
    private String imageUrl;

    public ProductRequest() {}

    public ProductRequest(String name, String description, BigDecimal price, String category, Integer availableQuantity, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.availableQuantity = availableQuantity;
        this.imageUrl = imageUrl;
    }

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
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private Integer availableQuantity;
        private String imageUrl;

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder category(String category) { this.category = category; return this; }
        public Builder availableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }

        public ProductRequest build() {
            return new ProductRequest(name, description, price, category, availableQuantity, imageUrl);
        }
    }
}
