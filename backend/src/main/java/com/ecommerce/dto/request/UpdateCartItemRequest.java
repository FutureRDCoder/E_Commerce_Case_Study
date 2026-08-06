package com.ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateCartItemRequest {

    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    private Integer quantity;

    public UpdateCartItemRequest() {
    }

    public UpdateCartItemRequest(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private Integer quantity;

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public UpdateCartItemRequest build() {
            return new UpdateCartItemRequest(quantity);
        }
    }
}
