package com.klp.user.infrastructure.repository;

import com.klp.user.domain.entity.UserAddress;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressJpaRepository extends JpaRepository<UserAddress, UUID> {

    List<UserAddress> findByUser_UserId(Long userId);
}
