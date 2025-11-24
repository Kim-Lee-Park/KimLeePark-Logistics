package com.klp.authservice.auth.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.authservice.auth.exception.AuthErrorCode;
import com.klp.authservice.auth.infrastructure.external.UserFeignClient;
import com.klp.authservice.auth.infrastructure.external.dto.response.UsernameDuplicateResponse;
import com.klp.common.exception.BusinessException;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
class UserFeignClientIntegrationTest {

    @Autowired
    private UserFeignClient userFeignClient;

    @Test
    @DisplayName("사용자 중복 체크 요청 테스트")
    void checkUsername_available() {
        // given
        stubFor(get(urlEqualTo("/v1/users/exists?username=testuser"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"available\":false}")));

        // when
        UsernameDuplicateResponse response =
            userFeignClient.checkUsernameAvailable("testuser");

        // then
        assertThat(response.available()).isFalse();
    }

    @Test
    @DisplayName("타임아웃 테스트")
    void handle_timeout() {
        // given
        stubFor(get(urlEqualTo("/v1/users/exists?username=testuser"))
            .willReturn(aResponse()
                .withFixedDelay(1000)));

        // when & then
        assertThatThrownBy(() ->
            userFeignClient.checkUsernameAvailable("testuser"))
            .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("4xx 에러 처리 - ErrorDecoder 작동 확인")
    void handle4xxError() {
        // given
        stubFor(get(urlEqualTo("/v1/users/exists?username=testuser"))
            .willReturn(aResponse()
                .withStatus(400)));

        // when & then
        assertThatThrownBy(() ->
            userFeignClient.checkUsernameAvailable("testuser"))
            .isInstanceOf(BusinessException.class)
            .hasMessage(AuthErrorCode.USER_SERVICE_BAD_REQUEST.getMessage());
    }
}
