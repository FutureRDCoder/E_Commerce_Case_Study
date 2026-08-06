package com.ecommerce.service;

import com.ecommerce.dto.response.NotificationResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedAccessException;
import com.ecommerce.model.Notification;
import com.ecommerce.model.Tenant;
import com.ecommerce.model.User;
import com.ecommerce.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public NotificationResponse notifyTenantAdminAssignment(User user, Tenant tenant) {

        String message = "You have been made TENANT ADMIN of "
                + tenant.getName()
                + ". You can now manage this brand's products and orders.";

        Notification notification = Notification.builder()
                .user(user)
                .tenant(tenant)
                .message(message)
                .read(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        log.info(
                "Notification created for user '{}': {}",
                user.getUsername(),
                message
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForUser(Long userId) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId
                ));

        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException(
                    "You cannot access another user's notification."
            );
        }

        notification.setRead(true);

        log.info(
                "Marked notification '{}' as read for user '{}'.",
                notificationId,
                userId
        );

        return mapToResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(Long userId) {

        List<Notification> unread =
                notificationRepository.findByUserIdAndReadFalse(userId);

        unread.forEach(notification -> notification.setRead(true));

        notificationRepository.saveAll(unread);

        log.info("Marked {} notification(s) as read for user '{}'.",
                unread.size(),
                userId
        );
    }

    private NotificationResponse mapToResponse(Notification notification) {

        Tenant tenant = notification.getTenant();

        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .tenantId(tenant != null ? tenant.getId() : null)
                .tenantName(tenant != null ? tenant.getName() : null)
                .tenantSlug(tenant != null ? tenant.getSlug() : null)
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
