package com.ecommerce.service;

import com.ecommerce.config.KeycloakProperties;
import com.ecommerce.exception.BadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class KeycloakTokenService {

    private static final String TOKEN_ENDPOINT =
            "/realms/{realm}/protocol/openid-connect/token";

    private final RestClient restClient;
    private final KeycloakProperties keycloakProperties;
    private final ObjectMapper objectMapper;

    public KeycloakTokenService(
            RestClient.Builder restClientBuilder,
            KeycloakProperties keycloakProperties,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClientBuilder.build();
        this.keycloakProperties = keycloakProperties;
        this.objectMapper = objectMapper;
    }

    public String loginAndGetAccessToken(
            String username,
            String password
    ) {

        log.info(
                "Authenticating Keycloak user '{}'.",
                username
        );

        MultiValueMap<String, String> form =
                buildPasswordGrantForm(username, password);

        Map<String, Object> response = restClient.post()
                .uri(
                        keycloakProperties.getServerUrl() + TOKEN_ENDPOINT,
                        keycloakProperties.getRealm().trim()
                )
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (request, clientResponse) ->
                                handleAuthenticationError(
                                        username,
                                        clientResponse
                                )
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (request, clientResponse) -> {
                            log.error(
                                    "Keycloak authentication service is unavailable."
                            );

                            throw new BadRequestException(
                                    "Keycloak service is unavailable."
                            );
                        }
                )
                .body(new ParameterizedTypeReference<>() {});

        String accessToken = extractAccessToken(
                response,
                "Keycloak did not return an access token."
        );

        log.info(
                "Successfully authenticated Keycloak user '{}'.",
                username
        );

        return accessToken;
    }

    private MultiValueMap<String, String> buildPasswordGrantForm(
            String username,
            String password
    ) {

        log.debug(
                "Building password grant request for '{}'.",
                username
        );

        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add("grant_type", "password");

        form.add(
                "client_id",
                keycloakProperties.getClientId()
        );

        addClientSecret(
                form,
                keycloakProperties.getClientSecret()
        );

        form.add(
                "scope",
                "openid profile email"
        );

        form.add(
                "username",
                username
        );

        form.add(
                "password",
                password
        );

        return form;
    }

    private void addClientSecret(
            MultiValueMap<String, String> form,
            String clientSecret
    ) {

        if (clientSecret != null &&
                !clientSecret.isBlank()) {

            form.add(
                    "client_secret",
                    clientSecret
            );
        }
    }

    public String getAdminAccessToken() {

        log.info("Requesting Keycloak admin access token.");

        MultiValueMap<String, String> form =
                buildClientCredentialsForm();

        Map<String, Object> response =
                requestToken(
                        form,
                        this::handleAdminTokenError
                );

        String accessToken = extractAccessToken(
                response,
                "Keycloak admin token response is invalid."
        );

        log.info("Successfully obtained Keycloak admin access token.");

        return accessToken;
    }

    private MultiValueMap<String, String> buildClientCredentialsForm() {

        log.debug("Building client credentials grant request.");

        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add(
                "grant_type",
                "client_credentials"
        );

        form.add(
                "client_id",
                adminClientId()
        );

        addClientSecret(
                form,
                adminClientSecret()
        );

        return form;
    }

    private Map<String, Object> requestToken(
            MultiValueMap<String, String> form,
            RestClient.ResponseSpec.ErrorHandler errorHandler
    ) {

        return restClient.post()
                .uri(
                        keycloakProperties.getServerUrl() + TOKEN_ENDPOINT,
                        keycloakProperties.getRealm().trim()
                )
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        errorHandler
                )
                .body(new ParameterizedTypeReference<>() {});
    }

    private String extractAccessToken(
            Map<String, Object> response,
            String errorMessage
    ) {

        log.debug("Extracting access token from Keycloak response.");

        if (response == null ||
                response.get("access_token") == null) {

            log.error(errorMessage);

            throw new BadRequestException(errorMessage);
        }

        return response
                .get("access_token")
                .toString();
    }

    private String adminClientId() {

        return keycloakProperties.getAdminClientId() != null
                && !keycloakProperties.getAdminClientId().isBlank()

                ? keycloakProperties.getAdminClientId()

                : keycloakProperties.getClientId();
    }

    private String adminClientSecret() {

        return keycloakProperties.getAdminClientSecret() != null
                && !keycloakProperties.getAdminClientSecret().isBlank()

                ? keycloakProperties.getAdminClientSecret()

                : keycloakProperties.getClientSecret();
    }

    private void handleAuthenticationError(
            String username,
            ClientHttpResponse response
    ) throws IOException {
        String body = "";
        try {
            body = new String(response.getBody().readAllBytes());
            if (body.contains("error_description")) {

                var json = objectMapper.readTree(body);

                if (json.has("error_description")) {
                    String message = json.get("error_description").asText();
                    log.warn( "Authentication failed for user '{}': {}", username, message );
                    throw new BadRequestException( "Keycloak authentication error: " + message );
                }
            }
        }
        catch (BadRequestException ex) { throw ex; }
        catch (Exception ex) {
            log.error( "Failed to parse Keycloak authentication response.", ex );
        }
        log.warn( "Invalid credentials supplied for user '{}'.", username );
        throw new BadRequestException( "Invalid username or password." ); }

    private void handleAdminTokenError(
            HttpRequest ignored,
            ClientHttpResponse response
    ) throws IOException {

        String body = "";

        try {

            body = new String(response.getBody().readAllBytes());

        } catch (Exception ex) {

            log.error(
                    "Failed to read Keycloak admin token error response.",
                    ex
            );
        }

        log.error(
                "Failed to obtain Keycloak admin access token. Status={}, Body={}",
                response.getStatusCode(),
                body
        );

        throw new BadRequestException(
                "Failed to obtain Keycloak admin access token. Status: "
                        + response.getStatusCode()
                        + ", Body: "
                        + body
        );
    }
}