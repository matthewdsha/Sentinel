package com.sentinel.inventory_service.event;

import java.time.Instant;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        Long customerId,
        List<OrderItemInfo> items,
        OrderStatus status,
        Instant createdAt
) {
    public record OrderItemInfo(
            String sku,
            Integer quantity
    ) {}
}
