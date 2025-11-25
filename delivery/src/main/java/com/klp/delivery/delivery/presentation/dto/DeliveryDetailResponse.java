package com.klp.delivery.delivery.presentation.dto;

import com.klp.delivery.common.enums.DeliveryStatus;
import com.klp.delivery.delivery.domain.entity.Delivery;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public record DeliveryDetailResponse(
    UUID deliveryId,
    UUID orderId,
    UUID departureId,
    UUID arrivalId,
    UUID receiverId,
    String receiverName,
    String address,
    String receiverSlackId,
    DeliveryStatus status
) {

    public static DeliveryDetailResponse from(Delivery delivery) {
        return new DeliveryDetailResponse(
            delivery.getDeliveryId(),
            delivery.getOrderId(),
            delivery.getDepartureId(),
            delivery.getArrivalId(),
            delivery.getReceiverId(),
            delivery.getReceiverName(),
            delivery.getAddress(),
            delivery.getReceiverSlackId(),
            delivery.getStatus()
        );
    }

    public static List<DeliveryDetailResponse> from(List<Delivery> deliveries) {
        return deliveries.stream()
            .map(DeliveryDetailResponse::from)
            .toList();
    }

    public static Page<DeliveryDetailResponse> from(Page<Delivery> deliveryPage) {
        List<DeliveryDetailResponse> content = deliveryPage.getContent().stream()
            .map(DeliveryDetailResponse::from)
            .toList();
        return new PageImpl<>(content, deliveryPage.getPageable(), deliveryPage.getTotalElements());
    }
}

