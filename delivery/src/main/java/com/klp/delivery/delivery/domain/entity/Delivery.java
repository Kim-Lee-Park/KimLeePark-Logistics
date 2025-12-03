package com.klp.delivery.delivery.domain.entity;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.common.entity.BaseEntity;
import com.klp.delivery.common.enums.CustomerDeliveryStatus;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.util.StringUtils;

@Entity
@Getter
@Table(name = "p_deliveries", schema = "delivery_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id
    @Column(name = "delivery_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deliveryId;

    @Comment("허브 경로 계획 ID")
    @Column(name = "route_plan_id")
    private UUID routePlanId;

    @Comment("주문 ID")
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Comment("고객 배송담당자 ID")
    @Column(name = "user_drvier_id", nullable = false)
    private Long userDrvierId;

    @Comment("고객 배송담당자 슬랙 ID")
    @Column(name = "user_driver_slack_id", nullable = false)
    private String userDriverSlackId;

    @Comment("출발 허브 ID")
    @Column(name = "departure_id", nullable = false)
    private UUID departureId;

    @Comment("출발 허브 이름")
    @Column(name = "departure_name", nullable = false)
    private String departureName;

    @Comment("도착 허브 ID")
    @Column(name = "arrival_id", nullable = false)
    private UUID arrivalId;

    @Comment("도착 허브 이름")
    @Column(name = "arrival_name", nullable = false)
    private String arrivalName;

    @Comment("구매자 이름")
    @Column(name = "user_name", nullable = false)
    private String userName;

    @Comment("배송지 주소")
    @Column(name = "user_address", nullable = false)
    private String userAddress;

    @Comment("배송상태")
    @Enumerated(EnumType.STRING)
    private CustomerDeliveryStatus status;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL)
    List<DeliveryItem> deliveryItems = new ArrayList<>();

    public Delivery(UUID orderId, Long userDrvierId, String userDriverSlackId, UUID departureId,
        String departureName, UUID arrivalId, String arrivalName, String userName,
        String userAddress,
        CustomerDeliveryStatus status, List<OrderItemCommand> orderItem) {
        this.orderId = orderId;
        this.userDrvierId = userDrvierId;
        this.userDriverSlackId = userDriverSlackId;
        this.departureId = departureId;
        this.departureName = departureName;
        this.arrivalId = arrivalId;
        this.arrivalName = arrivalName;
        this.userName = userName;
        this.userAddress = userAddress;
        this.status = status;
        for (OrderItemCommand deliveryItem : orderItem) {
            DeliveryItem item = DeliveryItem.create(this, deliveryItem.orderItemId());
            addDeliveryItem(item);
        }
    }

    public static Delivery create(UUID orderId, Long userDrvierId, String userDriverSlackId, UUID departureId,
        String departureName, UUID arrivalId, String arrivalName, String userName,
        String userAddress, List<OrderItemCommand> items) {
        validateDeliveryData(userDrvierId, userName, userAddress, userDriverSlackId);
        return new Delivery(orderId, userDrvierId, userDriverSlackId, departureId, departureName,
            arrivalId, arrivalName, userName, userAddress, CustomerDeliveryStatus.CREATED, items);

    }

    private static void validateDeliveryData(Long userDriverId, String userName,
        String address, String userDriverSlackId) {
        if (userDriverId == null) {
            throw new BusinessException(DeliveryErrorCode.INVALID_DELIVERY_DATA,
                "고객 배송 담당자는 필수입니다.");
        }
        if (!StringUtils.hasText(address)) {
            throw new BusinessException(DeliveryErrorCode.INVALID_DELIVERY_DATA,
                "고객 배송지 주소는 필수입니다.");
        }
        if (!StringUtils.hasText(userName)) {
            throw new BusinessException(DeliveryErrorCode.INVALID_DELIVERY_DATA, "고객 이름은 필수입니다.");
        }
        if (!StringUtils.hasText(userDriverSlackId)) {
            throw new BusinessException(DeliveryErrorCode.INVALID_DELIVERY_DATA,
                "고객 배송담당자 슬랙 ID는 필수입니다.");
        }
    }

    public void updateStatus(CustomerDeliveryStatus status) {
        this.status = status;
    }

    // 연관관계 설정
    public void addDeliveryItem(DeliveryItem item) {
        deliveryItems.add(item);
    }


    public boolean cancelUpdateUserDriver(CustomerDeliveryStatus status) {
        return switch (status) {
            case CREATED -> true;
            case SHIPPING, ARRIVED -> false;
        };
    }

    public void updateUserDriverId(Long newUserDriverId) {
        if (!cancelUpdateUserDriver(this.status)) {
            throw new BusinessException(DeliveryErrorCode.DELIVERY_CANNOT_BE_MODIFIED,
                String.format("배송 담당자 변경 불가 상태: %s", this.status)
            );
        }
        this.userDrvierId = newUserDriverId;
    }

    public void delete(Long deletedBy) {

        if (!cancelUpdateUserDriver(this.status)) {
            throw new BusinessException(DeliveryErrorCode.DELIVERY_CANNOT_BE_MODIFIED,
                "배송 삭제 불가 상태" + this.status);
        }
        super.delete(deletedBy);
    }

    public void updateRouteInfo(UUID routePlanId, CustomerDeliveryStatus status) {
        this.routePlanId = routePlanId;
        this.status = status;
    }

}
