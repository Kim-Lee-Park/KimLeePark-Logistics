package com.klp.delivery.delivery.application.service;


import com.klp.delivery.common.enums.CustomerDeliveryStatus;
import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.repository.DeliveryRepository;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.delivery.presentation.dto.DeliveryDetailResponse;
import com.klp.delivery.global.exception.BusinessException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public Delivery registerDelivery(DeliveryCommand command, List<OrderItemCommand> items) {
        try {

            return deliveryRepository.save(
                Delivery.create(
                    command.orderId(),
                    command.userDriverId(),
                    command.userDrvicerSlackId(),
                    command.departureId(),
                    command.departureName(),
                    command.arrivalId(),
                    command.arrivalName(),
                    command.userName(),
                    command.userAddress(),
                    items
                )
            );
        } catch (Exception e) {
            log.error("배송 저장 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.DELIVERY_CREATION_FAILED);
        }
    }

    public Delivery findDelivery(UUID deliveryId) {
        try {
            return deliveryRepository.findByDeliveryId(deliveryId);
        } catch (Exception e) {
            log.error("배송 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.DELIVERY_NOT_FOUND);
        }
    }

    public List<DeliveryDetailResponse> findDeliveriesByOrderId(UUID orderId) {
        try {
            List<Delivery> deliveries = deliveryRepository.findDeliveryByOrderId(orderId);
            return DeliveryDetailResponse.from(deliveries);
        } catch (Exception e) {
            log.error("주문 ID로 배송 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.DELIVERY_NOT_FOUND);
        }
    }

    public Page<DeliveryDetailResponse> findDeliveryAll(Pageable pageable) {
        try {
            Page<Delivery> deliveryPage = deliveryRepository.findDeliveryAll(pageable);
            return DeliveryDetailResponse.from(deliveryPage);
        } catch (Exception e) {
            log.error("배송 전체 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.DELIVERY_NOT_FOUND);
        }
    }

    @Transactional
    public void deleteDelivery(UUID deliveryId, Long deletedBy) {
        try {
            Delivery delivery = findDelivery(deliveryId);
            delivery.delete(deletedBy);
        } catch (Exception e) {
            log.error("배송 삭제 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.DELIVERY_DELETE_FAILED);
        }
    }

    public void applyRouteCreation(UUID deliveryId, UUID routePlanId,
        CustomerDeliveryStatus status) {
        try {
            Delivery delivery = findDelivery(deliveryId);
            delivery.updateRouteInfo(routePlanId, status);
        } catch (Exception e) {
            log.error("배송 경로 정보 적용 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR,
                "배송 경로 정보 적용에 실패했습니다.", e);
        }
    }

}
