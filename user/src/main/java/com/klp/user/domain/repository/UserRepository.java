package com.klp.user.domain.repository;

import com.klp.user.domain.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {

    boolean existsByUsername(String username);

    Optional<User> findById(Long userId);

    Page<User> findAll(Pageable pageable);

    Page<User> searchByKeyword(String keyword, Pageable pageable);

    User save(User user);

    Optional<User> findByUsername(String username);

    List<User> findDriversByHubId(UUID hubId);

    List<User> findDriversByLogistics();

    Optional<User> findDriverById(Long driverId);
}
