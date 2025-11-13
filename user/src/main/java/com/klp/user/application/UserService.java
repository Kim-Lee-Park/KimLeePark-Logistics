package com.klp.user.application;

import com.klp.common.model.PageResponse;
import com.klp.user.domain.entity.User;
import com.klp.user.domain.repository.UserRepository;
import com.klp.user.presentation.dto.request.UserUpdateRequest;
import com.klp.user.presentation.dto.response.UserDetailResponse;
import com.klp.user.presentation.dto.response.UserInfoResponse;
import com.klp.user.presentation.dto.response.UsernameCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
