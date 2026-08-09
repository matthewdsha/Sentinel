package com.sentinel.order_service.controller;

import com.sentinel.order_service.dto.CreateOrderRequest;
import com.sentinel.order_service.dto.OrderItemResponse;
import com.sentinel.order_service.dto.OrderResponse;
import com.sentinel.order_service.entity.Order;
import com.sentinel.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(toResponse(order));
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();

        response.setOrderId(order.getId());
        response.setStatus(order.getStatus());

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(item -> {
                    OrderItemResponse itemResponse = new OrderItemResponse();
                    itemResponse.setSku(item.getSku());
                    itemResponse.setQuantity(item.getQuantity());
                    return itemResponse;
                }
        ).toList();

        response.setItems(items);
        return response;
    }
}
