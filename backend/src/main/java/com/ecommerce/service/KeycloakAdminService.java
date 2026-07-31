package com.ecommerce.service;

import com.ecommerce.config.KeycloakProperties;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.model.Role;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class KeycloakAdminService {

    private final RestClient restClient;
    private final KeycloakProperties keycloakProperties;
    private final KeycloakTokenService keycloakTokenService;

    public KeycloakAdminService(RestClient.Builder restClientBuilder, KeycloakProperties keycloakProperties, KeycloakTokenService keycloakTokenService) {
        this.restClient = restClientBuilder.build();
        this.keycloakProperties = keycloakProperties;
        this.keycloakTokenService = keycloakTokenService;
    }

    public String createUser(RegisterRequest request, Role roleToAssign) {
        String adminToken = keycloakTokenService.getAdminAccessToken();
        String name = request.getName() != null ? request.getName().trim() : "";
        String firstName = name;
        String lastName = "User";
        if (name.contains(" ")) {
            int idx = name.indexOf(' ');
            firstName = name.substring(0, idx).trim();
            lastName = name.substring(idx + 1).trim();
        }
        if (lastName.isBlank()) {
            lastName = "User";
        }

        Map<String, Object> payload = Map.of(
                "username", request.getUsername(),
                "email", request.getEmail(),
                "enabled", true,
                "emailVerified", true,
                "firstName", firstName,
                "lastName", lastName,
                "requiredActions", List.of(),
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", request.getPassword(),
                        "temporary", false
                )),
                "attributes", request.getTenantSlug() == null || request.getTenantSlug().isBlank()
                        ? Map.of()
                        : Map.of("tenantSlug", List.of(request.getTenantSlug().trim().toLowerCase()))
        );

        String location = restClient.post()
                .uri(adminBaseUrl() + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .body(payload)
                .retrieve()
                .onStatus(status -> status.value() == 409, (requestSpec, clientResponse) -> {
                    throw new BadRequestException("Username or email already exists in Keycloak");
                })
                .onStatus(HttpStatusCode::isError, (requestSpec, clientResponse) -> {
                    throw new BadRequestException("Failed to create user in Keycloak");
                })
                .toBodilessEntity()
                .getHeaders()
                .getFirst(HttpHeaders.LOCATION);

        if (location == null || location.isBlank()) {
            throw new BadRequestException("Keycloak user creation did not return user location");
        }
        String keycloakUserId = extractUserIdFromLocation(location);
        setPasswordAndClearActions(adminToken, keycloakUserId, request.getPassword(), firstName, lastName);
        assignRealmRole(adminToken, keycloakUserId, roleToAssign.name());
        return keycloakUserId;
    }

    private void setPasswordAndClearActions(String adminToken, String keycloakUserId, String password, String firstName, String lastName) {
        try {
            Map<String, Object> passwordPayload = Map.of(
                    "type", "password",
                    "value", password,
                    "temporary", false
            );
            restClient.put()
                    .uri(adminBaseUrl() + "/users/" + keycloakUserId + "/reset-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .body(passwordPayload)
                    .retrieve()
                    .toBodilessEntity();

            Map<String, Object> updatePayload = Map.of(
                    "firstName", firstName,
                    "lastName", lastName,
                    "requiredActions", List.of(),
                    "emailVerified", true,
                    "enabled", true
            );
            restClient.put()
                    .uri(adminBaseUrl() + "/users/" + keycloakUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .body(updatePayload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // Log or handle gracefully
        }
    }

    private void assignRealmRole(String adminToken, String keycloakUserId, String roleName) {
        try {
            String resolvedRoleName = resolveRoleName(roleName);
            Map<String, Object> roleRepresentation = fetchOrCreateRealmRole(adminToken, resolvedRoleName);
            if (roleRepresentation != null) {
                restClient.post()
                        .uri(adminBaseUrl() + "/users/" + keycloakUserId + "/role-mappings/realm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .body(List.of(roleRepresentation))
                        .retrieve()
                        .toBodilessEntity();
            }
        } catch (Exception e) {
            // Non-fatal: if Keycloak realm role is not configured or admin client lacks role mapping permission,
            // the user registration in Keycloak and local DB still completes successfully.
        }
    }

    private Map<String, Object> fetchOrCreateRealmRole(String adminToken, String roleName) {
        try {
            return restClient.get()
                    .uri(adminBaseUrl() + "/roles/" + roleName)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            try {
                restClient.post()
                        .uri(adminBaseUrl() + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .body(Map.of("name", roleName, "description", roleName + " role"))
                        .retrieve()
                        .toBodilessEntity();

                return restClient.get()
                        .uri(adminBaseUrl() + "/roles/" + roleName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private String resolveRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return "USER";
        }
        String normalizedRole = roleName.trim().toUpperCase();
        return switch (normalizedRole) {
            case "ADMIN" -> "ADMIN";
            case "TENANT_ADMIN" -> "TENANT_ADMIN";
            case "USER" -> "USER";
            default -> normalizedRole;
        };
    }

    private String extractUserIdFromLocation(String location) {
        String path = URI.create(location).getPath();
        int idx = path.lastIndexOf('/');
        if (idx < 0 || idx == path.length() - 1) {
            throw new BadRequestException("Unable to parse Keycloak user id from location: " + location);
        }
        return path.substring(idx + 1);
    }

    private String adminBaseUrl() {
        return keycloakProperties.getServerUrl() + "/admin/realms/" + keycloakProperties.getRealm();
    }
}
