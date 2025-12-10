//package com.klp.order.domain.repository;
//
//import com.klp.order.domain.entity.saga.OrderSaga;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//public interface OrderSagaRepository {
//
//    OrderSaga save(OrderSaga saga);
//
//    Optional<OrderSaga> findById(UUID sagaId);
//
//    Optional<OrderSaga> findByOrder_OrderId(UUID orderId);
//
//    List<OrderSaga> findAll();
//}