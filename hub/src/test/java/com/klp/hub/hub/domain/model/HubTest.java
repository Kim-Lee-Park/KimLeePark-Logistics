package com.klp.hub.hub.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.klp.hub.hub.application.command.hub.RegisterHubCommand;
import com.klp.hub.hub.application.command.hub.UpdateHubCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HubTest {

    @Test
    @DisplayName("허브 생성 성공")
    void createHubAndStatusIsActive(){
        //given
        RegisterHubCommand command =new RegisterHubCommand(
            "testHub",
            11.,
            12.,
            "서울특별시"
        );

        //when
        Hub hub= Hub.create(command.name(), command.latitude(), command.longitude(), command.address());

        //then
        assertThat(hub.getName()).isEqualTo("testHub");
        assertThat(hub.getLatitude()).isEqualTo(11L);
        assertThat(hub.getLongitude()).isEqualTo(12L);
        assertThat(hub.getAddress()).isEqualTo("서울특별시");
        assertThat(hub.getStatus()).isEqualTo(HubStatus.ACTIVE);
    }

    @Test
    @DisplayName("허브 수정 성공")
    void update(){
        //given
        RegisterHubCommand registerHubCommand =new RegisterHubCommand(
            "testHub",
            11.,
            12.,
            "서울특별시"
        );
        Hub hub= Hub.create(registerHubCommand.name(), registerHubCommand.latitude(), registerHubCommand.longitude(), registerHubCommand.address());

        UpdateHubCommand updateHubCommand = new UpdateHubCommand(
            "testHubUpdated",
            15.,
            151.,
            "서울특별시 updated"
        );

        //when
        hub.update(updateHubCommand.name(), updateHubCommand.latitude(), updateHubCommand.longitude(), updateHubCommand.address());

        //then
        assertThat(hub.getName()).isEqualTo("testHubUpdated");
        assertThat(hub.getLatitude()).isEqualTo(15L);
        assertThat(hub.getLongitude()).isEqualTo(151L);
        assertThat(hub.getAddress()).isEqualTo("서울특별시 updated");
    }
}
