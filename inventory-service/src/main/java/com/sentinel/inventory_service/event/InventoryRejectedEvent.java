package com.sentinel.inventory_service.event;

import java.time.Instant;

public record InventoryRejectedEvent(
        Long orderId,
        String sku,
        RejectionReason reason,
        Instant timestamp
) {
}
