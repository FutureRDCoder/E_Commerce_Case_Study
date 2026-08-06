package com.ecommerce.dto.response;

import java.math.BigDecimal;

public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productCategory;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String productImageUrl;

    public OrderItemResponse() {}

    public OrderItemResponse(Long id, Long productId, String productName, String productCategory, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal, String productImageUrl) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productCategory = productCategory;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.productImageUrl = productImageUrl;
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

        public Builder id(Long id) { this.id = id; return this; }
        public Builder productId(Long productId) { this.productId = productId; return this; }
        public Builder productName(String productName) { this.productName = productName; return this; }
        public Builder productCategory(String productCategory) { this.productCategory = productCategory; return this; }
        public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public Builder unitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; return this; }
        public Builder subtotal(BigDecimal subtotal) { this.subtotal = subtotal; return this; }
        public Builder productImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; return this; }

        public OrderItemResponse build() {
            return new OrderItemResponse(id, productId, productName, productCategory, quantity, unitPrice, subtotal, productImageUrl);
        }
    }
}
