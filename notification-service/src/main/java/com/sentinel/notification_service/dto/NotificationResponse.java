package com.sentinel.notification_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class NotificationResponse {
    private Long orderId;
    private String message;
    private Instant timestamp;
}
