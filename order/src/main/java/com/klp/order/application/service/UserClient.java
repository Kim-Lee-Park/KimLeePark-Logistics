package com.klp.order.application.service;

import com.klp.order.domain.vo.UserAddressHubId;
import com.klp.order.domain.vo.UserProfile;
import java.util.UUID;

public interface UserClient {

    UserProfile getUserProfileById(Long userId);

    UserAddressHubId getUserAddressHubIdByAddressId(UUID addressId);
}
