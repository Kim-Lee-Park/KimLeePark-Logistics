package com.klp.order.application.query;

import com.klp.order.application.cache.UserProfileCache;
import com.klp.order.domain.vo.UserProfile;
import com.klp.order.global.exception.BusinessException;
import com.klp.order.global.exception.UserRefErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserProfileCache userProfileCache;

    public UserProfile getUserProfile(Long userId) {
        UserProfile userProfile = userProfileCache.getUserProfile(userId);
        if (userProfile == null) {
            throw new BusinessException(UserRefErrorCode.USER_NOT_FOUND);
        }
        return userProfile;
    }
}
