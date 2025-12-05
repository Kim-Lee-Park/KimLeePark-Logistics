package com.klp.user.application;

import com.klp.common.exception.BusinessException;
import com.klp.user.application.command.ValidateUserCommand;
import com.klp.user.application.event.UserProfileChangedEvent;
import com.klp.user.domain.entity.User;
import com.klp.user.domain.exception.UserErrorCode;
import com.klp.user.domain.repository.UserRepository;
import com.klp.user.presentation.dto.request.UserCreateRequest;
import com.klp.user.presentation.dto.request.UserUpdateRequest;
import com.klp.user.presentation.dto.response.UserDataResponse;
import com.klp.user.presentation.dto.response.UsernameCheckResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    public UsernameCheckResponse checkUserNameAvailable(String username) {
        boolean exist = userRepository.existsByUsername(username);
        return new UsernameCheckResponse(!exist);
    }

    @Transactional(readOnly = true)
    public Page<User> getUserList(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        } else {
            return userRepository.searchByKeyword(keyword, pageable);
        }
    }

    @Transactional
    public void updateUserInfo(User user, UserUpdateRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        user.update(request.username(), encodedPassword, request.slackId(), request.phone(), request.email(),
            request.role());

        applicationEventPublisher.publishEvent(new UserProfileChangedEvent(userId));
    }

    @Transactional
    public User createPendingUser(UserCreateRequest request, UUID affiliationId) {
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.create(
            affiliationId,
            request.affiliationType(),
            request.username(),
            encodedPassword,
            request.slackId(),
            request.phone(),
            request.email(),
            request.role()
        );

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserDataResponse getUserByUsername(ValidateUserCommand command) {
        User user = userRepository.findByUsername(command.username())
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (!validatePassword(command.password(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.INVALID_PASSWORD);
        }

        return UserDataResponse.from(user);
    }

    @Transactional
    public void approvePendingUser(User user) {
        user.approve();
    }

    @Transactional
    public void rejectPendingUser(User user) {
        user.reject();
    }

    @Transactional(readOnly = true)
    public List<User> findDriversByHubId(UUID hubId) {
        return userRepository.findDriversByHubId(hubId);
    }

    @Transactional(readOnly = true)
    public List<User> findDriversByLogistics() {
        return userRepository.findDriversByLogistics();
    }

    @Transactional(readOnly = true)
    public User findDriverById(Long driverId) {
        return userRepository.findDriverById(driverId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public User findNotDeletedUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        return user;
    }
}
