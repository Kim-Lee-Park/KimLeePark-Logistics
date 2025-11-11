package com.klp.user.application;

import com.klp.common.model.PageResponse;
import com.klp.user.presentation.dto.request.UserChangeRequest;
import com.klp.user.presentation.dto.request.UserUpdateRequest;
import com.klp.user.presentation.dto.response.UserChangeResponse;
import com.klp.user.presentation.dto.response.UserDetailResponse;
import com.klp.user.presentation.dto.response.UserInfoResponse;
import com.klp.user.presentation.dto.response.UsernameCheckResponse;
import java.awt.print.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;

    public UsernameCheckResponse checkUserNameAvailable(String userName) {
        return null;
    }

    public UserDetailResponse getMyDetails(Long userId) {
        return null;
    }

    public PageResponse<UserChangeResponse> getChangeRequests(Pageable pageable) {
        return null;
    }

    public void infoChangeRequest(UserChangeRequest request) {

    }

    public PageResponse<UserInfoResponse> getUserList(Pageable pageable) {
        return null;
    }

    public void updateUserInfo(Long userId, UserUpdateRequest request) {

    }
}
