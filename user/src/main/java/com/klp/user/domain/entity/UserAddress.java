package com.klp.user.domain.entity;

import com.klp.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "p_user_address", schema = "user_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_address_id")
    @Comment("회원주소 ID")
    private UUID userAddressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("유저ID")
    private User user;

    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "detail")
    private String detail;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    public static UserAddress create(
        User user,
        UUID hubId,
        String address,
        String detail,
        boolean isDefault,
        Double latitude,
        Double longitude
    ) {
        UserAddress userAddress = new UserAddress();
        userAddress.user = user;
        userAddress.hubId = hubId;
        userAddress.address = address;
        userAddress.detail = detail;
        userAddress.isDefault = isDefault;
        userAddress.latitude = latitude;
        userAddress.longitude = longitude;

        return userAddress;
    }

    public void update(
        UUID hubId,
        String address,
        String detail,
        boolean isDefault,
        Double latitude,
        Double longitude
    ) {
        this.hubId = hubId;
        this.address = address;
        this.detail = detail;
        this.isDefault = isDefault;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
