package com.ecommerce.dto.response;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private String message;
    private Long tenantId;
    private String tenantName;
    private String tenantSlug;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationResponse() {}

    public NotificationResponse(Long id, String message, Long tenantId, String tenantName, String tenantSlug, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.tenantSlug = tenantSlug;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getTenantSlug() { return tenantSlug; }
    public void setTenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String message;
        private Long tenantId;
        private String tenantName;
        private String tenantSlug;
        private boolean read;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder tenantId(Long tenantId) { this.tenantId = tenantId; return this; }
        public Builder tenantName(String tenantName) { this.tenantName = tenantName; return this; }
        public Builder tenantSlug(String tenantSlug) { this.tenantSlug = tenantSlug; return this; }
        public Builder read(boolean read) { this.read = read; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public NotificationResponse build() {
            return new NotificationResponse(id, message, tenantId, tenantName, tenantSlug, read, createdAt);
        }
    }
}
