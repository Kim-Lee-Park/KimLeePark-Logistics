package com.klp.logistics.order.infrastructure.repository;

import com.klp.logistics.order.domain.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
