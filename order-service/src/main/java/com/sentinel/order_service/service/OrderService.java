package com.sentinel.order_service.service;

import com.sentinel.order_service.dto.CreateOrderRequest;
import com.sentinel.order_service.entity.Order;
import com.sentinel.order_service.entity.OrderItem;
import com.sentinel.order_service.event.OrderCreatedEvent;
import com.sentinel.order_service.exception.OrderNotFoundException;
import com.sentinel.order_service.respository.OrderRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order();

        order.setCustomerId(request.getCustomerId());
        order.setStatus("PENDING");

        List<OrderItem> items = request.getItems()
                .stream()
                .map(itemRequest -> {
                    OrderItem item = new OrderItem();
                    item.setSku(itemRequest.getSku());
                    item.setQuantity(itemRequest.getQuantity());
                    item.setOrder(order);
                    return item;
                }).toList();

        order.setItems(items);
        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getItems().stream()
                        .map(item -> new OrderCreatedEvent.OrderItemInfo(
                                item.getSku(),
                                item.getQuantity()
                        )).toList(),
                savedOrder.getStatus(),
                savedOrder.getCreatedAt()
        );
        kafkaTemplate.send("order-created", order.getId().toString(), event);
        return savedOrder;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}
