package com.klp.delivery.delivery.presentation.controller;


import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ORDER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createCompany;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDeliveryRequest;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDriver;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.createOrderItems;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.klp.delivery.common.enums.DeliveryStatus;
import com.klp.delivery.delivery.application.service.CompanyApiClient;
import com.klp.delivery.delivery.application.service.DriverApiClient;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.repository.DeliveryRepository;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import groovy.util.logging.Slf4j;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(DeliveryIntegrationTest.class);
    @LocalServerPort
  private int port;

  @Autowired
  private DeliveryRepository deliveryRepository;

  @TestConfiguration
  static class TestConfig {

    @Bean
    @Primary
    public CompanyApiClient companyApiClient() {
      return companyId -> createCompany();
    }

    @Bean
    @Primary
    public DriverApiClient driverApiClient() {
      return receiverId -> createDriver();
    }
  }

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.basePath = "/v1";
  }

  @Test
  void 배송생성_조회_E2E() {
    // given
      DeliveryCreateRequest request = createDeliveryRequest(createOrderItems());

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

    // DB 저장 확인
    Delivery savedDelivery = deliveryRepository.findByDeliveryId(UUID.fromString(deliveryId))
        .orElseThrow(() -> new AssertionError("배송이 DB에 저장되지 않았습니다."));
    assertThat(savedDelivery.getOrderId()).isEqualTo(DEFAULT_ORDER_ID);
    assertThat(savedDelivery.getStatus()).isEqualTo(DeliveryStatus.CREATED);
  }
}
