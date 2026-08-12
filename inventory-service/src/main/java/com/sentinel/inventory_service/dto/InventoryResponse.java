package com.sentinel.inventory_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class InventoryResponse {
    private String sku;
    private Integer quantity;
    private Integer reservedQuantity;
    private Instant lastUpdated;
}
