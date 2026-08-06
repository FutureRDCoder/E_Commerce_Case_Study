# Workflow & Request-Flow Documentation

End-to-end flow documentation for the multi-tenant **eCommerce** platform ("Omni Store"), covering **both frontend and backend**, for each of the three roles:

| Role | Meaning | Can shop? | Can manage products/orders? | Can manage platform? |
| --- | --- | --- | --- | --- |
| `USER` | Regular customer | ✅ | ❌ | ❌ |
| `TENANT_ADMIN` | Admin of a single brand (tenant) | ✅ (same as USER) | ✅ (only their own brand) | ❌ |
| `ADMIN` | Platform / super admin | ❌ | ✅ (any brand) | ✅ (brands, users, all orders) |

Companion docs: `FRONTEND_DOCUMENTATION.md` (line-by-line frontend reference) and `README.md` (project overview).

---

## 1. System Overview

```
┌─────────────────────────────┐        ┌──────────────────────────────────────────────┐
│  Frontend (React + Vite)    │        │  Backend (Spring Boot 3.5 / Java 21)         │
│  http://localhost:5173      │        │  http://localhost:8080                        │
│                             │  HTTP   │                                              │
│  Page ──▶ Hook ──▶ Service ─┼────────▶│  SecurityFilterChain ─▶ Controller ─▶ Service│
│  (useX)     (fetch fn)      │  JSON   │      │                                        │
│  ▲                            Bearer  │  KeycloakJwtAuthenticationConverter          │
│  │  React Query cache          token  │      │                                        │
│  └─ mutations invalidate       ▲       │  UserIdentityService (provision User)       │
│       query keys               │       │                                              │
│  Zustand authStore             │       │  Service ─▶ Repository ─▶ H2 (file DB)      │
│  (persisted "auth-storage")    │       │      │                                       │
│                                │       │      ▼                                       │
│                                │       │  Keycloak (http://localhost:8081)           │
│                                │       │  identity, tokens, roles, tenantSlug attr   │
└─────────────────────────────┘        └──────────────────────────────────────────────┘
```

### Key config
- Frontend API base: `VITE_API_BASE_URL=http://localhost:8080` (`frontend/.env`).
- Backend: `server.port=8080`, H2 file DB `./data/ecommercedb` (`application.properties`).
- Keycloak: `http://localhost:8081`, realm `Omni_Store Realm`, client `Omni_Store_client` (server-side password + admin client-credentials grants).
- CORS allows `localhost:5173` / `localhost:3000` (and `127.0.0.1`, `https`) — `SecurityConfig.java`.

---

## 2. Shared Request Pipeline

Every feature follows the same two halves.

### Frontend half (component → server)
1. **Component** (page or feature component) triggers an action or renders data.
2. **Hook** (`src/hooks/useX.js`) wraps the call in TanStack Query (`useQuery` for reads, `useMutation` for writes) and defines the cache key + what to invalidate on success.
3. **Service** (`src/services/xService.js`) builds the request URL and calls `apiClient`.
4. **`apiClient`** (`src/api/apiClient.js`) — the single Axios instance:
   - Attaches `Authorization: Bearer <token>` **unless** the URL is public (`/api/tenants…`, `/api/public/…`).
   - On a **401 response that had a token**, auto-logout via `authStore.logout()` (session expired).
5. **Store refresh** — successful mutations call `queryClient.invalidateQueries(...)`; queries re-fetch and the UI updates automatically. Toasts come from `react-hot-toast` (`Toaster` mounted in `main.jsx`).

### Backend half (request → response)
1. **`SecurityFilterChain`** (`SecurityConfig.java`): stateless, CORS on, CSRF off, H2 frames allowed, OAuth2 resource server with JWT.
2. **`KeycloakJwtAuthenticationConverter`** (`security/KeycloakJwtAuthenticationConverter.java`): for every request with a valid JWT it resolves-or-provisions the local `User` (via `UserIdentityService.resolveOrProvisionUserFromJwt`) and builds an `Authentication` with authority `ROLE_<ROLE>`.
3. **Authorization** (`configureAuthorization`): public allow-list first, then `/api/platform/** → ROLE_ADMIN`, then `anyRequest().authenticated()`. Method-level `@PreAuthorize` (via `@EnableMethodSecurity`) refines access on individual endpoints.
4. **Controller** maps URL/method → service call; injects the authenticated `User` with `@AuthenticationPrincipal`.
5. **Service** enforces business rules (tenant ownership, stock, soft-delete, role guard-rails) and talks to repositories → **H2** database. Some services talk to **Keycloak** (registration, token issuance, role/attribute management).
6. **Errors** flow through `GlobalExceptionHandler` → JSON `ErrorResponse { status, message, timestamp }`:
   - `ResourceNotFoundException` → 404, `BadRequestException`/`InsufficientStockException` → 400, `UnauthorizedAccessException`/`AccessDeniedException` → 403, validation errors → 400, everything else → 500.

### Role resolution (backend, `UserIdentityService.extractRole`)
From the JWT `realm_access.roles` claim:
1. contains `ADMIN` → `ADMIN`
2. else contains `TENANT_ADMIN` → `TENANT_ADMIN`
3. else username is `adminuser`/`platform_admin` → `ADMIN`
4. else → `USER`

### Tenant resolution for the authenticated user
Claim `tenantSlug` (or `tenant`) in the JWT → else the user's existing local tenant → else (for TENANT_ADMIN) looked up in Keycloak by username → else `null`. A TENANT_ADMIN token without any resolvable tenant throws `400`.

---

## 3. Authentication — Registration & Login (all roles)

### Registration (`USER` or `TENANT_ADMIN`)
**Frontend:** `RegisterPage` → `RegisterForm.jsx` (Zod validation via `registerSchema.js`) → `authService.register()` → **`POST /api/auth/register`**.

**Backend:** `AuthController.register` → `AuthService.register`:
1. Reject if username or email already in the local DB.
2. Role = `TENANT_ADMIN` if a `tenantSlug` was supplied, else `USER`.
3. If `tenantSlug` given → resolve the brand (404 if unknown).
4. `KeycloakAdminService.createUser` — creates the Keycloak user (password set, `requiredActions` cleared, realm role `USER`/`TENANT_ADMIN` assigned, `tenantSlug` attribute stored).
5. Save local `User` row (`keycloakUserId`, `role`, `tenant`).
6. `KeycloakTokenService.loginAndGetAccessToken` (password grant) → return `AuthResponse { token, userId, name, username, email, role, tenantId, tenantSlug, tenantName }`.

**Frontend:** `loginStore(token, user)` persists to localStorage (`auth-storage`), toast, `navigate("/")`.

> Note: `ADMIN` can **not** self-register; an ADMIN is provisioned through Keycloak (realm role `ADMIN` or the special username rule).

### Login (all roles)
**Frontend:** `LoginPage` → `LoginForm.jsx` → `authService.login()` → **`POST /api/auth/login`**.

**Backend:** `AuthService.login`:
1. `KeycloakTokenService.loginAndGetAccessToken` (password grant) → Keycloak returns the JWT (403/400 on bad credentials).
2. `AuthService.decodeJwt` — verified via `JwtDecoder`, falls back to unverified parsing.
3. `UserIdentityService.resolveOrProvisionUserFromJwt` — finds or creates the local user, sets role + tenant.
4. Return `AuthResponse`.

**Frontend:** `loginStore(data.token, data)` → toast → `navigate("/")`. `GuestRoute` then keeps authenticated users out of `/login` and `/register`.

### Session expiry
On any 401 where a token was attached, `apiClient`'s response interceptor calls `logout()` and the app returns to the guest experience.

---

## 4. Shared "Customer" Surface (`USER` and `TENANT_ADMIN`)

`TENANT_ADMIN` inherits every `USER` capability below (guards: `ProtectedRoute` for authenticated pages, `CustomerRoute` for favourites). Role-specific differences are marked. All prices render as `₹`.

### Entry & navigation
- **Entry:** open `/` or `/products` → `MainLayout` → `Navbar`. Authenticated users see: notifications bell, favourites heart, cart (with live count from `useCart("global")`), **Orders**, **Profile**, **Logout**. TENANT_ADMIN additionally sees a **Dashboard** button.
- Guest users see **Login / Register** links.
- `Navbar.jsx` reads `authStore.user.role` to show/hide links; the cart badge comes from `useCart("global")` → **`GET /global/cart`**.

### 4.1 Browse products
**Entry flow:** `/` → `ProductsPage.jsx` (global) or `/{tenantSlug}/products` (via `BrandsPage` → `BrandCard`, and `/brands`).

1. Filters state (`page`, `size=12`, debounced `search`, `category`, `priceRange`) → `useProducts(tenantSlug, params)` → `productService.getProducts`.
2. **Request:** `GET /api/public/products` (global) or `GET /{tenantSlug}/products` (brand).
   - Global path is public (no token header); brand path is `permitAll` but the token **is** attached when logged in → backend sets `isFavourite` per product.
3. **Backend:** `PublicProductController` / `MultiTenantProductController` → `ProductService.getAllProducts` / `getProducts` → `productRepository.searchProducts` (filters category, search, price range; sorted `id DESC`) → `ProductResponse` page (`content`, `number`, `totalPages`).
4. **Frontend:** `ProductGrid`/`ProductCard` renders name, category chip, `₹price`, stock chip, **View Details** + **Add to Cart**.

**Request params sent:** `page`, `size`, `search`, `category`, `minPrice`, `maxPrice`.

### 4.2 View product details
- **Entry flow:** `/{tenantSlug}/products/{productId}` → `ProductDetailsPage.jsx` → `useProduct(tenantSlug, productId)` → **`GET /{tenantSlug}/products/{id}`**.
- **Backend:** `ProductService.getProductById` — resolve tenant, `findByIdAndTenantId`, reject inactive product (404), return `ProductResponse`.
- Quantity input is clamped `1..availableQuantity`; **Add to Cart** uses the chosen quantity.

### 4.3 Add to cart
**Frontend:** `AddToCartButton.jsx` (card or details page) → `useAddToCart()` → `cartService.addToCart` → **`POST /{tenantSlug}/cart`** body `{ productId, quantity }`.
- Not logged in → redirect to `/login`.
- **Backend:** `MultiTenantCartController.addToCart` → `CartService.addToCart`: block `ADMIN` (403) → resolve tenant + product (404 if inactive) → stock check (400 `InsufficientStock`) → find-or-create the user's cart line, add quantity, re-check stock → save → `CartItemResponse`.
- **Frontend:** invalidates `["cart"]` (navbar badge + cart page refresh), success toast `"<name> added to cart"`.

### 4.4 Manage cart
- **Entry flow:** `/cart` → `CartPage.jsx` (`tenantSlug` defaults to `"global"`) → `useCart("global")` → **`GET /global/cart`**.
- **Backend:** `CartService.getCart` — `"global"`/blank returns **all** cart items across brands; otherwise filtered by `product.tenant.slug`. Each `CartItemResponse` includes `productId`, `productName`, `category`, `quantity`, `unitPrice`, `subtotal`, `productImageUrl`, `availableQuantity`, `tenantSlug`.
- **Quantity +/−:** `useUpdateCartItem` → **`PUT /global/cart/{itemId}`** body `{ quantity }` → `CartService.updateQuantity` (ownership check `findByIdAndUserId`; stock check).
- **Remove item:** `useRemoveCartItem` → **`DELETE /global/cart/{itemId}`** → ownership check, delete.
- **Place order:** see §4.5.
- **Backend block:** `ADMIN` is refused by `CartService.addToCart` (403); the UI hides cart/favourites/orders for ADMIN entirely.

### 4.5 Place an order (per-brand checkout)
**Frontend:** "Place Order" → `CartPage.handlePlaceOrder`:
1. Groups cart items by `item.tenantSlug` → one order per brand.
2. For each brand, `useCreateOrder().mutateAsync({ tenantSlug, order: { items: [{ productId, quantity }] } })` → **`POST /{brandSlug}/orders`**.
3. `clearCart.mutate("global")` → **`DELETE /global/cart`**.
4. Toast success → `navigate("/orders")`.

**Backend:** `MultiTenantOrderController.createOrder` → `OrderService.createOrder`:
1. Block `ADMIN` (403).
2. Validate ≥ 1 item.
3. `resolveOrderTenant` — use the path slug, or derive a single brand from the products (multiple brands → 400 "place a separate order for each brand").
4. For each item: load product (`findByIdAndTenantId`), must be active, stock check (400), **reduce stock** (save product), subtotal = `price × qty`; accumulate totals.
5. Save `Order` (`status=COMPLETED`, `orderDate=now`, totals) + `OrderItem` rows (unit price + subtotal snapshot).
6. Return `OrderResponse` with `items` (name, category, qty, unitPrice, subtotal, imageUrl) — this is what the expandable order panels render.

**Frontend after success:** `useCreateOrder` invalidates `["orders"]`, `["products"]`, `["cart"]`.

### 4.6 View order history
- **Entry flow:** `/orders` → `OrdersPage.jsx` → `useOrders("global")` → **`GET /global/orders/my-history`**.
- **Backend:** `OrderService.getUserOrderHistory` — `"global"`/blank → all the user's orders (`findByUserIdOrderByOrderDateDesc`); otherwise filtered to one brand.
- **Frontend:** each order card shows `#id`, status chip, `totalQuantity`, `₹totalAmount`, date. **Clicking a card** (`OrdersPage` `expandedOrderId` state, keyboard accessible) expands `<OrderDetails items={order.items} />` showing per-item image/placeholder, name, category, `Unit Price`, `Qty`, and gradient `₹subtotal`.

### 4.7 Favourites
- **Toggle on cards:** `FavouriteButton.jsx` → `useAddFavourite` / `useRemoveFavourite` → **`POST /{tenantSlug}/favourites/{productId}`** / **`DELETE /{tenantSlug}/favourites/{productId}`**.
  - Backend: `MultiTenantFavouriteController` (class-level `@PreAuthorize USER|TENANT_ADMIN` blocks ADMIN → 403) → `FavouriteService`: duplicate → 400; inactive product → 404.
  - Frontend: invalidates `["products"]`, `["product"]`, `["favourites"]` (hearts re-sync via `isFavourite`).
- **List:** `/favourites` → `FavouritesPage.jsx` → `useFavourites("global")` → **`GET /global/favourites`** → `FavouriteService.getUserFavourites` (all, or one brand) → array of `ProductResponse`.

### 4.8 Profile
- **Entry flow:** `/profile` → `ProfilePage.jsx` → `useProfile()` → **`GET /api/auth/me`**.
- **Backend:** `AuthController.getCurrentUser` — returns current `AuthResponse` from the authenticated principal (name, username, email, role, brand if any).

### 4.9 Notifications
- `NotificationBell.jsx` / `NotificationAlert.jsx` → `useNotifications()` (polls every 10 s while authenticated) → **`GET /api/notifications`** → `NotificationService.getNotificationsForUser(userId)` (newest first).
- Unread badge on the bell; unread items trigger a toast alert (once per session per id).
- Mark one → `useMarkNotificationAsRead` → **`PUT /api/notifications/{id}/read`** (ownership check → 403 if not yours).
- Mark all → `useMarkAllNotificationsAsRead` → **`PUT /api/notifications/read-all`**.

---

## 5. Role: `USER` — Complete Action Map

| Action | Frontend entry | Hook | Service | HTTP Endpoint (method) | Backend controller → service → DB |
| --- | --- | --- | --- | --- | --- |
| Register | `/register` → `RegisterForm` | (local mutation) | `authService.register` | `POST /api/auth/register` | `AuthController` → `AuthService` → Keycloak + `UserRepository` |
| Login | `/login` → `LoginForm` | (local mutation) | `authService.login` | `POST /api/auth/login` | `AuthController` → `AuthService` → Keycloak + `UserIdentityService` |
| Browse global products | `/` or `/products` | `useProducts` | `productService.getProducts` | `GET /api/public/products` | `PublicProductController` → `ProductService.getAllProducts` → `ProductRepository` |
| Browse brand products | `/brands` → `/{slug}/products` | `useProducts` | `productService.getProducts` | `GET /{slug}/products` | `MultiTenantProductController` → `ProductService.getProducts` |
| Product details | `/{slug}/products/{id}` | `useProduct` | `productService.getProductById` | `GET /{slug}/products/{id}` | `MultiTenantProductController` → `ProductService.getProductById` |
| Add to cart | `ProductCard`/details `AddToCartButton` | `useAddToCart` | `cartService.addToCart` | `POST /{slug}/cart` | `MultiTenantCartController` → `CartService.addToCart` |
| View cart | `/cart` | `useCart` | `cartService.getCart` | `GET /global/cart` | `MultiTenantCartController` → `CartService.getCart` |
| Change qty | `CartPage` +/− | `useUpdateCartItem` | `cartService.updateCartItem` | `PUT /global/cart/{itemId}` | `MultiTenantCartController` → `CartService.updateQuantity` |
| Remove item | `CartPage` trash | `useRemoveCartItem` | `cartService.removeCartItem` | `DELETE /global/cart/{itemId}` | `MultiTenantCartController` → `CartService.removeItem` |
| Place order | `CartPage` → Order Summary | `useCreateOrder` | `orderService.createOrder` | `POST /{slug}/orders` | `MultiTenantOrderController` → `OrderService.createOrder` |
| Clear cart | after placing order | `useClearCart` | `cartService.clearCart` | `DELETE /global/cart` | `MultiTenantCartController` → `CartService.clearCart` |
| Order history | `/orders` | `useOrders` | `orderService.getOrders` | `GET /global/orders/my-history` | `MultiTenantOrderController` → `OrderService.getUserOrderHistory` |
| Expand order items | `OrdersPage` card click | — | — | (uses `order.items` already returned) | `OrderService.mapToResponse` → `OrderItemResponse` |
| Add favourite | `FavouriteButton` heart | `useAddFavourite` | `favouriteService.addFavourite` | `POST /{slug}/favourites/{productId}` | `MultiTenantFavouriteController` → `FavouriteService.addFavourite` |
| Remove favourite | `FavouriteButton` heart | `useRemoveFavourite` | `favouriteService.removeFavourite` | `DELETE /{slug}/favourites/{productId}` | `MultiTenantFavouriteController` → `FavouriteService.removeFavourite` |
| Favourites list | `/favourites` | `useFavourites` | `favouriteService.getFavourites` | `GET /global/favourites` | `MultiTenantFavouriteController` → `FavouriteService.getUserFavourites` |
| Profile | `/profile` | `useProfile` | `profileService.getProfile` | `GET /api/auth/me` | `AuthController.getCurrentUser` |
| Notifications | `NotificationBell`/`NotificationAlert` | `useNotifications` | `notificationService.getNotifications` | `GET /api/notifications` | `NotificationController` → `NotificationService.getNotificationsForUser` |
| Mark notification read | `NotificationBell` item | `useMarkNotificationAsRead` | `notificationService.markNotificationAsRead` | `PUT /api/notifications/{id}/read` | `NotificationController` → `NotificationService.markAsRead` |
| Mark all read | `NotificationBell` header | `useMarkAllNotificationsAsRead` | `notificationService.markAllNotificationsAsRead` | `PUT /api/notifications/read-all` | `NotificationController` → `NotificationService.markAllAsRead` |

> **Denied to USER:** every product-management endpoint (`POST/PUT/PATCH/DELETE /{slug}/products…` → `@PreAuthorize ADMIN|TENANT_ADMIN`), tenant orders listing (`GET /{slug}/orders`), and all `/api/platform/**` endpoints (403 via filter chain + `@PreAuthorize`).

---

## 6. Role: `TENANT_ADMIN`

### Entry flow
Two ways to become a TENANT_ADMIN:
1. **Self-register** with a valid brand slug → role `TENANT_ADMIN` immediately (§3 registration).
2. **Assigned by ADMIN** (§7.3) → receives a notification "You have been made TENANT ADMIN of <brand>…". Note: the user must **log out and log in again** to receive the new role/slug in the JWT and therefore in the frontend `authStore`.

Navigation: `MainLayout` + `Navbar` shows everything a USER sees **plus** a **Dashboard** button → `/tenant/dashboard` (guarded by `TenantAdminRoute`, rendered inside `TenantDashboardLayout` sidebar: Dashboard / Products / Orders). The tenant's brand slug comes from `authStore.user.tenantSlug`.

TENANT_ADMIN also exercises **every USER action** (§4, §5). Their `useCart`, `useOrders`, etc. behave identically. Additionally:

### 6.1 Dashboard home
- `/tenant/dashboard` → `TenantDashboardHome.jsx` — placeholder welcome (no data yet).

### 6.2 Manage products
- **Entry flow:** `/tenant/dashboard/products` → `TenantProductsPage.jsx` → `useProducts(user.tenantSlug, { page: 0, size: 100 })` → **`GET /{slug}/products`** → `TenantProductTable`.

| Action | Frontend | HTTP Endpoint | Backend |
| --- | --- | --- | --- |
| Add product | `CreateProductModal` → `useCreateProduct` → `productService.createProduct` | `POST /{slug}/products` | `MultiTenantProductController.addProduct` → `ProductService.addProduct` (tenant access check) |
| Edit product | `EditProductModal` → `useUpdateProduct` → `productService.updateProduct` | `PUT /{slug}/products/{id}` | `MultiTenantProductController.updateProduct` → `ProductService.updateProduct` (active check) |
| Update stock | `UpdateStockForm` → `useUpdateProductStock` → `productService.updateProductStock` | `PATCH /{slug}/products/{id}/stock` | `MultiTenantProductController.updateStock` → `ProductService.updateStock` |
| Delete product | `DeleteProductButton` → `useDeleteProduct` → `productService.deleteProduct` | `DELETE /{slug}/products/{id}` | `MultiTenantProductController.deleteProduct` → `ProductService.deleteProduct` (soft-delete) |

**Tenant ownership rule** (`ProductService.validateTenantAccess`, called by all four):
- `ADMIN` → always allowed (any brand).
- `TENANT_ADMIN` → allowed **only** if the user's tenant id equals the target brand's id; otherwise 403 `UnauthorizedAccessException`.
- `USER` → always 403.

**Soft delete:** deleting a product deletes its cart items, then sets `active=false` (storefront/product reads then return 404 for it) while **historical orders and favourites stay intact**.

### 6.3 View incoming orders
- **Entry flow:** `/tenant/dashboard/orders` → `TenantOrdersPage.jsx` → `useTenantOrders(user.tenantSlug)` → **`GET /{slug}/orders`** (requires `@PreAuthorize ADMIN|TENANT_ADMIN`).
- **Backend:** `OrderService.getTenantOrders` — resolve brand, `validateTenantAccess` (must match own brand), return all orders for that brand (`findByTenantIdOrderByOrderDateDesc`).
- **Frontend:** `TenantOrderTable.jsx` — click a row (`expandedOrderId` state) to expand an itemized detail row (`colSpan={6}`) rendering `<OrderDetails items={order.items} />` with customer, date, status, totals, and per-item price/qty/subtotal.

### 6.4 Notifications
TENANT_ADMIN receives the "made TENANT ADMIN" notification (§4.9 pipeline). Bell + alert both poll `GET /api/notifications`.

### Endpoint summary (unique to TENANT_ADMIN, beyond USER)

| Action | Endpoint (method) | Guard |
| --- | --- | --- |
| Add product | `POST /{slug}/products` | `@PreAuthorize ADMIN\|TENANT_ADMIN` + service tenant check |
| Edit product | `PUT /{slug}/products/{id}` | `@PreAuthorize ADMIN\|TENANT_ADMIN` + service tenant check |
| Update stock | `PATCH /{slug}/products/{id}/stock` | `@PreAuthorize ADMIN\|TENANT_ADMIN` + service tenant check |
| Delete product | `DELETE /{slug}/products/{id}` | `@PreAuthorize ADMIN\|TENANT_ADMIN` + service tenant check |
| List tenant orders | `GET /{slug}/orders` | `@PreAuthorize ADMIN\|TENANT_ADMIN` + service tenant check |

> **Denied to TENANT_ADMIN:** `POST/DELETE /api/platform/tenants…`, `GET /api/platform/orders`, `GET /api/platform/users`, `PUT /api/platform/users/{id}/tenant` — all blocked by `configureAuthorization` (`/api/platform/**` → `ROLE_ADMIN`) and method security.

---

## 7. Role: `ADMIN` (Platform Admin)

### Entry flow
- **Identity:** provisioned in Keycloak with realm role `ADMIN` (or username `adminuser`/`platform_admin`). **Cannot self-register** and cannot be assigned a brand.
- **Login:** standard §3 login → JWT role `ADMIN` → `authStore` → redirect `/`.
- **Navigation:** `Navbar` shows **Admin** (→ `/admin/dashboard`); heart, cart, and orders links are hidden. `CartPage` and `OrdersPage` show an explanatory "Admin accounts cannot…" panel if forced.
- **Guard:** `/admin/dashboard` is wrapped in `AdminRoute` (requires `user.role === "ADMIN"`, else redirect `/`).

### 7.1 Dashboard
- **Entry flow:** `/admin/dashboard` → `AdminDashboardPage.jsx`:
  - `useTenants(0, 100)` → `GET /api/tenants?page=0&size=100` (public) → `AdminTenantTable`.
  - `useAllOrders()` → **`GET /api/platform/orders`** → `AdminOrderTable`.
  - `AssignTenantForm` → `useUsers(0,100)` (**`GET /api/platform/users?page=0&size=100&role=USER`**) + `useTenants(0,100)`.

### 7.2 Manage brands (create / delete)
| Action | Frontend | HTTP Endpoint | Backend |
| --- | --- | --- | --- |
| Create brand | `CreateTenantForm` (Zod `tenantSchema`) → `useCreateTenant` → `tenantService.createTenant` | `POST /api/platform/tenants` | `PlatformTenantController.createTenant` (`@PreAuthorize ADMIN`) → `TenantService.createTenant` (slug lowercased, uniqueness checks) |
| Delete brand | `DeleteTenantButton` (confirm dialog) → `useDeleteTenant` → `tenantService.deleteTenant` | `DELETE /api/platform/tenants/{id}` | `PlatformTenantController.deleteTenant` (`@PreAuthorize ADMIN`) → `TenantService.deleteTenant` |

**`TenantService.deleteTenant` cascade (soft-delete):**
1. Dissociate all users from the brand (`tenant = null`).
2. For each product: delete its cart items, `active=false`.
3. Deactivate the brand (`active=false`) — historical orders/favourites preserved.

> Brand reads (`GET /api/tenants`, `GET /api/tenants/{slug}`) are public and used by the storefront.

### 7.3 Assign a user as brand admin
**Frontend:** `AssignTenantForm.jsx` (user dropdown from `useUsers`, brand dropdown from `useTenants`) → `useAssignTenant` → `userService.assignTenantToUser` → **`PUT /api/platform/users/{userId}/tenant`** body `{ tenantId }`.

**Backend:** `PlatformUserController.assignTenant` (`@PreAuthorize ADMIN`) → `PlatformUserService.assignTenant`:
1. Load user (404); reject if target is `ADMIN` (400).
2. Load brand (404).
3. If the user has a Keycloak id → `KeycloakAdminService.assignTenantToUser`: fetch attributes, set `tenantSlug`, update user, assign realm role `TENANT_ADMIN`.
4. Set local user `role=TENANT_ADMIN` + `tenant`, save.
5. `NotificationService.notifyTenantAdminAssignment` — create an **unread notification** for that user.
6. Return `UserResponse`.

### 7.4 View all orders (platform-wide)
- **Frontend:** `AdminDashboardPage` → `useAllOrders()` → `orderService.getAllOrders` → **`GET /api/platform/orders`**.
- **Backend:** `PlatformOrderController.getAllOrders` (`@PreAuthorize ADMIN`) → `OrderService.getAllOrders` → `findAllByOrderByOrderDateDesc` → list of `OrderResponse` (includes brand + customer + items). Rendered read-only in `AdminOrderTable` (order id, brand, customer, date, status, quantity, total).

### 7.5 ADMIN as a shopkeeper
The backend **actively blocks** ADMIN from customer actions (defense in depth even though the UI hides them):
- `OrderService.createOrder` → 403 "Admin accounts are not allowed to place orders."
- `CartService.addToCart` → 403 "…not allowed to add products to the cart."
- `FavouriteService` → class-level `@PreAuthorize USER|TENANT_ADMIN` → 403.
- ADMIN **is** allowed product management (`POST/PUT/PATCH/DELETE /{slug}/products…` and `GET /{slug}/orders`) on any brand (no tenant-match restriction).

### Endpoint summary (unique to ADMIN)

| Action | Endpoint (method) | Guard |
| --- | --- | --- |
| Create brand | `POST /api/platform/tenants` | `/api/platform/**` → `ROLE_ADMIN` + `@PreAuthorize ADMIN` |
| Delete brand | `DELETE /api/platform/tenants/{id}` | same |
| All orders | `GET /api/platform/orders` | `@PreAuthorize ADMIN` |
| List users | `GET /api/platform/users?role=USER` | `@PreAuthorize ADMIN` |
| Assign brand admin | `PUT /api/platform/users/{userId}/tenant` | `@PreAuthorize ADMIN` |
| Manage any brand's products | `POST/PUT/PATCH/DELETE /{slug}/products…` | `@PreAuthorize ADMIN\|TENANT_ADMIN` (ADMIN skips tenant check) |
| List any brand's orders | `GET /{slug}/orders` | `@PreAuthorize ADMIN\|TENANT_ADMIN` |

> **Denied to ADMIN:** placing orders, cart mutations, favourites, personal order history — enforced in service/controller logic (403).

---

## 8. Full Endpoint Reference

Authorization legend: **public** = permitAll · **auth** = any authenticated · **ADMIN** = ROLE_ADMIN · **T-ADMIN** = ROLE_TENANT_ADMIN · **USER** = ROLE_USER.

### Public (no token required)
| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register USER / TENANT_ADMIN |
| `POST` | `/api/auth/login` | Login (returns JWT + user) |
| `GET` | `/api/tenants` | List active brands (paged) |
| `GET` | `/api/tenants/{slug}` | Single brand by slug |
| `GET` | `/api/public/products` | Global product listing |
| `GET` | `/{slug}/products` | Brand product listing (token optional → `isFavourite`) |
| `GET` | `/{slug}/products/{id}` | Brand product detail |
| `GET` | `/api/platform/tenants`, `/**` | Brand reads via platform path (permitAll GET) |
| `OPTIONS` | `/**` | CORS preflight |
| `GET` | `/h2-console/**` | H2 console (dev) |

### Authenticated — any role
| Method | Path | Purpose | Role note |
| --- | --- | --- | --- |
| `GET` | `/api/auth/me` | Current user profile | any |
| `GET` | `/global/cart` (or `/{slug}/cart`) | Cart contents | any (ADMIN gets empty/usable; mutations blocked) |
| `POST` | `/{slug}/cart` | Add to cart | **ADMIN → 403** (service) |
| `PUT` | `/{slug}/cart/{itemId}` | Update quantity | any (ownership enforced) |
| `DELETE` | `/{slug}/cart/{itemId}` | Remove item | any (ownership enforced) |
| `DELETE` | `/{slug}/cart` | Clear cart | any |
| `POST` | `/{slug}/orders` | Create order | **ADMIN → 403** (service) |
| `GET` | `/{slug}/orders/my-history` | Own order history | any (incl. ADMIN, though none exist) |
| `GET` | `/{slug}/favourites` | Favourites list | **USER/T-ADMIN only** (403 for ADMIN) |
| `POST` | `/{slug}/favourites/{productId}` | Add favourite | **USER/T-ADMIN only** |
| `DELETE` | `/{slug}/favourites/{productId}` | Remove favourite | **USER/T-ADMIN only** |
| `GET` | `/api/notifications` | List notifications | any |
| `PUT` | `/api/notifications/{id}/read` | Mark one read | any (ownership enforced) |
| `PUT` | `/api/notifications/read-all` | Mark all read | any |

### ADMIN and/or TENANT_ADMIN
| Method | Path | Purpose | Guard |
| --- | --- | --- | --- |
| `GET` | `/{slug}/orders` | Tenant's incoming orders | ADMIN \| T-ADMIN + service tenant check |
| `POST` | `/{slug}/products` | Create product | ADMIN \| T-ADMIN + service tenant check |
| `PUT` | `/{slug}/products/{id}` | Update product | ADMIN \| T-ADMIN + service tenant check |
| `PATCH` | `/{slug}/products/{id}/stock` | Update stock | ADMIN \| T-ADMIN + service tenant check |
| `DELETE` | `/{slug}/products/{id}` | Soft-delete product | ADMIN \| T-ADMIN + service tenant check |

### ADMIN only
| Method | Path | Purpose | Guard |
| --- | --- | --- | --- |
| `POST` | `/api/platform/tenants` | Create brand | `ROLE_ADMIN` |
| `DELETE` | `/api/platform/tenants/{id}` | Delete brand | `ROLE_ADMIN` |
| `GET` | `/api/platform/orders` | All orders across brands | `ROLE_ADMIN` |
| `GET` | `/api/platform/users` | List users (optional `role` filter) | `ROLE_ADMIN` |
| `PUT` | `/api/platform/users/{userId}/tenant` | Make user a brand admin | `ROLE_ADMIN` |

---

## 9. Cross-Cutting Behaviour Notes

- **Global vs brand scoping:** cart/orders/favourites treat `"global"` (or blank/`null`) as "all brands" and any real slug as "that brand only". Product reads always require a brand context (global storefront = `/api/public/products`).
- **Soft deletes:** products and brands are deactivated (`active=false`), never hard-deleted, so order history and favourites remain stable. Deleting a product/brand first removes its cart items.
- **Order status:** created orders are stored `COMPLETED` (initialized `PENDING` then set to `COMPLETED` in `OrderService.createOrder`).
- **`isFavourite`:** computed on every `ProductResponse` for an authenticated user; for the public global listing the token isn't sent (public path), so hearts there render empty.
- **Notification delivery:** there is no push; the frontend polls `GET /api/notifications` every 10 s and the `NotificationAlert` toasts new unread entries once per session.
- **Keycloak side-effects:** registration, role assignment, and tenant-slug attributes are written to Keycloak via the admin REST API; the app's own DB is the source of truth for orders/products/tenants, with Keycloak the source for identity/tokens.
- **Known quirks:** (1) TENANT_ADMIN promotion requires a fresh login to take effect in the UI (role/slug come from the JWT). (2) The mobile menu links ADMIN to `/admin/orders` which has no route (falls through to 404). (3) GET product/brand reads are `permitAll`, so `apiClient` deliberately sends **no** token for `/api/tenants…` and `/api/public/…`.
