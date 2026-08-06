package com.ecommerce.service;

import com.ecommerce.config.KeycloakProperties;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.model.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KeycloakAdminService {

    private final RestClient restClient;
    private final KeycloakProperties keycloakProperties;
    private final KeycloakTokenService keycloakTokenService;

    public KeycloakAdminService(
            RestClient.Builder restClientBuilder,
            KeycloakProperties keycloakProperties,
            KeycloakTokenService keycloakTokenService
    ) {
        this.restClient = restClientBuilder.build();
        this.keycloakProperties = keycloakProperties;
        this.keycloakTokenService = keycloakTokenService;
    }

    public String createUser(RegisterRequest request, Role role) {

        log.info("Creating Keycloak user '{}'.", request.getUsername());

        String adminToken = keycloakTokenService.getAdminAccessToken();

        NameParts nameParts = splitName(request.getName());

        Map<String, Object> payload =
                buildUserPayload(request, nameParts);

        String location = restClient.post()
                .uri(realmAdminBaseUrl() + "/users",
                        keycloakProperties.getRealm().trim())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION,
                        bearerToken(adminToken))
                .body(payload)
                .retrieve()
                .onStatus(status -> status.value() == 409,
                        (req, res) -> {

                            log.warn(
                                    "Keycloak user already exists. Username='{}', Email='{}'.",
                                    request.getUsername(),
                                    request.getEmail()
                            );

                            throw new BadRequestException(
                                    "Username or email already exists in Keycloak"
                            );
                        })
                .onStatus(HttpStatusCode::isError,
                        (req, res) -> {

                            String body = "";

                            try {
                                body = new String(
                                        res.getBody().readAllBytes()
                                );
                            } catch (Exception ignored) {
                            }

                            log.error(
                                    "Keycloak user creation failed. Status={}, Body={}",
                                    res.getStatusCode(),
                                    body
                            );

                            throw new BadRequestException(
                                    "Failed to create user in Keycloak. Status: "
                                            + res.getStatusCode()
                                            + ", Body: "
                                            + body
                            );
                        })
                .toBodilessEntity()
                .getHeaders()
                .getFirst(HttpHeaders.LOCATION);

        if (location == null || location.isBlank()) {

            log.error("Keycloak did not return Location header.");

            throw new BadRequestException(
                    "Keycloak user creation did not return user location"
            );
        }

        String keycloakUserId =
                extractUserIdFromLocation(location);

        log.debug("Keycloak user created with ID '{}'.",
                keycloakUserId);

        setPasswordAndClearActions(
                adminToken,
                keycloakUserId,
                request.getPassword(),
                nameParts
        );

        assignRealmRole(
                adminToken,
                keycloakUserId,
                role
        );

        log.info(
                "Successfully created Keycloak user '{}' with role '{}'.",
                request.getUsername(),
                role
        );

        return keycloakUserId;
    }

    private NameParts splitName(String fullName) {

        if (fullName == null || fullName.isBlank()) {
            return new NameParts("", "User");
        }

        String name = fullName.trim();

        if (!name.contains(" ")) {
            return new NameParts(name, "User");
        }

        int index = name.indexOf(' ');

        return new NameParts(
                name.substring(0, index).trim(),
                name.substring(index + 1).trim()
        );
    }

    private Map<String, Object> buildUserPayload(
            RegisterRequest request,
            NameParts nameParts
    ) {

        return Map.of(

                "username", request.getUsername(),

                "email", request.getEmail(),

                "enabled", true,

                "emailVerified", true,

                "firstName", nameParts.firstName(),

                "lastName", nameParts.lastName(),

                "requiredActions", List.of(),

                "credentials",
                List.of(
                        Map.of(
                                "type", "password",
                                "value", request.getPassword(),
                                "temporary", false
                        )
                ),

                "attributes",
                request.getTenantSlug() == null
                        || request.getTenantSlug().isBlank()

                        ? Map.of()

                        : Map.of(
                        "tenantSlug",
                        List.of(
                                request.getTenantSlug()
                                        .trim()
                                        .toLowerCase()
                        )
                )
        );
    }

    private String bearerToken(String token) {
        return "Bearer " + token;
    }

    private String realmAdminBaseUrl() {
        return keycloakProperties.getServerUrl()
                + "/admin/realms/{realm}";
    }

    private record NameParts(
            String firstName,
            String lastName
    ) {
    }

    private void setPasswordAndClearActions(
            String adminToken,
            String keycloakUserId,
            String password,
            NameParts nameParts
    ) {

        try {

            log.debug(
                    "Resetting password for Keycloak user '{}'.",
                    keycloakUserId
            );

            restClient.put()
                    .uri(
                            realmAdminBaseUrl() + "/users/{userId}/reset-password",
                            keycloakProperties.getRealm().trim(),
                            keycloakUserId
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            bearerToken(adminToken)
                    )
                    .body(buildPasswordPayload(password))
                    .retrieve()
                    .toBodilessEntity();

            log.debug(
                    "Updating Keycloak profile for user '{}'.",
                    keycloakUserId
            );

            restClient.put()
                    .uri(
                            realmAdminBaseUrl() + "/users/{userId}",
                            keycloakProperties.getRealm().trim(),
                            keycloakUserId
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            bearerToken(adminToken)
                    )
                    .body(buildUserUpdatePayload(nameParts))
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception ex) {

            log.error(
                    "Failed to update password/profile for Keycloak user '{}'.",
                    keycloakUserId,
                    ex
            );
        }
    }

    private Map<String, Object> buildPasswordPayload(
            String password
    ) {

        return Map.of(
                "type", "password",
                "value", password,
                "temporary", false
        );
    }

    private Map<String, Object> buildUserUpdatePayload(
            NameParts nameParts
    ) {

        return Map.of(
                "firstName", nameParts.firstName(),
                "lastName", nameParts.lastName(),
                "requiredActions", List.of(),
                "emailVerified", true,
                "enabled", true
        );
    }

    private void assignRealmRole(
            String adminToken,
            String keycloakUserId,
            Role role
    ) {

        try {

            log.debug(
                    "Assigning realm role '{}' to user '{}'.",
                    role.name(),
                    keycloakUserId
            );

            Map<String, Object> roleRepresentation =
                    fetchOrCreateRealmRole(
                            adminToken,
                            role.name()
                    );

            if (roleRepresentation == null) {

                log.warn(
                        "Unable to assign role '{}' because it could not be resolved.",
                        role.name()
                );

                return;
            }

            restClient.post()
                    .uri(
                            realmAdminBaseUrl()
                                    + "/users/{userId}/role-mappings/realm",
                            keycloakProperties.getRealm().trim(),
                            keycloakUserId
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            bearerToken(adminToken)
                    )
                    .body(List.of(roleRepresentation))
                    .retrieve()
                    .toBodilessEntity();

            log.info(
                    "Successfully assigned role '{}' to Keycloak user '{}'.",
                    role.name(),
                    keycloakUserId
            );

        } catch (Exception ex) {

            log.warn(
                    "Failed to assign realm role '{}' to user '{}'. Registration will continue.",
                    role.name(),
                    keycloakUserId,
                    ex
            );
        }
    }

    private Map<String, Object> fetchOrCreateRealmRole(
            String adminToken,
            String roleName
    ) {

        Map<String, Object> role =
                findRealmRole(adminToken, roleName);

        if (role != null) {
            return role;
        }

        log.warn(
                "Realm role '{}' not found. Creating it.",
                roleName
        );

        createRealmRole(adminToken, roleName);

        return findRealmRole(adminToken, roleName);
    }

    private Map<String, Object> findRealmRole(
            String adminToken,
            String roleName
    ) {

        try {

            log.debug(
                    "Looking up realm role '{}'.",
                    roleName
            );

            return restClient.get()
                    .uri(
                            realmAdminBaseUrl()
                                    + "/roles/{roleName}",
                            keycloakProperties.getRealm().trim(),
                            roleName
                    )
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            bearerToken(adminToken)
                    )
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

        } catch (Exception ex) {

            return null;
        }
    }

    private void createRealmRole(
            String adminToken,
            String roleName
    ) {

        try {

            log.info(
                    "Creating missing realm role '{}'.",
                    roleName
            );

            restClient.post()
                    .uri(
                            realmAdminBaseUrl() + "/roles",
                            keycloakProperties.getRealm().trim()
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            bearerToken(adminToken)
                    )
                    .body(
                            Map.of(
                                    "name", roleName,
                                    "description", roleName + " role"
                            )
                    )
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception ex) {

            log.error(
                    "Failed to create realm role '{}'.",
                    roleName,
                    ex
            );
        }
    }

    public void assignTenantToUser(String keycloakUserId, String tenantSlug) {

        log.info(
                "Assigning tenant '{}' to Keycloak user '{}'.",
                tenantSlug,
                keycloakUserId
        );

        String adminToken = keycloakTokenService.getAdminAccessToken();

        Map<String, Object> attributes =
                fetchUserAttributes(adminToken, keycloakUserId);

        attributes.put(
                "tenantSlug",
                List.of(tenantSlug.trim().toLowerCase())
        );

        updateUserAttributes(adminToken, keycloakUserId, attributes);

        assignRealmRole(adminToken, keycloakUserId, Role.TENANT_ADMIN);
    }

    private Map<String, Object> fetchUserAttributes(
            String adminToken,
            String keycloakUserId
    ) {

        try {

            Map<String, Object> user = restClient.get()
                    .uri(
                            realmAdminBaseUrl() + "/users/{userId}",
                            keycloakProperties.getRealm().trim(),
                            keycloakUserId
                    )
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            bearerToken(adminToken)
                    )
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (user != null
                    && user.get("attributes")
                    instanceof Map<?, ?> attributes) {

                Map<String, Object> result = new java.util.HashMap<>();

                attributes.forEach((key, value) ->
                        result.put(String.valueOf(key), value)
                );

                return result;
            }

        } catch (Exception ex) {

            log.warn(
                    "Failed to fetch attributes for Keycloak user '{}'.",
                    keycloakUserId,
                    ex
            );
        }

        return new java.util.HashMap<>();
    }

    private void updateUserAttributes(
            String adminToken,
            String keycloakUserId,
            Map<String, Object> attributes
    ) {

        restClient.put()
                .uri(
                        realmAdminBaseUrl() + "/users/{userId}",
                        keycloakProperties.getRealm().trim(),
                        keycloakUserId
                )
                .contentType(MediaType.APPLICATION_JSON)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        bearerToken(adminToken)
                )
                .body(Map.of("attributes", attributes))
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        (req, res) -> {

                            String body = "";

                            try {
                                body = new String(
                                        res.getBody().readAllBytes()
                                );
                            } catch (Exception ignored) {
                            }

                            log.error(
                                    "Keycloak tenant assignment failed. Status={}, Body={}",
                                    res.getStatusCode(),
                                    body
                            );

                            throw new BadRequestException(
                                    "Failed to update tenant assignment in Keycloak. Status: "
                                            + res.getStatusCode()
                                            + ", Body: "
                                            + body
                            );
                        })
                .toBodilessEntity();
    }

    public String findTenantSlugByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        log.debug("Looking up tenant slug for Keycloak user '{}'.", username);

        try {

            String adminToken = keycloakTokenService.getAdminAccessToken();

            List<Map<String, Object>> users = restClient.get()
                    .uri(
                            realmAdminBaseUrl() + "/users?username={username}",
                            keycloakProperties.getRealm().trim(),
                            username
                    )
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            bearerToken(adminToken)
                    )
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (users == null || users.isEmpty()) {
                return null;
            }

            Object attributes = users.get(0).get("attributes");

            if (attributes instanceof Map<?, ?> attributeMap) {
                Object tenantSlug = attributeMap.get("tenantSlug");
                if (tenantSlug instanceof List<?> values && !values.isEmpty()) {
                    return String.valueOf(values.get(0));
                }
                if (tenantSlug instanceof String value && !value.isBlank()) {
                    return value;
                }
            }

        } catch (Exception ex) {
            log.warn("Failed to resolve tenant slug for Keycloak user '{}'.", username, ex);
        }

        return null;
    }

    private String extractUserIdFromLocation(String location) {

        log.debug("Extracting Keycloak user ID from Location header.");

        try {

            String path = URI.create(location).getPath();

            int index = path.lastIndexOf('/');

            if (index < 0 || index == path.length() - 1) {

                log.error(
                        "Unable to extract Keycloak user ID from Location '{}'.",
                        location
                );

                throw new BadRequestException(
                        "Unable to parse Keycloak user id from location: "
                                + location
                );
            }

            String userId = path.substring(index + 1);

            log.debug(
                    "Successfully extracted Keycloak user ID '{}'.",
                    userId
            );

            return userId;

        } catch (IllegalArgumentException ex) {

            log.error(
                    "Invalid Keycloak Location URI '{}'.",
                    location,
                    ex
            );

            throw new BadRequestException(
                    "Invalid Keycloak Location header returned."
            );
        }
    }
}
