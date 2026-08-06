package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;

public class AssignTenantRequest {

    @NotNull(message = "tenantId is required.")
    private Long tenantId;

    public AssignTenantRequest() {}

    public AssignTenantRequest(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long tenantId;

        public Builder tenantId(Long tenantId) { this.tenantId = tenantId; return this; }

        public AssignTenantRequest build() {
            return new AssignTenantRequest(tenantId);
        }
    }
}
