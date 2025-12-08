//package com.klp.order.infrastructure.repository;
//
//import com.klp.order.domain.entity.saga.OrderSaga;
//import java.util.Optional;
//import java.util.UUID;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//public interface OrderSagaJpaRepository extends JpaRepository<OrderSaga, UUID> {
//
//    Optional<OrderSaga> findByOrder_OrderId(UUID orderId);
//}
