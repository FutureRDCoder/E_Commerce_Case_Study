# Ecommerce Backend API Manual Testing Guide

This document lists all available API endpoints for the backend, shows the request body you should send, and explains how to set up Keycloak and Docker for manual testing.

Base URLs:
- Backend: http://localhost:8080
- Keycloak: http://localhost:8081
- H2 Console: http://localhost:8080/h2-console

Default database credentials for H2:
- JDBC URL: jdbc:h2:mem:ecommercedb
- Username: sa
- Password: (leave empty)

---

## 1. Prerequisites

Before testing the API, make sure you have:
- Java 25
- Maven
- Docker Desktop (for Keycloak and optional backend container)
- Postman or Insomnia (recommended)

Check Java version:
```bash
java -version
mvn -version
```

---

## 2. Run the backend locally

From the project root:
```bash
mvn clean spring-boot:run
```

The app should start on port 8080.

If you see errors related to Keycloak, make sure Keycloak is already running and the settings in `src/main/resources/application.properties` match your Keycloak instance.

---

## 3. Keycloak setup (step by step)

The backend expects Keycloak to be available at:
- Server URL: http://localhost:8081
- Realm: ecommerce-realm
- Client: ecommerce-client

### 3.1 Start Keycloak with Docker

Run this command:
```bash
docker run -d --name ecommerce-keycloak \
  -p 8081:8080 \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin123 \
  -e KC_HTTP_ENABLED=true \
  quay.io/keycloak/keycloak:26.2.4 start-dev
```

Open the admin console:
- http://localhost:8081
- Username: admin
- Password: admin123

### 3.2 Create the realm

In Keycloak admin console:
1. Click Create realm
2. Name it: `ecommerce-realm`
3. Click Create

### 3.3 Create the client

Create a client named `ecommerce-client`:
1. Go to Clients → Create client
2. Client type: OpenID Connect
3. Client ID: `ecommerce-client`
4. Click Next
5. Enable Standard flow and Direct access grants
6. Click Save

Then configure:
- Access Type: confidential
- Service Accounts Enabled: on
- Valid redirect URIs: `http://localhost:8080/*`
- Web origins: `http://localhost:8080`

If a client secret is shown, copy it. Update `application.properties` if needed.

### 3.4 Create realm roles

Go to Realm roles and create these roles:
- `ADMIN`
- `TENANT_ADMIN`
- `USER`

### 3.5 Create an admin user

Create a user such as `adminuser` and set a password.

Then:
1. Go to Role mapping
2. Assign the `ADMIN` role

This allows testing the admin-only tenant endpoints.

### 3.6 Verify the app settings

The backend currently expects these values in `src/main/resources/application.properties`:
```properties
app.keycloak.server-url=http://localhost:8081
app.keycloak.realm=ecommerce-realm
app.keycloak.client-id=ecommerce-client
app.keycloak.client-secret=change-me-client-secret
app.keycloak.admin-client-id=admin-cli
app.keycloak.admin-client-secret=

spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081/realms/ecommerce-realm
```

If your Keycloak client has a different secret, update `app.keycloak.client-secret` accordingly.

---

## 4. Docker setup for the backend (optional)

The project does not yet include a Dockerfile, but you can use the following example.

Create a file named `Dockerfile` in the project root:
```dockerfile
FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests package
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/ecommerce-backend-1.0.0.jar"]
```

Example `docker-compose.yml`:
```yaml
version: "3.9"
services:
  keycloak:
    image: quay.io/keycloak/keycloak:26.2.4
    container_name: ecommerce-keycloak
    ports:
      - "8081:8080"
    environment:
      KC_BOOTSTRAP_ADMIN_USERNAME: admin
      KC_BOOTSTRAP_ADMIN_PASSWORD: admin123
      KC_HTTP_ENABLED: "true"
    command: start-dev

  backend:
    build: .
    container_name: ecommerce-backend
    ports:
      - "8080:8080"
    depends_on:
      - keycloak
```

Run:
```bash
docker compose up --build
```

---

## 5. Authentication flow for manual testing

### 5.1 Register a user

Endpoint:
```http
POST /api/auth/register
```

Body:
```json
{
  "name": "Test User",
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "password123",
  "role": "USER",
  "tenantSlug": "demo"
}
```

Notes:
- Public registration only supports `USER` role.
- `tenantSlug` is optional.
- If you send a tenant slug, the tenant must already exist.

Response includes a JWT token in the `token` field.

### 5.2 Login

Endpoint:
```http
POST /api/auth/login
```

Body:
```json
{
  "username": "testuser",
  "password": "password123"
}
```

Response includes a JWT token.

### 5.3 Get current user profile

Endpoint:
```http
GET /api/auth/me
```

Headers:
```http
Authorization: Bearer <token>
```

---

## 6. API endpoint reference

All protected endpoints require a bearer token in the `Authorization` header.

### 6.1 Tenant management

#### Get all tenants
```http
GET /api/platform/tenants
```

No auth required.

#### Get tenant by slug
```http
GET /api/platform/tenants/{slug}
```

Example:
```http
GET /api/platform/tenants/demo
```

#### Create tenant
```http
POST /api/platform/tenants
```

Headers:
```http
Authorization: Bearer <admin-token>
Content-Type: application/json
```

Body:
```json
{
  "name": "Demo Store",
  "slug": "demo",
  "description": "Sample tenant for testing",
  "logoUrl": "https://example.com/logo.png"
}
```

Required role: `ADMIN`

#### Delete tenant
```http
DELETE /api/platform/tenants/{id}
```

Headers:
```http
Authorization: Bearer <admin-token>
```

Required role: `ADMIN`

---

### 6.2 Authentication

#### Register
```http
POST /api/auth/register
```

Body:
```json
{
  "name": "Test User",
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "password123",
  "role": "USER",
  "tenantSlug": "demo"
}
```

#### Login
```http
POST /api/auth/login
```

Body:
```json
{
  "username": "testuser",
  "password": "password123"
}
```

#### Get current user
```http
GET /api/auth/me
```

Headers:
```http
Authorization: Bearer <token>
```

---

### 6.3 Product endpoints

Base path:
```http
/{tenantSlug}/products
```

#### Get all products
```http
GET /{tenantSlug}/products
```

Optional query params:
- `category`
- `search`
- `page` (default 0)
- `size` (default 10)

Example:
```http
GET /demo/products?page=0&size=5
```

No auth required for GET.

#### Get a single product
```http
GET /{tenantSlug}/products/{id}
```

Example:
```http
GET /demo/products/1
```

#### Create product
```http
POST /{tenantSlug}/products
```

Headers:
```http
Authorization: Bearer <token>
Content-Type: application/json
```

Body:
```json
{
  "name": "Laptop",
  "description": "Gaming laptop",
  "price": 999.99,
  "category": "Electronics",
  "availableQuantity": 10,
  "imageUrl": "https://example.com/laptop.png"
}
```

Required roles: `ADMIN` or `TENANT_ADMIN`

#### Update product
```http
PUT /{tenantSlug}/products/{id}
```

Body:
```json
{
  "name": "Updated Laptop",
  "description": "Updated gaming laptop",
  "price": 1099.99,
  "category": "Electronics",
  "availableQuantity": 12,
  "imageUrl": "https://example.com/laptop.png"
}
```

#### Update stock
```http
PATCH /{tenantSlug}/products/{id}/stock
```

Body:
```json
{
  "availableQuantity": 25
}
```

#### Delete product
```http
DELETE /{tenantSlug}/products/{id}
```

Headers:
```http
Authorization: Bearer <token>
```

---

### 6.4 Order endpoints

Base path:
```http
/{tenantSlug}/orders
```

#### Create order
```http
POST /{tenantSlug}/orders
```

Headers:
```http
Authorization: Bearer <token>
Content-Type: application/json
```

Body:
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

#### Get my order history
```http
GET /{tenantSlug}/orders/my-history
```

Headers:
```http
Authorization: Bearer <token>
```

#### Get tenant orders (admin/tenant admin only)
```http
GET /{tenantSlug}/orders
```

Headers:
```http
Authorization: Bearer <token>
```

Required roles: `ADMIN` or `TENANT_ADMIN`

---

### 6.5 Favourite endpoints

Base path:
```http
/{tenantSlug}/favourites
```

#### Add favourite
```http
POST /{tenantSlug}/favourites/{productId}
```

Headers:
```http
Authorization: Bearer <token>
```

#### Remove favourite
```http
DELETE /{tenantSlug}/favourites/{productId}
```

Headers:
```http
Authorization: Bearer <token>
```

#### Get favourites
```http
GET /{tenantSlug}/favourites
```

Headers:
```http
Authorization: Bearer <token>
```

---

## 7. Recommended manual test flow

### Step 1: Start Keycloak
Use Docker to start Keycloak as shown above.

### Step 2: Start the backend
Run:
```bash
mvn spring-boot:run
```

### Step 3: Create a tenant
Use the admin user and call:
```http
POST /api/platform/tenants
```

Body:
```json
{
  "name": "Demo Store",
  "slug": "demo",
  "description": "Sample tenant",
  "logoUrl": ""
}
```

### Step 4: Register a normal user
```http
POST /api/auth/register
```

Body:
```json
{
  "name": "Test User",
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "password123",
  "role": "USER",
  "tenantSlug": "demo"
}
```

### Step 5: Login and copy the token
```http
POST /api/auth/login
```

### Step 6: Test product endpoints
Use the token to create, update, and fetch products.

### Step 7: Test order endpoints
Create an order using a product that exists in the tenant.

### Step 8: Test favourite endpoints
Add and remove a favourite product.

---

## 8. Common issues

### Keycloak connection errors
- Check that Keycloak is running on port 8081
- Confirm the realm name is exactly `ecommerce-realm`
- Confirm the client id is exactly `ecommerce-client`

### 401 Unauthorized
- Make sure the `Authorization` header is set correctly
- Use `Bearer <token>` with the token from login/register

### 403 Forbidden
- The user role does not have permission for that endpoint
- For admin endpoints, the Keycloak user needs the `ADMIN` role

### Tenant not found
- Create the tenant first before using its slug in product/order/favourite endpoints

---

## 9. Useful curl examples

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "password123",
    "role": "USER",
    "tenantSlug": "demo"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### Create a tenant
```bash
curl -X POST http://localhost:8080/api/platform/tenants \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Demo Store",
    "slug": "demo",
    "description": "Sample tenant",
    "logoUrl": ""
  }'
```

### Create a product
```bash
curl -X POST http://localhost:8080/demo/products \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "Gaming laptop",
    "price": 999.99,
    "category": "Electronics",
    "availableQuantity": 10,
    "imageUrl": "https://example.com/laptop.png"
  }'
```

---

If you want, I can also add a ready-to-use Dockerfile and docker-compose.yml to the project so you can run everything with one command.
