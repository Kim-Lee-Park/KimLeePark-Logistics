package com.klp.order.application.service;

import com.klp.order.domain.vo.UserProfile;

public interface UserClient {

    UserProfile getUserProfileById(Long userId);
}
