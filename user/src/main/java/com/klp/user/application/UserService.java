package com.klp.user.application;

import com.klp.common.exception.BusinessException;
import com.klp.common.model.PageResponse;
import com.klp.user.application.command.ValidateUserCommand;
import com.klp.user.domain.entity.User;
import com.klp.user.domain.enums.AffiliationType;
import com.klp.user.domain.enums.UserRole;
import com.klp.user.domain.exception.ExternalApiException;
import com.klp.user.domain.exception.UserErrorCode;
import com.klp.user.domain.repository.UserRepository;
import com.klp.user.infrastructure.client.CompanyClient;
import com.klp.user.infrastructure.client.PromotionClient;
import com.klp.user.infrastructure.client.dto.request.CreateUserGradeRequest;
import com.klp.user.infrastructure.client.dto.response.CompanyListResponse;
import com.klp.user.infrastructure.client.dto.response.CompanyResponse;
import com.klp.user.presentation.dto.request.UserCreateRequest;
import com.klp.user.presentation.dto.request.UserUpdateRequest;
import com.klp.user.presentation.dto.response.DriverDetailResponse;
import com.klp.user.presentation.dto.response.DriverInfo;
import com.klp.user.presentation.dto.response.HubDriverListResponse;
import com.klp.user.presentation.dto.response.LogisticsDriverListResponse;
import com.klp.user.presentation.dto.response.UserDataResponse;
import com.klp.user.presentation.dto.response.UserDetailResponse;
import com.klp.user.presentation.dto.response.UserInfoResponse;
import com.klp.user.presentation.dto.response.UsernameCheckResponse;
import java.util.List;
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
    private final CompanyClient companyClient;
    private final PromotionClient promotionClient;

    @Transactional(readOnly = true)
    public UsernameCheckResponse checkUserNameAvailable(String username) {
        boolean exist = userRepository.existsByUsername(username);
        return new UsernameCheckResponse(!exist);
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetails(Long userId) {
        User user = findNotDeletedUser(userId);
        String affiliationName = getAffiliationName(user);

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

        return PageResponse.of(userPage, user ->
            UserInfoResponse.of(user, getAffiliationName(user))
        );
    }

    @Transactional
    public void updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = findNotDeletedUser(userId);
        String encodedPassword = passwordEncoder.encode(request.password());

        user.update(request.username(), encodedPassword, request.slackId(), request.phone(), request.email(),
            request.role());
    }

    @Transactional
    public Long createPendingUser(UserCreateRequest request) {
        UUID affiliationId = getAffiliationId(request.affiliationName(), request.affiliationType());

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

        User saved = userRepository.save(user);
        return saved.getUserId();
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
    public void approvePendingUser(Long userId) {
        User user = findNotDeletedUser(userId);

        user.approve();

        if (user.getRole() == UserRole.CUSTOMER) {
            try {
                promotionClient.createUserGrade(new CreateUserGradeRequest(userId));
                log.info("고객 승인 완료(등급 NONE 설정) - userId: {}", userId);
            } catch (ExternalApiException e) {
                log.error("회원 등급 생성 실패 - userId: {}, errorCode: {}", userId, e.getErrorCode().name());
                throw e;
            } catch (Exception e) {
                log.error("회원 등급 생성 중 예상치 못한 오류 발생 - userId: {}", userId, e);
                throw new BusinessException(UserErrorCode.INTERNAL_SERVER_ERROR,
                    "회원 등급 생성 중 예상치 못한 오류가 발생했습니다.");
            }
        } else {
            log.info("사용자 가입 승인 완료 - userId: {}", userId);
        }
    }

    @Transactional
    public void rejectPendingUser(Long userId) {
        User user = findNotDeletedUser(userId);

        user.reject();
    }

    @Transactional(readOnly = true)
    public HubDriverListResponse getDriversByHubId(UUID hubId) {
        List<User> drivers = userRepository.findDriversByHubId(hubId);

        List<DriverInfo> driverInfoList = drivers.stream()
            .map(DriverInfo::from)
            .toList();

        return HubDriverListResponse.of(hubId, driverInfoList);
    }

    @Transactional(readOnly = true)
    public LogisticsDriverListResponse getDriversByLogistics() {
        List<User> drivers = userRepository.findDriversByLogistics();

        List<DriverInfo> driverInfoList = drivers.stream()
            .map(DriverInfo::from)
            .toList();

        return LogisticsDriverListResponse.of(driverInfoList);
    }

    @Transactional(readOnly = true)
    public DriverDetailResponse getDriverById(Long driverId) {
        User driver = userRepository.findDriverById(driverId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        return DriverDetailResponse.from(driver);
    }

    /**
     * 사용자의 소속 이름을 조회합니다. CUSTOMER인 경우 "고객"을 반환하고, 그 외에는 CompanyClient를 통해 업체 이름을 조회합니다.
     */
    private String getAffiliationName(User user) {
        if (user.getAffiliationType() == AffiliationType.CUSTOMER) {
            return "고객";
        }

        if (user.getAffiliationId() == null) {
            log.warn("affiliationId가 null입니다 - userId: {}", user.getUserId());
            throw new BusinessException(UserErrorCode.INVALID_AFFILIATION);
        }

        try {
            CompanyResponse companyResponse = companyClient.getCompanyById(user.getAffiliationId());
            return companyResponse.name();
        } catch (ExternalApiException e) {
            log.error("업체 정보 조회 실패 - affiliationId: {}, errorCode: {}",
                user.getAffiliationId(), e.getErrorCode().name());
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 예외 발생 - affiliationId: {}", user.getAffiliationId(), e);
            throw new BusinessException(UserErrorCode.INTERNAL_SERVER_ERROR,
                "업체 정보 조회 중 예상치 못한 오류가 발생했습니다.");
        }
    }

    /**
     * 업체 이름으로 업체 ID를 조회합니다. CUSTOMER인 경우 null을 반환하고, 그 외에는 CompanyClient를 통해 업체 ID를 조회합니다.
     */
    private UUID getAffiliationId(String affiliationName, AffiliationType affiliationType) {
        if (affiliationType == AffiliationType.CUSTOMER) {
            return null;
        }

        try {
            CompanyListResponse response = companyClient.getCompaniesByName(affiliationName);

            if (response.companies().isEmpty()) {
                log.warn("업체 정보를 찾을 수 없음 - affiliationName: {}", affiliationName);
                throw new BusinessException(UserErrorCode.BAD_REQUEST,
                    "해당 이름의 업체를 찾을 수 없습니다: " + affiliationName);
            }

            // 첫 번째 결과를 사용 (동일한 이름의 업체가 여러 개일 경우 첫 번째 선택) -> 원래라면 사용자가 업체를 선택하는 부분이 존재해야 함
            UUID companyId = response.companies().get(0).companyId();
            log.debug("업체 ID 조회 성공 - affiliationName: {}, companyId: {}", affiliationName, companyId);
            return companyId;

        } catch (BusinessException e) {
            throw e;
        } catch (ExternalApiException e) {
            log.error("업체 정보 조회 실패 - affiliationName: {}, errorCode: {}",
                affiliationName, e.getErrorCode().name());
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 예외 발생 - affiliationName: {}", affiliationName, e);
            throw new BusinessException(UserErrorCode.INTERNAL_SERVER_ERROR,
                "업체 정보 조회 중 예상치 못한 오류가 발생했습니다.");
        }
    }

    private boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private User findNotDeletedUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        return user;
    }
}
