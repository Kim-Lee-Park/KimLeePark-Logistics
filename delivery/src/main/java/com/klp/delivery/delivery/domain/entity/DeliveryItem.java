package com.klp.delivery.delivery.domain.entity;

import com.klp.delivery.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "p_delivery_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryItem extends BaseEntity {

    @Id
    @Column(name = "delivery_item_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deliveryItemId;

    @Comment("배송 ID")
    @ManyToOne(optional = false)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;


    @Comment("주문아이템 ID")
    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;


    private DeliveryItem(Delivery delivery, UUID orderItemId) {
        this.delivery = delivery;
        this.orderItemId = orderItemId;
    }


    public static DeliveryItem create(Delivery delivery, UUID orderItemId) {
        return new DeliveryItem(delivery, orderItemId);
    }
}
