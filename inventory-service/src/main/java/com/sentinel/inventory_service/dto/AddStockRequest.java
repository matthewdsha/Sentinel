package com.sentinel.inventory_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddStockRequest {
    @NotBlank
    private String sku;

    @NotNull
    @Min(1)
    private Integer quantity;
}
