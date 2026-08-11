package com.sentinel.notification_service.event;

import java.time.Instant;

public record InventoryReservedEvent(
        Long orderId,
        String sku,
        Integer quantity,
        Instant timestamp
) {
}
