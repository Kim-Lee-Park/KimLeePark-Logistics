package com.klp.hub.hub.application.service;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.hub.hub.application.command.hub.RegisterHubCommand;
import com.klp.hub.hub.application.command.hub.UpdateHubCommand;
import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.domain.model.HubStatus;
import com.klp.hub.hub.domain.repository.HubRepository;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubListResponse;
import com.klp.hub.hub.presentation.dto.response.hub.RegisterHubResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Test
    @DisplayName("허브 목록 조회 성공")
    void getHubsSuccess(){
        //given
        Pageable pageable = PageRequest.of(0, 10);
        Hub hub1 = Hub.create(new RegisterHubCommand("제주허브", 334455L, 126123L, "제주시 노형동"));
        Hub hub2 = Hub.create(new RegisterHubCommand("서귀포허브", 444555L, 127333L, "서귀포시 중문동"));
        List<Hub> hubList = List.of(hub1, hub2);
        Page<Hub> hubs = new PageImpl<>(hubList, pageable, hubList.size());

        when(hubRepository.getHubs(pageable)).thenReturn(hubs);

        //when
        GetHubListResponse response= hubService.getHubs(pageable);

        //then
        assertThat(response).isNotNull();
        assertThat(response.pageable().page()).isEqualTo(0);
        assertThat(response.pageable().size()).isEqualTo(10);
        assertThat(response.hubs()).hasSize(2);
        assertThat(response.hubs().get(0).name()).isEqualTo("제주허브");
        assertThat(response.hubs().get(1).name()).isEqualTo("서귀포허브");
    }

    @Test
    @DisplayName("허브 수정 실패: 존재하지 않는 허브")
    void updateHubFailNotFound(){
        //given
        UUID hubId = UUID.randomUUID();
        UpdateHubCommand command = new UpdateHubCommand(
            "testHub",
            11L,
            12L,
            "서울특별시"
        );
        when(hubRepository.getHubById(hubId)).thenReturn(Optional.empty());

        //then
        assertThatThrownBy(()->hubService.updateHub(hubId,command))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("허브 수정 실패: 중복된 이름")
    void updateHubFailDuplicateName(){
        //given
        UUID hubId = UUID.randomUUID();
        RegisterHubCommand registerHubCommand = new RegisterHubCommand("oldHub", 11L, 12L, "서울");

        Hub original = Hub.create(registerHubCommand);
        ReflectionTestUtils.setField(original, "hubId", hubId);

        when(hubRepository.getHubById(hubId)).thenReturn(Optional.of(original));

        UpdateHubCommand command = new UpdateHubCommand(
            "testHub",
            11L,
            12L,
            "서울특별시"
        );
        when(hubRepository.existsByName(command.name())).thenReturn(true);

        //when

        //then
        assertThatThrownBy(() -> hubService.updateHub(hubId, command))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("허브 수정 실패: 중복된 주소")
    void updateHubFailDuplicateAddress(){
        //given
        UUID hubId = UUID.randomUUID();
        RegisterHubCommand registerHubCommand = new RegisterHubCommand("oldHub", 11L, 12L, "서울");

        Hub original = Hub.create(registerHubCommand);
        ReflectionTestUtils.setField(original, "hubId", hubId);

        when(hubRepository.getHubById(hubId)).thenReturn(Optional.of(original));

        UpdateHubCommand command = new UpdateHubCommand(
            "testHub",
            11L,
            12L,
            "서울특별시"
        );
        when(hubRepository.existsByName(command.name())).thenReturn(false);
        when(hubRepository.existsByAddress(command.address())).thenReturn(true);

        //when

        //then
        assertThatThrownBy(() -> hubService.updateHub(hubId, command))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("허브 수정 성공")
    void updateHubSuccess(){
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

        UpdateHubCommand updateHubCommand = new UpdateHubCommand(
            "testHubUpdated",
            15L,
            151L,
            "서울특별시 updated"
        );

        //when
        hubService.updateHub(hubId, updateHubCommand);

        // then
        assertThat(hub.getName()).isEqualTo("testHubUpdated");
        assertThat(hub.getLatitude()).isEqualTo(15L);
        assertThat(hub.getLongitude()).isEqualTo(151L);
        assertThat(hub.getAddress()).isEqualTo("서울특별시 updated");

        // 만약 서비스에서 save()를 호출하지 않는 설계라면 다음 라인으로 보장
        verify(hubRepository, never()).save(any(Hub.class));
    }
}
