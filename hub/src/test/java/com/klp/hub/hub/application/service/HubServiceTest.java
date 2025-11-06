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
import com.klp.hub.hub.presentation.dto.response.hub.RegisterHubResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
