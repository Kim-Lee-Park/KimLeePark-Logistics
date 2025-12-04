package com.klp.user.infrastructure.repository;

import com.klp.user.domain.entity.UserAddress;
import com.klp.user.domain.repository.UserAddressRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAddressRepositoryImpl implements UserAddressRepository {

    private final UserAddressJpaRepository userAddressJpaRepository;

    @Override
    public UserAddress save(UserAddress userAddress) {
        return userAddressJpaRepository.save(userAddress);
    }

    @Override
    public Optional<UserAddress> findById(UUID userAddressId) {
        return userAddressJpaRepository.findById(userAddressId);
    }

    @Override
    public List<UserAddress> findByUserId(Long userId) {
        return userAddressJpaRepository.findByUser_UserId(userId);
    }
}
