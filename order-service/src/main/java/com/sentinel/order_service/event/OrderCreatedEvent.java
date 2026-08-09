package com.sentinel.order_service.event;

import java.time.Instant;
import java.util.List;

public record OrderCreatedEvent(
        Long orderId,
        Long customerId,
        List<OrderItemInfo> items,
        String status,
        Instant createdAt
) {
    public record OrderItemInfo(
            String sku,
            Integer quantity
    ) {}
}
