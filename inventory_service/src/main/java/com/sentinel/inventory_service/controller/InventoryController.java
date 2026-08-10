package com.sentinel.inventory_service.controller;

import com.sentinel.inventory_service.dto.AddStockRequest;
import com.sentinel.inventory_service.dto.InventoryResponse;
import com.sentinel.inventory_service.entity.Inventory;
import com.sentinel.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) { this.inventoryService = inventoryService;}

    @PostMapping
    public ResponseEntity<InventoryResponse> addStock(@Valid @RequestBody AddStockRequest request) {
        Inventory inventory = inventoryService.addStock(request);
        return ResponseEntity.ok(toResponse(inventory));
    }

    @GetMapping("/{sku}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable String sku) {
        Inventory inventory = inventoryService.getInventory(sku);
        return ResponseEntity.ok(toResponse(inventory));
    }

    private InventoryResponse toResponse(Inventory inventory) {
        InventoryResponse inventoryResponse = new InventoryResponse();

        inventoryResponse.setSku(inventory.getSku());
        inventoryResponse.setQuantity(inventory.getQuantity());
        inventoryResponse.setReservedQuantity(inventory.getReservedQuantity());
        inventoryResponse.setLastUpdated(inventory.getLastUpdated());

        return inventoryResponse;
    }
}
