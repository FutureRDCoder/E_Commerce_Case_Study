package com.ecommerce.service;

import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.TenantRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @Mock
    private KeycloakTokenService keycloakTokenService;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private UserIdentityService userIdentityService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        mockUser = User.builder()
                .id(1L)
                .name("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role(Role.USER)
                .build();
    }

    @Test
    void testRegister_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(keycloakAdminService.createUser(registerRequest, Role.USER)).thenReturn("kc-user-1");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(keycloakTokenService.loginAndGetAccessToken("testuser", "password123")).thenReturn("mock_jwt_token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mock_jwt_token", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals(Role.USER, response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateUsername_ThrowsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        Jwt jwt = Jwt.withTokenValue("mock_jwt_token")
                .header("alg", "none")
                .claim("sub", "kc-user-1")
                .claim("preferred_username", "testuser")
                .build();
        when(keycloakTokenService.loginAndGetAccessToken("testuser", "password123")).thenReturn("mock_jwt_token");
        when(jwtDecoder.decode("mock_jwt_token")).thenReturn(jwt);
        when(userIdentityService.resolveOrProvisionUserFromJwt(jwt)).thenReturn(mockUser);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mock_jwt_token", response.getToken());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void testLogin_InvalidPassword_ThrowsException() {
        when(keycloakTokenService.loginAndGetAccessToken("wrong_username", "wrong_password"))
                .thenThrow(new BadRequestException("Invalid username or password"));

        loginRequest.setUsername("wrong_username");
        loginRequest.setPassword("wrong_password");
        assertThrows(BadRequestException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_UserNotFound_ThrowsException() {
        when(keycloakTokenService.loginAndGetAccessToken("nonexistent", "password123"))
                .thenThrow(new BadRequestException("Invalid username or password"));

        loginRequest.setUsername("nonexistent");
        assertThrows(BadRequestException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testGetCurrentUser_Success() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(mockUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        Optional<User> result = authService.getCurrentUser();

        assertTrue(result.isPresent());
        assertEquals(mockUser.getUsername(), result.get().getUsername());

        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUser_NotAuthenticated() {
        SecurityContextHolder.clearContext();

        Optional<User> result = authService.getCurrentUser();

        assertFalse(result.isPresent());
    }
}
