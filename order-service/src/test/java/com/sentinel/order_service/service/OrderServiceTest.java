package com.sentinel.order_service.service;

import com.sentinel.order_service.entity.Order;
import com.sentinel.order_service.entity.OrderItem;
import com.sentinel.order_service.event.InventoryRejectedEvent;
import com.sentinel.order_service.event.InventoryReservedEvent;
import com.sentinel.order_service.event.OrderStatus;
import com.sentinel.order_service.event.RejectionReason;
import com.sentinel.order_service.respository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    void confirmsOrder_whenAllItemsReserved() {
        OrderItem item = new OrderItem();
        item.setSku("WIDGET-1");
        item.setQuantity(2);
        item.setReserved(false);

        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(List.of(item));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        InventoryReservedEvent event = new InventoryReservedEvent(1L, "WIDGET-1", 2, Instant.now());

        orderService.handleInventoryReserved(event);

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void stayPending_whenNotAllItemsReserved() {
        OrderItem reservedItem = new OrderItem();
        reservedItem.setSku("WIDGET-1");
        reservedItem.setReserved(true);

        OrderItem unreservedItem = new OrderItem();
        unreservedItem.setSku("GADGET-1");
        unreservedItem.setReserved(false);

        Order order = new Order();
        order.setId(2L);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(List.of(reservedItem, unreservedItem));

        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));

        InventoryReservedEvent event = new InventoryReservedEvent(2L, "WIDGET-1", 1, Instant.now());

        orderService.handleInventoryReserved(event);

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void rejectsOrder_setRejectedStatus() {
        Order order = new Order();
        order.setId(3L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        InventoryRejectedEvent event = new InventoryRejectedEvent(3L, "WIDGET-1", RejectionReason.INSUFFICIENT_STOCK, Instant.now());

        orderService.handleInventoryRejected(event);

        assertEquals(OrderStatus.REJECTED, order.getStatus());
        verify(orderRepository).save(order);
    }
}
