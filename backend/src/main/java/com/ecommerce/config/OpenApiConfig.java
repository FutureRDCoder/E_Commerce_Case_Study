package com.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
