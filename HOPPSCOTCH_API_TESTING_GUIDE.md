# Ecommerce API Manual Testing Guide (Hoppscotch + Keycloak)

## 1) What this guide covers

This guide gives you an end-to-end flow to manually test every backend API with Hoppscotch:

- Keycloak setup
- Backend setup
- Auth flow (`register`, `login`, `me`)
- Tenant APIs
- Product APIs
- Favourite APIs
- Order APIs
- Role-based checks (`USER`, `TENANT_ADMIN`, `ADMIN`)

---

## 2) Local ports and URLs

- Backend base URL: `http://localhost:8080`
- Keycloak base URL: `http://localhost:8081`
- Realm: `ecommerce-realm`
- Frontend base URL (optional): `http://localhost:5173`

---

## 3) Start Keycloak

## Option A: Docker (recommended)

```bash
docker run --name ecommerce-keycloak -p 8081:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:25.0.6 start-dev
```

Then open:

- Admin console: `http://localhost:8081/admin`
- Login: `admin / admin`

---

## 4) Configure Keycloak realm and clients

## 4.1 Create realm

1. Open Keycloak Admin Console
2. Create realm: `ecommerce-realm`

## 4.2 Create realm roles

Create these realm roles:

- `ADMIN`
- `TENANT_ADMIN`
- `USER`

## 4.3 Create client for backend login flow

Create client:

- Client ID: `ecommerce-client`
- Client authentication: `ON` (confidential client)
- Standard flow: optional
- Direct access grants: `ON` (required for password grant in `/api/auth/login`)
- Service accounts roles: optional

Copy generated client secret and set it in backend:

`backend/src/main/resources/application.properties`

```properties
app.keycloak.client-secret=<your-client-secret>
```

## 4.4 Create admin API client

You can use either:

- `admin-cli` (no secret), or
- your own confidential admin client with service account and secret

If you use your own admin client, set:

```properties
app.keycloak.admin-client-id=<admin-client-id>
app.keycloak.admin-client-secret=<admin-client-secret>
```

If using `admin-cli`, keep:

```properties
app.keycloak.admin-client-id=admin-cli
app.keycloak.admin-client-secret=
```

---

## 5) Start backend

From `backend/` run your Spring Boot app with your normal method.

Expected:

- API online at `http://localhost:8080`
- Public endpoint works: `GET /api/platform/tenants`

---

## 6) Open Hoppscotch and create environment

Create environment variables:

- `baseUrl = http://localhost:8080`
- `tenantSlug = nike`
- `userToken =` (empty initially)
- `adminToken =` (empty initially)
- `tenantAdminToken =` (empty initially)
- `productId =` (empty initially)
- `tenantId =` (empty initially)
- `orderId =` (empty initially)

For authenticated endpoints add header:

- `Authorization: Bearer {{userToken}}` (or admin/tenant token depending on test)

---

## 7) API quick map

- Auth:
  - `POST /api/auth/register` (public, USER self-registration)
  - `POST /api/auth/login` (public)
  - `GET /api/auth/me` (auth required)
- Platform tenants:
  - `GET /api/platform/tenants` (public)
  - `GET /api/platform/tenants/{slug}` (public)
  - `POST /api/platform/tenants` (ADMIN only)
  - `DELETE /api/platform/tenants/{id}` (ADMIN only)
- Products:
  - `GET /{tenantSlug}/products` (public)
  - `GET /{tenantSlug}/products/{id}` (public)
  - `POST /{tenantSlug}/products` (ADMIN, TENANT_ADMIN)
  - `PUT /{tenantSlug}/products/{id}` (ADMIN, TENANT_ADMIN)
  - `PATCH /{tenantSlug}/products/{id}/stock` (ADMIN, TENANT_ADMIN)
  - `DELETE /{tenantSlug}/products/{id}` (ADMIN, TENANT_ADMIN)
- Favourites:
  - `POST /{tenantSlug}/favourites/{productId}` (auth)
  - `DELETE /{tenantSlug}/favourites/{productId}` (auth)
  - `GET /{tenantSlug}/favourites` (auth)
- Orders:
  - `POST /{tenantSlug}/orders` (auth)
  - `GET /{tenantSlug}/orders/my-history` (auth)
  - `GET /{tenantSlug}/orders` (ADMIN, TENANT_ADMIN)

---

## 8) Core test flow (recommended order)

## Step 1: List tenants (public)

Request:

- `GET {{baseUrl}}/api/platform/tenants`

Expected:

- `200 OK`
- Array response

If empty, create tenant in Step 8.9 as admin.

## Step 2: Register USER

Request:

- `POST {{baseUrl}}/api/auth/register`
- Body:

```json
{
  "name": "John Tester",
  "username": "john_tester",
  "email": "john.tester@example.com",
  "password": "Password@123",
  "role": "USER",
  "tenantSlug": null
}
```

Expected:

- `200 OK`
- JSON with `token`, `username`, `role`
- role should be `USER`

Copy `token` to `{{userToken}}`.

## Step 3: Login USER

Request:

- `POST {{baseUrl}}/api/auth/login`
- Body:

```json
{
  "username": "john_tester",
  "password": "Password@123"
}
```

Expected:

- `200 OK`
- JSON with `token`

Replace `{{userToken}}` with latest token.

## Step 4: Get current user profile

Request:

- `GET {{baseUrl}}/api/auth/me`
- Header: `Authorization: Bearer {{userToken}}`

Expected:

- `200 OK`
- User details including role and tenant fields

## Step 5: List products (public)

Request:

- `GET {{baseUrl}}/{{tenantSlug}}/products?page=0&size=10`

Expected:

- `200 OK`
- Paginated response with `content`

Take a product id and set `{{productId}}`.

## Step 6: Add favourite

Request:

- `POST {{baseUrl}}/{{tenantSlug}}/favourites/{{productId}}`
- Header: `Authorization: Bearer {{userToken}}`

Expected:

- `200 OK`
- Product response

## Step 7: View favourites

Request:

- `GET {{baseUrl}}/{{tenantSlug}}/favourites`
- Header: `Authorization: Bearer {{userToken}}`

Expected:

- `200 OK`
- List containing your favourite product

## Step 8: Create order

Request:

- `POST {{baseUrl}}/{{tenantSlug}}/orders`
- Header: `Authorization: Bearer {{userToken}}`
- Body:

```json
{
  "items": [
    {
      "productId": {{productId}},
      "quantity": 1
    }
  ]
}
```

Expected:

- `201 Created`
- Order response with totals and status

## Step 9: Get order history

Request:

- `GET {{baseUrl}}/{{tenantSlug}}/orders/my-history`
- Header: `Authorization: Bearer {{userToken}}`

Expected:

- `200 OK`
- List including created order

---

## 9) Admin and tenant-admin testing

Public registration only creates `USER`, so create admin users in Keycloak admin console and assign roles there.

For `TENANT_ADMIN`, set Keycloak user attribute:

- key: `tenantSlug`
- value: existing tenant slug (example: `nike`)

Then login via `/api/auth/login` to get token.

## 9.1 ADMIN login

Request:

- `POST {{baseUrl}}/api/auth/login`
- Body:

```json
{
  "username": "platform_admin",
  "password": "Password@123"
}
```

Save token into `{{adminToken}}`.

## 9.2 TENANT_ADMIN login

Request:

- `POST {{baseUrl}}/api/auth/login`
- Body:

```json
{
  "username": "nike_admin",
  "password": "Password@123"
}
```

Save token into `{{tenantAdminToken}}`.

## 9.3 Create tenant (ADMIN only)

Request:

- `POST {{baseUrl}}/api/platform/tenants`
- Header: `Authorization: Bearer {{adminToken}}`
- Body:

```json
{
  "name": "Reebok",
  "slug": "reebok",
  "description": "Performance shoes and apparel",
  "logoUrl": "https://example.com/reebok-logo.png"
}
```

Expected:

- `201 Created`

Negative check:

- Same request with `{{userToken}}` should return `403 Forbidden`

## 9.4 Create product (TENANT_ADMIN or ADMIN)

Request:

- `POST {{baseUrl}}/{{tenantSlug}}/products`
- Header: `Authorization: Bearer {{tenantAdminToken}}`
- Body:

```json
{
  "name": "Test Running Shoe",
  "description": "High cushion running shoe",
  "price": 129.99,
  "category": "Footwear",
  "availableQuantity": 20,
  "imageUrl": "https://example.com/shoe.png"
}
```

Expected:

- `201 Created`
- Save returned id as `{{productId}}`

## 9.5 Update product

Request:

- `PUT {{baseUrl}}/{{tenantSlug}}/products/{{productId}}`
- Header: `Authorization: Bearer {{tenantAdminToken}}`
- Body:

```json
{
  "name": "Test Running Shoe v2",
  "description": "Updated description",
  "price": 139.99,
  "category": "Footwear",
  "availableQuantity": 18,
  "imageUrl": "https://example.com/shoe-v2.png"
}
```

Expected:

- `200 OK`

## 9.6 Update stock

Request:

- `PATCH {{baseUrl}}/{{tenantSlug}}/products/{{productId}}/stock`
- Header: `Authorization: Bearer {{tenantAdminToken}}`
- Body:

```json
{
  "availableQuantity": 50
}
```

Expected:

- `200 OK`
- `availableQuantity` updated

## 9.7 Tenant-wide order view

Request:

- `GET {{baseUrl}}/{{tenantSlug}}/orders`
- Header: `Authorization: Bearer {{tenantAdminToken}}`

Expected:

- `200 OK`
- Tenant orders list

Negative check:

- Same request with `{{userToken}}` should return `403 Forbidden`

## 9.8 Delete product

Request:

- `DELETE {{baseUrl}}/{{tenantSlug}}/products/{{productId}}`
- Header: `Authorization: Bearer {{tenantAdminToken}}`

Expected:

- `204 No Content`

---

## 10) Full negative test checklist

## Authentication

- Invalid login password => `400`
- `/api/auth/me` without token => `401`
- Register existing username/email => `400`
- Register with role `ADMIN` or `TENANT_ADMIN` => `400`

## Authorization

- USER creating tenant => `403`
- USER creating product => `403`
- USER reading tenant orders endpoint => `403`

## Validation

- Missing required fields in request body => `400`
- Invalid email format in register => `400`
- Order with empty items => `400`

## Domain rules

- Add duplicate favourite => `400`
- Order quantity larger than stock => `400`
- Non-existing tenant or product => `404`

---

## 11) Suggested Hoppscotch collections structure

Create folders:

1. `01-Auth`
2. `02-Tenants`
3. `03-Products`
4. `04-Favourites`
5. `05-Orders`
6. `99-Negative-Tests`

Save each request with:

- Proper headers
- Example payload
- Test notes in description

---

## 12) Useful troubleshooting

- `401 Unauthorized`:
  - Token expired/invalid
  - Wrong realm/client config
  - Backend `issuer-uri` not matching Keycloak realm URL
- `403 Forbidden`:
  - Token user does not have required role
- `400 Invalid username or password`:
  - Keycloak user/client setup issue
  - Direct Access Grants disabled for client
- `Tenant not found`:
  - tenant slug mismatch between path and DB seed

---

## 13) Final sanity run

Before submission, validate this exact sequence once:

1. Register USER
2. Login USER
3. Fetch profile (`/me`)
4. List products
5. Add favourite + list favourites
6. Place order + list order history
7. Login TENANT_ADMIN and create/update product
8. Login ADMIN and create tenant
9. Run key negative tests (`401`, `403`, `400`, `404`)

If all pass, your manual API verification is complete.
