package com.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockUpdateRequest {

    @NotNull
    @Min(0)
    private Integer availableQuantity;

    public StockUpdateRequest() {}

    public StockUpdateRequest(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer availableQuantity;

        public Builder availableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; return this; }

        public StockUpdateRequest build() {
            return new StockUpdateRequest(availableQuantity);
        }
    }
}
