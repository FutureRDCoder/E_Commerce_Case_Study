package com.ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OrderItemRequest {

    @NotNull(message = "Product ID is required.")
    @Positive(message = "Product ID must be greater than zero.")
    private Long productId;

    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    private Integer quantity;

    public OrderItemRequest() {
    }

    public OrderItemRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long productId;
        private Integer quantity;

        public Builder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public OrderItemRequest build() {
            return new OrderItemRequest(productId, quantity);
        }
    }
}