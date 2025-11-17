package com.klp.delivery.delivery.domain.entity;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.common.BaseEntity;
import com.klp.delivery.common.DeliveryStatus;
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

    @Comment("업체 배송담당자 ID")
    @Column(name = "vendor_drvier_id", nullable = false)
    private Long vendorDrvierId;

    @Comment("주문 ID")
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Comment("출발 허브 ID")
    @Column(name = "departure_id", nullable = false)
    private UUID departureId;

    @Comment("도착 허브 ID")
    @Column(name = "arrival_id", nullable = false)
    private UUID arrivalId;

    @Comment("발송업체 ID")
    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Comment("수령업체 ID")
    @Column(name = "receiver_id", nullable = false)
    private UUID receiverId;

    @Comment("수령업체명")
    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Comment("배송지 주소")
    @Column(name = "address", nullable = false)
    private String address;

    @Comment("수령업체 슬랙 ID")
    @Column(name = "receiver_slack_id", nullable = false)
    private String receiverSlackId;

    @Comment("배송경로 ID")
    @Column(name = "routes_id")
    private UUID routesId;

    @Comment("배송상태")
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL)
    List<DeliveryItem> deliveryItems = new ArrayList<>();

    public Delivery(Long vendorDrvierId, UUID orderId, UUID departureId, UUID arrivalId,
        UUID senderId, UUID receiverId, String receiverName, String address, String receiverSlackId,
        DeliveryStatus status, List<OrderItemCommand> orderItem) {
        this.vendorDrvierId = vendorDrvierId;
        this.orderId = orderId;
        this.departureId = departureId;
        this.arrivalId = arrivalId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.address = address;
        this.receiverSlackId = receiverSlackId;
        this.status = status;
        for(OrderItemCommand deliveryItem : orderItem){
            DeliveryItem item = DeliveryItem.create(this, deliveryItem.orderItemId());
            addDeliveryItem(item);
        }
    }

    public static Delivery create(Long vendorDriverId, UUID orderId, UUID departureId,
        UUID arrivalId, UUID senderId, UUID receiverId, String receiverName, String address,
        String receiverSlackId, List<OrderItemCommand> items) {
        validateDeliveryData(vendorDriverId, receiverName, address, receiverSlackId);
        return new Delivery(vendorDriverId, orderId, departureId, arrivalId, senderId, receiverId,
            receiverName, address, receiverSlackId, DeliveryStatus.CREATED, items);

    }

    private static void validateDeliveryData(Long vendorDriverId, String receiverName,
        String address, String receiverSlackId) {
        if (vendorDriverId == null) {
            throw new BusinessException(DeliveryErrorCode.INVALID_DELIVERY_DATA, "배송 담당자는 필수입니다.");
        }
        if (!StringUtils.hasText(address)) {
            throw new BusinessException(DeliveryErrorCode.INVALID_DELIVERY_DATA, "배송지 주소는 필수입니다.");
        }
        if (!StringUtils.hasText(receiverName)) {
            throw new BusinessException(DeliveryErrorCode.INVALID_DELIVERY_DATA, "수령인 이름은 필수입니다.");
        }
        if (!StringUtils.hasText(receiverSlackId)) {
            throw new BusinessException(DeliveryErrorCode.INVALID_DELIVERY_DATA,
                "수령인 슬랙 ID는 필수입니다.");
        }
    }

    public void updateStatus(DeliveryStatus status) {
        this.status = status;
    }

    // 연관관계 설정
    public void addDeliveryItem(DeliveryItem item) {
        deliveryItems.add(item);
        item.assignTo(this);
    }

}
