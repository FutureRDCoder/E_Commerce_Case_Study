package com.ecommerce.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductSearchRequest {

    @Size(max = 100,
            message = "Category cannot exceed 100 characters.")
    private String category;

    @Size(max = 150,
            message = "Search text cannot exceed 150 characters.")
    private String search;

    @Min(value = 0,
            message = "Page number cannot be negative.")
    private Integer page = 0;

    @Min(value = 1,
            message = "Page size must be at least 1.")
    @Max(value = 100,
            message = "Page size cannot exceed 100.")
    private Integer size = 10;

    @DecimalMin(value = "0",
            message = "Minimum price cannot be negative.")
    private BigDecimal minPrice;

    @DecimalMin(value = "0",
            message = "Maximum price cannot be negative.")
    private BigDecimal maxPrice;

    public ProductSearchRequest() {
    }

    public ProductSearchRequest(String category, String search, Integer page, Integer size, BigDecimal minPrice, BigDecimal maxPrice) {
        this.category = category;
        this.search = search;
        this.page = page;
        this.size = size;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = (page == null) ? 0 : page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = (size == null) ? 10 : size;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String category;
        private String search;
        private Integer page = 0;
        private Integer size = 10;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder search(String search) {
            this.search = search;
            return this;
        }

        public Builder page(Integer page) {
            this.page = page;
            return this;
        }

        public Builder size(Integer size) {
            this.size = size;
            return this;
        }

        public Builder minPrice(BigDecimal minPrice) {
            this.minPrice = minPrice;
            return this;
        }

        public Builder maxPrice(BigDecimal maxPrice) {
            this.maxPrice = maxPrice;
            return this;
        }

        public ProductSearchRequest build() {
            return new ProductSearchRequest(
                    category,
                    search,
                    page,
                    size,
                    minPrice,
                    maxPrice
            );
        }
    }
}