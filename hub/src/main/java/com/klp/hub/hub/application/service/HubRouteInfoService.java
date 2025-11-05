package com.klp.hub.hub.application.service;

import com.klp.hub.hub.application.command.hubRouteInfo.RegisterHubRouteInfoCommand;
import com.klp.hub.hub.application.command.hubRouteInfo.UpdateHubRouteInfoCommand;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.GetHubRouteInfoDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.GetHubRouteInfoListResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.RegisterHubRouteInfoResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.UpdatedHubRouteInfoResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HubRouteInfoService {

    @Transactional
    public RegisterHubRouteInfoResponse registerHubRouteInfo(RegisterHubRouteInfoCommand request){

        return new RegisterHubRouteInfoResponse(null);
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
