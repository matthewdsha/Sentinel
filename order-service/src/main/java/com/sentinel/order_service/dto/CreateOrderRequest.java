package com.sentinel.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {
    @NotNull
    private Long customerId;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}
