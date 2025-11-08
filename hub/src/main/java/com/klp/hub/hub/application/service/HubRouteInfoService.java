package com.klp.hub.hub.application.service;

import com.klp.common.exception.BusinessException;
import com.klp.hub.common.util.DistanceTimeUtil;
import com.klp.hub.hub.application.command.hubRouteInfo.RegisterHubRouteInfoCommand;
import com.klp.hub.hub.application.command.hubRouteInfo.UpdateHubRouteInfoCommand;
import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.model.HubRouteInfo;
import com.klp.hub.hub.domain.repository.HubRouteInfoRepository;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.GetHubRouteInfoDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.GetHubRouteInfoListResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.RegisterHubRouteInfoResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.UpdatedHubRouteInfoResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubRouteInfoService {
    private final HubRouteInfoRepository hubRouteInfoRepository;
    private final HubService hubService;

    //허브간 이동 정보 생성
    @Transactional
    public RegisterHubRouteInfoResponse registerHubRouteInfo(RegisterHubRouteInfoCommand request){
        Hub departureHub=hubService.getHubById(request.departureId());
        Hub arrivalHub=hubService.getHubById(request.arrivalId());

        Long durationMin=DistanceTimeUtil.estimateDurationMinutes(departureHub.getLatitude(),departureHub.getLongitude(),arrivalHub.getLatitude(),arrivalHub.getLongitude());
        Double distanceKm=DistanceTimeUtil.calculateDistanceKm(departureHub.getLatitude(),departureHub.getLongitude(),arrivalHub.getLatitude(),arrivalHub.getLongitude());

        HubRouteInfo info = HubRouteInfo.create(
            departureHub.getHubId(),
            arrivalHub.getHubId(),
            durationMin,
            distanceKm
        );

        return new RegisterHubRouteInfoResponse(hubRouteInfoRepository.save(info).getHubRouteId());
    }

    @Transactional(readOnly = true)
    public GetHubRouteInfoDetailResponse getHubRouteInfoDetail(UUID hubRouteInfoId){
        return null;
    }

    @Transactional(readOnly = true)
    public GetHubRouteInfoListResponse getHubRouteInfos(Pageable pageable){
        return null;
    }

    @Transactional
    public UpdatedHubRouteInfoResponse updateHubRouteInfo(UUID hubRouteInfoId, UpdateHubRouteInfoCommand request){
        return null;
    }

    @Transactional
    public void deleteHubRouteInfo(UUID hubRouteInfoId){
    }
}
