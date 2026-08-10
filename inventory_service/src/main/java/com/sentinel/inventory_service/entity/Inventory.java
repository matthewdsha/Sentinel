package com.sentinel.inventory_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name="reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @UpdateTimestamp
    @Column(name="last_updated", nullable = false)
    private Instant lastUpdated;
}
