package com.sentinel.order_service.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sentinel.order_service.entity.Order;


public interface OrderRepository extends JpaRepository<Order, Long> {

}
