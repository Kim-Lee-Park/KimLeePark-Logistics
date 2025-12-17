package com.klp.order.application.query;

import com.klp.global.exception.BusinessException;
import com.klp.global.exception.UserRefErrorCode;
import com.klp.order.application.cache.UserAddressCache;
import com.klp.order.application.cache.UserProfileCache;
import com.klp.order.domain.vo.UserAddress;
import com.klp.order.domain.vo.UserProfile;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserProfileCache userProfileCache;
    private final UserAddressCache userAddressCache;

    public UserProfile getUserProfile(Long userId) {
        UserProfile userProfile = userProfileCache.getUserProfile(userId);
        if (userProfile == null) {
            throw new BusinessException(UserRefErrorCode.USER_NOT_FOUND);
        }
        return userProfile;
    }

    public UserAddress getUserAddress(UUID addressId) {
        UserAddress userAddress = userAddressCache.getUserAddress(addressId);
        if (userAddress == null) {
            throw new BusinessException(UserRefErrorCode.USER_ADDRESS_NOT_FOUND);
        }
        return userAddress;
    }
}
