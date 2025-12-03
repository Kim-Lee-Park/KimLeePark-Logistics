package com.klp.user.domain.repository;

import com.klp.user.domain.entity.UserAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository {

    UserAddress save(UserAddress userAddress);

    Optional<UserAddress> findById(UUID userAddressId);

    List<UserAddress> findByUserId(Long userId);
}
