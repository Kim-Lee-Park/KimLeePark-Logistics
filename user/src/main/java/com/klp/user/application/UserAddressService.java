package com.klp.user.application;

import com.klp.global.exception.BusinessException;
import com.klp.user.application.command.UserAddressCreateCommand;
import com.klp.user.domain.entity.User;
import com.klp.user.domain.entity.UserAddress;
import com.klp.user.domain.exception.UserErrorCode;
import com.klp.user.domain.repository.UserAddressRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;

    @Transactional
    public void createUserAddress(User user, UserAddressCreateCommand command) {
        UserAddress userAddress = UserAddress.create(
            user,
            command.hubId(),
            command.address(),
            command.detail(),
            command.isDefault(),
            command.latitude(),
            command.longitude()
        );

        userAddressRepository.save(userAddress);
    }

    @Transactional(readOnly = true)
    public UserAddress getUserAddress(UUID userAddressId) {
        return userAddressRepository.findById(userAddressId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_ADDRESS_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<UserAddress> getUserAddressList(Long userId) {
        return userAddressRepository.findByUserId(userId);
    }

    @Transactional
    public void updateUserAddress(UUID userAddressId, UserAddressCreateCommand command) {
        UserAddress userAddress = userAddressRepository.findById(userAddressId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_ADDRESS_NOT_FOUND));

        userAddress.update(
            command.hubId(),
            command.address(),
            command.detail(),
            command.isDefault(),
            command.latitude(),
            command.longitude()
        );

        userAddressRepository.save(userAddress);
    }

    @Transactional
    public void deleteUserAddress(UUID userAddressId, Long deletedBy) {
        UserAddress userAddress = userAddressRepository.findById(userAddressId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_ADDRESS_NOT_FOUND));
        userAddress.delete(deletedBy);
        userAddressRepository.save(userAddress);
    }
}
