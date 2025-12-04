package com.klp.hub.hub.application.service;

import com.klp.hub.common.model.UserDetailsImpl;
import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.hub.application.command.hubRouteInfo.RegisterHubRouteInfoCommand;
import com.klp.hub.hub.application.command.hubRouteInfo.UpdateHubRouteInfoCommand;
import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.model.HubRouteInfo;
import com.klp.hub.hub.domain.repository.HubRouteInfoRepository;
import com.klp.hub.hub.exception.HubRouteInfoErrorCode;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.GetHubRouteInfoDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.GetHubRouteInfoListResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.RegisterHubRouteInfoResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.UpdatedHubRouteInfoResponse;
import com.klp.hub.hub.util.DistanceTimeUtil;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubRouteInfoService {

    private final HubRouteInfoRepository hubRouteInfoRepository;
    private final HubService hubService;

    //허브간 이동 정보 생성
    @Transactional
    public RegisterHubRouteInfoResponse registerHubRouteInfo(RegisterHubRouteInfoCommand request) {
        Hub departureHub = hubService.getHubById(request.departureId());
        Hub arrivalHub = hubService.getHubById(request.arrivalId());

        if (hubRouteInfoRepository.existsByDepartureIdAndArrivalId(request.departureId(),
            request.arrivalId())) {
            log.warn("ALREADY_EXISTS_HUB_ROUTE_INFO Error departureId: {}, arrivalId: {}",
                request.departureId(), request.arrivalId());
            throw new BusinessException(HubRouteInfoErrorCode.ALREADY_EXISTS_HUB_ROUTE_INFO);
        }

        Long durationMin = DistanceTimeUtil.estimateDurationMinutes(departureHub.getLatitude(),
            departureHub.getLongitude(), arrivalHub.getLatitude(), arrivalHub.getLongitude());
        Double distanceKm = DistanceTimeUtil.calculateDistanceKm(departureHub.getLatitude(),
            departureHub.getLongitude(), arrivalHub.getLatitude(), arrivalHub.getLongitude());

        HubRouteInfo info = HubRouteInfo.create(
            departureHub.getHubId(),
            arrivalHub.getHubId(),
            durationMin,
            distanceKm
        );

        return new RegisterHubRouteInfoResponse(hubRouteInfoRepository.save(info).getHubRouteId());
    }

    //허브간 이동 정보 단일 조회
    @Transactional(readOnly = true)
    public GetHubRouteInfoDetailResponse getHubRouteInfoDetail(UUID hubRouteInfoId) {
        HubRouteInfo routeInfo = getHubRouteInfoById(hubRouteInfoId);
        return GetHubRouteInfoDetailResponse.from(routeInfo);
    }

    //허브간 이동 정보 목록 조회
    @Transactional(readOnly = true)
    public GetHubRouteInfoListResponse getHubRouteInfos(UUID departureId, UUID arrivalId,
        Pageable pageable) {
        Page<HubRouteInfo> routeInfos = hubRouteInfoRepository.getHubRoutes(departureId, arrivalId,
            pageable);
        return GetHubRouteInfoListResponse.from(routeInfos);
    }

    //허브간 이동 정보 수정
    @Transactional
    public UpdatedHubRouteInfoResponse updateHubRouteInfo(UUID hubRouteInfoId,
        UpdateHubRouteInfoCommand request) {
        HubRouteInfo hubRouteInfo = getHubRouteInfoById(hubRouteInfoId);
        hubRouteInfo.update(request.durationMin(), request.distanceKm());
        return UpdatedHubRouteInfoResponse.from(hubRouteInfo);
    }

    //허브간 이동 정보 삭제
    @Transactional
    public void deleteHubRouteInfo(UUID hubRouteInfoId, UserDetailsImpl userDetails) {
        HubRouteInfo routeInfo = getHubRouteInfoById(hubRouteInfoId);
        routeInfo.delete(userDetails.getUserId());
    }

    //hubId와 관련된 허브간 이동 정보 삭제
    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteHubRouteInfoByHubId(UUID hubId, UserDetailsImpl userDetails) {
        List<HubRouteInfo> infos = hubRouteInfoRepository.findAllByHubId(hubId);
        infos.forEach(info -> {
            info.delete(userDetails.getUserId());
        });
    }

    //허브간 이동 정보 ID로 조회
    @Transactional(readOnly = true)
    public HubRouteInfo getHubRouteInfoById(UUID hubRouteInfoId) {
        return hubRouteInfoRepository.getHubRouteInfoById(hubRouteInfoId)
            .orElseThrow(() -> {
                log.warn("HUB ROUTE INFO NOT_EXISTS hubRouteInfoId: {}", hubRouteInfoId);
                return new BusinessException(HubRouteInfoErrorCode.NOT_EXISTS);
            });
    }

    //모든 허브간 이동 정보 조회
    @Transactional(readOnly = true)
    public GetHubRouteInfoListResponse getAllHubRouteInfos() {
        List<HubRouteInfo> routeInfos = hubRouteInfoRepository.getAllHubRouteInfos();
        log.debug("모든 허브 이동 정보 조회 리스트 사이즈: {}", routeInfos.size());
        return GetHubRouteInfoListResponse.from(routeInfos);
    }
}
