package com.sentinel.inventory_service.service;

import com.sentinel.inventory_service.dto.AddStockRequest;
import com.sentinel.inventory_service.entity.Inventory;
import com.sentinel.inventory_service.event.InventoryRejectedEvent;
import com.sentinel.inventory_service.event.InventoryReservedEvent;
import com.sentinel.inventory_service.event.OrderCreatedEvent;
import com.sentinel.inventory_service.event.RejectionReason;
import com.sentinel.inventory_service.exception.InventoryNotFoundException;
import com.sentinel.inventory_service.repository.InventoryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryService(InventoryRepository inventoryRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryRepository = inventoryRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Inventory addStock(AddStockRequest request) {
        Inventory inventory = inventoryRepository.findBySku(request.getSku())
                .orElseGet(() -> {
            Inventory newInventory = new Inventory();
            newInventory.setSku(request.getSku());
            newInventory.setQuantity(0);
            newInventory.setReservedQuantity(0);
            return newInventory;
        });
        inventory.setQuantity(request.getQuantity() + inventory.getQuantity());
        return inventoryRepository.save(inventory);
    }

    public Inventory getInventory(String sku) {
        return inventoryRepository.findBySku(sku).orElseThrow(() -> new InventoryNotFoundException(sku));
    }

    @Transactional
    @KafkaListener(topics = "order-created", groupId = "inventory-service")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        for (OrderCreatedEvent.OrderItemInfo item : event.items()) {
            Optional<Inventory> maybeInventory = inventoryRepository.findBySku(item.sku());

            boolean unavailable = maybeInventory.isEmpty()
                    || (maybeInventory.get().getQuantity() - maybeInventory.get().getReservedQuantity()) < item.quantity();
            if (unavailable) {
                RejectionReason reason = maybeInventory.isEmpty() ? RejectionReason.SKU_NOT_FOUND : RejectionReason.INSUFFICIENT_STOCK;
                InventoryRejectedEvent rejectedEvent = new InventoryRejectedEvent(
                        event.orderId(),
                        item.sku(),
                        reason,
                        Instant.now()
                );
                kafkaTemplate.send("inventory-rejected", event.orderId().toString(), rejectedEvent);
                return;
            }
        }

        for (OrderCreatedEvent.OrderItemInfo item: event.items()) {
            Inventory inventory = inventoryRepository.findBySku(item.sku()).get();
            inventory.setReservedQuantity(inventory.getReservedQuantity() + item.quantity());
            inventoryRepository.save(inventory);

            InventoryReservedEvent reservedEvent = new InventoryReservedEvent(
                    event.orderId(),
                    item.sku(),
                    item.quantity(),
                    Instant.now()
            );
            kafkaTemplate.send("inventory-reserved", event.orderId().toString(), reservedEvent);
        }
    }
}
