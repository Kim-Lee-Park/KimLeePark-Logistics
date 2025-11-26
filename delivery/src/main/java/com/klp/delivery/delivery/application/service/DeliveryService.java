package com.klp.delivery.delivery.application.service;


import com.klp.common.exception.BusinessException;
import com.klp.delivery.common.enums.DeliveryStatus;
import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.application.command.CompanyCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.repository.DeliveryRepository;
import com.klp.delivery.delivery.application.command.DriverCommand;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.delivery.presentation.dto.DeliveryDetailResponse;
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
    private final CompanyApiClient companyApiClient;
    private final DriverApiClient driverApiClient;

    public CompanyCommand findCompany(String customerId) {
        try {
            return companyApiClient.findCompany(customerId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("업체 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR, "업체 조회에 실패했습니다.", e);
        }
    }

    public DriverCommand findArrivalHubDrivers(String customerId) {
        try {
            return driverApiClient.findArrivalHubDrivers(customerId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("배송 담당자 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR, "배송 담당자 조회에 실패했습니다.",
                e);
        }
    }

    public DriverCommand findDriverAtArrivalHub(long vendorDriverId) {
        try {
            return driverApiClient.findDriverAtArrivalHub(vendorDriverId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("배송 담당자 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR, "배송 담당자 조회에 실패했습니다.",
                e);
        }
    }


    public Delivery registerDelivery(DeliveryCommand command, List<OrderItemCommand> items) {
        try {

            return deliveryRepository.save(
                Delivery.create(
                    command.vendorDriverId(),
                    command.orderId(),
                    command.departureId(),
                    command.arrivalId(),
                    command.senderId(),
                    command.receiverId(),
                    command.receiverName(),
                    command.address(),
                    command.receiverSlackId(),
                    items
                )
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("배송 저장 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR, "배송 저장에 실패했습니다.", e);
        }
    }

    public Delivery findDelivery(UUID deliveryId) {
        return deliveryRepository.findByDeliveryId(deliveryId);
    }

    public List<DeliveryDetailResponse> findDeliveriesByOrderId(UUID orderId) {
        List<Delivery> deliveries = deliveryRepository.findDeliveryByOrderId(orderId);

        return DeliveryDetailResponse.from(deliveries);
    }

    public void updateDeliveryStatus(UUID deliveryId, DeliveryStatus status) {
        Delivery delivery = findDelivery(deliveryId);
        delivery.updateStatus(status);
        deliveryRepository.save(delivery);
    }

    public Page<DeliveryDetailResponse> findDeliveryAll(Pageable pageable) {
        Page<Delivery> deliveryPage = deliveryRepository.findDeliveryAll(pageable);
        return DeliveryDetailResponse.from(deliveryPage);
    }

    @Transactional
    public void updateVendorDriver(UUID deliveryId, Long newVendorDriverId) {
        Delivery delivery = findDelivery(deliveryId);
        DriverCommand driver = findDriverAtArrivalHub(newVendorDriverId);
        if (driver == null) {
            throw new BusinessException(
                DeliveryErrorCode.DELIVERY_CANNOT_BE_MODIFIED, "배송 담당자를 찾을 수 없습니다");
        }
        delivery.updateVendorDriverId(newVendorDriverId);

    }
}
