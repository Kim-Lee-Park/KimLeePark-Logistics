package com.klp.delivery.delivery.presentation.dto;

import com.klp.delivery.common.enums.CustomerDeliveryStatus;
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
    String userName,
    String userAddress,
    String userDriverSlackId,
    CustomerDeliveryStatus status
) {

    public static DeliveryDetailResponse from(Delivery delivery) {
        return new DeliveryDetailResponse(
            delivery.getDeliveryId(),
            delivery.getOrderId(),
            delivery.getDepartureId(),
            delivery.getArrivalId(),
            delivery.getUserName(),
            delivery.getUserAddress(),
            delivery.getUserDriverSlackId(),
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

