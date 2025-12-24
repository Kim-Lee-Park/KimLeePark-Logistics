package com.klp.order.application.service;

import com.klp.order.domain.vo.UserAddress;
import com.klp.order.domain.vo.UserProfile;
import java.util.UUID;

public interface UserClient {

    UserProfile getUserProfileById(Long userId);

    UserAddress getUserAddressHubIdByAddressId(UUID addressId);
}
