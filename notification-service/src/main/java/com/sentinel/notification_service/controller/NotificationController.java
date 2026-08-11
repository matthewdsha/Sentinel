package com.sentinel.notification_service.controller;

import com.sentinel.notification_service.dto.NotificationResponse;
import com.sentinel.notification_service.model.Notification;
import com.sentinel.notification_service.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        List<NotificationResponse> responses = notificationService.getNotifications().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setOrderId(notification.getOrderId());
        response.setMessage(notification.getMessage());
        response.setTimestamp(notification.getTimestamp());
        return response;
    }
}
