//package com.klp.order.presentation.controller;
//
//import com.klp.order.infrastructure.event.event.DeliveryCreatedEvent;
//import com.klp.order.infrastructure.event.event.DeliveryCreatedEvent.DeliveryItem;
//import com.klp.order.infrastructure.event.event.PaymentApprovedEvent;
//import java.util.List;
//import java.util.UUID;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@Slf4j
//@RestController
//@RequestMapping("/test/kafka")
//@RequiredArgsConstructor
//public class KafkaTestController {
//
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//
//    @PostMapping("/payment/{orderId}")
//    public String publishPaymentCompleted(@PathVariable UUID orderId) {
//        log.info("=== Payment Completed 이벤트 테스트 발행: orderId={} ===", orderId);
//
//        PaymentApprovedEvent event = new PaymentApprovedEvent(
//            orderId,
//            UUID.randomUUID()
//        );
//
//        kafkaTemplate.send("payment.completed", orderId.toString(), event);
//
//        return "Payment completed event published for orderId: " + orderId;
//    }
//
//    @PostMapping("/delivery/{orderId}/{orderItemId}")
//    public String publishDeliveryCreated(
//        @PathVariable UUID orderId,
//        @PathVariable UUID orderItemId) {
//
//        log.info("=== Delivery Created 이벤트 테스트 발행: orderId={}, orderItemId={} ===",
//            orderId, orderItemId);
//
//        DeliveryItem deliveryItem = new DeliveryItem(
//            orderItemId,
//            UUID.randomUUID()
//        );
//
//        DeliveryCreatedEvent event = new DeliveryCreatedEvent(
//            orderId,
//            List.of(deliveryItem)
//        );
//
//        kafkaTemplate.send("delivery.created", orderId.toString(), event);
//
//        return "Delivery created event published for orderId: " + orderId;
//    }
//}