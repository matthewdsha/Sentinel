package com.sentinel.order_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
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

    @AssertTrue(message = "items must not contain duplicate skus")
    public boolean isSkusUnique() {
        if (items == null) return true;
        long uniqueCount = items.stream().map(OrderItemRequest::getSku).distinct().count();
        return uniqueCount == items.size();
    }
}
