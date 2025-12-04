package com.klp.hub.hub.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.hub.application.command.hubRouteInfo.RegisterHubRouteInfoCommand;
import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.model.HubRouteInfo;
import com.klp.hub.hub.domain.repository.HubRouteInfoRepository;
import com.klp.hub.hub.exception.HubErrorCode;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.RegisterHubRouteInfoResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HubRouteInfoServiceTest {

    @Mock
    HubRouteInfoRepository hubRouteInfoRepository;
    @Mock
    HubService hubService;

    @InjectMocks
    HubRouteInfoService hubRouteInfoService;

    @Test
    @DisplayName("허브간 이동 정보 생성 실패: 출발 허브 존재 X")
    void registerHubRouteInfoFailDepartureHubNotExists() {
        //given
        UUID departureId = UUID.randomUUID();
        UUID arrivalId = UUID.randomUUID();
        RegisterHubRouteInfoCommand command = new RegisterHubRouteInfoCommand(departureId,
            arrivalId);
        Hub departureHub = Hub.create("test1", 11., 12., "test1Address");
        Hub arrivalHub = Hub.create("test2", 13., 14., "test2Address");
        given(hubService.getHubById(departureId)).willThrow(
            new BusinessException(HubErrorCode.NOT_EXISTS));

        //when

        //then
        assertThatThrownBy(() -> hubRouteInfoService.registerHubRouteInfo(command))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", HubErrorCode.NOT_EXISTS);
    }

    @Test
    @DisplayName("허브간 이동 정보 생성 실패: 도착 허브 존재 X")
    void registerHubRouteInfoFailArrivalHubNotExists() {
        //given
        UUID departureId = UUID.randomUUID();
        UUID arrivalId = UUID.randomUUID();
        RegisterHubRouteInfoCommand command = new RegisterHubRouteInfoCommand(departureId,
            arrivalId);
        Hub departureHub = Hub.create("test1", 11., 12., "test1Address");
        given(hubService.getHubById(departureId)).willReturn(departureHub);
        given(hubService.getHubById(arrivalId)).willThrow(
            new BusinessException(HubErrorCode.NOT_EXISTS));
        //when

        //then
        assertThatThrownBy(() -> hubRouteInfoService.registerHubRouteInfo(command))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", HubErrorCode.NOT_EXISTS);
    }

    @Test
    @DisplayName("허브간 이동 정보 생성 성공")
    void registerHubRouteInfoSuccess() {
        //given
        UUID departureId = UUID.randomUUID();
        UUID arrivalId = UUID.randomUUID();
        RegisterHubRouteInfoCommand command = new RegisterHubRouteInfoCommand(departureId,
            arrivalId);
        Hub departureHub = Hub.create("test1", 11., 12., "test1Address");
        Hub arrivalHub = Hub.create("test2", 13., 14., "test2Address");
        given(hubService.getHubById(departureId)).willReturn(departureHub);
        given(hubService.getHubById(arrivalId)).willReturn(arrivalHub);

        HubRouteInfo saved = HubRouteInfo.create(
            departureHub.getHubId(), arrivalHub.getHubId(), 11L, 11.11
        );
        given(hubRouteInfoRepository.save(any(HubRouteInfo.class))).willReturn(saved);

        //when
        RegisterHubRouteInfoResponse response = hubRouteInfoService.registerHubRouteInfo(command);

        //then
        assertThat(response).isNotNull();
    }
}
