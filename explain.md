# 📘 Multi-Tenant E-Commerce Platform — Ultimate Line-by-Line Code Handbook & Interview Guide (`explain.md`)

This document is an exhaustive, line-by-line and block-by-block code explanation handbook for the entire application. It covers every major backend service, controller, configuration, entity model, DTO, security class, and frontend component to ensure you can explain and answer questions about every single line of code during project evaluations, code walkthroughs, viva examinations, or technical interviews.

---

## 📑 Table of Contents
1. [System Overview & Architecture](#1-system-overview--architecture)
2. [Why Keycloak IAM? (Evaluation & Interview Q&A)](#2-why-keycloak-iam-evaluation--interview-qa)
3. [Multi-Tenancy & URL Routing Architecture](#3-multi-tenancy--url-routing-architecture)
4. [Backend Line-by-Line Code Explanation](#4-backend-line-by-line-code-explanation)
   - [4.1 Entry Point & Configuration](#41-entry-point--configuration)
   - [4.2 Security & Keycloak IAM Layer](#42-security--keycloak-iam-layer)
   - [4.3 Service Layer (Core Business Logic)](#43-service-layer-core-business-logic)
   - [4.4 Controller Layer (REST API Endpoints)](#44-controller-layer-rest-api-endpoints)
   - [4.5 Domain Models & JPA Entities](#45-domain-models--jpa-entities)
   - [4.6 Data Transfer Objects (DTOs)](#46-data-transfer-objects-dtos)
5. [Frontend Line-by-Line Code Explanation](#5-frontend-line-by-line-code-explanation)
   - [5.1 API Client (`api.js`)](#51-api-client-apijs)
   - [5.2 Main Application Component (`App.jsx`)](#52-main-application-component-appjsx)
   - [5.3 Stylesheets (`index.css` & `App.css`)](#53-stylesheets-indexcss--appcss)
6. [Assignment Requirements Compliance Matrix](#6-assignment-requirements-compliance-matrix)
7. [Top 10 Interview & Viva Questions with Full Answers](#7-top-10-interview--viva-questions-with-full-answers)

---

## 1. System Overview & Architecture

This application is a **Production-Grade Multi-Tenant E-Commerce Platform** built using:
- **Backend Framework**: Spring Boot 3.5.0 (Java 21)
- **Identity & Access Management (IAM)**: Keycloak 24.0.1 (OAuth2 / OIDC / Service Accounts)
- **Database Layer**: H2 Database (Spring Data JPA / Hibernate ORM)
- **Frontend Framework**: Vite 8 + React 18 (Vanilla Glassmorphic CSS + Lucide Icons)

---

## 2. Why Keycloak IAM? (Evaluation & Interview Q&A)

### ❓ Why use Keycloak over custom JWT/Bcrypt authentication?
1. **Industry Standard OAuth2 & OpenID Connect 1.0**: Implements PKCE, bearer tokens, service accounts, and token validation out of the box.
2. **Centralized Identity & Security Isolation**: Passwords and policies are stored inside Keycloak's encrypted identity realm (`ecommerce-realm`), keeping sensitive user secrets out of the application database.
3. **Role-Based Access Control (RBAC)**: Keycloak manages realm roles (`ADMIN`, `TENANT_ADMIN`, `USER`), which Spring Security converts into granted authorities.
4. **Single Sign-On (SSO) Ready**: Multiple microservices or tenant applications can authenticate against a single Keycloak realm.

---

## 3. Multi-Tenancy & URL Routing Architecture

### Domain Isolation (`/{tenantSlug}/...`)
All brand operations are isolated under their URL slug (`/nike/...`, `/adidas/...`, `/puma/...`, `/apple/...`):
- `GET /{tenantSlug}/products` — Browse tenant products
- `POST /{tenantSlug}/products` — Create product (Admin / Tenant Admin)
- `POST /{tenantSlug}/orders` — Place order with stock validation
- `GET /{tenantSlug}/orders/my-history` — User order history
- `GET /{tenantSlug}/orders` — Store Manager view of all tenant customer orders

---

## 4. Backend Line-by-Line Code Explanation

### 4.1 Entry Point & Configuration

#### File: `EcommerceApplication.java`
```java
package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
```
- Line 1: `package com.ecommerce;` — Defines package location.
- Line 6: `@SpringBootApplication` — Composite annotation enabling `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`.
- Line 8-10: `main(String[] args)` — Launches Tomcat server on port 8080 and initializes Spring ApplicationContext.

---

#### File: `SecurityConfig.java`
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder, UserIdentityService userIdentityService) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/platform/tenants/**", "/{tenantSlug}/products/**", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(keycloakJwtAuthenticationConverter(userIdentityService)))
            );
        return http.build();
    }
```
- Line 2: `@EnableWebSecurity` — Enables Spring Security web protection.
- Line 3: `@EnableMethodSecurity` — Enables annotation-based authorization (`@PreAuthorize("hasRole('ADMIN')")`).
- Line 10: `.cors(...)` — Enables Cross-Origin Resource Sharing for Vite frontend (`http://localhost:5173`).
- Line 11: `.csrf(csrf -> csrf.disable())` — Disables CSRF for stateless REST API requests using Bearer JWT tokens.
- Line 12: `SessionCreationPolicy.STATELESS` — Configures Spring Security not to create HTTP sessions (`HttpSession`).
- Line 14: `.permitAll()` — Allows unauthenticated access to public catalog endpoints, tenant lists, and authentication endpoints.
- Line 17-18: `.oauth2ResourceServer(...)` — Configures Spring Security as an OAuth2 Resource Server validating incoming `Authorization: Bearer <jwt>` headers.

---

#### File: `DataInitializer.java`
```java
@Component
public class DataInitializer implements CommandLineRunner {
    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;
```
- Line 11: `implements CommandLineRunner` — Executes automatically after Spring Boot startup finishes.
- Line 23-25: `if (tenantRepository.count() > 0) return;` — Idempotency check ensuring initial seed data is created only once.
- Line 27-53: Creates initial brand tenants (`Nike`, `Adidas`, `Apple`, `Puma`).
- Line 55-163: Seeds realistic products with Unsplash photo URLs across categories (`Footwear`, `Apparel`, `Electronics`, `Accessories`).

---

### 4.2 Security & Keycloak IAM Layer

#### File: `KeycloakAdminService.java`
```java
public String createUser(RegisterRequest request, Role roleToAssign) {
    String adminToken = keycloakTokenService.getAdminAccessToken();
    Map<String, Object> payload = Map.of(
        "username", request.getUsername(),
        "email", request.getEmail(),
        "enabled", true,
        "emailVerified", true,
        "firstName", firstName,
        "lastName", lastName,
        "credentials", List.of(Map.of("type", "password", "value", request.getPassword(), "temporary", false))
    );
    // REST call to Keycloak Admin API
    restClient.post().uri(adminBaseUrl() + "/users").header("Authorization", "Bearer " + adminToken)...
```
- Line 33: Obtains Service Account Access Token using client credentials grant.
- Line 34-44: Splits user `name` into `firstName` and `lastName` to prevent Keycloak 24+ User Profile missing field errors.
- Line 46-62: Constructs JSON payload for Keycloak User Creation endpoint (`POST /admin/realms/ecommerce-realm/users`).
- Line 84: Calls `setPasswordAndClearActions` to set permanent password (`temporary: false`) and clear required user actions.
- Line 85: Calls `assignRealmRole` to grant the Keycloak Realm Role (`ADMIN`, `TENANT_ADMIN`, `USER`).

---

#### File: `KeycloakTokenService.java`
```java
public String loginAndGetAccessToken(String username, String password) {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "password");
    body.add("client_id", keycloakProperties.getClientId());
    body.add("client_secret", keycloakProperties.getClientSecret());
    body.add("username", username);
    body.add("password", password);
    // POST request to /protocol/openid-connect/token
```
- Line 2-7: Prepares standard OAuth2 Password Grant (`grant_type=password`) form payload.
- Line 8-15: Executes POST call to Keycloak token endpoint and extracts `access_token` string from response JSON.

---

### 4.3 Service Layer (Core Business Logic)

#### File: `AuthService.java`
```java
public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new BadRequestException("Username is already taken");
    }
    Role role = request.getRole();
    if (role == null) {
        role = (request.getTenantSlug() != null && !request.getTenantSlug().isBlank()) ? Role.TENANT_ADMIN : Role.USER;
    }
    Tenant tenant = null;
    if (request.getTenantSlug() != null && !request.getTenantSlug().isBlank()) {
        tenant = tenantRepository.findBySlugIgnoreCase(request.getTenantSlug().trim()).orElseThrow(...);
    }
    String keycloakUserId = keycloakAdminService.createUser(request, role);
    User user = User.builder().name(request.getName()).username(request.getUsername()).email(request.getEmail()).role(role).tenant(tenant).build();
    User savedUser = userRepository.save(user);
    String token = keycloakTokenService.loginAndGetAccessToken(request.getUsername(), request.getPassword());
    return AuthResponse.builder().token(token)...build();
}
```
- Line 43-48: Validates uniqueness of `username` and `email` in database.
- Line 50-53: Role resolution logic: Assigns `TENANT_ADMIN` if `tenantSlug` or role `TENANT_ADMIN` is supplied; otherwise defaults to `USER`.
- Line 55-60: Resolves `Tenant` entity from DB when `tenantSlug` is supplied.
- Line 61: Provisions user inside Keycloak IAM.
- Line 62-72: Saves local `User` entity linked to `Tenant`.
- Line 73-85: Automatically logs user in and returns `AuthResponse` containing token, user ID, username, role, and tenant details.

---

#### File: `UserIdentityService.java`
```java
@Transactional
public User resolveOrProvisionUserFromJwt(Jwt jwt) {
    String keycloakUserId = jwt.getSubject();
    String username = trimToNull(jwt.getClaimAsString("preferred_username"));
    String email = trimToNull(jwt.getClaimAsString("email"));
    
    User existing = findExistingUser(keycloakUserId, username, email);
    Role mappedRole = extractRole(jwt);
    Tenant tenant = resolveTenant(jwt, mappedRole);

    if (existing != null) {
        existing.setKeycloakUserId(keycloakUserId);
        if (mappedRole != Role.USER || existing.getRole() == null) {
            existing.setRole(mappedRole);
        }
        if (tenant != null) {
            existing.setTenant(tenant);
        }
        return userRepository.save(existing);
    }
```
- Line 29-33: Extracts JWT subject (`sub`), `preferred_username`, `email`, and `name` claims.
- Line 35: Checks database for existing user by Keycloak ID, username, or email.
- Line 41-43: **Role Preservation Fix**: Preserves provisioned database roles (`TENANT_ADMIN` / `ADMIN`) so token revalidation (`GET /api/auth/me`) does not demote store managers to regular `USER`.

---

#### File: `ProductService.java`
```java
public void validateTenantAccess(User user, Tenant tenant) {
    if (user.getRole() == Role.ADMIN) return;
    if (user.getRole() == Role.TENANT_ADMIN) {
        if (user.getTenant() == null || !user.getTenant().getId().equals(tenant.getId())) {
            throw new UnauthorizedAccessException("Tenant admin cannot access another tenant's domain: " + tenant.getSlug());
        }
    }
}

@Transactional
public void deleteProduct(Long productId, String tenantSlug, User currentUser) {
    Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
    Product product = productRepository.findByIdAndTenantId(productId, tenant.getId()).orElseThrow(...);
    validateTenantAccess(currentUser, tenant);
    favouriteProductRepository.deleteByProductId(productId);
    orderItemRepository.deleteByProductId(productId);
    productRepository.delete(product);
}
```
- Line 1-8: `validateTenantAccess` — Enforces strict domain boundaries. `ADMIN` has cross-tenant access, while `TENANT_ADMIN` can only manage their own tenant's domain.
- Line 10-18: `deleteProduct` — Cascading delete logic removing child records in `favourite_products` and `order_items` before deleting `product`, preventing SQL `23503` foreign key constraint errors.

---

#### File: `OrderService.java`
```java
@Transactional
public OrderResponse createOrder(String tenantSlug, CreateOrderRequest request, User currentUser) {
    Tenant tenant = tenantService.getTenantEntityBySlug(tenantSlug);
    for (OrderItemRequest itemReq : request.getItems()) {
        Product product = productRepository.findByIdAndTenantId(itemReq.getProductId(), tenant.getId()).orElseThrow(...);
        if (itemReq.getQuantity() > product.getAvailableQuantity()) {
            throw new InsufficientStockException("Cannot order " + itemReq.getQuantity() + " units. Available: " + product.getAvailableQuantity());
        }
        product.setAvailableQuantity(product.getAvailableQuantity() - itemReq.getQuantity());
        productRepository.save(product);
        ...
    }
}
```
- Line 54-56: Stock Availability Check — Throws `InsufficientStockException` if `requestedQuantity > availableQuantity`.
- Line 58-59: Atomic Stock Deduction — Transactionally decrements `availableQuantity` in database upon successful order creation.

---

### 4.4 Controller Layer (REST API Endpoints)

#### File: `MultiTenantProductController.java`
- `@GetMapping("/{tenantSlug}/products")` — Public endpoint to search and filter products by tenant slug, category, and name query.
- `@PostMapping("/{tenantSlug}/products")` — `@PreAuthorize("hasRole('ADMIN') or hasRole('TENANT_ADMIN')")` endpoint to create a new product under tenant domain.
- `@PATCH("/{tenantSlug}/products/{id}/stock")` — Updates stock level.
- `@DELETE("/{tenantSlug}/products/{id}")` — Cleanly deletes product with cascading cleanup.

#### File: `MultiTenantOrderController.java`
- `@PostMapping("/{tenantSlug}/orders")` — Customer endpoint to place an order under active tenant.
- `@GetMapping("/{tenantSlug}/orders/my-history")` — Customer endpoint to view personal order history.
- `@GetMapping("/{tenantSlug}/orders")` — Store Manager endpoint (`ADMIN` / `TENANT_ADMIN`) to view all customer orders under brand store.

---

### 4.5 Domain Models & JPA Entities

- **`Tenant.java`**: `@Entity` table `tenants` (`id`, `name`, `slug` [unique], `description`, `logoUrl`).
- **`User.java`**: `@Entity` table `users` (`id`, `username` [unique], `email` [unique], `name`, `keycloakUserId`, `role` [enum: `ADMIN`, `TENANT_ADMIN`, `USER`], `@ManyToOne Tenant`).
- **`Product.java`**: `@Entity` table `products` (`id`, `@ManyToOne Tenant`, `name`, `description`, `price`, `category`, `availableQuantity`, `imageUrl`).
- **`Order.java`**: `@Entity` table `orders` (`id`, `@ManyToOne User`, `@ManyToOne Tenant`, `orderDate`, `totalQuantity`, `totalAmount`, `status`, `@OneToMany List<OrderItem>`).
- **`OrderItem.java`**: `@Entity` table `order_items` (`id`, `@ManyToOne Order`, `@ManyToOne Product`, `quantity`, `unitPrice`, `subtotal`).
- **`FavouriteProduct.java`**: `@Entity` table `favourite_products` (`id`, `@ManyToOne User`, `@ManyToOne Product`).

---

## 5. Frontend Line-by-Line Code Explanation

### 5.1 API Client (`api.js`)
```javascript
export const apiClient = axios.create({
  baseURL: getApiBaseUrl(),
  headers: { 'Content-Type': 'application/json' },
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config;
})
```
- Sets up Axios base client pointing to `http://localhost:8080`.
- Implements an Axios Request Interceptor that automatically attaches `Authorization: Bearer <token>` header to all outgoing API requests when `auth_token` exists in `localStorage`.

---

### 5.2 Main Application Component (`App.jsx`)
- **`useCallback` functions (`fetchTenants`, `fetchCurrentUser`, `fetchProducts`, `fetchFavourites`, `fetchOrders`)**: Placed at top level to ensure lexical variable hoisting compliance and prevent React runtime `ReferenceError`.
- **`fetchOrders`**: Dynamically switches URL:
  - If `isManager` (`ADMIN` or `TENANT_ADMIN`) $\rightarrow$ calls `GET /{tenantSlug}/orders` (fetches store orders with customer name badges).
  - If regular `USER` $\rightarrow$ calls `GET /{tenantSlug}/orders/my-history`.
- **Order Checkout Modal**: Form overlay with real-time quantity multiplier and disabled input styles preventing browser default background overlays.
- **Tab Navigation System**: Store Catalog, Order History & Receipts, Saved Favourites, Add Product (Manager), Platform Admin Console (Admin), Sign In / Register.

---

## 6. Assignment Requirements Compliance Matrix

| # | Assignment Feature / Requirement | Implementation File(s) | Status |
|---|----------------------------------|------------------------|--------|
| **1** | Users can sign up, login & purchase available products. Unique username. | `AuthService.java`, `UserRepository.java` | ✅ Complete |
| **2** | Platform admin can add/remove tenants & manage tenant users. | `PlatformTenantController.java`, `TenantService.java` | ✅ Complete |
| **3** | Tenants can manage products & stock in their domain. | `MultiTenantProductController.java`, `ProductService.java` | ✅ Complete |
| **4** | Tenant user cannot login/manage another tenant's domain. | `validateTenantAccess` in `ProductService.java` & `OrderService.java` | ✅ Complete |
| **5** | Products categorized and filtered based on category. | `getProductsByTenant` in `ProductService.java` | ✅ Complete |
| **6** | Products searched by name containing query. | `findByTenantIdAndNameContainingIgnoreCase` in `ProductRepository.java` | ✅ Complete |
| **7 & 8** | Order items created only when `quantity <= availableQuantity`. Deduct stock. | `createOrder` in `OrderService.java` | ✅ Complete |
| **9** | Order consists of order items, total quantity, total amount. | `OrderResponse.java`, `OrderItemResponse.java` | ✅ Complete |
| **10** | Users can view order history. | `getUserOrderHistory` in `OrderService.java` | ✅ Complete |
| **Bonus** | Favourite products mark/unmark & retrieve list. | `MultiTenantFavouriteController.java`, `FavouriteService.java` | ✅ Complete |

---

## 7. Top 10 Interview & Viva Questions with Full Answers

1. **Q: How is Multi-Tenancy implemented in your project?**
   - *A*: Via logical domain separation using URL path parameters (`/{tenantSlug}/...`). Products and Orders are linked to a `Tenant` entity, and `validateTenantAccess` verifies tenant managers can only mutate resources inside their brand domain.

2. **Q: Why did you use Keycloak instead of custom JWT authentication?**
   - *A*: Keycloak provides standard OIDC/OAuth2 protocols, centralized credential security, role mapping (`ADMIN`, `TENANT_ADMIN`, `USER`), service account integration, and SSO readiness.

3. **Q: How do you prevent stock overselling during concurrent order placements?**
   - *A*: Within `@Transactional createOrder`, requested quantities are validated against available stock. Stock is transactionally decremented (`availableQuantity - quantity`). If stock is insufficient, transaction rolls back.

4. **Q: How do you solve Foreign Key Constraint Violations (SQL Error 23503) when deleting a Tenant or Product?**
   - *A*: By using `@Transactional` cascading cleanup routines. Deleting a product removes its entries from `favourite_products` and `order_items` first. Deleting a tenant unlinks user references and deletes tenant orders, items, products, and favourites before removing the tenant.

5. **Q: How does Role-Based Access Control (RBAC) work in Spring Security?**
   - *A*: `KeycloakJwtAuthenticationConverter` extracts roles from JWT token claims (`realm_access.roles`) and assigns Spring `GrantedAuthority` objects (`ROLE_ADMIN`, `ROLE_TENANT_ADMIN`, `ROLE_USER`) checked via `@PreAuthorize`.

6. **Q: What happens when a user registers as a Tenant Store Manager?**
   - *A*: `AuthService` provisions the account in Keycloak with `TENANT_ADMIN` role and saves the local `User` entity linked to the requested `Tenant`.

7. **Q: How does the frontend attach authentication tokens to requests?**
   - *A*: `api.js` uses an Axios Request Interceptor that retrieves `auth_token` from `localStorage` and sets `Authorization: Bearer <token>`.

8. **Q: What details are included in an Order receipt response?**
   - *A*: Metadata (ID, order date, total quantity, total amount, status), Customer info (username, full name), and itemized order items (product ID, name, category, image URL, quantity, unit price, subtotal).

9. **Q: How are search and category filters implemented?**
   - *A*: In `ProductService.java`, JPA repository query methods (`findByTenantIdAndNameContainingIgnoreCase` and `findByTenantIdAndCategoryIgnoreCase`) filter products directly in SQL.

10. **Q: How do you test and verify all API endpoints?**
    - *A*: We run automated integration scripts (`test_all_apis.ps1`) executing full HTTP requests against all 20 API endpoints.
