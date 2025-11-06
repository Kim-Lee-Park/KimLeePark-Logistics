package com.klp.hub.hub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.klp.hub.hub.application.command.hub.RegisterHubCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HubTest {

    @Test
    @DisplayName("허브 생성 성공")
    void createHubAndStatusIsActive(){
        //given
        RegisterHubCommand command =new RegisterHubCommand(
            "testHub",
            11L,
            12L,
            "서울특별시"
        );

        //when
        Hub hub= Hub.create(command);

        //then
        assertThat(hub.getName()).isEqualTo("testHub");
        assertThat(hub.getLatitude()).isEqualTo(11L);
        assertThat(hub.getLongitude()).isEqualTo(12L);
        assertThat(hub.getAddress()).isEqualTo("서울특별시");
        assertThat(hub.getStatus()).isEqualTo(HubStatus.ACTIVE);
    }
}
