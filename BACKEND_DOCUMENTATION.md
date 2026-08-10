# BACKEND_DOCUMENTATION

Complete line-by-line reference for the `backend/` folder of the **Multi-Tenant E-Commerce System** ("Omni Store").

This document explains every file and directory under `backend/src/` plus `backend/pom.xml` — covering every annotation, attribute, property, value, method, and behavior in the source code.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Directory Tree](#2-directory-tree)
3. [pom.xml (Maven Build File) — line-by-line](#3-pomxml-maven-build-file--line-by-line)
4. [src/main/resources/application.properties — line-by-line](#4-srcmainresourcesapplicationproperties--line-by-line)
5. [Main Application — EcommerceApplication.java](#5-main-application--ecommerceapplicationjava)
6. [config package](#6-config-package)
7. [model package (JPA Entities + Enums)](#7-model-package-jpa-entities--enums)
8. [repository package (Spring Data JPA)](#8-repository-package-spring-data-jpa)
9. [dto/request package (Request DTOs)](#9-dtorequest-package-request-dtos)
10. [dto/response package (Response DTOs)](#10-dtoresponse-package-response-dtos)
11. [exception package](#11-exception-package)
12. [security package](#12-security-package)
13. [service package (Business Logic)](#13-service-package-business-logic)
14. [controller package (REST API)](#14-controller-package-rest-api)
15. [test package (JUnit 5 + Mockito)](#15-test-package-junit-5--mockito)
16. [Cross-Cutting Reference Tables](#16-cross-cutting-reference-tables)
17. [OpenAPI / Swagger UI](#17-openapi--swagger-ui)

---

## 1. Project Overview

- **Project name (Maven):** `ecommerce-backend` v`1.0.0`
- **Description:** Multi-Tenant E-Commerce System Backend
- **Language / JDK:** Java 21
- **Build tool:** Maven (Spring Boot parent 3.5.0)
- **Web framework:** Spring Boot 3.5.0 (spring-web MVC, embedded Tomcat)
- **Persistence:** Spring Data JPA + Hibernate over **H2** file database (`./data/ecommercedb`)
- **Security:** Spring Security 6.x stateless filter chain + **OAuth2 Resource Server (JWT)**; Keycloak acts as the OAuth2 authorization server at `http://localhost:8081`
- **Validation:** Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Boilerplate reduction:** Lombok (`@Slf4j` only; no `@Getter`/`@Setter` — entities use explicit getters/setters)
- **Active Spring profile:** `dev` (enables `DataInitializer` seeding)
- **Roles (enum `Role`):** `ADMIN` (platform), `TENANT_ADMIN` (brand/tenant manager), `USER` (shopper)
- **Order states (enum `OrderStatus`):** `PENDING`, `COMPLETED`, `CANCELLED`
- A "tenant" is called a **brand** throughout the UI and error messages. `"global"` (or blank) tenant slug means "across all brands".
- Tests: 9 test classes using JUnit 5 (`junit-jupiter`) + Mockito, located under `backend/src/test/java/com/ecommerce/service/`.

---

## 2. Directory Tree

```
backend/
├── pom.xml                        ← Maven build configuration (analyzed in §3)
├── src/
│   ├── main/
│   │   ├── java/com/ecommerce/
│   │   │   ├── EcommerceApplication.java      ← Spring Boot entry point
│   │   │   ├── config/                        ← @Configuration beans
│   │   │   │   ├── DataInitializer.java        ← dev-only seed data (14 brands, 84 products)
│   │   │   │   ├── KeycloakProperties.java     ← app.keycloak.* @ConfigurationProperties
│   │   │   │   └── OpenApiConfig.java          ← OpenAPI info + bearer security scheme (§6.3)
│   │   │   ├── controller/                    ← REST endpoints (11 classes)
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── MultiTenantCartController.java
│   │   │   │   ├── MultiTenantFavouriteController.java
│   │   │   │   ├── MultiTenantOrderController.java
│   │   │   │   ├── MultiTenantProductController.java
│   │   │   │   ├── NotificationController.java
│   │   │   │   ├── PlatformOrderController.java
│   │   │   │   ├── PlatformTenantController.java
│   │   │   │   ├── PlatformUserController.java
│   │   │   │   ├── PublicProductController.java
│   │   │   │   └── PublicTenantController.java
│   │   │   ├── dto/
│   │   │   │   ├── request/                    ← request body DTOs (11 classes)
│   │   │   │   │   ├── AssignTenantRequest.java
│   │   │   │   │   ├── CartItemRequest.java
│   │   │   │   │   ├── CreateOrderRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── OrderItemRequest.java
│   │   │   │   │   ├── ProductRequest.java
│   │   │   │   │   ├── ProductSearchRequest.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   ├── StockUpdateRequest.java
│   │   │   │   │   ├── TenantRequest.java
│   │   │   │   │   └── UpdateCartItemRequest.java
│   │   │   │   └── response/                   ← response DTOs (8 classes)
│   │   │   │       ├── AuthResponse.java
│   │   │   │       ├── CartItemResponse.java
│   │   │   │       ├── NotificationResponse.java
│   │   │   │       ├── OrderItemResponse.java
│   │   │   │       ├── OrderResponse.java
│   │   │   │       ├── ProductResponse.java
│   │   │   │       ├── TenantResponse.java
│   │   │   │       └── UserResponse.java
│   │   │   ├── exception/                      ← custom exceptions + handler (6 classes)
│   │   │   │   ├── BadRequestException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InsufficientStockException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── UnauthorizedAccessException.java
│   │   │   ├── model/                          ← JPA entities + enums (10 files)
│   │   │   │   ├── CartItem.java
│   │   │   │   ├── FavouriteProduct.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── OrderStatus.java  (enum)
│   │   │   │   ├── Product.java
│   │   │   │   ├── Role.java         (enum)
│   │   │   │   ├── Tenant.java
│   │   │   │   └── User.java
│   │   │   ├── repository/                     ← Spring Data JPA repositories (8 interfaces)
│   │   │   │   ├── CartItemRepository.java
│   │   │   │   ├── FavouriteProductRepository.java
│   │   │   │   ├── NotificationRepository.java
│   │   │   │   ├── OrderItemRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── TenantRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/                       ← Spring Security wiring (2 classes)
│   │   │   │   ├── KeycloakJwtAuthenticationConverter.java
│   │   │   │   └── SecurityConfig.java
│   │   │   └── service/                        ← business logic (11 classes)
│   │   │       ├── AuthService.java
│   │   │       ├── CartService.java
│   │   │       ├── FavouriteService.java
│   │   │       ├── KeycloakAdminService.java
│   │   │       ├── KeycloakTokenService.java
│   │   │       ├── NotificationService.java
│   │   │       ├── OrderService.java
│   │   │       ├── PlatformUserService.java
│   │   │       ├── ProductService.java
│   │   │       ├── TenantService.java
│   │   │       └── UserIdentityService.java
│   │   └── resources/
│   │       └── application.properties          ← all runtime configuration (§4)
│   └── test/java/com/ecommerce/service/        ← 9 Mockito unit test classes
│       ├── AuthServiceTest.java
│       ├── CartServiceTest.java
│       ├── FavouriteServiceTest.java
│       ├── NotificationServiceTest.java
│       ├── OrderServiceTest.java
│       ├── PlatformUserServiceTest.java
│       ├── ProductServiceTest.java
│       ├── TenantServiceTest.java
│       └── UserIdentityServiceTest.java
└── target/                                    ← Maven build output (git-tracked artifacts; NOT source)
```

Note: `backend/src/main/resources/` contains **only** `application.properties` — no `application.yml`, no static resources, no templates, and no test resources (`backend/src/test/resources/` does not exist).

---

## 3. pom.xml (Maven Build File) — line-by-line

### Lines 1–4: XML declaration + project root

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
```

- `<?xml version="1.0" encoding="UTF-8"?>` — XML prolog; version 1.0, UTF-8 character encoding.
- `<project>` root element:
  - `xmlns="http://maven.apache.org/POM/4.0.0"` — default XML namespace (Maven POM 4.0.0).
  - `xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"` — XSI namespace for schema validation attributes.
  - `xsi:schemaLocation="..."` — maps the namespace to the official Maven POM XSD URL.

### Line 5: `<modelVersion>4.0.0</modelVersion>`

- Declares the POM model version. Must be `4.0.0` for Maven 2+.

### Lines 6–11: Parent (Spring Boot Starter Parent)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.16</version>
    <relativePath/>
</parent>
```

- Inherits dependency management, plugin versions, and default Maven configuration from `spring-boot-starter-parent` **3.5.16**.
- `<relativePath/>` (empty) tells Maven the parent is a **remote** artifact (from Maven Central), not a file on disk.
- The parent locks compatible versions for all Spring Boot starters and transitively pins versions like Spring Framework 6.2.x, Spring Security 6.5.x, Hibernate 6.6.x, Jackson, Tomcat, etc.
- **Why 3.5.16?** springdoc-openapi 2.8.15+ cannot start on Spring Boot 3.5.0 because Spring Framework 6.2.5 (bundled with 3.5.0) rejects the resource-handler pattern springdoc registers (`/swagger-ui/**/*swagger-initializer.js`). The bug was fixed in Spring Web 6.2.8, which is bundled from Spring Boot 3.5.x patches onward, so the parent was bumped to the latest 3.5.x.

### Lines 12–16: Project coordinates

```xml
<groupId>com.ecommerce</groupId>
<artifactId>ecommerce-backend</artifactId>
<version>1.0.0</version>
<name>ecommerce-backend</name>
<description>Multi-Tenant E-Commerce System Backend</description>
```

- `groupId`: `com.ecommerce` — reverse-domain package owner.
- `artifactId`: `ecommerce-backend` — the unique artifact/binary name (`ecommerce-backend-1.0.0.jar`).
- `version`: `1.0.0` — release version.
- `name`: `ecommerce-backend` — human-readable Maven project name.
- `description`: `Multi-Tenant E-Commerce System Backend` — project purpose (not packaging `jar` explicitly, so it inherits the default `jar` packaging).

### Lines 17–21: `<properties>`

```xml
<properties>
    <java.version>21</java.version>
    <mockito.version>5.23.0</mockito.version>
    <byte-buddy.version>1.18.10</byte-buddy.version>
</properties>
```

- `<java.version>21</java.version>` — compiler source/target level Java 21 (Spring Boot parent maps this to `maven.compiler.release=21`).
- `<mockito.version>5.23.0</mockito.version>` — user-defined property pinning Mockito to 5.23.0.
- `<byte-buddy.version>1.18.10</byte-buddy.version>` — user-defined property pinning Byte Buddy (Mockito's mocking engine) to 1.18.10.

### Lines 22–93: `<dependencies>` (all dependencies)

| Lines | Coordinates | Scope | Purpose |
|---|---|---|---|
| 23–26 | `org.springframework.boot:spring-boot-starter-web` | compile | Embedded Tomcat + Spring MVC + Jackson + `@RestController` support |
| 27–30 | `org.springframework.boot:spring-boot-starter-data-jpa` | compile | Hibernate + Spring Data JPA, `@Entity`, repositories, transactions |
| 31–34 | `org.springframework.boot:spring-boot-starter-security` | compile | Spring Security 6 filter chain, method security |
| 35–38 | `org.springframework.boot:spring-boot-starter-oauth2-resource-server` | compile | JWT OAuth2 resource-server: `JwtDecoder`, bearer-token parsing |
| 39–42 | `org.springframework.boot:spring-boot-starter-validation` | compile | Jakarta Bean Validation (`@Valid`, `@NotBlank`, …) |
| 43–47 | `com.mysql:mysql-connector-j` | **runtime** | MySQL JDBC driver (database used by `application.properties`) |
| 48–52 | `org.projectlombok:lombok` | **optional** | Annotation processing for Lombok (`@Slf4j`); not packaged into the jar |
| 53–56 | `org.springdoc:springdoc-openapi-starter-webmvc-ui` `2.8.17` | compile | **springdoc-openapi**: auto-generates the OpenAPI 3 spec and serves the interactive Swagger UI |
| 58–62 | `org.springframework.boot:spring-boot-starter-test` | **test** | JUnit 5, Spring Test, Mockito, AssertJ, JSONassert, Hamcrest |
| 63–68 | `org.mockito:mockito-core` `${mockito.version}` | **test** | Core Mockito mocking framework (5.23.0) |
| 69–74 | `org.mockito:mockito-junit-jupiter` `${mockito.version}` | **test** | Mockito + JUnit 5 integration (`MockitoExtension`) |
| 75–80 | `net.bytebuddy:byte-buddy` `${byte-buddy.version}` | **runtime** | Bytecode generation used by Mockito to create proxies at runtime |
| 81–86 | `net.bytebuddy:byte-buddy-agent` `${byte-buddy.version}` | **test** | Java agent for inline mock making of final classes |
| 87–91 | `org.springframework.security:spring-security-test` | **test** | Security test utilities (`@WithMockUser`, `SecurityMockMvcRequestBuilders`) |

Note: `<optional>true</optional>` on Lombok (line 51) means downstream consumers of this artifact do not get Lombok. The `mysql-connector-j` scope is `runtime` so it is on the classpath at runtime but not needed at compile time.

The `springdoc-openapi-starter-webmvc-ui` dependency (lines 53–56) pulls in `springdoc-openapi-starter-webmvc-api` and `springdoc-openapi-starter-common` transitively. It auto-registers the `/v3/api-docs` (JSON), `/v3/api-docs.yaml` (YAML) and `/swagger-ui/**` endpoints, generates schemas from the DTOs, and reads bean-validation annotations (`@NotNull`, `@Size`, …) into the spec.

### Lines 95–124: `<build><plugins>`

#### maven-compiler-plugin (lines 96–106)

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

- Configures the Java compiler plugin to run **Lombok as an annotation processor** at compile time. This is what makes `@Slf4j` generate the `log` field.
- The explicit `annotationProcessorPaths` keeps the compiler isolated from the full classpath when processing annotations.

#### spring-boot-maven-plugin (lines 108–114)

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <mainClass>com.ecommerce.EcommerceApplication</mainClass>
    </configuration>
</plugin>
```

- Enables `mvn spring-boot:run` and repackaging into an executable fat jar.
- `<mainClass>` explicitly sets the runnable main class to `com.ecommerce.EcommerceApplication`.

#### maven-surefire-plugin (lines 115–123)

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <useModulePath>false</useModulePath>
        <forkCount>1</forkCount>
        <reuseForks>false</reuseForks>
    </configuration>
</plugin>
```

- Runs JUnit 5 tests during `mvn test`.
- `<version>3.2.5</version>` — explicitly pinned surefire version.
- `<useModulePath>false</useModulePath>` — uses classpath mode (avoids JPMS module-path surprises).
- `<forkCount>1</forkCount>` — runs tests in a single forked JVM.
- `<reuseForks>false</reuseForks>` — a fresh JVM is forked for each test run (no JVM reuse).

### Line 125: `</project>`

- Closes the POM root element.

---

## 4. src/main/resources/application.properties — line-by-line

| Line | Key | Value | Meaning |
|---|---|---|---|
| 1 | `spring.application.name` | `ecommerce-backend` | Application name shown in logs/actuator |
| 2 | `spring.profiles.active` | `dev` | Active profile = `dev` → enables `@Profile("dev")` `DataInitializer` |
| 3 | `server.port` | `8080` | Embedded Tomcat listens on HTTP port 8080 |
| 5 | `spring.datasource.url` | `jdbc:mysql://localhost:3306/ecommercedb?createDatabaseIfNotExist=true` | MySQL database `ecommercedb` on localhost:3306 (auto-created if missing) |
| 6 | `spring.datasource.driverClassName` | `com.mysql.cj.jdbc.Driver` | MySQL JDBC driver class |
| 7 | `spring.datasource.username` | `root` | MySQL connection username |
| 8 | `spring.datasource.password` | `##Root@1928374655` | MySQL connection password (dev credentials) |
| 10 | `spring.jpa.database-platform` | `org.hibernate.dialect.MySQLDialect` | Hibernate uses the MySQL dialect for SQL generation |
| 11 | `spring.jpa.hibernate.ddl-auto` | `update` | Hibernate auto-updates the schema from entities (never drops tables) |
| 12 | `spring.jpa.show-sql` | `false` | Do **not** print each SQL statement to the log |
| 13 | `spring.jpa.properties.hibernate.format_sql` | `true` | If SQL were shown, it would be pretty-printed |
| 15 | `app.cors.allowed-origins` | comma-separated list: `http://localhost:3000`, `http://localhost:5173`, `http://127.0.0.1:3000`, `http://127.0.0.1:5173`, plus the same four with `https://` | Origins permitted by CORS (Vite dev server on 5173; React on 3000) |
| 18 | `springdoc.api-docs.path` | `/v3/api-docs` | OpenAPI spec (JSON) served at `/v3/api-docs`; YAML at `/v3/api-docs.yaml` |
| 19 | `springdoc.swagger-ui.path` | `/swagger-ui.html` | Swagger UI entry point (`/swagger-ui.html` redirects to `/swagger-ui/index.html`) |
| 20 | `springdoc.swagger-ui.operationsSorter` | `method` | Sort operations alphabetically by method in the UI |
| 21 | `springdoc.swagger-ui.tagsSorter` | `alpha` | Sort tags alphabetically in the UI |
| 22 | `springdoc.swagger-ui.default-models-expand-depth` | `-1` | Schemas are collapsed by default in the UI |
| 23 | `springdoc.show-actuator` | `false` | Do not document actuator endpoints |
| 24 | `springdoc.cache.disabled` | `true` | Disable spec caching so the docs always reflect the current runtime |
| 25 | `springdoc.server-url` | `http://localhost:8080` | Server URL shown in the OpenAPI spec (used by `OpenApiConfig`) |
| 27 | `app.keycloak.server-url` | `http://localhost:8081` | Base URL of the Keycloak server |
| 28 | `app.keycloak.realm` | `Omni_Store Realm` | Keycloak realm name (note the space — URL-encoded as `%20`) |
| 29 | `app.keycloak.client-id` | `Omni_Store_client` | Public/confidential client used for the password grant |
| 30 | `app.keycloak.client-secret` | `npTpWYLrwIFgyKnr7eRXIkNzNRhUmctZ` | Client secret for the password grant |
| 31 | `app.keycloak.admin-client-id` | `Omni_Store_client` | Client used to obtain the **admin** token (client-credentials grant) |
| 32 | `app.keycloak.admin-client-secret` | `npTpWYLrwIFgyKnr7eRXIkNzNRhUmctZ` | Admin client secret (same client in this setup) |
| 34 | `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` | `http://localhost:8081/realms/Omni_Store%20Realm/protocol/openid-connect/certs` | JWKS endpoint used by the resource server to fetch signing keys and verify JWT signatures |
| 35 | `spring.security.oauth2.resourceserver.jwt.issuer-uri` | `http://localhost:8081/realms/Omni_Store%20Realm` | Validates the token's `iss` claim against this issuer |
| 37 | `logging.level.org.springframework.security` | `TRACE` | Verbose security logs |
| 38 | `logging.level.org.springframework.security.oauth2` | `TRACE` | Verbose OAuth2 logs |
| 39 | `logging.level.org.springframework.security.web.FilterChainProxy` | `TRACE` | Per-request filter chain trace |
| 40 | `logging.level.org.springframework.security.oauth2.server.resource` | `TRACE` | Verbose resource-server/JWT logs |

**Security note:** `app.keycloak.client-secret` and `app.keycloak.admin-client-secret` are committed in this file (development setup).

The `springdoc.*` block (lines 17–25) configures the springdoc-openapi integration. It only wires up the *endpoint URLs and UI behaviour* — the actual content of the spec is generated from the controllers (`@RestController`/`@RequestMapping`), DTOs and Bean Validation annotations, then customised by `config/OpenApiConfig.java` (title, description, bearer security scheme).

---

## 5. Main Application — EcommerceApplication.java

`package com.ecommerce;` (14 lines)

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
```

- Imports: `SpringApplication` (static launcher), `@SpringBootApplication` (combo: `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan` of `com.ecommerce`), and `@ConfigurationPropertiesScan`.

```java
@SpringBootApplication
@ConfigurationPropertiesScan
public class EcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
```

- `@ConfigurationPropertiesScan` — **critical**: scans the package tree for `@ConfigurationProperties` beans (enables `KeycloakProperties` to bind `app.keycloak.*`). Without it, `KeycloakProperties` would not be registered.
- `main` → `SpringApplication.run(EcommerceApplication.class, args)` boots the embedded Tomcat and the full Spring context.

---

## 6. config package

### 6.1 KeycloakProperties.java

`package com.ecommerce.config;` (62 lines) — typed binding for `app.keycloak.*`.

```java
@ConfigurationProperties(prefix = "app.keycloak")
public class KeycloakProperties {
    private String serverUrl;        // ← app.keycloak.server-url
    private String realm;            // ← app.keycloak.realm
    private String clientId;         // ← app.keycloak.client-id
    private String clientSecret;     // ← app.keycloak.client-secret
    private String adminClientId;    // ← app.keycloak.admin-client-id
    private String adminClientSecret;// ← app.keycloak.admin-client-secret
```

- `@ConfigurationProperties(prefix = "app.keycloak")` binds every property whose key starts with `app.keycloak.` to a field by relaxed binding (e.g. `server-url` → `serverUrl`).
- Every field has an explicit **getter and setter** (lines 15–61) — required for property binding.
- Uses by `KeycloakTokenService` and `KeycloakAdminService`.

### 6.2 DataInitializer.java

`package com.ecommerce.config;` (1078 lines) — **dev-only** seed data runner.

Class-level annotations:

```java
@Component
@Profile("dev")
@Transactional
public class DataInitializer implements CommandLineRunner {
```

- `@Component` — Spring bean.
- `@Profile("dev")` — only instantiated when profile `dev` is active (see §4 line 2).
- `@Transactional` — every `run` call executes inside one transaction (all seed inserts commit/roll back together).
- `implements CommandLineRunner` — Spring calls `run(String... args)` once after the application context is fully initialized.

Fields + constructor injection:

```java
private final TenantRepository tenantRepository;
private final ProductRepository productRepository;
public DataInitializer(TenantRepository tenantRepository, ProductRepository productRepository) {...}
```

`run(String... args)`:

```java
if (tenantRepository.count() > 0) {
    return;                       // Idempotent: seed only when the DB has no tenants
}
seedSamsung(); seedSony(); seedIkea(); seedUniqlo(); seedLego(); seedCanon(); seedBose();
seedNorthFace(); seedApple(); seedNike(); seedAdidas(); seedPuma(); seedReebok(); seedLevis();
```

- If any tenant already exists, seeding is skipped entirely.

Helper methods:

- `createTenant(String name, String slug, String description, String logoUrl)` → `tenantRepository.save(Tenant.builder().name(...).slug(...).description(...).logoUrl(...).build())`.
- `createProduct(Tenant tenant, String name, String description, BigDecimal price, String category, int quantity, String imageUrl)` → `productRepository.save(Product.builder()...build())`.

Seed summary (14 brands × 6 products = **84 products**, all priced in **INR**):

| Brand method | Tenant (name / slug) | Products (name, price INR, category, qty) |
|---|---|---|
| `seedSamsung` | Samsung Store / `samsung` | Galaxy S25 Ultra (512GB) ₹1,29,999 Smartphones 30 · Galaxy Z Fold7 ₹1,64,999 Smartphones 20 · Galaxy Tab S10 Ultra ₹99,999 Tablets 25 · Galaxy Watch Ultra ₹54,999 Wearables 40 · Galaxy Buds3 Pro ₹24,999 Audio 75 · Odyssey OLED G9 Monitor ₹1,39,999 Monitors 12 |
| `seedSony` | Sony Center / `sony` | PlayStation 5 Pro ₹59,990 Gaming 25 · WH-1000XM6 ₹34,990 Audio 60 · Xperia 1 VII ₹1,19,990 Smartphones 18 · Alpha A7 IV ₹2,14,990 Cameras 12 · BRAVIA XR OLED 65" TV ₹2,49,990 Televisions 10 · INZONE H9 ₹24,990 Gaming Accessories 35 |
| `seedIkea` | IKEA Home / `ikea` | MALM Queen Bed Frame ₹32,999 Furniture 20 · BILLY Bookcase ₹11,999 Furniture 45 · POÄNG Armchair ₹18,999 Furniture 30 · KALLAX Shelf Unit ₹12,999 Storage 40 · LACK Coffee Table ₹4,999 Furniture 75 · HEMNES Study Desk ₹24,999 Office Furniture 18 |
| `seedUniqlo` | UNIQLO / `uniqlo` | AIRism Crew Neck T-Shirt ₹1,990 Clothing 120 · Ultra Light Down Jacket ₹7,990 Outerwear 65 · Premium Linen Shirt ₹3,990 Clothing 80 · Selvedge Slim Jeans ₹4,990 Clothing 55 · Cargo Utility Jogger ₹3,490 Clothing 70 · HEATTECH Crew ₹1,990 Winter Wear 95 |
| `seedLego` | LEGO Official / `lego` | Millennium Falcon ₹84,999 Collector Sets 8 · Ferrari Daytona SP3 ₹38,999 Technic 15 · Tokyo Skyline ₹6,999 Architecture 35 · Botanical Orchid ₹4,999 Botanical Collection 45 · Hogwarts Castle ₹47,999 Collector Sets 12 · McLaren F1 ₹2,999 Vehicles 70 |
| `seedCanon` | Canon Imaging / `canon` | EOS R6 Mark II ₹2,09,995 Cameras 15 · EOS R50 ₹76,995 Cameras 30 · RF 24-70mm f/2.8L ₹1,89,995 Camera Lenses 12 · PIXMA G3770 ₹18,999 Printers 35 · Speedlite EL-5 ₹32,999 Camera Accessories 22 · PowerShot V10 ₹35,995 Content Creation 28 |
| `seedBose` | Bose Audio / `bose` | QuietComfort Ultra Headphones ₹37,999 Headphones 40 · QC Ultra Earbuds ₹25,999 Earbuds 65 · Smart Soundbar 900 ₹89,999 Soundbars 20 · SoundLink Flex ₹13,999 Portable Speakers 55 · Portable Smart Speaker ₹34,999 Smart Speakers 28 · Bass Module 700 ₹72,999 Home Audio 18 |
| `seedNorthFace` | The North Face / `the-north-face` | 1996 Retro Nuptse Jacket ₹28,999 Outerwear 45 · Borealis Backpack ₹10,999 Backpacks 70 · Base Camp Duffel ₹13,999 Travel Gear 35 · Summit Series Down Hoodie ₹34,999 Outerwear 20 · Hedgehog FUTURELIGHT Shoes ₹13,499 Footwear 40 · Apex Bionic Softshell ₹15,999 Outerwear 55 |
| `seedApple` | Apple Official / `apple` | iPhone 16 Pro Max (512GB) ₹1,59,900 Smartphones 25 · MacBook Pro 16" M4 Max ₹3,99,900 Laptops 12 · iPad Pro 13" M4 ₹1,29,900 Tablets 20 · Watch Ultra 2 ₹89,900 Wearables 35 · AirPods Pro 2 (USB-C) ₹24,900 Audio 80 · HomePod 2nd Gen ₹26,900 Smart Speakers 30 |
| `seedNike` | Nike Store / `nike` | Air Max DN ₹15,995 Footwear 45 · Air Force 1 '07 ₹10,995 Footwear 60 · Pegasus 41 ₹12,995 Running 35 · Tech Fleece Hoodie ₹11,999 Apparel 55 · Heritage Duffel ₹4,995 Accessories 75 · Dri-FIT Shorts ₹3,995 Apparel 90 |
| `seedAdidas` | Adidas Originals / `adidas` | Ultraboost 5 ₹17,999 Running 35 · Samba OG ₹10,999 Footwear 60 · Gazelle Indoor ₹11,999 Footwear 50 · Adicolor Track Jacket ₹7,999 Apparel 70 · Tiro 24 Pants ₹5,999 Apparel 65 · Defender 5 Duffel ₹5,499 Accessories 85 |
| `seedPuma` | Puma Lifestyle / `puma` | Palermo Vintage ₹8,999 Footwear 55 · Suede XL ₹9,999 Footwear 45 · Deviate Nitro Elite 3 ₹19,999 Running 28 · Essentials Logo Hoodie ₹5,999 Apparel 65 · TeamGOAL Backpack ₹3,999 Accessories 85 · Train Woven Shorts ₹3,499 Apparel 90 |
| `seedReebok` | Reebok Fitness / `reebok` | Nano X5 ₹12,999 Training 40 · Club C 85 ₹7,999 Footwear 60 · Classic Leather ₹8,999 Footwear 55 · FloatZig 1 ₹11,999 Running 35 · Identity Fleece Hoodie ₹5,499 Apparel 70 · Active Core Duffel ₹4,999 Accessories 80 |
| `seedLevis` | Levi's / `levis` | 501 Original Jeans ₹7,999 Jeans 80 · 511 Slim Jeans ₹6,999 Jeans 75 · Trucker Denim Jacket ₹9,999 Outerwear 45 · Sunset One Pocket Shirt ₹5,999 Shirts 60 · Graphic Crewneck T-Shirt ₹2,999 T-Shirts 120 · Reversible Leather Belt ₹3,499 Accessories 90 |

All seed image URLs are `https://images.unsplash.com/...?w=600&auto=format&fit=crop`.

### 6.3 OpenApiConfig.java

`package com.ecommerce.config;` (65 lines) — customisation of the springdoc-openapi generated OpenAPI document.

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ecommerceOpenAPI(
            @Value("${springdoc.server-url:http://localhost:8080}") String serverUrl) {

        return new OpenAPI()
                .info(new Info()
                        .title("Omni Store — Multi-Tenant E-Commerce API")
                        .description("""
                                REST API for the Omni Store multi-tenant e-commerce platform.

                                The platform hosts independent brand stores (tenants) such as Samsung, Sony,
                                IKEA, Apple, Nike and Adidas. Each brand runs its own storefront and product
                                catalogue, while a central platform administrator governs the brands and the
                                users attached to them.

                                ## Authentication
                                Most endpoints require a JWT bearer token obtained from `POST /api/auth/login`
                                (or Keycloak directly). Use the **Authorize** button and paste the token to
                                unlock protected endpoints. The backend resolves the JWT into a user with one
                                of the roles `ADMIN`, `TENANT_ADMIN` or `USER`.

                                ## Roles
                                | Role           | Scope                                                                 |
                                | -------------- | ---------------------------------------------------------------------- |
                                | `ADMIN`        | Platform administrator — manages brands, users and all orders.         |
                                | `TENANT_ADMIN` | Brand administrator — manages the products and orders of one brand.    |
                                | `USER`         | Customer — browses brands, manages a cart, favourites and orders.      |
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Omni Store Backend Team"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://example.com/license")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description("Default server URL")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .name("bearer-jwt")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token returned by `POST /api/auth/login`.")));
    }
}
```

- `@Bean` — registers the `OpenAPI` model that springdoc merges with the auto-generated spec (paths/schemas). The returned object is the top-level `info`, `servers` and `components` block.
- `.info(...)` — the spec's metadata: title, multi-line Markdown description (including the roles table), version, contact and license.
- `.servers(...)` — the server URL, taken from `springdoc.server-url` in `application.properties` (defaults to `http://localhost:8080`).
- `.components().addSecuritySchemes("bearer-jwt", ...)` — defines the HTTP **bearer** security scheme. It makes the **Authorize** button appear in Swagger UI so a JWT can be pasted to unlock protected endpoints. It is defined here rather than applied as a global requirement so the public endpoints are not shown as "locked" — operations still carry a lock only where the backend actually demands a token.

The file uses the `io.swagger.v3.oas.models.*` classes that ship inside the springdoc-openapi dependency (Swagger Core).

---

## 7. model package (JPA Entities + Enums)

All entities follow the same conventions:
- No-args constructor (required by JPA) `public X() {}`
- All-args constructor
- Explicit getters + setters
- Static `builder()` + nested `Builder` class with fluent setters returning `this` and a `build()` that calls the all-args constructor
- `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)` auto-increment PKs

### 7.1 Role.java (enum, 7 lines)

```java
public enum Role {
    ADMIN,          // platform administrator
    TENANT_ADMIN,   // brand/tenant manager
    USER            // regular shopper
}
```

### 7.2 OrderStatus.java (enum, 7 lines)

```java
public enum OrderStatus {
    PENDING,        // order created but not yet finalized
    COMPLETED,      // order finalized (the only state actually set by OrderService)
    CANCELLED       // not used anywhere in current code
}
```

### 7.3 Tenant.java (entity → table `tenants`)

| Line/Area | Detail |
|---|---|
| `@Entity @Table(name = "tenants")` | Maps to table `tenants` |
| `id` | `@Id @GeneratedValue(IDENTITY)` → `Long` auto PK |
| `name` | `@Column(nullable = false, unique = true)` → unique, required brand name |
| `slug` | `@Column(nullable = false, unique = true)` → unique, required URL slug |
| `description` | `@Column(length = 1000)` → max 1000 chars, nullable |
| `logoUrl` | plain `String` column, nullable |
| `active` | `@Column(nullable = false)`, field default `= true` → **soft-delete flag** (never hard-deleted) |
| Constructors | `Tenant()` no-arg; `Tenant(Long id, String name, String slug, String description, String logoUrl, boolean active)` |
| Getters/setters | for all 6 fields; boolean getter is `isActive()` |
| Builder | static `builder()`, nested static class with matching setters, default `active = true` |

### 7.4 User.java (entity → table `users`)

| Line/Area | Detail |
|---|---|
| `@Table(name = "users", indexes = @Index(name = "idx_user_tenant", columnList = "tenant_id"))` | Index on the FK column for faster tenant lookups |
| `id` | `Long` auto PK |
| `name` | `@Column(nullable = false)` required display name |
| `username` | `@Column(nullable = false, unique = true)` unique login name |
| `email` | `@Column(nullable = false, unique = true)` unique email |
| `password` | `@Column(nullable = true)` + `@JsonIgnore` → password is **never serialized to JSON**; also nullable because JWT-provisioned users have no local password |
| `keycloakUserId` | `@Column(unique = true)` nullable — links to Keycloak user UUID |
| `role` | `@Enumerated(EnumType.STRING) @Column(nullable = false)` → stored as the enum **name** string (`ADMIN`/`TENANT_ADMIN`/`USER`) |
| `tenant` | `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tenant_id")` — lazy-loaded relation to `Tenant`, nullable |
| Constructor | `User(Long id, String name, String username, String email, String password, String keycloakUserId, Role role, Tenant tenant)` |

### 7.5 Product.java (entity → table `products`)

| Line/Area | Detail |
|---|---|
| `@Table(name = "products", indexes = {idx_product_tenant(tenant_id), idx_product_category(category), idx_product_name(name)})` | 3 indexes for filtering/search |
| `id` | `Long` auto PK |
| `tenant` | `@ManyToOne(LAZY) @JoinColumn(name = "tenant_id", nullable = false)` — every product must belong to a tenant |
| `name` | `@Column(nullable = false)` required |
| `description` | `@Column(length = 2000)` nullable, max 2000 |
| `price` | `@Column(nullable = false)` `BigDecimal` required |
| `category` | `@Column(nullable = false)` required |
| `availableQuantity` | `@Column(nullable = false)` `Integer` stock level |
| `imageUrl` | plain nullable `String` |
| `active` | `@Column(nullable = false)`, default `true` — **soft-delete flag** |
| Constructor | `Product(Long id, Tenant tenant, String name, String description, BigDecimal price, String category, Integer availableQuantity, String imageUrl, boolean active)` |

### 7.6 CartItem.java (entity → table `cart_items`)

| Line/Area | Detail |
|---|---|
| `@Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","product_id"}), indexes = {idx_cart_user(user_id), idx_cart_product(product_id)})` | A user can have each product in the cart **once**; two indexes |
| `id` | `Long` auto PK |
| `user` | `@ManyToOne(LAZY) @JoinColumn(name = "user_id", nullable = false)` |
| `product` | `@ManyToOne(LAZY) @JoinColumn(name = "product_id", nullable = false)` |
| `quantity` | `@Column(nullable = false)` `Integer` |
| Constructor | `CartItem(Long id, User user, Product product, Integer quantity)` |

### 7.7 Order.java (entity → table `orders`)

| Line/Area | Detail |
|---|---|
| `@Table(name = "orders", indexes = {idx_order_user(user_id), idx_order_tenant(tenant_id), idx_order_date(order_date)})` | 3 indexes |
| `id` | `Long` auto PK |
| `user` | `@ManyToOne(LAZY) @JoinColumn(name = "user_id", nullable = false)` buyer |
| `tenant` | `@ManyToOne(LAZY) @JoinColumn(name = "tenant_id", nullable = false)` → orders are **per-brand** |
| `orderDate` | `@Column(nullable = false)` `LocalDateTime` |
| `totalQuantity` | `@Column(nullable = false)` `Integer` sum of item quantities |
| `totalAmount` | `@Column(nullable = false)` `BigDecimal` sum of item subtotals |
| `status` | `@Enumerated(EnumType.STRING) @Column(nullable = false)` `OrderStatus` |
| `items` | `@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)` → owning side is `OrderItem.order`; cascade saves/deletes children; orphans auto-removed. Initialized to `new ArrayList<>()` |
| Helper methods | `addItem(OrderItem)` (adds + sets back-reference) and `removeItem(OrderItem)` (removes + nulls back-reference) |
| Constructor | `Order(Long id, User user, Tenant tenant, LocalDateTime orderDate, Integer totalQuantity, BigDecimal totalAmount, OrderStatus status, List<OrderItem> items)` — guards `items != null` |

### 7.8 OrderItem.java (entity → table `order_items`)

| Line/Area | Detail |
|---|---|
| `@Table(name = "order_items", indexes = {idx_order_item_order(order_id), idx_order_item_product(product_id)})` | 2 indexes |
| `id` | `Long` auto PK |
| `order` | `@ManyToOne(LAZY) @JoinColumn(name = "order_id", nullable = false)` + `@JsonIgnore` → prevents infinite JSON recursion when an `Order` is serialized with its items |
| `product` | `@ManyToOne(LAZY) @JoinColumn(name = "product_id", nullable = false)` |
| `quantity` | `@Column(nullable = false)` `Integer` |
| `unitPrice` | `@Column(nullable = false)` `BigDecimal` — price **snapshot** at purchase time (immune to later price changes) |
| `subtotal` | `@Column(nullable = false)` `BigDecimal` = unitPrice × quantity |
| Constructor | `OrderItem(Long id, Order order, Product product, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal)` |

### 7.9 FavouriteProduct.java (entity → table `favourite_products`)

| Line/Area | Detail |
|---|---|
| `@Table(name = "favourite_products", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","product_id"}), indexes = {idx_favourite_user(user_id), idx_favourite_product(product_id)})` | One favourite per (user, product) pair |
| `id` | `Long` auto PK |
| `user` | `@ManyToOne(LAZY) @JoinColumn(name = "user_id", nullable = false)` |
| `product` | `@ManyToOne(LAZY) @JoinColumn(name = "product_id", nullable = false)` |
| Constructor | `FavouriteProduct(Long id, User user, Product product)` |

### 7.10 Notification.java (entity → table `notifications`)

| Line/Area | Detail |
|---|---|
| `@Table(name = "notifications", indexes = @Index(name = "idx_notification_user", columnList = "user_id"))` | Index on recipient |
| `id` | `Long` auto PK |
| `user` | `@ManyToOne(LAZY) @JoinColumn(name = "user_id", nullable = false)` + `@JsonIgnore` |
| `message` | `@Column(nullable = false, length = 500)` max 500 chars |
| `tenant` | `@ManyToOne(LAZY) @JoinColumn(name = "tenant_id")` nullable (context tenant, e.g. the assigned brand) |
| `read` | `@Column(nullable = false)` `boolean` flag |
| `createdAt` | `@Column(nullable = false, updatable = false)` `LocalDateTime` — set only at insert |
| `@PrePersist void onCreate()` | JPA lifecycle callback that stamps `createdAt = LocalDateTime.now()` before first persist (so callers never set it manually) |
| Constructor | `Notification(Long id, User user, String message, Tenant tenant, boolean read, LocalDateTime createdAt)` |

---

## 8. repository package (Spring Data JPA)

All repositories are `interface XxxRepository extends JpaRepository<Entity, Long>` annotated with `@Repository`. Derived query names (Spring Data parses method names) and their SQL semantics:

### 8.1 TenantRepository.java (28 lines)

| Method | Meaning |
|---|---|
| `Optional<Tenant> findBySlugIgnoreCase(String slug)` | Tenant by slug, case-insensitive (used for TENANT_ADMIN token resolution, may return inactive) |
| `Optional<Tenant> findBySlugIgnoreCaseAndActiveTrue(String slug)` | Active tenant by slug, case-insensitive (used for storefront/management) |
| `Optional<Tenant> findByNameIgnoreCase(String name)` | Tenant by name, case-insensitive (duplicate check) |
| `boolean existsBySlugIgnoreCase(String slug)` | Duplicate-slug check |
| `boolean existsByNameIgnoreCase(String name)` | Duplicate-name check |
| `Page<Tenant> findByActiveTrue(Pageable pageable)` | Paged list of active tenants |
| `List<Tenant> findAllByActiveTrue()` | All active tenants (unused in services currently) |

### 8.2 UserRepository.java (29 lines)

| Method | Meaning |
|---|---|
| `Optional<User> findByUsername(String username)` | Exact-match login name lookup |
| `Optional<User> findByEmail(String email)` | Email lookup |
| `Optional<User> findByKeycloakUserId(String keycloakUserId)` | Provision lookup by Keycloak subject |
| `boolean existsByUsername(String username)` | Duplicate username check |
| `boolean existsByEmail(String email)` | Duplicate email check |
| `List<User> findByTenantId(Long tenantId)` | All users linked to a tenant (used when deleting a tenant) |
| `Page<User> findByRole(Role role, Pageable pageable)` | Paged users filtered by role (platform user listing) |

### 8.3 ProductRepository.java (52 lines)

| Method | Meaning |
|---|---|
| `List<Product> findByTenantId(Long tenantId)` | All products of a tenant (used on tenant delete) |
| `Page<Product> findByTenantId(Long tenantId, Pageable)` | Paged products by tenant |
| `Page<Product> findByTenantIdAndCategoryIgnoreCase(...)` | By tenant + category (ci) |
| `Page<Product> findByTenantIdAndNameContainingIgnoreCase(...)` | By tenant + name contains (ci) |
| `Page<Product> findByTenantIdAndCategoryIgnoreCaseAndNameContainingIgnoreCase(...)` | By tenant + category + name |
| `Optional<Product> findByIdAndTenantId(Long id, Long tenantId)` | Product scoped to a tenant — used everywhere to prevent cross-brand access |
| `Page<Product> findByCategoryIgnoreCase(...)` | Global category filter |
| `Page<Product> findByNameContainingIgnoreCase(...)` | Global name filter |
| `Page<Product> findByCategoryIgnoreCaseAndNameContainingIgnoreCase(...)` | Global category + name |
| `@Query(""" SELECT p FROM Product p WHERE p.active = TRUE AND (:tenantId IS NULL OR p.tenant.id = :tenantId) AND (:category IS NULL OR LOWER(p.category) = LOWER(:category)) AND (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND (:minPrice IS NULL OR p.price >= :minPrice) AND (:maxPrice IS NULL OR p.price <= :maxPrice) """) Page<Product> searchProducts(@Param("tenantId") Long, @Param("category") String, @Param("name") String, @Param("minPrice") BigDecimal, @Param("maxPrice") BigDecimal, Pageable)` | **The unified search query** — JPQL with null-safe optional filters: only active products; `:tenantId IS NULL` = all tenants; case-insensitive category equality; case-insensitive `LIKE %name%`; min/max price bounds |

### 8.4 CartItemRepository.java (43 lines)

| Method | Meaning |
|---|---|
| `List<CartItem> findByUserIdAndProduct_Tenant_Slug(Long userId, String tenantSlug)` | Cart items whose product belongs to a tenant slug — nested property traversal `product.tenant.slug` |
| `List<CartItem> findByUserId(Long userId)` | Whole cart for a user (global) |
| `Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId)` | Existing line for incrementing quantity |
| `Optional<CartItem> findByIdAndUserId(Long id, Long userId)` | Ownership check (item must belong to caller) |
| `@Modifying @Transactional void deleteByProductId(Long productId)` | Bulk delete (removes cart lines when a product is soft-deleted) |
| `@Modifying @Transactional void deleteByUserIdAndProduct_Tenant_Slug(Long userId, String tenantSlug)` | Clear a brand's lines |
| `@Modifying @Transactional void deleteByUserId(Long userId)` | Clear the whole cart |

### 8.5 OrderRepository.java (24 lines)

| Method | Meaning |
|---|---|
| `List<Order> findByUserIdOrderByOrderDateDesc(Long userId)` | User's order history, newest first (global) |
| `List<Order> findByUserIdAndTenantSlugOrderByOrderDateDesc(Long userId, String tenantSlug)` | User's orders for one brand (nested `tenant.slug`), newest first |
| `List<Order> findByTenantIdOrderByOrderDateDesc(Long tenantId)` | All orders for a brand (tenant admin incoming orders) |
| `List<Order> findAllByOrderByOrderDateDesc()` | All orders across the platform, newest first |

### 8.6 OrderItemRepository.java (17 lines)

| Method | Meaning |
|---|---|
| `@Modifying @Transactional void deleteByProductId(Long productId)` | Bulk delete of order items for a product (declared but currently only used indirectly; historical order items are intentionally preserved via soft deletes) |

### 8.7 FavouriteProductRepository.java (33 lines)

| Method | Meaning |
|---|---|
| `List<FavouriteProduct> findByUserIdAndProduct_Tenant_Slug(Long userId, String tenantSlug)` | Favourites scoped to a brand |
| `List<FavouriteProduct> findByUserId(Long userId)` | All favourites (global) |
| `boolean existsByUserIdAndProductId(Long userId, Long productId)` | Duplicate-favourite check |
| `@Modifying @Transactional void deleteByUserIdAndProductId(Long userId, Long productId)` | Remove a favourite |
| `@Modifying @Transactional void deleteByProductId(Long productId)` | Declared cleanup (favourites are preserved on soft delete by design) |

### 8.8 NotificationRepository.java (15 lines)

| Method | Meaning |
|---|---|
| `List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId)` | User's notifications, newest first |
| `List<Notification> findByUserIdAndReadFalse(Long userId)` | Unread notifications (for "mark all read") |

---

## 9. dto/request package (Request DTOs)

Every request DTO: no-arg constructor, all-args constructor, getters/setters, static `builder()` + nested `Builder`. Validation uses Jakarta Bean Validation annotations; violations are turned into HTTP 400 by `GlobalExceptionHandler`.

### 9.1 RegisterRequest.java (91 lines)

| Field | Annotations | Constraints / Message |
|---|---|---|
| `name` | `@NotBlank` + `@Size(min=2, max=100)` | "Name is required." / "Name must be between 2 and 100 characters." |
| `username` | `@NotBlank` + `@Size(min=3, max=30)` + `@Pattern("^[a-zA-Z0-9._-]+$")` | allowed chars: letters, digits, dot, underscore, hyphen |
| `email` | `@NotBlank` + `@Email` + `@Size(max=254)` | valid email format, max 254 |
| `password` | `@NotBlank` + `@Size(min=8, max=128)` + `@Pattern("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")` | 8–128 chars with ≥1 uppercase, ≥1 lowercase, ≥1 digit |
| `tenantSlug` | `@Size(max=100)` + `@Pattern("^[a-z0-9-]*$")` | nullable; if provided, the registering user becomes `TENANT_ADMIN` |

### 9.2 LoginRequest.java (63 lines)

| Field | Annotations | Constraints |
|---|---|---|
| `username` | `@NotBlank` + `@Size(max=50)` | required, ≤50 chars |
| `password` | `@NotBlank` + `@Size(max=128)` | required, ≤128 chars |

### 9.3 TenantRequest.java (73 lines)

| Field | Annotations | Constraints |
|---|---|---|
| `name` | `@NotBlank` + `@Size(min=2, max=100)` | required |
| `slug` | `@NotBlank` + `@Size(min=2, max=50)` + `@Pattern("^[a-z0-9-]+$")` | lowercase letters/digits/hyphens |
| `description` | `@Size(max=1000)` | optional |
| `logoUrl` | `@Size(max=500)` + `@Pattern("^(https?://).+")` | optional but must start with `http://` or `https://` |

### 9.4 AssignTenantRequest.java (30 lines)

| Field | Annotations | Constraints |
|---|---|---|
| `tenantId` | `@NotNull` | required (`@NotNull` on a `Long`) |

### 9.5 ProductRequest.java (86 lines)

| Field | Annotations | Constraints |
|---|---|---|
| `name` | `@NotBlank` + `@Size(min=2, max=150)` | required |
| `description` | `@Size(max=2000)` | optional |
| `price` | `@NotNull` + `@DecimalMin("0.01")` | required, > 0 |
| `category` | `@NotBlank` + `@Size(max=100)` | required |
| `availableQuantity` | `@NotNull` + `@PositiveOrZero` | required, ≥ 0 |
| `imageUrl` | `@Size(max=500)` | optional |

(Note the `import jakarta.validation.constraints.*;` wildcard import.)

### 9.6 StockUpdateRequest.java (44 lines)

| Field | Annotations | Constraints |
|---|---|---|
| `availableQuantity` | `@NotNull` + `@PositiveOrZero` | required, ≥ 0 |

### 9.7 ProductSearchRequest.java (152 lines)

| Field | Annotations | Default / Constraints |
|---|---|---|
| `category` | `@Size(max=100)` | nullable |
| `search` | `@Size(max=150)` | nullable (maps to product-name LIKE) |
| `page` | `@Min(0)` | default `0` (field initializer) |
| `size` | `@Min(1)` + `@Max(100)` | default `10` (field initializer) |
| `minPrice` | `@DecimalMin("0")` | nullable |
| `maxPrice` | `@DecimalMin("0")` | nullable |

Important null-guard behavior:
- `setPage(Integer page) { this.page = (page == null) ? 0 : page; }`
- `setSize(Integer size) { this.size = (size == null) ? 10 : size; }`

### 9.8 CartItemRequest.java (52 lines)

| Field | Annotations | Constraints |
|---|---|---|
| `productId` | `@NotNull` + `@Positive` | required, > 0 |
| `quantity` | `@NotNull` + `@Min(1)` | required, ≥ 1 |

### 9.9 UpdateCartItemRequest.java (37 lines)

| Field | Annotations | Constraints |
|---|---|---|
| `quantity` | `@NotNull` + `@Min(1)` | required, ≥ 1 |

### 9.10 CreateOrderRequest.java (48 lines)

| Field | Annotations | Constraints |
|---|---|---|
| `items` | `@NotNull` + `@NotEmpty` + `@Valid` | required list, ≥1 element, each element validated (`@Valid` cascades to `OrderItemRequest`) |

### 9.11 OrderItemRequest.java (64 lines)

| Field | Annotations | Constraints |
|---|---|---|
| `productId` | `@NotNull` + `@Positive` | required, > 0 |
| `quantity` | `@NotNull` + `@Min(1)` | required, ≥ 1 |

---

## 10. dto/response package (Response DTOs)

All response DTOs: no-arg + all-args constructors, getters/setters, builder. They are plain POJOs serialized to JSON by Jackson. (Field order = JSON key order.)

### 10.1 AuthResponse.java (85 lines)

Fields: `token` (String), `userId` (Long), `name`, `username`, `email` (String), `role` (Role), `tenantId` (Long), `tenantSlug`, `tenantName` (String). Returned by register/login/`/me`.

### 10.2 TenantResponse.java (55 lines)

Fields: `id` (Long), `name`, `slug`, `description`, `logoUrl` (String). **Note:** `active` is NOT exposed.

### 10.3 ProductResponse.java (99 lines)

Fields: `id` (Long), `tenantId` (Long), `tenantName`, `tenantSlug`, `name`, `description` (String), `price` (BigDecimal), `category`, `availableQuantity` (Integer), `imageUrl` (String), `isFavourite` (Boolean — derived per current user; getter is `getIsFavourite()`).

### 10.4 CartItemResponse.java (92 lines)

Fields: `id` (Long), `productId` (Long), `productName`, `productCategory` (String), `quantity` (Integer), `unitPrice` (BigDecimal), `subtotal` (BigDecimal = unitPrice × quantity), `productImageUrl` (String), `availableQuantity` (Integer), `tenantSlug` (String).

### 10.5 OrderResponse.java (110 lines)

Fields: `id` (Long), `userId` (Long), `username`, `userFullName` (String), `tenantId` (Long), `tenantName`, `tenantSlug` (String), `orderDate` (LocalDateTime), `totalQuantity` (Integer), `totalAmount` (BigDecimal), `status` (OrderStatus), `items` (List<OrderItemResponse>).

### 10.6 OrderItemResponse.java (78 lines)

Fields: `id` (Long), `productId` (Long), `productName`, `productCategory` (String), `quantity` (Integer), `unitPrice` (BigDecimal), `subtotal` (BigDecimal), `productImageUrl` (String).

### 10.7 NotificationResponse.java (71 lines)

Fields: `id` (Long), `message` (String), `tenantId` (Long), `tenantName`, `tenantSlug` (String), `read` (boolean, getter `isRead()`), `createdAt` (LocalDateTime).

### 10.8 UserResponse.java (78 lines)

Fields: `id` (Long), `name`, `username`, `email` (String), `role` (Role), `tenantId` (Long), `tenantName`, `tenantSlug` (String). Used by the platform user listing/assignment.

---

## 11. exception package

### 11.1 Custom exceptions (4 identical-shaped classes)

| Class | Extends | Thrown for | HTTP status (see handler) |
|---|---|---|---|
| `ResourceNotFoundException` | `RuntimeException` | Missing tenant/product/order/cart item/user/notification | 404 NOT_FOUND |
| `BadRequestException` | `RuntimeException` | Duplicate username/email/slug/name, multi-brand order, invalid token, price range, duplicate favourite | 400 BAD_REQUEST |
| `InsufficientStockException` | `RuntimeException` | Requested quantity exceeds `availableQuantity` | 400 BAD_REQUEST |
| `UnauthorizedAccessException` | `RuntimeException` | ADMIN shopping, cross-tenant management, other user's notification | 403 FORBIDDEN |

Each is just a `public class X extends RuntimeException { public X(String message) { super(message); } }`.

### 11.2 ErrorResponse.java (42 lines)

Response body wrapper: `int status` (getter `getStatus()`), `String message`, `LocalDateTime timestamp`. Has no-arg, all-args constructors, getters/setters, and a `Builder`.

### 11.3 GlobalExceptionHandler.java (141 lines)

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler { ... }
```

`@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody` — every method's return is written directly as the HTTP body.

| Handler (`@ExceptionHandler`) | Returns |
|---|---|
| `ResourceNotFoundException` | 404 with message |
| `BadRequestException` | 400 with message |
| `InsufficientStockException` | 400 with message |
| `UnauthorizedAccessException` | 403 with message |
| `AccessDeniedException` (Spring Security) | 403 with message |
| `MethodArgumentNotValidException` | 400, joins all field errors as `"field: message, field: message"` |
| `BindException` | 400, same joining; fallback `"Invalid request parameters."` |
| `HandlerMethodValidationException` | 400, distinct non-null messages |
| `ConstraintViolationException` | 400, `propertyPath: message` joined |
| `MethodArgumentTypeMismatchException` | 400, `Invalid value '<v>' for parameter '<name>'. Expected type: <SimpleName>` |
| `MissingServletRequestParameterException` | 400, `Missing required parameter: <name>` |
| `HttpMessageNotReadableException` | 400 `"Request body is malformed or contains invalid values."` (logs a warn) |
| `HttpMediaTypeNotSupportedException` | 415 UNSUPPORTED_MEDIA_TYPE |
| `HttpRequestMethodNotSupportedException` | 405 METHOD_NOT_ALLOWED |
| `NoResourceFoundException` | 404 `"Requested resource does not exist."` |
| `Exception` (catch-all) | 500 `"An unexpected error occurred. Please try again later."` (logs `error`) |

Shared private helper:

```java
private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
    ErrorResponse err = ErrorResponse.builder()
            .status(status.value())
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    return new ResponseEntity<>(err, status);
}
```

---

## 12. security package

### 12.1 SecurityConfig.java (151 lines)

```java
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
```

- `@EnableWebSecurity` — activates Spring Security's filter chain.
- `@EnableMethodSecurity` — enables `@PreAuthorize` / `@PostAuthorize` method-level annotations.

Constructor:

```java
public SecurityConfig(
        KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter,
        @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}") String allowedOriginsCsv) {
    this.keycloakJwtAuthenticationConverter = keycloakJwtAuthenticationConverter;
    this.allowedOrigins = Arrays.stream(allowedOriginsCsv.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toArray(String[]::new);
    log.info("Configured {} allowed CORS origin(s).", this.allowedOrigins.length);
}
```

- Reads `app.cors.allowed-origins` (default: Vite localhosts), splits on commas, trims, drops blanks → `String[] allowedOrigins`.

`@Bean SecurityFilterChain filterChain(HttpSecurity http)`:

```java
http
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // apply CORS from bean
    .csrf(csrf -> csrf.disable())                                        // stateless API → CSRF off
    .headers(headers -> headers.frameOptions(frame -> frame.disable()))  // allow H2 console iframe
    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // no HTTP sessions
    .authorizeHttpRequests(this::configureAuthorization)                 // rules below
    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter)));
```

- The OAuth2 resource server validates bearer JWTs and uses `KeycloakJwtAuthenticationConverter` to build the `Authentication`.

`@Bean CorsConfigurationSource corsConfigurationSource()`:

- `setAllowedOrigins(Arrays.asList(allowedOrigins))`
- `setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"))`
- `setAllowedHeaders(List.of("*"))`
- `setExposedHeaders(List.of("Authorization"))` (so JS can read the auth header)
- `setAllowCredentials(true)`
- Registers the config for `/**` via `UrlBasedCorsConfigurationSource`.

`configureAuthorization(...)` — the URL authorization rules in order (first match wins):

| Rule | Result |
|---|---|
| `OPTIONS /**` | `permitAll()` (CORS preflight) |
| `/api/auth/register`, `/api/auth/login` | `permitAll()` |
| `/v3/api-docs/**`, `/v3/api-docs.yaml`, `/v3/api-docs.json`, `/swagger-ui.html`, `/swagger-ui/**`, `/swagger-resources/**` | `permitAll()` (OpenAPI spec + Swagger UI are public) |
| `/h2-console/**` | `permitAll()` |
| `GET /api/platform/tenants`, `GET /api/platform/tenants/**` | `permitAll()` (brand list is public for the storefront) |
| `GET /*/products`, `GET /*/products/**` | `permitAll()` (any `/brand/products` read is public) |
| `GET /api/public/products`, `GET /api/public/products/**` | `permitAll()` |
| `GET /api/tenants/**` | `permitAll()` |
| `/api/platform/**` (anything else under platform) | `hasRole("ADMIN")` |
| `anyRequest()` | `authenticated()` |

The swagger rules keep the interactive docs usable without a token: `/v3/api-docs/**` covers the JSON spec and its config endpoint, `/v3/api-docs.yaml` is a **sibling path** (not under `/v3/api-docs/`) so it needs its own rule, and `/swagger-ui/**` plus `/swagger-ui.html` serve the UI itself. The **Authorize** button inside Swagger UI still lets you paste a JWT for calling protected endpoints from the browser.

There is a commented-out leftover block (`/{tenantSlug}/products/**` permitAll) — dead code.

### 12.2 KeycloakJwtAuthenticationConverter.java (38 lines)

```java
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UserIdentityService userIdentityService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        User user = userIdentityService.resolveOrProvisionUserFromJwt(jwt);
        return new UsernamePasswordAuthenticationToken(
                user,
                "N/A",                       // credentials placeholder
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
```

- Every authenticated request runs this converter: it resolves-or-provisions the local `User` from the JWT, then builds an `Authentication` whose **principal is the `User` entity** (hence `@AuthenticationPrincipal User currentUser` works everywhere) and whose authority is `ROLE_ADMIN` / `ROLE_TENANT_ADMIN` / `ROLE_USER`.

---

## 13. service package (Business Logic)

All services are `@Service`, use constructor injection, and are `@Slf4j` (Lombok-provided `log`).

### 13.1 UserIdentityService.java (151 lines)

Dependencies: `UserRepository`, `TenantRepository`, `KeycloakAdminService`.

`@Transactional resolveOrProvisionUserFromJwt(Jwt jwt)`:

1. Reads claims: `sub` → keycloakUserId; `preferred_username`; `email`; `name` (each trimmed via `trimToNull`).
2. `findExistingUser(...)` — lookup order: by keycloak ID, then username, then email.
3. `extractRole(jwt)` (see below) and `resolveTenant(...)` (see below).
4. **Existing user path:** updates `keycloakUserId`; sets role only if `mappedRole != USER || existing.getRole() == null` (i.e. does not overwrite an existing TENANT_ADMIN with USER); updates tenant/name/username/email when present; saves.
5. **New user path:** builds with fallbacks — username = `keycloakUserId` if missing; email = `username + "@keycloak.local"` if missing; name = username if missing; `password(null)`; saves.

`extractRole(Jwt)`:
- Reads `realm_access.roles`; if any role equalsIgnoreCase `ADMIN` → `Role.ADMIN`; else if `TENANT_ADMIN` → `Role.TENANT_ADMIN`.
- Fallback: if `preferred_username` is `adminuser` or `platform_admin` → `Role.ADMIN`.
- Otherwise `Role.USER`.

`resolveTenant(jwt, role, existing, username)`:
- tenant slug from claim `tenantSlug`, else claim `tenant`, else existing user's tenant slug, else (if `TENANT_ADMIN`) `keycloakAdminService.findTenantSlugByUsername(username)`.
- If still null: TENANT_ADMIN → throws `BadRequestException("TENANT_ADMIN token must include tenantSlug or tenant claim")`; USER → returns `null` (no tenant).
- Resolves via `tenantRepository.findBySlugIgnoreCase(...)`, else `BadRequestException("Tenant not found with slug: ...")`. (Note: uses the non-active-filtered lookup, so TENANT_ADMIN of a deactivated brand still resolves.)

`trimToNull(String)` — trims and returns `null` for blank strings.

### 13.2 AuthService.java (226 lines)

Dependencies: `UserRepository`, `TenantRepository`, `KeycloakAdminService`, `KeycloakTokenService`, `JwtDecoder`, `UserIdentityService`.

`@Transactional register(RegisterRequest)`:
1. Logs request.
2. Duplicate username → `BadRequestException("Username is already taken.")`; duplicate email → `BadRequestException("Email is already in use.")`.
3. Role = `TENANT_ADMIN` if `tenantSlug` non-blank else `USER`.
4. If tenantSlug present: resolve via `tenantRepository.findBySlugIgnoreCase` else `ResourceNotFoundException("Tenant not found with slug: ...")`.
5. `keycloakAdminService.createUser(request, role)` → Keycloak user UUID.
6. Save local `User` (`password(null)`).
7. Obtain token via `keycloakTokenService.loginAndGetAccessToken(username, password)`.
8. `buildAuthResponse(user, token)`.

`login(LoginRequest)`:
1. Token from Keycloak password grant.
2. `decodeJwt(token)`.
3. `userIdentityService.resolveOrProvisionUserFromJwt(jwt)`.
4. `buildAuthResponse(user, token)`.

`getCurrentUser()`: returns `Optional<User>` from `SecurityContextHolder` if principal is a `User` (used by `PublicProductController` to enrich `isFavourite` for logged-in visitors).

`decodeJwt(String)`:
- Tries `jwtDecoder.decode(token)` (verified via JWKS).
- **Fallback:** `parseJwtUnverified(token)` — splits on `.`, base64-url-decodes the payload, parses claims with `ObjectMapper`, re-derives `iat`/`exp` from numeric claims (defaults `iat=now`, `exp=now+3600s`), and returns a `Jwt` with header `{"alg":"none"}`. Throws `BadRequestException("Unable to decode authentication token")` on failure. (This fallback exists so login works even when the local JWKS call is unavailable.)

`buildAuthResponse(User, token)`: copies token + identity + role + tenant (`tenantId`/`tenantSlug`/`tenantName` all null when tenant is null).

### 13.3 KeycloakTokenService.java (320 lines)

Dependencies: `RestClient.Builder` (builds its own `RestClient`), `KeycloakProperties`, `ObjectMapper`.

Constant: `TOKEN_ENDPOINT = "/realms/{realm}/protocol/openid-connect/token"`.

`loginAndGetAccessToken(username, password)`:
- Builds password-grant form: `grant_type=password`, `client_id=<clientId>`, optional `client_secret`, `scope=openid profile email`, `username`, `password`.
- POSTs to `<serverUrl>/realms/{realm}/protocol/openid-connect/token`.
- 4xx → `handleAuthenticationError` (parses `error_description` JSON if present → `BadRequestException("Keycloak authentication error: <desc>")`; else `BadRequestException("Invalid username or password.")`).
- 5xx → `BadRequestException("Keycloak service is unavailable.")`.
- Extracts `access_token` from JSON body or throws `BadRequestException("Keycloak did not return an access token.")`.

`getAdminAccessToken()`:
- Builds **client-credentials** form: `grant_type=client_credentials`, `client_id=<adminClientId-or-clientId>`, optional `client_secret`.
- `adminClientId()` returns `adminClientId` if non-blank else `clientId`; `adminClientSecret()` same logic.
- Error → `BadRequestException("Failed to obtain Keycloak admin access token. Status: ..., Body: ...")`.

`requestToken(form, errorHandler)`: shared POST + `onStatus(HttpStatusCode::isError, errorHandler)` + `.body(new ParameterizedTypeReference<>() {})`.

`extractAccessToken(response, errorMessage)`: null/`access_token`-missing guard then `.toString()`.

### 13.4 KeycloakAdminService.java (666 lines)

Dependencies: `RestClient`, `KeycloakProperties`, `KeycloakTokenService`.

`createUser(RegisterRequest, Role)`:
1. Gets admin token (`getAdminAccessToken()`).
2. `splitName(fullName)` → record `NameParts(firstName, lastName)`; no space → `(name, "User")`; blank → `("", "User")`.
3. `buildUserPayload(request, nameParts)` → JSON body:
   - `username`, `email`, `enabled: true`, `emailVerified: true`, `firstName`, `lastName`, `requiredActions: []`
   - `credentials: [{type:"password", value:<pw>, temporary:false}]`
   - `attributes`: empty `Map.of()` for USER; `{"tenantSlug": ["<slug trimmed/lowercased>"]}` for TENANT_ADMIN.
4. `POST <server>/admin/realms/{realm}/users` with `Authorization: Bearer <token>`.
   - 409 → `BadRequestException("Username or email already exists in Keycloak")`.
   - any other error → `BadRequestException("Failed to create user in Keycloak. Status: ...")`.
5. Reads `Location` header (must be non-blank) → `extractUserIdFromLocation` (parses UUID after last `/` of the path; `IllegalArgumentException` → `BadRequestException("Invalid Keycloak Location header returned.")`).
6. `setPasswordAndClearActions(...)`: two PUTs — `/users/{id}/reset-password` (password payload) then `/users/{id}` (profile payload: firstName/lastName/requiredActions=[]/emailVerified/enabled). Errors are logged but swallowed (non-fatal).
7. `assignRealmRole(adminToken, id, role)`:
   - `fetchOrCreateRealmRole`: GET `/roles/{roleName}`; if not found, POST `/roles` with `{"name":..., "description": roleName+" role"}`, then GET again. Lookup failures return `null` and assignment is skipped.
   - POST `/users/{id}/role-mappings/realm` with `List.of(roleRepresentation)`. Failures are logged and swallowed (registration continues).
8. Returns the Keycloak user UUID.

`assignTenantToUser(keycloakUserId, tenantSlug)` (used by platform admin):
1. Admin token.
2. `fetchUserAttributes` — GET `/users/{id}`, extract `attributes` map (as a fresh `HashMap`; failures → empty map).
3. Puts `tenantSlug: List.of(slug.trim().toLowerCase())`.
4. `updateUserAttributes` — PUT `/users/{id}` body `{"attributes": ...}`; any HTTP error → `BadRequestException("Failed to update tenant assignment in Keycloak. Status: ...")`.
5. `assignRealmRole(..., TENANT_ADMIN)`.

`findTenantSlugByUsername(username)` (TENANT_ADMIN role-resolution fallback):
- GET `/users?username={username}`, take first result's `attributes`, read `tenantSlug` (handles `List` or plain `String`); failures return `null`.

`extractUserIdFromLocation(String)` — described above.

### 13.5 TenantService.java (204 lines)

Dependencies: `TenantRepository`, `UserRepository`, `ProductRepository`, `CartItemRepository`.

`createTenant(TenantRequest)`:
- `slug = request.getSlug().toLowerCase().trim()`.
- `existsBySlugIgnoreCase(slug)` → `BadRequestException("Tenant slug already exists: <slug>")`.
- `existsByNameIgnoreCase(name)` → `BadRequestException("Tenant name already exists: <name>")`.
- Saves `Tenant` (active defaults true), returns `TenantResponse`.

`@Transactional updateTenant(Long id, TenantRequest)`:
- Find by id or 404.
- Slug/name uniqueness checks that **exclude self** (`filter(existing -> !existing.getId().equals(id))`).
- Sets all fields, saves, returns response.

`@Transactional(readOnly=true) getAllTenants(Pageable)` → `findByActiveTrue(pageable).map(mapToResponse)` (only active brands).

`@Transactional(readOnly=true) getTenantBySlug(slug)` → `findBySlugIgnoreCaseAndActiveTrue` or 404.

`@Transactional deleteTenant(Long id)` — **soft-delete cascade**:
1. `userRepository.findByTenantId(id)` → set each user's tenant to `null`, save (dissociate users).
2. `productRepository.findByTenantId(id)` → for each product: `cartItemRepository.deleteByProductId(...)` (remove cart lines), `product.setActive(false)`, save (deactivate products; orders & favourites preserved).
3. `tenant.setActive(false)`, save (deactivate brand; historical orders/favourites remain visible).
4. Never hard-deletes.

`getTenantEntityBySlug(slug)` → active tenant entity or `ResourceNotFoundException("Tenant brand not found: <slug>")`.

`mapToResponse` (private) → `TenantResponse` (id/name/slug/description/logoUrl).

### 13.6 ProductService.java (336 lines)

Dependencies: `ProductRepository`, `TenantService`, `FavouriteProductRepository`, `CartItemRepository`.

`addProduct(tenantSlug, ProductRequest, currentUser)`:
- Resolve active tenant; `validateTenantAccess`; build + save `Product`; `mapToResponse`.

`updateProduct(...)`:
- Resolve tenant; `validateTenantAccess`; find by `id + tenantId` or 404; `ensureProductActive`; set all fields (imageUrl only when non-null); save; map.

`updateStock(...)`:
- Resolve tenant; `validateTenantAccess`; find or 404; `ensureProductActive`; set `availableQuantity`; save; map.

`@Transactional deleteProduct(...)`:
- Resolve tenant; `validateTenantAccess`; find or 404.
- `cartItemRepository.deleteByProductId(productId)` (clean cart lines), `product.setActive(false)`, save. Favourites and historical orders are intentionally preserved.

`@Transactional(readOnly=true) getAllProducts(ProductSearchRequest, currentUser)`:
- `PageRequest.of(page, size, Sort.by("id").descending())` (newest id first).
- `normalize(...)` trims/blanks→null for category & search.
- `validatePriceRange` → `min > max` → `BadRequestException("Minimum price cannot be greater than maximum price.")`.
- `productRepository.searchProducts(null, category, search, min, max, pageable)` → map each to response.

`getProducts(tenantSlug, request, currentUser)` — same but scoped to the resolved tenant's id.

`getProductById(...)` — tenant-resolved, `ensureProductActive`, map.

`validateTenantAccess(User, Tenant)`:
- `ADMIN` → allowed (returns).
- `TENANT_ADMIN` whose tenant id equals target tenant id → allowed; otherwise `UnauthorizedAccessException("Tenant user cannot perform management operations on another tenant's domain: <slug>")`.
- `USER` → `UnauthorizedAccessException("User does not have permission to perform management operations.")`.
- (Package-private so `OrderService` and `FavouriteService` can reuse it.)

`normalize`, `ensureProductActive`, `validatePriceRange` — helpers described above.

`@Transactional(readOnly=true) mapToResponse(Product, User currentUser)`:
- `isFav = currentUser != null && favouriteProductRepository.existsByUserIdAndProductId(...)` (null user → false).
- Builds `ProductResponse` with tenant id/name/slug, name, description, price, category, quantity, imageUrl, `isFavourite`.

### 13.7 CartService.java (333 lines)

Dependencies: `CartItemRepository`, `ProductRepository`, `UserRepository`, `TenantService`.

`@Transactional addToCart(tenantSlug, CartItemRequest, currentUser)`:
1. `currentUser.getRole() == ADMIN` → `UnauthorizedAccessException("Admin accounts are not allowed to add products to the cart.")`.
2. Resolve active tenant.
3. `getTenantProduct(tenant, tenantSlug, productId)` — tenant-scoped, `ensureProductActive` (404 if inactive).
4. `validateStock(product, requested)` — requested > available → `InsufficientStockException("Cannot add X units of <name>. Available stock: Y")`.
5. `getPersistentUser` (DB reload).
6. Look up existing line (`findByUserIdAndProductId`); if absent build one with `quantity(0)`.
7. `newQuantity = existing + requested`; if `newQuantity > available` → `InsufficientStockException("Cannot add X more units of <name>. Available stock: Y")`.
8. Set & save; `mapToResponse`.

`@Transactional(readOnly=true) getCart(tenantSlug, currentUser)`:
- Global slug (`null`/blank/`global`, case-insensitive via `isGlobalSlug`) → `findByUserId`; else `findByUserIdAndProduct_Tenant_Slug`.

`@Transactional updateQuantity(tenantSlug, itemId, UpdateCartItemRequest, currentUser)`:
- `getOwnedCartItem(itemId, userId)` (404 if not owned).
- Re-fetch product via its tenant + `tenantSlug` param.
- quantity > available → `InsufficientStockException("Cannot set quantity to X for <name>. Available stock: Y")`.
- Save; map.

`@Transactional removeItem(tenantSlug, itemId, currentUser)`:
- `getOwnedCartItem` then `cartItemRepository.delete(cartItem)`.

`@Transactional clearCart(tenantSlug, currentUser)`:
- Global → `deleteByUserId`; else `deleteByUserIdAndProduct_Tenant_Slug`.

`getOwnedCartItem` — `findByIdAndUserId` or 404.
`getTenantProduct` — tenant-scoped find or 404; inactive → 404.
`validateStock` — see above.
`getPersistentUser` — `findById` or 404.
`isGlobalSlug` — `null || blank || equalsIgnoreCase("global")`.
`mapToResponse` — computes `subtotal = price × quantity` and copies product/tenant data.

### 13.8 OrderService.java (403 lines)

Dependencies: `OrderRepository`, `ProductRepository`, `UserRepository`, `TenantService`, `ProductService`.

`@Transactional createOrder(tenantSlug, CreateOrderRequest, currentUser)`:
1. `ADMIN` → `UnauthorizedAccessException("Admin accounts are not allowed to place orders.")`.
2. `validateOrderRequest` — empty/null items → `BadRequestException("Order must contain at least one order item.")`.
3. `resolveOrderTenant(tenantSlug, request)`:
   - If a concrete non-global slug → `tenantService.getTenantEntityBySlug(slug)`.
   - If global/blank → load all products by id; empty → `ResourceNotFoundException("None of the requested products were found.")`; distinct tenant ids > 1 → `BadRequestException("Your cart contains products from multiple brands. Please place a separate order for each brand.")`; else the first product's tenant.
4. `getPersistentUser`.
5. `initializeOrder(user, tenant)` → status `PENDING`, `orderDate = now`, empty items list.
6. Per item: `getTenantProduct` (tenant-scoped, 404), `ensureProductActive`, `validateStock` (`InsufficientStockException("Cannot order X units of <name>. Available stock: Y")`), `reduceStock` (subtract + save), `subtotal = price × quantity`, accumulate `totalQuantity`/`totalAmount`, `buildOrderItem` (unitPrice snapshot = current price), add to order.
7. Set totals + `status = COMPLETED`; save; map.

`@Transactional(readOnly=true) getUserOrderHistory(tenantSlug, currentUser)`:
- Global → `findByUserIdOrderByOrderDateDesc`; else `findByUserIdAndTenantSlugOrderByOrderDateDesc`.

`@Transactional(readOnly=true) getTenantOrders(tenantSlug, currentUser)`:
- Resolve tenant; `productService.validateTenantAccess(currentUser, tenant)` (so ADMIN sees all, TENANT_ADMIN only own brand, USER blocked); `findByTenantIdOrderByOrderDateDesc`.

`@Transactional(readOnly=true) getAllOrders()` → `findAllByOrderByOrderDateDesc()`.

`mapToResponse` — includes user id/username/name, tenant id/name/slug, totals, status, and mapped items (`mapOrderItem` copies id, productId, name, category, quantity, unitPrice, subtotal, imageUrl).

### 13.9 FavouriteService.java (196 lines)

Dependencies: `FavouriteProductRepository`, `ProductRepository`, `UserRepository`, `TenantService`, `ProductService`.

`@Transactional addFavourite(tenantSlug, productId, currentUser)`:
- `ensureNotAdmin` → ADMIN → `UnauthorizedAccessException("Admin accounts are not allowed to use favourites.")`.
- `getTenantProduct` (tenant-scoped) + active check (404).
- `existsByUserIdAndProductId` → `BadRequestException("Product is already in your favourites.")`.
- `getPersistentUser`; save `FavouriteProduct`; return `productService.mapToResponse(product, user)`.

`@Transactional removeFavourite(tenantSlug, productId, currentUser)`:
- `ensureNotAdmin`; `deleteByUserIdAndProductId`. **Not tenant-scoped** (works even for deleted products — test `testRemoveFavourite_Success_WorksForDeletedProducts`).

`@Transactional(readOnly=true) getUserFavourites(tenantSlug, currentUser)`:
- `ensureNotAdmin`; global slug → `findByUserId` else `findByUserIdAndProduct_Tenant_Slug`; map each product via `productService.mapToResponse(product, currentUser)`.

Helpers: `ensureNotAdmin`, `getTenantProduct`, `getPersistentUser`, `isGlobalSlug`.

### 13.10 NotificationService.java (117 lines)

Dependency: `NotificationRepository`.

`@Transactional notifyTenantAdminAssignment(User, Tenant)`:
- Message: `"You have been made TENANT ADMIN of <tenantName>. You can now manage this brand's products and orders."`
- Saves `Notification` with `read=false` (createdAt stamped by `@PrePersist`); returns response.

`@Transactional(readOnly=true) getNotificationsForUser(userId)` → `findByUserIdOrderByCreatedAtDesc`.

`@Transactional markAsRead(notificationId, userId)`:
- Find or 404; **ownership check** (`notification.getUser().getId().equals(userId)`) else `UnauthorizedAccessException("You cannot access another user's notification.")`; set `read=true`; save.

`@Transactional markAllAsRead(userId)`:
- `findByUserIdAndReadFalse`; set all read; `saveAll`.

`mapToResponse` — copies id/message/read/createdAt and tenant id/name/slug (null-safe).

### 13.11 PlatformUserService.java (115 lines)

Dependencies: `UserRepository`, `TenantRepository`, `KeycloakAdminService`, `NotificationService`.

`@Transactional(readOnly=true) getUsers(Role role, Pageable)`:
- `role != null` → `findByRole(role, pageable)`; else `findAll(pageable)`; map to `UserResponse`.

`@Transactional assignTenant(userId, AssignTenantRequest)`:
1. User by id or 404.
2. User is `ADMIN` → `BadRequestException("A platform admin cannot be assigned a brand.")`.
3. Tenant by id or 404.
4. If user has a Keycloak ID → `keycloakAdminService.assignTenantToUser(keycloakId, tenant.slug)` (sets Keycloak `tenantSlug` attribute + `TENANT_ADMIN` realm role).
5. Set `user.setRole(TENANT_ADMIN)`, `user.setTenant(tenant)`, save.
6. `notificationService.notifyTenantAdminAssignment(saved, tenant)`.
7. Return `UserResponse`.

`mapToResponse` — id/name/username/email/role + tenant id/name/slug (null-safe).

---

## 14. controller package (REST API)

All controllers are `@RestController` with `@RequestMapping` class paths; endpoints return `ResponseEntity`. The authenticated principal is always available as `@AuthenticationPrincipal User currentUser`.

### 14.1 AuthController.java — base `/api/auth`

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| `POST register` | `/api/auth/register` | **public** | `@Valid @RequestBody RegisterRequest` | `200 AuthResponse` |
| `POST login` | `/api/auth/login` | **public** | `@Valid @RequestBody LoginRequest` | `200 AuthResponse` |
| `GET me` | `/api/auth/me` | authenticated | `@AuthenticationPrincipal User` | `200 AuthResponse` (without `token`), or `401` empty if principal null |

### 14.2 MultiTenantProductController.java — base `/{tenantSlug}/products`

| Method | Path | Auth (`@PreAuthorize` / URL rule) | Request | Response |
|---|---|---|---|---|
| `GET` | `/{tenantSlug}/products` | **public** (URL `GET /*/products`) | `@Valid ProductSearchRequest` (query params) | `200 Page<ProductResponse>` |
| `GET /{id}` | `/{tenantSlug}/products/{id}` | **public** (URL `GET /*/products/**`) | `@PathVariable Long id` | `200 ProductResponse` |
| `POST` | `/{tenantSlug}/products` | `ADMIN` or `TENANT_ADMIN` | `@Valid @RequestBody ProductRequest` | `201 ProductResponse` |
| `PUT /{id}` | `/{tenantSlug}/products/{id}` | `ADMIN` or `TENANT_ADMIN` | `@Valid @RequestBody ProductRequest` | `200 ProductResponse` |
| `PATCH /{id}/stock` | `/{tenantSlug}/products/{id}/stock` | `ADMIN` or `TENANT_ADMIN` | `@Valid @RequestBody StockUpdateRequest` | `200 ProductResponse` |
| `DELETE /{id}` | `/{tenantSlug}/products/{id}` | `ADMIN` or `TENANT_ADMIN` | — | `204 No Content` |

### 14.3 MultiTenantCartController.java — base `/{tenantSlug}/cart`

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| `GET` | `/{tenantSlug}/cart` | authenticated | — | `200 List<CartItemResponse>` |
| `POST` | `/{tenantSlug}/cart` | authenticated | `@Valid @RequestBody CartItemRequest` | `201 CartItemResponse` |
| `PUT /{itemId}` | `/{tenantSlug}/cart/{itemId}` | authenticated | `@Valid @RequestBody UpdateCartItemRequest` | `200 CartItemResponse` |
| `DELETE /{itemId}` | `/{tenantSlug}/cart/{itemId}` | authenticated | — | `204 No Content` |
| `DELETE` | `/{tenantSlug}/cart` | authenticated | — | `204 No Content` |

Note: cart **read** endpoints are `authenticated` (not public), and backend blocks ADMIN at the service layer. `tenantSlug` may be `global`.

### 14.4 MultiTenantFavouriteController.java — base `/{tenantSlug}/favourites`

Class-level `@PreAuthorize("hasAnyRole('USER', 'TENANT_ADMIN')")` — **ADMIN is blocked for all three endpoints**.

| Method | Path | Request | Response |
|---|---|---|---|
| `POST /{productId}` | add favourite | `@PathVariable Long productId` | `200 ProductResponse` |
| `DELETE /{productId}` | remove favourite | `@PathVariable Long productId` | `204 No Content` |
| `GET` | list favourites | — | `200 List<ProductResponse>` |

### 14.5 MultiTenantOrderController.java — base `/{tenantSlug}/orders`

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| `POST` | `/{tenantSlug}/orders` | authenticated (ADMIN blocked in service) | `@Valid @RequestBody CreateOrderRequest` | `201 OrderResponse` |
| `GET /my-history` | `/{tenantSlug}/orders/my-history` | authenticated | — | `200 List<OrderResponse>` |
| `GET` | `/{tenantSlug}/orders` | `@PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN')")` (service further enforces own-brand for TENANT_ADMIN) | — | `200 List<OrderResponse>` |

### 14.6 NotificationController.java — base `/api/notifications`

| Method | Path | Auth | Response |
|---|---|---|---|
| `GET` | `/api/notifications` | authenticated (401 empty if principal null) | `200 List<NotificationResponse>` |
| `PUT /{notificationId}/read` | mark one read | authenticated (401 if null) | `200 NotificationResponse` |
| `PUT /read-all` | mark all read | authenticated (401 if null) | `204 No Content` |

### 14.7 PlatformTenantController.java — base `/api/platform/tenants`

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| `POST` | `/api/platform/tenants` | `@PreAuthorize("hasRole('ADMIN')")` | `@Valid @RequestBody TenantRequest` | `201 TenantResponse` |
| `DELETE /{id}` | `/api/platform/tenants/{id}` | `@PreAuthorize("hasRole('ADMIN')")` | `@PathVariable Long id` | `204 No Content` |

(`GET /api/platform/tenants` is public at the URL level but has **no controller method** — it is consumed via `TenantService` only by other means or is simply permitted.)

### 14.8 PlatformUserController.java — base `/api/platform/users`

Class-level `@Validated` (enables validation on `@RequestParam`).

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| `GET` | `/api/platform/users` | `hasRole('ADMIN')` | `role` (optional enum, validated by Spring), `page` (default 0, `@Min(0)`), `size` (default 100, `@Min(1) @Max(100)`) | `200 Page<UserResponse>` |
| `PUT /{userId}/tenant` | `/api/platform/users/{userId}/tenant` | `hasRole('ADMIN')` | `@Valid @RequestBody AssignTenantRequest` | `200 UserResponse` |

### 14.9 PlatformOrderController.java — base `/api/platform/orders`

| Method | Path | Auth | Response |
|---|---|---|---|
| `GET` | `/api/platform/orders` | `@PreAuthorize("hasRole('ADMIN')")` | `200 List<OrderResponse>` (all orders, all brands) |

### 14.10 PublicTenantController.java — base `/api/tenants`

Class-level `@Validated`.

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| `GET` | `/api/tenants` | **public** | `page` (default 0, `@Min(0)`), `size` (default 10, `@Min(1) @Max(100)`) | `200 Page<TenantResponse>` (active only) |
| `GET /{slug}` | `/api/tenants/{slug}` | **public** | `@PathVariable String slug` | `200 TenantResponse` (active only) |

### 14.11 PublicProductController.java — base `/api/public/products`

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| `GET` | `/api/public/products` | **public** | `@Valid ProductSearchRequest` | `200 Page<ProductResponse>` |

Uses `authService.getCurrentUser().orElse(null)` so `isFavourite` is populated **only** when the caller is authenticated (anonymous → all `isFavourite=false`).

---

## 15. test package (JUnit 5 + Mockito)

All 9 test classes live under `backend/src/test/java/com/ecommerce/service/`. Common pattern:

```java
@ExtendWith(MockitoExtension.class)
public class XxxServiceTest {
    @Mock private RepositoryX repositoryX;   // mocked dependencies
    @InjectMocks private XxxService xxxService; // service under test
    @BeforeEach void setUp() { ... }          // shared fixtures
    @Test void testXxx_...() { ... }
}
```

Uses static imports: `org.junit.jupiter.api.Assertions.*`, `org.mockito.ArgumentMatchers.*`, `org.mockito.Mockito.*`.

### 15.1 AuthServiceTest.java (176 lines)

Mocks: `UserRepository`, `TenantRepository`, `KeycloakAdminService`, `KeycloakTokenService`, `JwtDecoder`, `UserIdentityService`.

Fixtures: `registerRequest` (Test User / testuser / test@example.com / password123), `loginRequest`, `mockUser` (id 1, role USER).

Tests:
- `testRegister_Success` — verifies token/username/role + `save` called once.
- `testRegister_DuplicateUsername_ThrowsException` — `BadRequestException`, `save` never called.
- `testRegister_DuplicateEmail_ThrowsException` — `BadRequestException`.
- `testLogin_Success` — stubs `JwtDecoder.decode` with a `Jwt` (header `alg=none`, claims `sub`, `preferred_username`), verifies response.
- `testLogin_InvalidPassword_ThrowsException` — `KeycloakTokenService` throws `BadRequestException`.
- `testLogin_UserNotFound_ThrowsException` — same.
- `testGetCurrentUser_Success` — mocks `Authentication` + `SecurityContext`, asserts present, clears context.
- `testGetCurrentUser_NotAuthenticated` — clears context, asserts empty.

### 15.2 CartServiceTest.java (293 lines)

Mocks: `CartItemRepository`, `ProductRepository`, `UserRepository`, `TenantService`.

Fixtures: Nike tenant (id 1), customer (id 5, USER), Air Max product (id 101, price 100.0, qty 10).

Tests: new-item add (subtotal 200.0), existing-item increment (2+3=5, subtotal 500.0), insufficient stock (11 > 10), ADMIN blocked (`UnauthorizedAccessException`, `findById` never called), tenant-scoped get, global-slug get (uses `findByUserId`), update quantity success, update insufficient stock, item-not-owned (`ResourceNotFoundException`), remove item, clear tenant cart, clear global cart.

### 15.3 FavouriteServiceTest.java (188 lines)

Mocks: `FavouriteProductRepository`, `ProductRepository`, `UserRepository`, `TenantService`, `ProductService`.

Tests: tenant-scoped list, global list, duplicate favourite (`BadRequestException`), product not found, inactive product (404), ADMIN blocked (`UnauthorizedAccessException`), remove favourite (verifies no tenant lookup — works for deleted products), ADMIN blocked on list.

### 15.4 NotificationServiceTest.java (186 lines)

Mocks: `NotificationRepository`.

Tests: notify creates unread notification with message containing `"TENANT ADMIN of Nike"`, list returns user's notifications, mark read success, not found (404), **other user's notification** (`UnauthorizedAccessException`), mark-all-read sets flags and calls `saveAll`.

### 15.5 OrderServiceTest.java (392 lines)

Mocks: `OrderRepository`, `ProductRepository`, `UserRepository`, `TenantService`, `ProductService`.

Tests: success (COMPLETED, stock 10→8, `save` once), insufficient stock (no order save), exact-stock success (10→0), ADMIN blocked, get all orders, history for a tenant, history global (uses `findByUserIdOrderByOrderDateDesc`), **global-slug tenant inference** (uses `productRepository.findById`; `getTenantEntityBySlug` never called), **global multi-brand → BadRequestException**, global unknown products → 404, tenant admin own-brand orders allowed, wrong-tenant admin → `UnauthorizedAccessException` (order query never called), platform admin sees tenant orders.

### 15.6 PlatformUserServiceTest.java (160 lines)

Mocks: `UserRepository`, `TenantRepository`, `KeycloakAdminService`, `NotificationService`.

Tests: filter by role (uses `findByRole`), assign tenant success (role becomes TENANT_ADMIN, `assignTenantToUser("kc-user-1","nike")` + notification + save verified), user not found (404), tenant not found (404), admin user assignment → `BadRequestException`.

### 15.7 ProductServiceTest.java (314 lines)

Mocks: `ProductRepository`, `TenantService`, `FavouriteProductRepository`, `CartItemRepository`.

Tests: add by tenant admin, add by platform admin, add by **wrong** tenant admin → `UnauthorizedAccessException`, update stock, delete by wrong tenant admin → unauthorized, **soft-delete** (product inactive, `save` called, `delete` never, `deleteByProductId` on cart items called, favourites never deleted), get inactive product → 404, `validateTenantAccess` matrix (platform admin passes / same tenant passes / different tenant throws), search with category+price bounds, max-only bound, tenant-scoped price filter, min>max → `BadRequestException`, blank category normalizes to null.

### 15.8 TenantServiceTest.java (261 lines)

Mocks: `TenantRepository`, `UserRepository`, `ProductRepository`, `CartItemRepository`.

Tests: create success (slug lowercased), duplicate slug → `BadRequestException`, list active only, get by slug found, get by slug 404, update success, update 404, **delete tenant soft-delete** (tenant inactive, product inactive, `save` not `delete`, cart items cleaned), update duplicate slug (excluding self), update duplicate name, delete 404.

### 15.9 UserIdentityServiceTest.java (83 lines)

Mocks: `UserRepository`, `TenantRepository`, `KeycloakAdminService`.

Tests:
- `resolveOrProvisionUserFromJwt_createsTenantAdminUserWithTenant` — JWT with `realm_access.roles=[TENANT_ADMIN]`, `tenantSlug=nike` → role TENANT_ADMIN, tenant resolved.
- `resolveOrProvisionUserFromJwt_defaultsToUserRoleWithoutRealmRoles` — bare JWT → role USER, tenant null, email fallback `john@keycloak.local`.

---

## 16. Cross-Cutting Reference Tables

### 16.1 Authorization model (how a request is allowed)

| Layer | Mechanism |
|---|---|
| URL rules (`SecurityConfig`) | permitAll public paths, `hasRole("ADMIN")` for `/api/platform/**`, everything else `authenticated()` |
| Method security (`@PreAuthorize`) | favourites `USER/TENANT_ADMIN`; tenant orders `ADMIN/TENANT_ADMIN`; product mutations `ADMIN/TENANT_ADMIN`; platform user/tenant/order `ADMIN` |
| Service-layer checks | ADMIN blocked from cart/order/favourite; `ProductService.validateTenantAccess` enforces own-brand for TENANT_ADMIN; notification ownership check |

### 16.2 Role → capability map

| Capability | ADMIN | TENANT_ADMIN | USER |
|---|---|---|---|
| Browse products (public) | ✅ | ✅ | ✅ |
| Add favourite / cart / place order | ❌ (403) | ✅ | ✅ |
| Manage own brand's products & stock | ✅ (any brand) | ✅ (own brand only) | ❌ |
| View own brand's orders | ✅ (any brand) | ✅ (own brand only) | ❌ |
| View own order history | ✅ (via `/my-history`) | ✅ | ✅ |
| Platform: create/delete brands | ✅ | ❌ | ❌ |
| Platform: list users / assign tenants | ✅ | ❌ | ❌ |
| Platform: view all orders | ✅ | ❌ | ❌ |

### 16.3 "global" slug semantics

- Cart read/clear, favourites list, order history: `null`/blank/`global` → across all brands.
- Order creation with `global`: tenant is **inferred** from the products; multi-brand request → HTTP 400.

### 16.4 Soft-delete strategy

- `Product.active=false` + cart lines removed → disappears from storefront/search, but historical **orders** and **favourites** preserved.
- `Tenant.active=false` + all its products deactivated + users dissociated → brand disappears from storefront, historical data preserved.
- No hard deletes anywhere in the service layer (only repository bulk deletes for cart cleanup).

### 16.5 HTTP status mapping (GlobalExceptionHandler)

| Exception | Status |
|---|---|
| `ResourceNotFoundException`, `NoResourceFoundException` | 404 |
| `BadRequestException`, `InsufficientStockException`, validation errors, malformed body/params | 400 |
| `UnauthorizedAccessException`, `AccessDeniedException` | 403 |
| `HttpMediaTypeNotSupportedException` | 415 |
| `HttpRequestMethodNotSupportedException` | 405 |
| anything else | 500 |

### 16.6 Request → Response flow (example: place order)

```
Frontend → POST /{tenantSlug}/orders  (Bearer JWT)
  → SecurityConfig URL rule: authenticated()
  → JwtDecoder verifies signature via JWKS (Keycloak 8081)
  → KeycloakJwtAuthenticationConverter
       → UserIdentityService.resolveOrProvisionUserFromJwt
       → principal = User entity, authority = ROLE_<role>
  → @Valid CreateOrderRequest (cascades to OrderItemRequest)
  → MultiTenantOrderController.createOrder → OrderService.createOrder
       → ADMIN check → resolveOrderTenant → per-item stock/product checks
       → stock reduction, totals, status COMPLETED
  → OrderResponse (200/201)
  → GlobalExceptionHandler if any exception → ErrorResponse JSON
```

---

## 17. OpenAPI / Swagger UI

### 17.1 What it is

The backend auto-generates an **OpenAPI 3.1** specification from the source of truth (controllers, DTOs, validation annotations) and serves it through **springdoc-openapi 2.8.17**. No hand-maintained spec file — the documentation can never drift from the code.

Endpoints exposed:

| URL | Content |
|---|---|
| `/swagger-ui.html` | 302 → `/swagger-ui/index.html` (Swagger UI) |
| `/swagger-ui/index.html` | Interactive Swagger UI (try-it-out console) |
| `/v3/api-docs` | OpenAPI spec in JSON |
| `/v3/api-docs.yaml` | OpenAPI spec in YAML |

### 17.2 How it works

- `pom.xml` → `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17` (see §3). It registers springdoc's controllers, resource handlers and auto-configuration.
- `application.properties` `springdoc.*` block (see §4) → controls the spec path, UI path and UI behaviour.
- `config/OpenApiConfig.java` (see §6.3) → the top-level `info` block (title, description, version, contact, license), the server list, and the `bearer-jwt` HTTP security scheme.
- `security/SecurityConfig.java` → the docs and UI URLs are `permitAll()` (see §12.1).

### 17.3 What springdoc infers automatically

- **Endpoints** from `@RestController` + `@RequestMapping`/`@GetMapping`/… — all 23 paths of this API.
- **Request bodies & responses** from the DTOs (`dto/request/*`, `dto/response/*`), e.g. `LoginRequest`, `AuthResponse`, `ProductResponse`.
- **Validation** from Jakarta Bean Validation annotations — `@NotNull`, `@NotBlank`, `@Size`, `@Min`/`@Max`, `@Pattern`, `@DecimalMin`, `@Positive`, `@Email`… show up as `required` flags, `minLength`/`maxLength`, `minimum`/`maximum`, and regex `pattern` constraints.
- **Query parameters** from `@RequestParam` and from `@Valid` model-attribute request objects (e.g. `ProductSearchRequest` → `category`, `search`, `page`, `size`, `minPrice`, `maxPrice`).
- **Path parameters** from `@PathVariable` (`tenantSlug`, `id`, `itemId`, `productId`, `notificationId`, `userId`).
- **Pagination** — Spring Data `Page<T>` responses become `Page{...}` schemas (content, number, size, totalElements, totalPages, …).

### 17.4 Using the Authorize button (JWT)

1. Call `POST /api/auth/login` from Swagger UI (public endpoint) or get a token from the app's login.
2. Copy the `token` value from the `AuthResponse` JSON.
3. Click **Authorize** (top-right), paste the token, save.
4. Protected endpoints are now callable from the UI; the token is sent as `Authorization: Bearer <token>`.

### 17.5 Why Spring Boot is 3.5.16

springdoc 2.8.15+ registers the Swagger UI's `swagger-initializer.js` through `PathPattern.combine`, which on Spring Framework < 6.2.8 produced the invalid pattern `/swagger-ui/**/*swagger-initializer.js` and failed at startup with *"Invalid mapping pattern detected"*. The fix shipped in Spring Web 6.2.8 (Spring bug #34986), bundled with later Spring Boot 3.5.x patches — hence the parent was bumped from 3.5.0 to 3.5.16. The `spring.mvc.pathmatch.matching-strategy=ant_path_matcher` workaround does **not** help here because the failing pattern is built inside springdoc, not during MVC matching.

---

*This document is generated from the actual source of `backend/pom.xml` and `backend/src/` and mirrors the code as of the current repository state.*
