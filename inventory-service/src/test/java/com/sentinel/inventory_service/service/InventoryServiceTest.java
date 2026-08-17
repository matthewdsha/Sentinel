package com.sentinel.inventory_service.service;

import com.sentinel.inventory_service.entity.Inventory;
import com.sentinel.inventory_service.event.OrderCreatedEvent;
import com.sentinel.inventory_service.event.OrderStatus;
import com.sentinel.inventory_service.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void reservesStock_whenEnoughInventoryExists() {
        Inventory inventory = new Inventory();
        inventory.setSku("WIDGET-1");
        inventory.setQuantity(10);
        inventory.setReservedQuantity(0);

        when(inventoryRepository.findBySku("WIDGET-1")).thenReturn(Optional.of(inventory));

        OrderCreatedEvent event = new OrderCreatedEvent(
                1L,
                100L,
                List.of(new OrderCreatedEvent.OrderItemInfo("WIDGET-1", 5)),
                OrderStatus.PENDING,
                Instant.now()
        );

        inventoryService.consumeOrderCreated(event);

        verify(kafkaTemplate).send(eq("inventory-reserved"), eq("1"), any());
        verify(kafkaTemplate, never()).send(eq("inventory-rejected"), any(), any());
    }

    @Test
    void rejectsOrder_whenInsufficientStock() {
        Inventory inventory = new Inventory();
        inventory.setSku("WIDGET-1");
        inventory.setQuantity(3);
        inventory.setReservedQuantity(0);

        when(inventoryRepository.findBySku("WIDGET-1")).thenReturn(Optional.of(inventory));

        OrderCreatedEvent event = new OrderCreatedEvent(
                2L,
                100L,
                List.of(new OrderCreatedEvent.OrderItemInfo("WIDGET-1", 5)),
                OrderStatus.PENDING,
                Instant.now()
        );

        inventoryService.consumeOrderCreated(event);

        verify(kafkaTemplate).send(eq("inventory-rejected"), eq("2"), any());
        verify(kafkaTemplate, never()).send(eq("inventory-reserved"), any(), any());
    }

    @Test
    void rejectsOrder_whenSkuNotFound() {
        when(inventoryRepository.findBySku("GHOST-SKU")).thenReturn(Optional.empty());

        OrderCreatedEvent event = new OrderCreatedEvent(
                3L,
                100L,
                List.of(new OrderCreatedEvent.OrderItemInfo("GHOST-SKU", 1)),
                OrderStatus.PENDING,
                Instant.now()
        );

        inventoryService.consumeOrderCreated(event);

        verify(kafkaTemplate).send(eq("inventory-rejected"), eq("3"), any());
    }
}
