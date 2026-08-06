# Omni Store — Multi-Tenant E-Commerce Platform

A full-stack, multi-tenant e-commerce application where the platform hosts multiple independent **brand stores** (tenants) such as Samsung, Sony, IKEA, Apple, Nike, and Adidas. Each brand runs its own storefront and product catalog, while a central platform administrator governs the brands and the users attached to them.

Authentication and authorization are handled by **Keycloak** (OAuth2 / OIDC), and every request is secured with JWT bearer tokens. The system supports three roles:

| Role          | Description                                                            |
| ------------- | ---------------------------------------------------------------------- |
| `ADMIN`       | Platform administrator. Manages brands and users across the platform.  |
| `TENANT_ADMIN`| Brand (tenant) administrator. Manages products and orders of one brand.|
| `USER`        | Customer. Browses brands, manages a cart, favourites and orders.       |

---

## Tech Stack

**Backend** (`backend/`)
- Java 21, Spring Boot 3.5.0
- Spring Web, Spring Data JPA (Hibernate)
- Spring Security + OAuth2 Resource Server (JWT / Keycloak)
- H2 in-memory database (with H2 web console)
- Lombok, Bean Validation, Mockito / JUnit tests (Maven)

**Frontend** (`frontend/`)
- React 19 + Vite 8
- Tailwind CSS 4
- React Router 7, TanStack React Query 5, Zustand (state)
- Axios (API client), React Hook Form + Zod (forms)
- lucide-react icons, react-hot-toast notifications

**IAM**
- Keycloak 25.x running on `http://localhost:8081`

---

## Project Structure

```
eCommerce_Project/
├── README.md
├── backend/                        # Spring Boot REST API
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/ecommerce/
│       │   │   ├── config/         # DataInitializer (seed data), KeycloakProperties
│       │   │   ├── controller/     # REST controllers (REST API surface)
│       │   │   ├── dto/            # request/response DTOs
│       │   │   ├── exception/      # custom exceptions + global error handler
│   │   │   ├── model/          # JPA entities (User, Tenant, Product, Cart, Order, Notification…)
│       │   │   ├── repository/     # Spring Data JPA repositories
│       │   │   ├── security/       # SecurityConfig, JWT -> User converter
│       │   │   └── service/        # business logic + Keycloak admin/token services
│       │   └── resources/
│       │       └── application.properties   # DB, CORS, Keycloak, JWT settings
│       └── test/java/com/ecommerce/service/ # unit tests for all services
└── frontend/                       # React SPA
    ├── package.json
    ├── .env                        # VITE_API_BASE_URL=http://localhost:8080
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── api/                    # axios client, react-query client
        ├── assets/                 # static images
        ├── components/             # shared UI (navbar, product cards, tables, notification bell…)
        ├── features/               # feature-scoped forms + schemas (auth, products, tenants)
        ├── hooks/                  # react-query data hooks
        ├── layouts/                # MainLayout, TenantDashboardLayout
        ├── pages/                  # route pages (public, user, tenant, admin)
        ├── routes/                 # ProtectedRoute, AdminRoute, TenantAdminRoute…
        ├── services/               # API call functions
        ├── store/                  # Zustand auth store (JWT + user)
        └── utils/                  # HTTP status helpers
```

### Backend architecture

The backend follows a layered architecture:

```
Controller ──> Service ──> Repository ──> H2 Database
                    │
                    └──> Keycloak Admin API (user creation, role/tenant assignment)
```

- `controller/` — REST endpoints; method-level authorization via `@PreAuthorize`.
- `service/` — business rules (stock validation, order totals, tenant isolation).
- `security/` — `KeycloakJwtAuthenticationConverter` resolves the JWT into a `User` principal (provisioning it in the DB if needed) and derives the Spring authority (`ROLE_ADMIN`, `ROLE_TENANT_ADMIN`, `ROLE_USER`).
- `config/DataInitializer` — seeds 14 brand tenants (Samsung, Sony, IKEA, UNIQLO, LEGO, Canon, Bose, The North Face, Apple, Nike, Adidas, Puma, Reebok, Levi's) with sample products on startup (`dev` profile).

### Multi-tenancy model

Every brand is a `Tenant` identified by a unique **slug** (e.g. `nike`). Tenant-scoped endpoints are routed through the slug in the URL:

```
/{tenantSlug}/products          browse / manage a brand's products
/{tenantSlug}/cart              a user's cart for that brand
/{tenantSlug}/favourites        a user's favourites for that brand
/{tenantSlug}/orders            place orders / order history
```

Public, cross-brand endpoints live under `/api/...`:
- `GET /api/tenants` — list all brands
- `GET /api/tenants/{slug}` — brand details
- `GET /api/public/products` — browse all products across brands
- `/api/auth/register`, `/api/auth/login`, `/api/auth/me` — authentication
- `/api/notifications/**` — in-app alerts for the signed-in user (e.g. promoted to `TENANT_ADMIN`)
- `/api/platform/**` — **ADMIN only** (manage tenants, users, all orders)

---

## Prerequisites

- **Java 21+** and **Maven 3.8+**
- **Node.js 18+** and **npm**
- **Docker** (to run Keycloak)
- A Keycloak instance reachable at `http://localhost:8081`

---

## 1. Start Keycloak (IAM)

Run Keycloak on port `8081`:

```bash
docker run --name omni-store-keycloak -d \
  -p 8081:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:25.0.6 start-dev
```

Admin console: `http://localhost:8081/admin` (login with `admin / admin`).

### Configure realm and client

The backend expects the following (already configured in `backend/src/main/resources/application.properties`):

- **Realm:** `Omni_Store Realm`
- **Client:** `Omni_Store_client` (confidential client)
  - Client authentication: **ON**
  - **Direct access grants: ON** (required for the password-grant login used by `/api/auth/login`)
  - Copy the client secret into `app.keycloak.client-secret` (and `admin-client-secret`) in `application.properties`.

The realm roles `ADMIN`, `TENANT_ADMIN`, and `USER` are **created automatically** by the backend (`KeycloakAdminService`) on first registration if they don't exist — no manual role setup is required.

---

## 2. Run the backend

From the `backend/` directory:

```bash
cd backend
mvn clean spring-boot:run
```

- API base URL: `http://localhost:8080`
- H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:ecommercedb`, user `sa`, empty password)
- On startup, the `DataInitializer` seeds 14 brand stores and their products.
- Run tests with: `mvn test`

---

## 3. Run the frontend

From the `frontend/` directory:

```bash
cd frontend
npm install
npm run dev
```

- Open `http://localhost:5173`
- The API base URL is read from `frontend/.env` (`VITE_API_BASE_URL=http://localhost:8080`). Update it if your backend runs elsewhere.
- Lint with `npm run lint`, build with `npm run build`.

---

## How the roles work (end-to-end check)

> Make sure Keycloak, the backend, and the frontend are all running before testing.

### Registration & role assignment

- Open `http://localhost:5173` and use **Register**.
- **USER:** leave the *Tenant Slug* field empty → account is created with role `USER`.
- **TENANT_ADMIN:** enter a valid brand slug (e.g. `apple`, `nike`, `samsung`) in the *Tenant Slug* field → account is created with role `TENANT_ADMIN` for that brand.
- **ADMIN:** cannot be created through the UI. Create the admin user manually in the Keycloak Admin Console (Realm `Omni_Store Realm` → Users → add user, set a password, assign the `ADMIN` realm role), or use a username `adminuser` / `platform_admin`, which the backend maps to `ADMIN` automatically. Login with that user.

---

### Role: USER (customer)

1. Register as a normal user (no tenant slug) and log in.
2. **Browse:** on the home page (`/products`) and `/brands`, browse all products and open any brand store (e.g. `http://localhost:5173/nike/products`). Use the search bar, category and price filters, and pagination.
3. **Product details:** open a product to see full details and available stock.
4. **Favourites:** click the heart icon on a product, then view them under **Favourites**.
5. **Cart:** add items to the cart (cart badge updates), adjust quantities, and remove items on the cart page.
6. **Order:** place an order from the cart → stock is decremented and the order is created. View your order history under **Orders**.
7. **Profile:** check your account details on the **Profile** page.
8. **Negative check:** browsing `/admin/dashboard` or `/tenant/dashboard` redirects back to the store (route guards).

---

### Role: TENANT_ADMIN (brand manager)

1. Register with the *Tenant Slug* of an existing brand (e.g. `apple`), or have a platform `ADMIN` promote you via the admin dashboard. Log in as that user.
2. When promoted by an `ADMIN`, you are **alerted in-app** — a toast pops up and a notification appears in the bell icon (top-right) saying you have been made `TENANT ADMIN` of that brand. Sign out and back in to refresh your token so the **Dashboard** button appears.
3. The **Dashboard** button (top-right, shown only for `TENANT_ADMIN` in both the desktop and mobile navbars) → `/tenant/dashboard`.
4. **Manage products** (`/tenant/dashboard/products`):
   - Add new products (name, description, price, category, stock, image).
   - Edit existing products.
   - Update stock quantities.
   - Delete products.
   - These changes appear immediately on the public storefront of that brand.
5. **View orders** (`/tenant/dashboard/orders`): every order successfully placed on your brand is saved permanently to the database and listed here the moment it is created — customer, totals, status and items.
6. **Tenant isolation check:** you can only manage products and view orders of *your* brand. Attempts to modify another brand's products (e.g. a `PATCH /sony/products/{id}/stock`) **or** to read another brand's orders (`GET /sony/orders`) are rejected with `403` — even though the endpoint exists, the backend only returns orders that belong to your tenant.
7. You can still shop as a normal customer (cart, favourites, orders) for your own brand.

---

### Role: ADMIN (platform administrator)

1. Login as the platform admin user (created in Keycloak; username `platform_admin` or `adminuser` works out of the box).
2. Click **Admin** (top-right) → `/admin/dashboard`. The dashboard has three sections:
   - **Brands:** add a new brand (name, slug, description, logo) or delete an existing one. Deleting a brand removes it from the storefront.
   - **Assign Brand Admin:** pick any regular `USER` and assign them a brand → they are promoted to `TENANT_ADMIN` (role + `tenantSlug` attribute are applied in Keycloak **behind the scenes**) and the promoted user is **informed by an in-app alert** that they have been made `TENANT ADMIN` of that brand.
   - **All Orders:** view every order placed across all brands — each successfully placed order is persisted forever, whether or not the brand still exists.
3. **Product oversight:** like a tenant admin, the `ADMIN` can also create / edit / delete products and update stock on any brand.
4. **Negative check:** an `ADMIN` cannot place orders (rejected by the backend), and cart navigation is hidden for admins.

---

## API overview

| Method & Path                              | Access                          |
| ------------------------------------------ | ------------------------------- |
| `POST /api/auth/register`                  | Public                          |
| `POST /api/auth/login`                     | Public                          |
| `GET  /api/auth/me`                        | Authenticated                   |
| `GET  /api/tenants` / `/api/tenants/{slug}`| Public                          |
| `GET  /api/public/products`                | Public                          |
| `GET  /{tenantSlug}/products`              | Public                          |
| `GET  /{tenantSlug}/products/{id}`         | Public                          |
| `POST/PUT/DELETE /{tenantSlug}/products…`  | `ADMIN`, `TENANT_ADMIN`         |
| `PATCH /{tenantSlug}/products/{id}/stock`  | `ADMIN`, `TENANT_ADMIN`         |
| `GET/POST/PUT/DELETE /{tenantSlug}/cart`   | Authenticated (`ADMIN` excluded)|
| `POST/DELETE/GET /{tenantSlug}/favourites` | Authenticated                   |
| `POST /{tenantSlug}/orders`                | Authenticated (`ADMIN` excluded)|
| `GET  /{tenantSlug}/orders/my-history`     | Authenticated                   |
| `GET  /{tenantSlug}/orders`                | `ADMIN`, `TENANT_ADMIN`         |
| `GET  /api/platform/orders`                | `ADMIN` only                    |
| `POST/DELETE /api/platform/tenants`        | `ADMIN` only                    |
| `GET  /api/platform/users`                 | `ADMIN` only                    |
| `PUT  /api/platform/users/{id}/tenant`     | `ADMIN` only                    |
| `GET  /api/notifications`                  | Authenticated                  |
| `PUT  /api/notifications/{id}/read`        | Authenticated (owner only)     |
| `PUT  /api/notifications/read-all`         | Authenticated (owner only)     |

All protected endpoints accept an `Authorization: Bearer <jwt>` header, where the token is obtained from `/api/auth/login`.
