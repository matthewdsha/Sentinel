package com.sentinel.order_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponse {
    private String sku;
    private Integer quantity;
}
