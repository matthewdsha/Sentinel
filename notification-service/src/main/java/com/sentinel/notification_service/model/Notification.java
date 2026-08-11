package com.sentinel.notification_service.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@RedisHash("notifications")
public class Notification implements Serializable {
    @Id
    private String id;
    private Long orderId;
    private String message;
    private Instant timestamp;
}
