package com.ecommerce.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class CreateOrderRequest {

    @NotEmpty
    private List<OrderItemRequest> items;

    public CreateOrderRequest() {}

    public CreateOrderRequest(List<OrderItemRequest> items) {
        this.items = items;
    }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private List<OrderItemRequest> items;

        public Builder items(List<OrderItemRequest> items) { this.items = items; return this; }

        public CreateOrderRequest build() {
            return new CreateOrderRequest(items);
        }
    }
}
