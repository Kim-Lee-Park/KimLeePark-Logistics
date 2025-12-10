package com.klp.order.order.application.service;

import com.klp.order.order.domain.vo.UserAddressHubId;
import com.klp.order.order.domain.vo.UserProfile;
import java.util.UUID;

public interface UserClient {

    UserProfile getUserProfileById(Long userId);

    UserAddressHubId getUserAddressHubIdByAddressId(UUID addressId);
}
