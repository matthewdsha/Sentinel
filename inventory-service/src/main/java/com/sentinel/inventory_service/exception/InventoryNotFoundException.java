package com.sentinel.inventory_service.exception;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(String sku) {
        super("Item with sku " + sku + " not found.");
    }
}
