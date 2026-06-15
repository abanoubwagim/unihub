package com.unihub.notifications.api.controllers;

import com.unihub.notifications.api.dto.req.UpdatePreferencesRequest;
import com.unihub.notifications.api.dto.res.NotificationPreferencesResponse;
import com.unihub.notifications.api.dto.res.NotificationResponse;
import com.unihub.notifications.application.usecase.NotificationUseCase;
import com.unihub.shared.api.dto.PageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationUseCase notificationUseCase;

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            Authentication authentication,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        UUID userId = UUID.fromString(authentication.getName());
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return ResponseEntity.ok(
                notificationUseCase.getNotifications(userId, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        long count = notificationUseCase.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markRead(
            Authentication authentication,
            @PathVariable UUID notificationId) {

        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(
                notificationUseCase.markRead(userId, notificationId));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        notificationUseCase.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            Authentication authentication,
            @PathVariable UUID notificationId) {
        UUID userId = UUID.fromString(authentication.getName());
        notificationUseCase.deleteNotification(userId, notificationId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/preferences")
    public ResponseEntity<List<NotificationPreferencesResponse>> getPreferences(
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(
                notificationUseCase.getPreferences(userId));
    }

    @PutMapping("/preferences")
    public ResponseEntity<List<NotificationPreferencesResponse>> updatePreferences(
            Authentication authentication,
            @RequestBody @Valid UpdatePreferencesRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(
                notificationUseCase.updatePreferences(userId, request));
    }
}