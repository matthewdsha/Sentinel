package com.sentinel.inventory_service.event;

import java.time.Instant;

public record InventoryReservedEvent(
        Long orderId,
        String sku,
        Integer quantity,
        Instant timestamp
) {
}
