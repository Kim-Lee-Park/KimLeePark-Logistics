package com.klp.delivery.delivery.presentation.controller;


import static com.klp.delivery.delivery.fixture.DeliveryFixture.createCompanyResponse;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDeliveryRequest;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDriver;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.createOrderItems;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.klp.delivery.common.enums.DeliveryStatus;
import com.klp.delivery.delivery.application.command.DriverCommand;
import com.klp.delivery.delivery.application.service.CompanyClientService;
import com.klp.delivery.delivery.application.service.DriverApiClient;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.repository.DeliveryRepository;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;

import com.klp.delivery.delivery.presentation.dto.DeliveryDetailResponse;
import groovy.util.logging.Slf4j;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;


import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@Import(DeliveryIntegrationTest.TestConfig.class)
class DeliveryIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public CompanyClientService companyApiClient() {
            return companyId -> createCompanyResponse();
        }

        @Bean
        @Primary
        public DriverApiClient driverApiClient() {
            return new DriverApiClient() {
                @Override
                public DriverCommand findArrivalHubDrivers(String receiverId) {
                    return createDriver();
                }

                @Override
                public DriverCommand findDriverAtArrivalHub(Long receiverId) {
                    return createDriver();
                }

            };
        }
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/v1";
    }

    @Test
    void 배송생성_조회_E2E() {
        // given: 고유한 orderId 사용하여 멱등키 중복 방지
        UUID uniqueOrderId = UUID.randomUUID();
        DeliveryCreateRequest request = createDeliveryRequest(uniqueOrderId, createOrderItems());

        // when: 배송 생성
        String deliveryId = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/deliveries")
            .then()
            .statusCode(200)
            .extract()
            .path("items[0].deliveryId");

        // then: 생성된 배송 조회
        given()
            .when()
            .get("/deliveries/{deliveryId}", deliveryId)
            .then()
            .statusCode(200)
            .body("status", equalTo(DeliveryStatus.CREATED.name()));

        // then: 생성된 배송 주문ID로 조회
        ExtractableResponse<Response> res = given()
            .when()
            .get("/deliveries/{deliveryId}/{orderId}", deliveryId, request.orderId())
            .then()
            .statusCode(200)
            .extract();

        List<DeliveryDetailResponse> list = res.jsonPath()
            .getList("", DeliveryDetailResponse.class);
        assertThat(list).hasSize(2);


        // DB 저장 확인
        Delivery savedDelivery = deliveryRepository.findByDeliveryId(UUID.fromString(deliveryId));
        assertThat(savedDelivery.getOrderId()).isEqualTo(uniqueOrderId);
        assertThat(savedDelivery.getStatus()).isEqualTo(DeliveryStatus.CREATED);
    }

    @Test
    void 배송_전체_조회_E2E() {
        // given: 배송 생성 및 저장 (고유한 orderId 사용)
        UUID uniqueOrderId = UUID.randomUUID();
        DeliveryCreateRequest request = createDeliveryRequest(uniqueOrderId, createOrderItems());
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/deliveries")
            .then()
            .statusCode(200);

        // when: 배송 전체 조회 (페이징, 정렬 없음)
        ExtractableResponse<Response> response = given()
            .when()
            .get("/deliveries?page=0&size=10")
            .then()
            .statusCode(200)
            .extract();

        // then: 조회 결과 검증
        List<DeliveryDetailResponse> content = response.jsonPath()
            .getList("content", DeliveryDetailResponse.class);
        int totalElements = response.jsonPath().getInt("totalElements");
        int number = response.jsonPath().getInt("number");
        int size = response.jsonPath().getInt("size");

        assertThat(content).isNotEmpty();
        assertThat(number).isEqualTo(0);
        assertThat(size).isEqualTo(10);
        assertThat(totalElements).isGreaterThanOrEqualTo(2); // 주문당 2개 배송 생성됨
    }

    @Test
    void 배송_전체_조회_정렬_E2E() {
        // given: 배송 생성 및 저장 (고유한 orderId 사용)
        UUID uniqueOrderId = UUID.randomUUID();
        DeliveryCreateRequest request = createDeliveryRequest(uniqueOrderId, createOrderItems());
        given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/deliveries")
            .then()
            .statusCode(200);

        // when: 정렬 파라미터로 배송 전체 조회
        ExtractableResponse<Response> response = given()
            .when()
            .get("/deliveries?page=0&size=10&sort=deliveryId,desc")
            .then()
            .statusCode(200)
            .extract();

        // then: 조회 결과 검증
        List<DeliveryDetailResponse> content = response.jsonPath()
            .getList("content", DeliveryDetailResponse.class);

        assertThat(content).isNotEmpty();
    }

    @Test
    void 배송상태변경_E2E() {
        // given: 배송 생성 (고유한 orderId 사용하여 멱등키 중복 방지)
        UUID uniqueOrderId = UUID.randomUUID();
        DeliveryCreateRequest request = createDeliveryRequest(uniqueOrderId, createOrderItems());
        String deliveryId = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/deliveries")
            .then()
            .statusCode(200)
            .extract()
            .path("items[0].deliveryId");

        // 초기 상태 확인
        given()
            .when()
            .get("/deliveries/{deliveryId}", deliveryId)
            .then()
            .statusCode(200)
            .body("status", equalTo(DeliveryStatus.CREATED.name()));

        // when: 배송 상태 변경 (CREATED -> IN_HUB_TRANSIT)
        String updateRequest = "{\"status\": \"IN_HUB_TRANSIT\"}";
        given()
            .contentType(ContentType.JSON)
            .body(updateRequest)
            .when()
            .patch("/deliveries/{deliveryId}/status", deliveryId)
            .then()
            .statusCode(204);

        // then: 상태 변경 확인 (API 조회)
        given()
            .when()
            .get("/deliveries/{deliveryId}", deliveryId)
            .then()
            .statusCode(200)
            .body("status", equalTo(DeliveryStatus.IN_HUB_TRANSIT.name()));

        // then: 상태 변경 확인 (DB 조회)
        Delivery savedDelivery = deliveryRepository.findByDeliveryId(UUID.fromString(deliveryId));
        assertThat(savedDelivery.getStatus()).isEqualTo(DeliveryStatus.IN_HUB_TRANSIT);
    }

    @Test
    void 배송삭제_E2E() {
        // given: 배송 생성 (고유한 orderId 사용하여 멱등키 중복 방지)
        UUID uniqueOrderId = UUID.randomUUID();
        DeliveryCreateRequest request = createDeliveryRequest(uniqueOrderId, createOrderItems());
        String deliveryId = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/deliveries")
            .then()
            .statusCode(200)
            .extract()
            .path("items[0].deliveryId");

        // 초기 상태 확인
        given()
            .when()
            .get("/deliveries/{deliveryId}", deliveryId)
            .then()
            .statusCode(200)
            .body("status", equalTo(DeliveryStatus.CREATED.name()));

        // when: 배송 삭제
        given()
            .when()
            .delete("/deliveries/{deliveryId}", deliveryId)
            .then()
            .statusCode(204);

        // then: 삭제 확인 (DB 조회)
        Delivery deletedDelivery = deliveryRepository.findByDeliveryId(UUID.fromString(deliveryId));
        assertThat(deletedDelivery.isDeleted()).isTrue();
    }
}
