package com.klp.user.application;

import com.klp.common.exception.BusinessException;
import com.klp.common.model.PageResponse;
import com.klp.user.application.command.ValidateUserCommand;
import com.klp.user.domain.entity.User;
import com.klp.user.domain.exception.UserErrorCode;
import com.klp.user.domain.repository.UserRepository;
import com.klp.user.presentation.dto.request.UserCreateRequest;
import com.klp.user.presentation.dto.request.UserUpdateRequest;
import com.klp.user.presentation.dto.response.UserDataResponse;
import com.klp.user.presentation.dto.response.UserDetailResponse;
import com.klp.user.presentation.dto.response.UserInfoResponse;
import com.klp.user.presentation.dto.response.UsernameCheckResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional(readOnly = true)
    public UsernameCheckResponse checkUserNameAvailable(String username) {
        boolean exist = userRepository.existsByUsername(username);
        return new UsernameCheckResponse(!exist);
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getMyDetails(Long userId) {
        // TODO 업체Id를 통해 업체 이름을 받아오는 요청 필요
        String affiliationName = "tempAffiliation";

        User user = userRepository.findById(userId);
        return UserDetailResponse.of(affiliationName, user);
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetails(Long userId) {
        // TODO 업체Id를 통해 업체 이름을 받아오는 요청 필요
        String affiliationName = "tempAffiliation";
        User user = userRepository.findById(userId);

        return UserDetailResponse.of(affiliationName, user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserInfoResponse> getUserList(String keyword, Pageable pageable) {
        Page<User> userPage;

        if (keyword == null || keyword.trim().isEmpty()) {
            userPage = userRepository.findAll(pageable);
        } else {
            userPage = userRepository.searchByKeyword(keyword, pageable);
        }

        // TODO 업체Id를 통해 업체 이름을 받아오는 요청 필요
        return PageResponse.of(userPage, user ->
            UserInfoResponse.of(user, "tempAffiliation")
        );
    }

    @Transactional
    public void updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId);
        String encodedPassword = passwordEncoder.encode(request.password());

        user.update(request.username(), encodedPassword, request.slackId(), request.phone(), request.role());
    }

    @Transactional
    public Long createPendingUser(UserCreateRequest request) {
        // TODO 업체 이름을 통해 업체 ID를 받아오는 요청 필요
        UUID affiliationId = UUID.randomUUID();

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.create(
            affiliationId,
            request.affiliationType(),
            request.username(),
            encodedPassword,
            request.slackId(),
            request.phone(),
            request.role()
        );

        User saved = userRepository.save(user);
        return saved.getUserId();
    }

    @Transactional(readOnly = true)
    public UserDataResponse getUserByUsername(ValidateUserCommand command) {
        User user = userRepository.findByUsername(command.username());

        if (!validatePassword(command.password(), user.getPassword())) {
            throw new BusinessException(UserErrorCode.INVALID_PASSWORD);
        }

        return UserDataResponse.from(user);
    }

    @Transactional
    public void approvePendingUser(Long userId) {
        User user = userRepository.findById(userId);

        user.approve();
    }

    @Transactional
    public void rejectPendingUser(Long userId) {
        User user = userRepository.findById(userId);

        user.reject();
    }

    private boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
