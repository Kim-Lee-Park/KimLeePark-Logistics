package com.klp.hub.hub.application.service;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.klp.hub.hub.application.command.hub.RegisterHubCommand;
import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.model.HubStatus;
import com.klp.hub.hub.domain.repository.HubRepository;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hub.RegisterHubResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class HubServiceTest {

    @Mock
    private HubRepository hubRepository;

    @InjectMocks
    private HubService hubService;

    @Test
    @DisplayName("허브 이름 중복으로 실패")
    void registerHubFailDuplicateName(){
        //given
        RegisterHubCommand command = new RegisterHubCommand(
            "testHub",
            11L,
            12L,
            "서울특별시"
        );
        when(hubRepository.existsByName("testHub")).thenReturn(true);

        //when

        //then
        assertThatThrownBy(() -> hubService.registerHub(command))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("허브 주소 중복으로 실패")
    void registerHubFailDuplicateAddress(){
        //given
        RegisterHubCommand command = new RegisterHubCommand(
            "testHub",
            11L,
            12L,
            "서울특별시"
        );
        when(hubRepository.existsByAddress("Address")).thenReturn(true);

        //when


        //then
        assertThatThrownBy(() -> hubService.registerHub(command))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("허브 등록 성공")
    void registerHubSuccess(){
        //given
        RegisterHubCommand command = new RegisterHubCommand(
            "testHub",
            11L,
            12L,
            "서울특별시"
        );
        Hub saved= mock(Hub.class);
        when(hubRepository.existsByName("testHub")).thenReturn(false);
        when(hubRepository.existsByAddress("서울특별시")).thenReturn(false);
        given(hubRepository.save(any(Hub.class))).willReturn(saved);

        //when
        RegisterHubResponse response=hubService.registerHub(command);

        //then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("허브 단일 조회 성공")
    void getHubDetailSuccess(){
        //given
        UUID hubId = UUID.randomUUID();
        RegisterHubCommand command = new RegisterHubCommand(
            "testHub",
            11L,
            12L,
            "서울특별시"
        );
        Hub hub = Hub.create(command);
        ReflectionTestUtils.setField(hub, "hubId", hubId);

        when(hubRepository.getHubById(hubId)).thenReturn(Optional.of(hub));

        //when
        GetHubDetailResponse response=hubService.getHubDetail(hubId);

        //then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("testHub");
        assertThat(response.address()).contains("서울특별시");
    }

    @Test
    @DisplayName("허브 단일 조회 실패: 존재하지 않는 허브")
    void getHubDetailFail(){
        //given
        UUID hubId = UUID.randomUUID();
        when(hubRepository.getHubById(hubId)).thenReturn(Optional.empty());

        //when

        //then
        assertThatThrownBy(() -> hubService.getHubDetail(hubId))
            .isInstanceOf(RuntimeException.class);
    }
}
