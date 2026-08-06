package com.ecommerce.dto.response;

import java.math.BigDecimal;

public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productCategory;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String productImageUrl;
    private Integer availableQuantity;
    private String tenantSlug;

    public CartItemResponse() {}

    public CartItemResponse(Long id, Long productId, String productName, String productCategory, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal, String productImageUrl, Integer availableQuantity, String tenantSlug) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productCategory = productCategory;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.productImageUrl = productImageUrl;
        this.availableQuantity = availableQuantity;
        this.tenantSlug = tenantSlug;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public String getTenantSlug() { return tenantSlug; }
    public void setTenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long productId;
        private String productName;
        private String productCategory;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private String productImageUrl;
        private Integer availableQuantity;
        private String tenantSlug;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder productId(Long productId) { this.productId = productId; return this; }
        public Builder productName(String productName) { this.productName = productName; return this; }
        public Builder productCategory(String productCategory) { this.productCategory = productCategory; return this; }
        public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public Builder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public Builder subtotal(BigDecimal subtotal) { this.subtotal = subtotal; return this; }
        public Builder productImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; return this; }
        public Builder availableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; return this; }
        public Builder tenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; return this; }

        public CartItemResponse build() {
            return new CartItemResponse(id, productId, productName, productCategory, quantity, unitPrice, subtotal, productImageUrl, availableQuantity, tenantSlug);
        }
    }
}
