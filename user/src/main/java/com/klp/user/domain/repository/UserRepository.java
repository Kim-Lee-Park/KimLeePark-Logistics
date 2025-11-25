package com.klp.user.domain.repository;

import com.klp.user.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {

    boolean existsByUsername(String username);

    User findById(Long userId);

    Page<User> findAll(Pageable pageable);

    Page<User> searchByKeyword(String keyword, Pageable pageable);

    User save(User user);

    User findByUsername(String username);
}
