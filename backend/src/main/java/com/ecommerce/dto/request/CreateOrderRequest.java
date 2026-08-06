package com.ecommerce.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateOrderRequest {

    @NotNull(message = "Items list is required.")
    @NotEmpty(message = "Order must contain at least one item.")
    @Valid
    private List<OrderItemRequest> items;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(List<OrderItemRequest> items) {
        this.items = items;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<OrderItemRequest> items;

        public Builder items(List<OrderItemRequest> items) {
            this.items = items;
            return this;
        }

        public CreateOrderRequest build() {
            return new CreateOrderRequest(items);
        }
    }
}