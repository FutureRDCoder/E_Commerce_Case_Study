package com.ecommerce.service;

import com.ecommerce.config.KeycloakProperties;
import com.ecommerce.exception.BadRequestException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class KeycloakTokenService {

    private final RestClient restClient;
    private final KeycloakProperties keycloakProperties;

    public KeycloakTokenService(RestClient.Builder restClientBuilder, KeycloakProperties keycloakProperties) {
        this.restClient = restClientBuilder.build();
        this.keycloakProperties = keycloakProperties;
    }

    public String loginAndGetAccessToken(String username, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", keycloakProperties.getClientId());
        if (keycloakProperties.getClientSecret() != null && !keycloakProperties.getClientSecret().isBlank()) {
            form.add("client_secret", keycloakProperties.getClientSecret());
        }
        form.add("scope", "openid profile email");
        form.add("username", username);
        form.add("password", password);

        Map<String, Object> response = restClient.post()
                .uri(tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                    String body = "";
                    try {
                        body = new String(clientResponse.getBody().readAllBytes());
                        if (body.contains("error_description")) {
                            com.fasterxml.jackson.databind.JsonNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
                            if (jsonNode.has("error_description")) {
                                throw new BadRequestException("Keycloak authentication error: " + jsonNode.get("error_description").asText());
                            }
                        }
                    } catch (BadRequestException bre) {
                        throw bre;
                    } catch (Exception ignored) {
                    }
                    throw new BadRequestException("Invalid username or password");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, clientResponse) -> {
                    throw new BadRequestException("Keycloak service is unavailable");
                })
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null || response.get("access_token") == null) {
            throw new BadRequestException("Keycloak did not return an access token");
        }
        return response.get("access_token").toString();
    }

    public String getAdminAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", adminClientId());
        if (adminClientSecret() != null && !adminClientSecret().isBlank()) {
            form.add("client_secret", adminClientSecret());
        }

        Map<String, Object> response = restClient.post()
                .uri(tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                    throw new BadRequestException("Failed to obtain Keycloak admin access token");
                })
                .body(new ParameterizedTypeReference<>() {
                });

        if (response == null || response.get("access_token") == null) {
            throw new BadRequestException("Keycloak admin token response is invalid");
        }
        return response.get("access_token").toString();
    }

    private String adminClientId() {
        if (keycloakProperties.getAdminClientId() != null && !keycloakProperties.getAdminClientId().isBlank()) {
            return keycloakProperties.getAdminClientId();
        }
        return keycloakProperties.getClientId();
    }

    private String adminClientSecret() {
        if (keycloakProperties.getAdminClientSecret() != null && !keycloakProperties.getAdminClientSecret().isBlank()) {
            return keycloakProperties.getAdminClientSecret();
        }
        return keycloakProperties.getClientSecret();
    }

    private String tokenUrl() {
        return keycloakProperties.getServerUrl() + "/realms/" + keycloakProperties.getRealm() + "/protocol/openid-connect/token";
    }
}
