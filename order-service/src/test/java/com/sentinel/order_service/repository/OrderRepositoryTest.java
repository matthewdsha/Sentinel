package com.sentinel.order_service.repository;

import com.sentinel.order_service.entity.Order;
import com.sentinel.order_service.entity.OrderItem;
import com.sentinel.order_service.event.OrderStatus;
import com.sentinel.order_service.respository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class OrderRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @org.springframework.beans.factory.annotation.Autowired
    private OrderRepository orderRepository;

    @Test
    void savesAndRetrievesOrderWithItems() {
        Order order = new Order();
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PENDING);

        OrderItem item = new OrderItem();
        item.setSku("WIDGET-1");
        item.setQuantity(2);
        item.setOrder(order);

        order.setItems(List.of(item));

        Order saved = orderRepository.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().getFirst().getSku()).isEqualTo("WIDGET-1");
    }

    @Test
    void findById_returnsEmpty_whenOrderDoesNotExist() {
        Optional<Order> result = orderRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void cascadeSaveIncludesItems_withoutSeparateSave() {
        Order order = new Order();
        order.setCustomerId(2L);
        order.setStatus(OrderStatus.PENDING);

        OrderItem itemA = new OrderItem();
        itemA.setSku("WIDGET-1");
        itemA.setQuantity(1);
        itemA.setOrder(order);

        OrderItem itemB = new OrderItem();
        itemB.setSku("GADGET-9");
        itemB.setQuantity(3);
        itemB.setOrder(order);

        order.setItems(List.of(itemA, itemB));

        Order saved = orderRepository.save(order);
        Optional<Order> reloaded = orderRepository.findById(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getItems()).hasSize(2);
    }
}
