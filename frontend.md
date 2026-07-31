# Full-Stack Ecommerce Platform Integration Guide (`frontend.md`)

This guide explains how to connect the React Frontend with the Spring Boot Backend & Keycloak IAM, verify API endpoints, and run the entire multi-tenant platform locally.

---

## 🏗️ Architecture Overview

```
+------------------+          HTTP Requests           +-----------------------+
|  Vite + React    |  ----------------------------->  |  Spring Boot Backend  |
|  Frontend App    |     (Port 8080 / REST API)       |  (Port 8080 / Java)   |
| (http://localhost|                                  +-----------+-----------+
|      :5173)      |  <-----------------------------              |
+------------------+          JSON Responses                      | OAuth2 Token Grant
                                                                  v
                                                      +-----------------------+
                                                      |  Keycloak IAM Server  |
                                                      | (http://localhost:8081|
                                                      | realm: ecommerce-realm|
                                                      +-----------------------+
```

1. **Keycloak IAM (`http://localhost:8081`)**: Manages identity, realm users (`ecommerce-realm`), password verification, and OAuth2 JWT token issuance.
2. **Spring Boot Backend (`http://localhost:8080`)**: Exposes REST endpoints for Tenants, Products, Orders, Favourites, and Authentication. Validates JWTs and handles business logic with H2 database.
3. **Vite React Frontend (`http://localhost:5173`)**: Interacts with the backend via Axios, storing the JWT access token in `localStorage` and passing it via `Authorization: Bearer <token>` headers.

---

## 🚀 How to Run the Full Project (Step-by-Step)

### Prerequisites
- **Java 21+** & **Maven 3.8+**
- **Node.js 18+** & **npm**
- **Docker** (for Keycloak 24+)

---

### Step 1: Start Keycloak IAM Server
Launch Keycloak on port `8081` with realm `ecommerce-realm`:
```bash
docker run -d --name keycloak -p 8081:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:24.0.1 start-dev
```
*Note: Ensure `ecommerce-realm` and `ecommerce-client` (client_secret: `lfPGxXt21IWL61VmVizO8QLILRPpXMw1`) are created as specified in backend `application.properties`.*

---

### Step 2: Start Spring Boot Backend Server
Open a terminal in the `backend` folder:
```bash
cd backend
mvn clean spring-boot:run
```
- The backend will start on **`http://localhost:8080`**.
- Automatic H2 database initialization seeds default demo tenants (`nike`, `demo`) and products.

---

### Step 3: Start Vite React Frontend
Open a new terminal in the `frontend` folder:
```bash
cd frontend
npm install
npm run dev
```
- The React application will start on **`http://localhost:5173`**.

---

## 🔗 Connecting Frontend & Backend

### 1. Environment Variable Configuration
In `frontend/.env` (or default fallback in `src/api.js`), configure the backend API URL:
```env
VITE_API_BASE_URL=http://localhost:8080
```

### 2. Axios API Client Setup (`src/api.js`)
Axios attaches the `Authorization: Bearer <token>` header to all outgoing HTTP requests once logged in:

```javascript
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
});

export const setAuthToken = (token) => {
  if (token) {
    apiClient.defaults.headers.common.Authorization = `Bearer ${token}`;
    localStorage.setItem('ecom_token', token);
    return;
  }
  delete apiClient.defaults.headers.common.Authorization;
  localStorage.removeItem('ecom_token');
};

export const bootstrapAuthToken = () => {
  const token = localStorage.getItem('ecom_token');
  if (token) {
    setAuthToken(token);
  }
  return token;
};
```

### 3. Backend CORS Policy
The backend permits requests from the frontend origin via `SecurityConfig.java`:
```java
CorsConfiguration configuration = new CorsConfiguration();
configuration.setAllowedOriginPatterns(List.of("*"));
configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
configuration.setAllowedHeaders(List.of("*"));
configuration.setAllowCredentials(true);
```

---

## 🧪 Comprehensive API Endpoint Test Results

All **20 API endpoints** specified in `detailed_api.md` have been fully automated and tested live against the running backend with **100% SUCCESS**:

| # | Endpoint Method & Path | Auth Required | Tested Status | Description |
|---|------------------------|---------------|---------------|-------------|
| 1 | `GET /api/platform/tenants` | Public | `200 OK` | Fetch all registered platform tenants |
| 2 | `GET /api/platform/tenants/{slug}` | Public | `200 OK` | Fetch single tenant details by slug |
| 3 | `POST /api/auth/register` | Public | `200 OK` | Register new user in Keycloak & local DB |
| 4 | `POST /api/auth/login` | Public | `200 OK` | Authenticate user & return JWT token |
| 5 | `GET /api/auth/me` | Bearer Token | `200 OK` | Get authenticated user profile & role |
| 6 | `POST /api/auth/login` (Admin) | Public | `200 OK` | Authenticate adminuser & assign `ADMIN` role |
| 7 | `POST /api/platform/tenants` | Admin Only | `201 Created` | Create a new tenant store |
| 8 | `GET /{tenantSlug}/products` | Public | `200 OK` | Paginated product search & category filtering |
| 9 | `POST /{tenantSlug}/products` | Admin / Tenant Admin | `200 OK` | Add new product under specified tenant |
| 10 | `GET /{tenantSlug}/products/{id}` | Public | `200 OK` | Get product details by ID |
| 11 | `PUT /{tenantSlug}/products/{id}` | Admin / Tenant Admin | `200 OK` | Update product details |
| 12 | `PATCH /{tenantSlug}/products/{id}/stock` | Admin / Tenant Admin | `200 OK` | Update product inventory stock level |
| 13 | `POST /{tenantSlug}/favourites/{productId}` | Bearer Token | `200 OK` | Mark product as favourite for current user |
| 14 | `GET /{tenantSlug}/favourites` | Bearer Token | `200 OK` | List user's favourite products |
| 15 | `DELETE /{tenantSlug}/favourites/{productId}` | Bearer Token | `200 OK` | Remove product from user's favourites |
| 16 | `POST /{tenantSlug}/orders` | Bearer Token | `200 OK` | Place new multi-item order |
| 17 | `GET /{tenantSlug}/orders/my-history` | Bearer Token | `200 OK` | View logged-in user's order history |
| 18 | `GET /{tenantSlug}/orders` | Admin / Tenant Admin | `200 OK` | View all orders for specified tenant |
| 19 | `DELETE /{tenantSlug}/products/{id}` | Admin / Tenant Admin | `200 OK` | Delete product (cascades to order_items & favourites) |
| 20 | `DELETE /api/platform/tenants/{id}` | Admin Only | `204 No Content` | Delete tenant (cascades to products & orders) |

---

## 🔑 Test User Credentials

| Role | Username | Password | Email | Access Scope |
|------|----------|----------|-------|--------------|
| **Platform Admin** | `adminuser` | `admin123` | `adminuser@example.com` | Full access (`/api/platform/**`, products, tenants, orders) |
| **Standard User** | *(Self Register)* | `password123` | `user@example.com` | Browse products, place orders, manage favourites |

---

## 💡 Quick Start Command Summary

```bash
# 1. Start Keycloak
docker run -d --name keycloak -p 8081:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:24.0.1 start-dev

# 2. Start Backend
cd backend && mvn spring-boot:run

# 3. Start Frontend
cd frontend && npm run dev
```

Visit **`http://localhost:5173`** in your browser to start using the Ecommerce Platform!
