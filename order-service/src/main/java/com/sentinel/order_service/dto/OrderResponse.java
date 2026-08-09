package com.sentinel.order_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Long orderId;
    private String status;
    private List<OrderItemResponse> items;
}
