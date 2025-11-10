package com.klp.delivery.delivery.presentation.controller;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_ADDRESS;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_NAME;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_CUSTOMER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_IDEMPOTENCY_KEY;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ORDER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_SUPPLIER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createCompany;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDriver;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createOrderItemDtoList;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.klp.delivery.common.DeliveryStatus;
import com.klp.delivery.delivery.application.service.CompanyApiClient;
import com.klp.delivery.delivery.application.service.DriverApiClient;
import com.klp.delivery.delivery.domain.Delivery;
import com.klp.delivery.delivery.domain.DeliveryRepository;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
    RestAssured.basePath = "/api";
  }

  @Test
  void 배송생성_조회_E2E() {
    // given
    DeliveryCreateRequest request = new DeliveryCreateRequest(
        DEFAULT_ORDER_ID,
        DEFAULT_SUPPLIER_ID,
        DEFAULT_CUSTOMER_ID,
        "2025-11-05 14:00까지 납품 요청",
        createOrderItemDtoList(),
        DEFAULT_IDEMPOTENCY_KEY
    );

    // when: 배송 생성
    String deliveryId = given()
        .contentType(ContentType.JSON)
        .body(request)
    .when()
        .post("/deliveries")
    .then()
        .statusCode(201)
        .extract()
        .path("deliveries[0].deliveryId");

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
