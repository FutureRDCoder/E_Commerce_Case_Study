package com.ecommerce.service;

import com.ecommerce.dto.response.NotificationResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedAccessException;
import com.ecommerce.model.Notification;
import com.ecommerce.model.Role;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = Tenant.builder()
                .id(1L)
                .name("Nike")
                .slug("nike")
                .build();

        user = User.builder()
                .id(1L)
                .name("Test User")
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .build();
    }

    @Test
    void testNotifyTenantAdminAssignment_CreatesUnreadNotification() {
        Notification saved = Notification.builder()
                .id(5L)
                .user(user)
                .tenant(tenant)
                .message("You have been made TENANT ADMIN of Nike. You can now manage this brand's products and orders.")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        NotificationResponse response =
                notificationService.notifyTenantAdminAssignment(user, tenant);

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertFalse(response.isRead());
        assertEquals("Nike", response.getTenantName());
        assertEquals("nike", response.getTenantSlug());
        assertTrue(response.getMessage().contains("TENANT ADMIN of Nike"));

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testGetNotificationsForUser_ReturnsOnlyThatUserNotifications() {
        Notification notification = Notification.builder()
                .id(1L)
                .user(user)
                .tenant(tenant)
                .message("Test message")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<NotificationResponse> response =
                notificationService.getNotificationsForUser(1L);

        assertEquals(1, response.size());
        assertEquals("Test message", response.get(0).getMessage());
        assertEquals("nike", response.get(0).getTenantSlug());
    }

    @Test
    void testMarkAsRead_Success() {
        Notification notification = Notification.builder()
                .id(1L)
                .user(user)
                .tenant(tenant)
                .message("Test message")
                .read(false)
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response =
                notificationService.markAsRead(1L, 1L);

        assertTrue(response.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void testMarkAsRead_NotFound_ThrowsException() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.markAsRead(99L, 1L)
        );
    }

    @Test
    void testMarkAsRead_OtherUsersNotification_ThrowsException() {
        User otherUser = User.builder()
                .id(2L)
                .name("Other")
                .username("other")
                .email("other@example.com")
                .role(Role.USER)
                .build();

        Notification notification = Notification.builder()
                .id(1L)
                .user(otherUser)
                .tenant(tenant)
                .message("Test message")
                .read(false)
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThrows(
                UnauthorizedAccessException.class,
                () -> notificationService.markAsRead(1L, 1L)
        );
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testMarkAllAsRead_MarksAllUnread() {
        Notification first = Notification.builder()
                .id(1L)
                .user(user)
                .tenant(tenant)
                .message("First")
                .read(false)
                .build();

        Notification second = Notification.builder()
                .id(2L)
                .user(user)
                .tenant(tenant)
                .message("Second")
                .read(false)
                .build();

        when(notificationRepository.findByUserIdAndReadFalse(1L))
                .thenReturn(List.of(first, second));

        notificationService.markAllAsRead(1L);

        assertTrue(first.isRead());
        assertTrue(second.isRead());
        verify(notificationRepository).saveAll(List.of(first, second));
    }
}
