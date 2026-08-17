package com.sentinel.inventory_service.repository;

import com.sentinel.inventory_service.entity.Inventory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class InventoryRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @org.springframework.beans.factory.annotation.Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void savesAndFindsBySku() {
        Inventory inventory = new Inventory();
        inventory.setSku("WIDGET-1");
        inventory.setQuantity(10);
        inventory.setReservedQuantity(0);

        inventoryRepository.save(inventory);

        Optional<Inventory> found = inventoryRepository.findBySku("WIDGET-1");

        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualTo(10);
    }

    @Test
    void findBySku_returnsEmpty_whenSkuDoesNotExist() {
        Optional<Inventory> found = inventoryRepository.findBySku("GHOST-SKU");

        assertThat(found).isEmpty();
    }

    @Test
    void skuUniqueConstraint_isEnforcedByDatabase() {
        Inventory first = new Inventory();
        first.setSku("WIDGET-1");
        first.setQuantity(3);
        first.setReservedQuantity(0);

        inventoryRepository.save(first);

        Inventory duplicate = new Inventory();
        duplicate.setSku("WIDGET-1");
        duplicate.setQuantity(3);
        duplicate.setReservedQuantity(0);

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> inventoryRepository.saveAndFlush(duplicate)
        );
    }
}
