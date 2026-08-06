package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class StockUpdateRequest {

    @NotNull(message = "Available quantity is required.")
    @PositiveOrZero(message = "Available quantity cannot be negative.")
    private Integer availableQuantity;

    public StockUpdateRequest() {
    }

    public StockUpdateRequest(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Integer availableQuantity;

        public Builder availableQuantity(Integer availableQuantity) {
            this.availableQuantity = availableQuantity;
            return this;
        }

        public StockUpdateRequest build() {
            return new StockUpdateRequest(availableQuantity);
        }
    }
}