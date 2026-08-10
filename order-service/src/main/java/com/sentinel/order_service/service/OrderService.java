package com.sentinel.order_service.service;

import com.sentinel.order_service.dto.CreateOrderRequest;
import com.sentinel.order_service.entity.Order;
import com.sentinel.order_service.entity.OrderItem;
import com.sentinel.order_service.event.InventoryRejectedEvent;
import com.sentinel.order_service.event.InventoryReservedEvent;
import com.sentinel.order_service.event.OrderCreatedEvent;
import com.sentinel.order_service.event.OrderStatus;
import com.sentinel.order_service.exception.OrderNotFoundException;
import com.sentinel.order_service.respository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        order.setStatus(OrderStatus.PENDING);

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

    @Transactional
    @KafkaListener(topics = "inventory-rejected", groupId = "order-service")
    public void handleInventoryRejected(InventoryRejectedEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElseThrow(() -> new OrderNotFoundException(event.orderId()));
        order.setStatus(OrderStatus.REJECTED);
        orderRepository.save(order);
    }

    @Transactional
    @KafkaListener(topics = "inventory-reserved", groupId = "order-service")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElseThrow(() -> new OrderNotFoundException(event.orderId()));

        order.getItems().stream()
                .filter(item -> item.getSku().equals(event.sku()))
                .findFirst()
                .ifPresent(item -> item.setReserved(true));

        boolean allReserved = order.getItems().stream().allMatch(OrderItem::isReserved);

        if (order.getStatus() == OrderStatus.PENDING && allReserved) {
            order.setStatus(OrderStatus.CONFIRMED);
        }

        orderRepository.save(order);
    }
}
