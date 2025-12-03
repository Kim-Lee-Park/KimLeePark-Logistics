package com.klp.user.application;

import com.klp.common.exception.BusinessException;
import com.klp.common.model.PageResponse;
import com.klp.user.application.command.UserAddressCreateCommand;
import com.klp.user.domain.entity.User;
import com.klp.user.domain.entity.UserAddress;
import com.klp.user.domain.entity.UserGrade;
import com.klp.user.domain.enums.AffiliationType;
import com.klp.user.domain.enums.UserRole;
import com.klp.user.domain.exception.ExternalApiException;
import com.klp.user.domain.exception.UserErrorCode;
import com.klp.user.infrastructure.client.CompanyClient;
import com.klp.user.infrastructure.client.PromotionClient;
import com.klp.user.infrastructure.client.dto.response.CompanyListResponse;
import com.klp.user.infrastructure.client.dto.response.CompanyResponse;
import com.klp.user.infrastructure.client.dto.response.DefaultGradeResponse;
import com.klp.user.presentation.dto.request.UserCreateRequest;
import com.klp.user.presentation.dto.request.UserUpdateRequest;
import com.klp.user.presentation.dto.response.DriverDetailResponse;
import com.klp.user.presentation.dto.response.DriverInfo;
import com.klp.user.presentation.dto.response.HubDriverListResponse;
import com.klp.user.presentation.dto.response.LogisticsDriverListResponse;
import com.klp.user.presentation.dto.response.UserAddressListResponse;
import com.klp.user.presentation.dto.response.UserAddressResponse;
import com.klp.user.presentation.dto.response.UserDetailResponse;
import com.klp.user.presentation.dto.response.UserGradeResponse;
import com.klp.user.presentation.dto.response.UserInfoResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserService userService;
    private final UserGradeService userGradeService;
    private final UserAddressService userAddressService;
    private final CompanyClient companyClient;
    private final PromotionClient promotionClient;

    /**
     * 회원가입 플로우 - PENDING 상태로 생성
     */
    @Transactional
    public Long createPendingUser(UserCreateRequest request) {
        UUID affiliationId = getAffiliationId(request.affiliationName(), request.affiliationType());
        User user = userService.createPendingUser(request, affiliationId);

        log.info("회원가입 대기 상태 생성 완료 - userId: {}, role: {}", user.getUserId(), user.getRole());

        return user.getUserId();
    }

    /**
     * 회원 승인 플로우 - CUSTOMER인 경우 기본 등급 생성
     */
    @Transactional
    public void approvePendingUser(Long userId) {
        User user = userService.findNotDeletedUser(userId);
        userService.approvePendingUser(user);

        if (user.getRole() == UserRole.CUSTOMER) {
            String gradeName = getDefaultGradeName();
            userGradeService.createUserGrade(user, gradeName);
            log.info("고객 승인 완료 - userId: {}, 등급: {}", userId, gradeName);
        } else {
            log.info("사용자 가입 승인 완료 - userId: {}, role: {}", userId, user.getRole());
        }
    }

    /**
     * 회원 거부 플로우
     */
    @Transactional
    public void rejectPendingUser(Long userId) {
        User user = userService.findNotDeletedUser(userId);
        userService.rejectPendingUser(user);
        log.info("사용자 가입 거부 완료 - userId: {}", userId);
    }

    /**
     * 회원 상세 조회 - Company 호출 + 등급 조회
     */
    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetails(Long userId) {
        User user = userService.findNotDeletedUser(userId);
        String affiliationName = getAffiliationName(user);

        String gradeName = null;
        if (user.getRole() == UserRole.CUSTOMER) {
            gradeName = userGradeService.getCurrentGradeName(userId);
        }

        return UserDetailResponse.of(user, affiliationName, gradeName);
    }

    /**
     * 회원 목록 조회 - Company 호출 + 등급 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<UserInfoResponse> getUserList(String keyword, Pageable pageable) {
        Page<User> userPage = userService.getUserList(keyword, pageable);

        return PageResponse.of(userPage, user -> {
            String affiliationName = getAffiliationName(user);
            String gradeName = null;

            if (user.getRole() == UserRole.CUSTOMER) {
                gradeName = userGradeService.getCurrentGradeName(user.getUserId());
            }

            return UserInfoResponse.of(user, affiliationName, gradeName);
        });
    }

    /**
     * 회원 정보 수정
     */
    @Transactional
    public void updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = userService.findNotDeletedUser(userId);
        userService.updateUserInfo(user, request);
        log.info("회원 정보 수정 완료 - userId: {}", userId);
    }

    /**
     * 드라이버 조회 - 허브별
     */
    @Transactional(readOnly = true)
    public HubDriverListResponse getDriversByHubId(UUID hubId) {
        List<User> drivers = userService.findDriversByHubId(hubId);

        List<DriverInfo> driverInfoList = drivers.stream()
            .map(DriverInfo::from)
            .toList();

        return HubDriverListResponse.of(hubId, driverInfoList);
    }

    /**
     * 드라이버 조회 - 물류 회사
     */
    @Transactional(readOnly = true)
    public LogisticsDriverListResponse getDriversByLogistics() {
        List<User> drivers = userService.findDriversByLogistics();

        List<DriverInfo> driverInfoList = drivers.stream()
            .map(DriverInfo::from)
            .toList();

        return LogisticsDriverListResponse.of(driverInfoList);
    }

    /**
     * 드라이버 상세 조회
     */
    @Transactional(readOnly = true)
    public DriverDetailResponse getDriverById(Long driverId) {
        User driver = userService.findDriverById(driverId);
        return DriverDetailResponse.from(driver);
    }

    /**
     * 회원 등급 조회
     */
    @Transactional(readOnly = true)
    public UserGradeResponse getCurrentUserGrade(Long userId) {
        userService.findNotDeletedUser(userId);
        UserGrade userGrade = userGradeService.getCurrentUserGrade(userId);
        return UserGradeResponse.from(userGrade);
    }

    /**
     * 회원 등급 변경
     */
    @Transactional
    public UserGradeResponse updateUserGrade(Long userId, String gradeName) {
        userService.findNotDeletedUser(userId);
        UserGrade newGrade = userGradeService.updateUserGrade(userId, gradeName);
        log.info("회원 등급 변경 완료 - userId: {}, gradeName: {}", userId, gradeName);
        return UserGradeResponse.from(newGrade);
    }

    /**
     * 기본 등급명 조회 (Promotion 서비스)
     */
    private String getDefaultGradeName() {
        try {
            DefaultGradeResponse response = promotionClient.getDefaultGrade();
            return response.gradeName();
        } catch (ExternalApiException e) {
            log.error("기본 등급 조회 실패", e);
            return "등급 없음";
        }
    }

    /**
     * 소속명 조회 (Company 서비스)
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
     * 소속 ID 조회 (Company 서비스)
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

    /**
     * 회원 주소 생성(요청으로 자신 주소 근처 hubId를 받음)
     */
    @Transactional
    public void createUserAddress(UserAddressCreateCommand command) {
        User user = userService.findNotDeletedUser(command.userId());
        userAddressService.createUserAddress(user, command);
        log.info("회원 주소 생성 완료 - userId: {}", command.userId());
    }

    /**
     * 회원 주소 단건 조회
     */
    @Transactional(readOnly = true)
    public UserAddressResponse getUserAddress(UUID userAddressId) {
        UserAddress userAddress = userAddressService.getUserAddress(userAddressId);
        return UserAddressResponse.from(userAddress);
    }

    /**
     * 회원 주소 목록 조회
     */
    @Transactional(readOnly = true)
    public UserAddressListResponse getUserAddressList(Long userId) {
        List<UserAddress> addresses = userAddressService.getUserAddressList(userId);
        List<UserAddressResponse> addressResponses = addresses.stream()
            .map(UserAddressResponse::from)
            .toList();
        return UserAddressListResponse.of(addressResponses);
    }

    /**
     * 회원 주소 수정
     */
    @Transactional
    public void updateUserAddress(UUID userAddressId, UserAddressCreateCommand command) {
        userAddressService.updateUserAddress(userAddressId, command);
        log.info("회원 주소 수정 완료 - addressId: {}", userAddressId);
    }

    /**
     * 회원 주소 삭제
     */
    @Transactional
    public void deleteUserAddress(UUID userAddressId, Long userId) {
        userAddressService.deleteUserAddress(userAddressId, userId);
        log.info("회원 주소 삭제 완료 - addressId: {}, deletedBy: {}", userAddressId, userId);
    }
}
