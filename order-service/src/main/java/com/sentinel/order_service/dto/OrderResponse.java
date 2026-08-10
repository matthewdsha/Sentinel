package com.sentinel.order_service.dto;

import com.sentinel.order_service.event.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Long orderId;
    private OrderStatus status;
    private List<OrderItemResponse> items;
}
