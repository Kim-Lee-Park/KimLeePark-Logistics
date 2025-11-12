package com.klp.order.infrastructure.repository;

import com.klp.order.domain.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
