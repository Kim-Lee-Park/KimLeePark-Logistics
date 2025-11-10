# Delivery Service - TDD 학습 과정

## 📋 목차

- [개요](#개요)
- [프로젝트 구조](#프로젝트-구조)
- [TDD 학습 과정](#tdd-학습-과정)
- [설계 결정 사항](#설계-결정-사항)
- [테스트 전략](#테스트-전략)
- [실행 방법](#실행-방법)
- [트러블슈팅](#트러블슈팅)
- [학습 회고](#학습-회고)

---

## 개요

배송 서비스를 **TDD(Test-Driven Development)** 방식으로 구현한 프로젝트입니다.

**Red → Green → Refactor** 사이클을 따라 기능을 단계적으로 개발했습니다.

### 프로젝트 목표

1. **TDD 실전 경험**: 테스트 주도 개발 방식으로 코드 작성
2. **레이어드 아키텍처**: 계층별 책임 분리 및 테스트
3. **외부 API 통합**: 외부 서비스와의 통합 패턴 학습
4. **멱등성 보장**: 중복 요청 방지를 위한 멱등키 처리
5. **Facade 패턴**: 복잡한 비즈니스 로직 관리

---

## 프로젝트 구조

```
delivery/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/klp/delivery/
│   │   │       ├── delivery/
│   │   │       │   ├── application/          # 애플리케이션 레이어
│   │   │       │   │   ├── command/          # 명령 객체 (CQRS 패턴)
│   │   │       │   │   ├── facade/           # Facade 패턴
│   │   │       │   │   └── service/          # 서비스 레이어
│   │   │       │   ├── domain/               # 도메인 레이어
│   │   │       │   ├── exception/            # 예외 처리
│   │   │       │   ├── presentation/         # 프레젠테이션 레이어
│   │   │       │   │   ├── controller/       # REST API 컨트롤러
│   │   │       │   │   └── dto/              # 데이터 전송 객체
│   │   │       │   └── repository/           # 인프라스트럭처 레이어
│   │   │       └── common/                   # 공통 모듈
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       ├── java/
│       │   └── com/klp/delivery/
│       │       └── delivery/
│       │           ├── application/           # 서비스 레이어 테스트
│       │           ├── domain/                # 도메인 모델 테스트
│       │           ├── facade/                # Facade 레이어 테스트
│       │           ├── presentation/          # Controller 테스트
│       │           ├── repository/            # Repository 테스트
│       │           └── fixture/               # 테스트 Fixture
│       └── resources/
│           ├── application-test.yml
│           └── schema.sql
```

### 레이어별 책임

#### 1. Domain Layer (도메인 레이어)

- **Delivery**: 배송 도메인 엔티티
- **IdempotencyKey**: 멱등키 도메인 엔티티
- **Company, Driver**: 외부 API 응답 도메인 모델
- 비즈니스 규칙 및 검증 로직 포함

#### 2. Application Layer (애플리케이션 레이어)

- **DeliveryService**: 배송 생성, 조회, 상태 변경
- **IdempotencyKeyService**: 멱등키 등록 및 상태 관리
- **DeliveryFacade**: 복잡한 배송 생성 플로우 관리
- **Command 객체**

#### 3. Presentation Layer (프레젠테이션 레이어)

- **DeliveryController**: REST API 엔드포인트
- **DTO**: 요청/응답 데이터 전송 객체

#### 4. Infrastructure Layer (인프라스트럭처 레이어)

- **DeliveryRepositoryImpl**: JPA Repository 구현
- **CompanyApiClientImpl, DriverApiClientImpl**: 외부 API 클라이언트 구현

---

## TDD 학습 과정

### Step 1: 도메인 모델 테스트 (Domain Model Tests)

**목표:** 도메인 모델의 비즈니스 규칙 검증

**학습 내용:**

- 도메인 모델의 불변성 보장
- 생성 시 필수 필드 검증
- 상태 변경 로직 검증
- given-when-then 패턴 적용

**구현 내용:**

#### Delivery 도메인 모델

```java

@Test
void 배송_생성시_배송상태는_CREATED_설정_검증() {
  // given: 배송 등록 데이터 준비
  UUID vendorDriverId = UUID.randomUUID();
  UUID orderId = UUID.randomUUID();
  // ... 기타 필드

  // when: 배송 생성
  Delivery delivery = Delivery.create(
      vendorDriverId, orderId, departureId, arrivalId,
      receiverId, receiverName, address, receiverSlackId
  );

  // then: 배송 상태값 검증
  assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CREATED);
}
```

**테스트 케이스:**

- [x] 배송 생성 시 상태가 `CREATED`로 설정
- [x] 배송 담당자가 없으면 예외 발생
- [x] 주소가 null/빈 문자열/공백이면 예외 발생
- [x] 수령인 이름이 null/빈 문자열이면 예외 발생
- [x] 수령인 슬랙 ID가 null/빈 문자열이면 예외 발생
- [x] 배송 중일 때 상태가 `ARRIVED_AT_FINAL_HUB`로 변경
- [x] 배송 완료 시 상태가 `DELIVERED`로 변경
- [x] 배송 생성 후 주소 등 주요 정보 수정 불가능

**느낀 점:**

- 도메인 모델의 검증 로직을 먼저 테스트로 정의하면, 비즈니스 규칙이 명확해짐
- 도메인 모델의 불변성을 보장하는 것이 중요함
- 필수 필드 검증을 도메인 레벨에서 처리하면, 애플리케이션 레이어가 간결해짐

---

### Step 2: 서비스 레이어 테스트 (Service Layer Tests)

**목표:** 비즈니스 로직 및 외부 의존성 처리

**학습 내용:**

- 서비스 레이어의 비즈니스 로직 검증
- 외부 API 호출 Mock 처리
- 예외 처리 검증
- Mockito를 사용한 의존성 격리

**구현 내용:**

#### DeliveryService

```java

@Service
@RequiredArgsConstructor
public class DeliveryService {

  private final DeliveryRepository deliveryRepository;
  private final CompanyApiClient companyApiClient;
  private final DriverApiClient driverApiClient;

  public Company findCompany(String customerId) {
    try {
      return companyApiClient.findCompany(customerId);
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("업체 조회 실패: {}", e.getMessage(), e);
      throw new BusinessException(
          DeliveryErrorCode.EXTERNAL_API_ERROR,
          "업체 조회에 실패했습니다.",
          e
      );
    }
  }

  public Driver findDriver(String customerId) {
    // ... 유사한 구현
  }

  public Delivery registerDelivery(DeliveryCommand command) {
    // 배송 생성 및 저장
  }

  public Delivery getDelivery(UUID deliveryId) {
    return deliveryRepository.findByDeliveryId(deliveryId)
        .orElseThrow(() -> new BusinessException(DeliveryErrorCode.DELIVERY_NOT_FOUND));
  }

  public void updateDeliveryStatus(UUID deliveryId, DeliveryStatus status) {
    Delivery delivery = getDelivery(deliveryId);
    delivery.updateStatus(status);
    deliveryRepository.save(delivery);
  }
}
```

**테스트 케이스:**

- [x] 배송 생성 성공
- [x] 배송 ID로 배송 조회 성공
- [x] 배송이 없을 때 예외 발생
- [x] 배송 상태 변경 성공
- [x] 외부 API 호출 실패 시 예외 처리

**느낀 점:**

- 외부 의존성을 Mock으로 격리하면 테스트가 빠르고 안정적임
- 예외 케이스도 테스트로 명확히 정의해야 함
- 서비스 레이어에서 외부 API 호출을 캡슐화하면, 테스트가 용이해짐

---

### Step 3: Repository 레이어 테스트 (Repository Layer Tests)

**목표:** 데이터 영속성 검증

**학습 내용:**

- JPA Repository 테스트
- `@DataJpaTest`를 사용한 슬라이스 테스트
- JPA Auditing 활성화
- 스키마 설정 및 테스트

**구현 내용:**

#### DeliveryRepositoryImpl

```java

@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepository {

  private final DeliveryJpaRepository deliveryJpaRepository;

  @Override
  public Delivery save(Delivery delivery) {
    return deliveryJpaRepository.save(delivery);
  }

  @Override
  public Optional<Delivery> findByDeliveryId(UUID deliveryId) {
    return deliveryJpaRepository.findByDeliveryId(deliveryId);
  }
}
```

**테스트 케이스:**

- [x] 배송 저장 및 조회 성공
- [x] 멱등키 저장 및 조회 성공
- [x] JPA Auditing을 통한 생성/수정 시간 자동 관리

**느낀 점:**

- `@DataJpaTest`를 사용하면 실제 DB 없이도 테스트 가능 (인메모리 H2)
- 하지만 E2E 테스트에서는 실제 DB를 사용하는 것이 더 신뢰할 수 있음
- JPA Auditing을 통해 생성/수정 시간을 자동으로 관리할 수 있음

---

### Step 4: Facade 패턴 (Facade Pattern)

**목표:** 복잡한 비즈니스 플로우 관리

**학습 내용:**

- Facade 패턴을 통한 복잡한 플로우 관리
- 여러 서비스를 조합하여 하나의 기능 완성
- 트랜잭션 관리

**구현 내용:**

#### DeliveryFacade

```java

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryFacade {

  private final DeliveryService deliveryService;
  private final IdempotencyKeyService idempotencyKeyService;

  public DeliveryResponse createDelivery(UUID orderId, DeliveryCreateRequest request) {
    // 멱등키 검증 → 외부 API 조회 → 배송 생성
    // ...
  }
}
```

**테스트 케이스:**

- [x] 단일 아이템 배송 생성 성공
- [x] 여러 아이템 배송 생성 성공 (다른 출발지)
- [x] 모든 아이템의 도착지가 같음
- [x] 멱등키 중복 시 예외 발생
- [x] 업체 조회 실패 시 예외 발생
- [x] 배송 담당자 조회 실패 시 예외 발생
- [x] 배송 저장 실패 시 예외 발생

**느낀 점:**

- 복잡한 플로우는 Facade 패턴으로 분리하면 Controller가 간결해짐
- 각 서비스의 책임이 명확해짐
- 트랜잭션 관리가 용이해짐

---

### Step 5: Controller 레이어 테스트 (Controller Layer Tests)

**목표:** HTTP API 계층 검증

**학습 내용:**

- MockMvc를 사용한 HTTP 요청/응답 검증
- `@WebMvcTest`를 사용한 슬라이스 테스트
- `@ParameterizedTest`를 사용한 경계 값 테스트
- Fixture를 사용한 테스트 데이터 공통화
- HTTP 상태 코드 및 JSON 응답 검증

**구현 내용:**

#### DeliveryController

```java

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

  private final DeliveryFacade deliveryFacade;
  private final DeliveryService deliveryService;

  @PostMapping
  public ResponseEntity<DeliveryResponse> createDelivery(
      @RequestBody @Valid DeliveryCreateRequest request) {
    DeliveryResponse response = deliveryFacade.createDelivery(request.orderId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{deliveryId}")
  public ResponseEntity<DeliveryDetailResponse> getDelivery(
      @PathVariable UUID deliveryId) {
    Delivery delivery = deliveryService.getDelivery(deliveryId);
    return ResponseEntity.ok(DeliveryDetailResponse.from(delivery));
  }

  @PatchMapping("/{deliveryId}/status")
  public ResponseEntity<Void> updateDeliveryStatus(
      @PathVariable UUID deliveryId,
      @RequestBody @Valid DeliveryStatusUpdateRequest request) {
    deliveryService.updateDeliveryStatus(deliveryId, request.status());
    return ResponseEntity.noContent().build();
  }
}
```

#### DeliveryControllerTest

```java

@WebMvcTest(controllers = DeliveryController.class)
class DeliveryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private DeliveryFacade deliveryFacade;

  @MockitoBean
  private DeliveryService deliveryService;

  @Test
  void 배송생성_성공_201Created() throws Exception {
    // given: 배송 생성 요청 데이터
    DeliveryCreateRequest request = new DeliveryCreateRequest(...);
    DeliveryResponse response = new DeliveryResponse(...);
    when(deliveryFacade.createDelivery(any(), any())).thenReturn(response);

    // when: 배송 생성 요청
    mockMvc.perform(post("/api/deliveries")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.deliveries").isArray())
        .andExpect(jsonPath("$.deliveries[0].orderItemId").value(...))
        .andExpect(jsonPath("$.deliveries[0].deliveryId").value(...));

    // then: Facade 호출 검증
    verify(deliveryFacade).createDelivery(any(), any());
  }

  @ParameterizedTest
  @EnumSource(DeliveryStatus.class)
  void 배송상태변경_모든상태변경_성공(DeliveryStatus status) throws Exception {
    // given: 배송 상태 변경 요청 데이터
    DeliveryStatusUpdateRequest request = new DeliveryStatusUpdateRequest(status);
    doNothing().when(deliveryService).updateDeliveryStatus(any(), any());

    // when: 배송 상태 변경 요청
    mockMvc.perform(patch("/api/deliveries/{deliveryId}/status", DEFAULT_DELIVERY_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    // then: 배송 상태 변경 서비스 호출 검증
    verify(deliveryService).updateDeliveryStatus(any(), eq(status));
  }
}
```

**테스트 케이스:**

- [x] 배송 생성 성공 시 201 Created 반환
- [x] 중복 멱등키로 요청 시 예외 발생
- [x] 외부 API 호출 실패 시 예외 발생
- [x] 배송 조회 성공 시 200 OK 반환
- [x] 배송이 없을 때 404 Not Found 반환
- [x] 배송 상태 변경 성공 시 204 No Content 반환
- [x] 모든 배송 상태로 변경 가능 (ParameterizedTest)

**느낀 점:**

- MockMvc를 사용하면 실제 HTTP 서버를 띄우지 않고도 API 테스트 가능
- Fixture를 사용하면 테스트 코드 중복을 줄일 수 있음
- `@ParameterizedTest`를 사용하면 경계 값 테스트가 편리함
- HTTP 상태 코드 및 JSON 응답을 검증하면 API 계약을 보장할 수 있음

---

### Step 6: E2E 통합 테스트 (End-to-End Integration Tests)

**목표:** 전체 시스템 통합 검증

**학습 내용:**

- REST Assured를 사용한 E2E 테스트
- 실제 DB를 사용한 통합 테스트
- `@SpringBootTest`를 사용한 전체 컨텍스트 로딩
- 도커 환경에서 PostgreSQL 연결
- 외부 API Mock 설정

**구현 내용:**

#### DeliveryIntegrationTest

```java

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
    // given: 배송 생성 요청 데이터
    DeliveryCreateRequest request = new DeliveryCreateRequest(...);

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
```

**테스트 케이스:**

- [x] 배송 생성 → 조회 플로우 E2E 테스트
- [x] 실제 DB에 저장되었는지 검증
- [x] 외부 API Mock을 통한 통합 테스트

**느낀 점:**

- E2E 테스트는 실제 환경과 유사하게 테스트할 수 있어 신뢰도가 높음
- 하지만 실행 시간이 길고 외부 의존성이 필요함
- 단위 테스트와 E2E 테스트의 균형이 중요함
- 도커 환경에서 실제 PostgreSQL을 사용하면 더 신뢰할 수 있는 테스트 가능

---

## 설계 결정 사항

### 1. Facade 패턴 선택

**문제:**

- 배송 생성 플로우가 복잡함
- Controller에서 여러 서비스를 직접 호출하면 복잡해짐

**해결:**

- Facade 패턴을 도입하여 복잡한 플로우를 하나의 서비스로 캡슐화
- Controller는 Facade만 호출하도록 단순화

**장점:**

- Controller가 간결해짐
- 비즈니스 로직 변경 시 Controller 수정 불필요
- 트랜잭션 관리가 용이함

### 2. 멱등키 처리 전략

**문제:**

- 네트워크 오류 등으로 인한 중복 요청 처리
- 동일한 주문에 대한 배송 중복 생성 방지

**해결:**

- 주문 단위로 멱등키 관리
- 멱등키 상태: PENDING → COMPLETED
- 중복 멱등키 요청 시 예외 발생

**구현:**

```java
public void registerIdempotencyKey(IdempotencyCommand command) {
  Optional<IdempotencyKey> existingKey =
      idempotencyKeyRepository.findByIdempotencyKey(command.idempotencyKey());

  if (existingKey.isPresent()) {
    throw new BusinessException(DeliveryErrorCode.DUPLICATE_IDEMPOTENCY_KEY);
  }

  IdempotencyKey idempotencyKey = IdempotencyKey.create(
      command.idempotencyKey(), command.orderId()
  );
  idempotencyKeyRepository.save(idempotencyKey);
}
```

### 2. 도메인 모델 검증

**문제:**

- 배송 생성 시 필수 필드 검증
- 비즈니스 규칙 보장

**해결:**

- 도메인 모델에서 검증 로직 구현
- `Delivery.create()` 메서드에서 필수 필드 검증
- 검증 실패 시 `BusinessException` 발생

**구현:**

```java
public static Delivery create(UUID vendorDriverId, UUID orderId,
    UUID departureId, UUID arrivalId, UUID receiverId,
    String receiverName, String address, String receiverSlackId) {
  validateDeliveryData(vendorDriverId, receiverName, address, receiverSlackId);
  return new Delivery(vendorDriverId, orderId, departureId, arrivalId,
      receiverId, receiverName, address, receiverSlackId, DeliveryStatus.CREATED);
}

private static void validateDeliveryData(UUID vendorDriverId, String receiverName,
    String address, String receiverSlackId) {
  if (vendorDriverId == null) {
    throw new BusinessException(DeliveryErrorCode.INVALID_DELIVERY_DATA,
        "배송 담당자는 필수입니다.");
  }
  if (!StringUtils.hasText(address)) {
    throw new BusinessException(DeliveryErrorCode.INVALID_DELIVERY_DATA,
        "배송지 주소는 필수입니다.");
  }
  // ... 기타 검증
}
```

---

## 테스트 전략

### 1. 테스트 피라미드 (Test Pyramid)

```
        /\
       /E2E\          (적음, 느림, 비쌈)
      /------\
     /Integration\    (적당, 중간)
    /------------\
   /  Unit Tests  \   (많음, 빠름, 쌈)
  /----------------\
```

- **Unit Tests (단위 테스트)**: 도메인 모델, 서비스 레이어, Repository 레이어
- **Integration Tests (통합 테스트)**: Controller 레이어, Facade 레이어
- **E2E Tests (E2E 테스트)**: 전체 시스템 통합

### 2. 테스트 격리 (Test Isolation)

- 각 테스트는 독립적으로 실행 가능해야 함
- `@Transactional`을 사용하여 테스트 후 롤백
- Mock을 사용하여 외부 의존성 격리
- `@DataJpaTest`를 사용하여 JPA 레이어만 테스트

### 3. 테스트 네이밍 (Test Naming)

- 한국어 메서드명 사용 (예: `배송_생성시_상태가_CREATED_설정`)
- given-when-then 패턴 적용
- 테스트 의도를 명확하게 표현

### 4. Fixture 사용 (Test Fixtures)

- `DeliveryFixture`: 테스트 데이터 공통화
- 중복 코드 제거
- 테스트 데이터 관리 용이

**예시:**

```java
public class DeliveryFixture {

  public static final UUID DEFAULT_ORDER_ID = UUID.fromString("...");
  public static final UUID DEFAULT_DELIVERY_ID = UUID.fromString("...");
  // ... 기타 상수

  public static Delivery createDelivery(UUID deliveryId) {
    return Delivery.create(
        DEFAULT_VENDOR_DRIVER_ID, DEFAULT_ORDER_ID,
        DEFAULT_DEPARTURE_ID, DEFAULT_ARRIVAL_ID,
        DEFAULT_RECEIVER_ID, DEFAULT_RECEIVER_NAME,
        DEFAULT_ADDRESS, DEFAULT_RECEIVER_SLACK_ID
    );
  }

}
```

---

### API 테스트

```bash
# 배송 생성
curl -X POST http://localhost:8080/api/deliveries \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "550e8400-e29b-41d4-a716-446655440000",
    "supplierId": 1,
    "customerId": 2,
    "comment": "2025-11-05 14:00까지 납품 요청",
    "orderItems": [
      {
        "orderItemId": "660e8400-e29b-41d4-a716-446655440000",
        "hubId": "770e8400-e29b-41d4-a716-446655440000",
        "productId": "880e8400-e29b-41d4-a716-446655440000",
        "quantity": 10
      }
    ],
    "idempotencyKey": "멱등키123"
  }'

# 배송 조회
curl http://localhost:8080/api/deliveries/{deliveryId}

# 배송 상태 변경
curl -X PATCH http://localhost:8080/api/deliveries/{deliveryId}/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "AT_HUB_WAITING"
  }'
```

---

## 트러블슈팅

### 1. JPA Metamodel 에러

**문제:**

```
java.lang.IllegalArgumentException: JPA metamodel must not be empty
```

**원인:**

- `@WebMvcTest`에서 JPA 컴포넌트를 로드하려고 할 때 발생
- `@EnableJpaAuditing`이 `DeliveryApplication`에 있어서 발생

**해결:**

- `JpaAuditingConfig`를 별도 클래스로 분리
- `@ConditionalOnBean(EntityManagerFactory.class)`를 사용하여 조건부 활성화

### 3. 트랜잭션 롤백 문제

**문제:**

- E2E 테스트에서 `@Transactional`을 사용하면 롤백되어 DB 검증 불가

**해결:**

- E2E 테스트에서는 `@Transactional` 제거
- 테스트 후 수동으로 데이터 정리 또는 별도 테스트 DB 사용

### 3. 스키마 생성 문제

**문제:**

```
Schema "delivery_schema" not found
```

**원인:**

- H2 데이터베이스에서 스키마가 자동으로 생성되지 않음

**해결:**

- `schema.sql`을 사용하여 스키마 수동 생성
- `application-test.yml`에서 스키마 생성 설정

---

## 학습 회고

### 잘한 점

1. **TDD 사이클을 꾸준히 적용**
    - Red → Green → Refactor 사이클을 일관되게 적용
    - 비즈니스 규칙이 명확함

2. **given-when-then 패턴을 일관되게 적용**
    - 테스트 가독성 향상
    - 테스트 의도가 명확함

3. **Fixture를 사용하여 테스트 데이터 관리**
    - 테스트 코드 중복 제거
    - 테스트 데이터 관리 용이

4. **레이어별로 테스트를 분리**
    - 각 레이어의 책임이 명확함
    - 테스트 격리가 잘 됨

5. **Facade 패턴 적용**
    - Controller가 간결해짐
    - 비즈니스 로직 변경 시 Controller 수정 불필요

6. **외부 API 통합 전략**
    - Service 레이어에서 외부 API 호출 캡슐화
    - 테스트 환경에서 Mock 처리 용이

### 개선할 점

1. **E2E 테스트를 더 다양하게 추가**
    - 현재는 배송 생성 → 조회 플로우만 테스트
    - 예외 케이스 E2E 테스트 추가 필요

2. **예외 케이스 테스트를 더 보강**
    - 경계 값 테스트 추가
    - 엣지 케이스 테스트 추가

3. **성능 테스트 추가 고려**
    - 대량 데이터 처리 성능 테스트
    - 동시성 테스트

4. **테스트 실행 시간 최적화**
    - 불필요한 테스트 제거
    - 테스트 병렬 실행

5. **문서화 개선**
    - API 문서화 (Swagger/OpenAPI)
    - 아키텍처 다이어그램

### 다음 단계

1. **이벤트 발행 기능 추가**
    - 배송 생성 시 경로 생성 이벤트 발행
    - 배송 상태 변경 시 알림 이벤트 발행

2. **배송 상태 변경 히스토리 관리**
    - 상태 변경 이력 저장
    - 상태 변경 조회 API

3. **배송 조회 페이징 처리**
    - 주문 ID로 배송 목록 조회
    - 페이징 및 정렬 기능
    -
4. **모니터링 및 로깅**
    - 애플리케이션 메트릭 수집
    - 분산 추적 (Distributed Tracing)

5. **보안 강화**
    - 인증/인가 추가
    - API 키 관리

---


# 팀원 회고

## 📌 각 Step별 학습 내용과 느낀 점

### 박성민
- Step 1: TDD에 대한 학습을 시작하며 Red-Green-Refactor 단계로의 수행 과정에 대해 학습했습니다. 테스이기 때문에 Red-Green 과정만을 중요하게 생각했지만, Refactor을 하는 과정을 통해 조금 더 완성도 있는 코드를 작성할 수 있다는 것을 느꼈습니다. 리팩토링을 하면서 기능을 구현함으로써 기능을 구현하고 리팩토링할 때 놓칠 수 있는 부분들을 조금 더 신경쓰게 됐습니다
- Step 2: 서비스 계층에서 `@Mock` , `@InjectMocks` , `when().thenReturn()` 등을 통해서 의존성 격리를 통해, 가볍고 빠른 단위 테스트를 작성할 수 있었습니다. 또한 서비스 계층에서는 메소드들의 실행 순서 및 흐름이 중요한데 중간에 예외가 발생했을 시, 흐름의 차단과 장애 전파 여부를 `verify()` 등을 통해 검증하며 안정성 있는 기능을 구현할 수 있게 됐습니다
- Step 3: 실제 DB의 검증을 위해 `@DataJpaTest` 를 활용해 테스트 코드 작성. H2 기반의 인메모리 DB를 사용 시 DB간 불일치로 인해 발생할 수 있는 문제를 생각하고 `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)` 를 적용했습니다. 하지만, 이는 실제 사용하는 DB를 쓰게 되기 때문에 추가로 `TestContainer` 등에 대해 학습하였습니다. 실제 데이터를 DB에 넣어서 쿼리의 동작을 테스트하는 것이기 떄문에 테스트 데이터 간 독립적인 것이 중요하다고 생각했습니다. 따라서, 이전에 작성된 테스트 데이터가 이후의 테스트에 영향을 주지 않도록 `@AfterEach`를 통해 tearDown 메소드를 작성해, 테스트 마다 DB를 초기화하는 작업을 진행했습니다
- Step 4: 요청 DTO의 검증, 응답에 대한 올바른 JSON 검증 및 HTTP 상태코드가 올바른지 `MockMvc` 를 통해 작성했습니다. 비즈니스 로직에서 하나의 공통된 예외인 `BusinessException` 을 사용하기로 했기 때문에, 에러 코드에 대한 메시지를 검증해 올바른 예외가 발생했는지도 검증할 수 있었습니다. 또한 통합 테스트를 작성하며 MSA 특성 상 다른 서버에 HTTP 요청 등을 보낼 일이 존재하는데, `WireMock` 을 통해, 가상의 서버 및 응답을 생성해 통합 테스트를 작성할 수 있었습니다
- Step 5: 테스트 코드를 먼저 작성하며, 어떠한 기능 및 문제가 발생할 수 있는지를 생각하고 먼저 서비스 계층에 이를 나열하듯이 작성하게 됐습니다. 그 결과, 단일 책임 원칙 등을 위반하고 가독성이 좋지 않은 코드가 되어, 이를 조금씩 리팩토링 하기 시작했습니다. 리팩토링을 진행하면서도 중간 중간 계속해서 미리 작성된 테스트를 실행하며, 리팩토링 진행 시에 어느 부분에서 문제가 발생했는 지를 빠르게 확인할 수 있었습니다. 단순히 흐름만을 통해 코드를 작성할 때와 다르게 확실하게 개선되는 부분을 눈으로 확인할 수 있었습니다

### 박주찬
- Step1: TDD 기초에 대해 학습을 진행하였습니다. - 도메인에서 튜터님이 올려주신 자려를 토대로 `given/when/then`을 이용하면서 `assertThat`과 `assertThatThronwnBy` 를 이용하면서 코드를 작성했고 한번에 완성되는 것보다 red하고 green 구현하는 그런 느낌을 유지하는 방향으로 진행하였습니다
- Step2: mock을 활용하여 작업을 진행하였습니다. → 속도도 빠르고 뭐랄까 given을 통해 내가 원하는 값을 땡겨와서 뭔가 테스트 코드를 짜기 수월했던거 같았습니다
- Step3: repository를 `@DataJpaTest` 를 통해 진행하였고 이 테스트를 위해 테스트 전용 프로필도 생성하고 `application-test.yml`을 통해 실제 db 환경과 같게 생성해서 진행하였습니다
- Step4:  현재 진행 중에 있지만 `webMvc` 테스트를 통해 진행할 예정이며 작업하는 과정에서 여러 Dto를 생성하고 테스트 때 security를 어떻게 처리할 건지에 대해 생각을 많이 하고 있습니다
- Step5: 현재 제대로 진행은 못하고 있지만 리팩토링 → command를 통해 매개변수를 조금 더 단순화 하고 있고 private 메서드롤 통해 검증 로직을 단순화 하고 있고 코드가 건강해지고 예뻐지고 있는것 같습니다

### 김한결
- Step 1: TDD 기초와 도메인 모델 테스트 - 허브 도메인에선 특별한 도메인 로직이 없어서 생성, 수정에 대해서만 테스트해서 학습한 내용이 많이 없지만, `given-when-then`패턴에 적응하는데 집중했습니다. 테스트 코드 작성 기본 `JUnit5`, `AssertJ`을 활용한 코드 작성을 학습했습니다
- Step 2: 서비스 레이어 테스트와 Mock 활용 - 테스트 더블의 `Mock`과 `Stub`의 개념을 공부하는데 도움이 되었습니다. `Red-Green-Refactor` 사이클을 가장 잘 준수했던 단위 테스트를 진행했습니다. 단위 테스트를 통해 서비스 계층의 책임에 대해 고민하는데 도움이 되었습니다
- Step 3: Repository 테스트 - 레포지토리 계층에 필요한 `@DataJpaTest`에 대해 학습할 수 있었습니다. 어떤 어노테이션들이 포함되어 있고 레포지토리 계층에 필요한 어노테이션들이 어떤 것들인지 인지할 수 있었습니다. 영속성 컨텍스트의 flush 시점과 flush가 발생하면 어떤 과정이 이루어지는지에 대한 개념을 복습할 수 있었습니다
- Step 4: API 테스트와 통합 테스트 - 진행하지 못했지만, 이전에 컨트롤러 계층 테스트할 때 Spring Security의 인가 문제를 겪었고 `MockUser`를 통해 해결했었습니다
- Step 5: 복잡한 비즈니스 로직과 리팩토링 - 리팩토링을 진행하지 못했지만 given에서 중복되는 코드들을 리팩토링할 필요성이 보입니다

### 이명규
- 각 Step 마다 필요한 테스트 의존성이 존재하고 이를 역할에 맞게 잘 활용해야한다 (ex. `@DataJpatTest`, `@WebMvcTest`)
- 각각의 Step 혹은 레이어마다 고려해야하는 테스트 목적이 다르다는 것을 인지했다
    - Repository 테스트의 경우 올바른 인자에 따라 예상했던 결과가 조회 됨 혹은 예상했던 결과로 변경됨을 테스트
    - Domain 테스트의 경우 각 도메인이 지켜야하는 핵심 규칙을 올바르게 지켰는지 그리고 지키지 못할 경우 도메인이 생성되지 않는지
    - Controller 테스트의 경우 비즈니스 수행보다는 사용자가 입력한 값이 제대로 검증이 되었는지 테스트
    - 통합 테스트의 경우 각자간의 책임을 모두 수행했을때 예상했던 결과가 나왔는지 테스트

### 김명진
- Step 1: TDD 기초와 도메인 모델 테스트 - 배송 도메인에서 불변성, 필수 필드 검증, 상태 변경 로직을 중심으로 테스트를 작성했습니다. given/when/then 패턴을 통해 테스트 흐름을 명확히 하며, 예외 검증에는 assertThatThrownBy를 사용했습니다. 도메인 모델의 책임과 유효성 검증을 테스트로 보장하는 것이 설계적으로 중요함을 체감했습니다.
- Step 2: 서비스 레이어 테스트와 Mock 활용 - @Mock, @InjectMocks, when().thenReturn() 등을 이용해 외부 의존성을 격리하고, 서비스 로직의 흐름 제어를 검증했습니다. 예외 발생 시 로깅과 전파 여부를 verify()로 확인하며 안정적인 흐름을 유지했습니다. 외부 API 호출을 Mock으로 대체하여 테스트 속도를 높이고, 로직을 독립적으로 검증할 수 있었습니다.
- Step 3: Repository 테스트 - @DataJpaTest를 통해 JPA 기반 저장 및 조회 로직을 검증했습니다. schema.sql을 활용해 스키마를 직접 정의하고, @AutoConfigureTestDatabase(replace = NONE)으로 실제 DB 환경에 가깝게 테스트했습니다. JPA Auditing의 생성·수정 시간 자동 관리도 검증하며, 데이터 무결성을 보장하는 구조를 확인했습니다.
- Step 4: Facade 패턴 및 트랜잭션 테스트 - 복잡한 배송 생성 플로우를 DeliveryFacade로 통합하여 트랜잭션 단위에서 검증했습니다. 여러 서비스 호출을 하나의 진입점으로 묶어 Controller가 단순해지도록 설계했습니다. 도메인 간 책임을 분리하고 트랜잭션 경계를 명확히 하여, 복잡한 플로우를 안정적으로 관리했습니다.
- Step 5: Controller 및 E2E 통합 테스트 - MockMvc와 RestAssured를 함께 사용해 API의 단위 및 통합 테스트를 진행했습니다. 실제 DB 연결과 외부 API Mock 환경을 구성하고, HTTP 상태 코드와 JSON 응답을 검증했습니다. 테스트를 통해 API 동작이 명세서와 일치함을 확인했고, 테스트 코드가 기능 명세 역할을 한다는 점을 느꼈습니다.

## 😥 TDD를 적용하면서 어려웠던 점

### 박성민
- 맨 처음 Red 테스트 작성 시, 테스트 경계값 범위를 어느 정도까지 진행해야 하는지에 대한 판단이 어려웠습니다. 회원가입에서 username 검증만으로 대략 8개의 테스트 코드가 작성되었는데, 하나의 요청 DTO 필드 검증을 위해 많은 테스트 코드가 작성되는 것이 맞는가? 생각을 하게 됐습니다. 결과적으로는, 비즈니스 요구사항이기 때문에 확실한 검증 테스트를 작성하는 것이 옳다고 결정했습니다
- 정상 케이스가 아닌 실패 케이스를 작성해야 하기 때문에, 단순한 코드의 흐름대로 작성했을 때와 다르게 어떤 실패가 발생할 수 있는지 생각하는 부분에서 많은 시간이 소요됐습니다. 기존 구현 방식으로는, 서비스 코드를 작성하며 분기점마다 예외가 발생할 수 있는 부분에서 나누는 것이 쉬웠지만, 머리속으로 흐름을 계속 생각하며 실패 상황만 가정해야 하는 것이 쉽지 않다고 생각했습니다
- 테스트 코드에 대한 리팩토링에 사용되는 시간이 생각보다 많았습니다. 이는 아직 테스트 코드 작성에 미숙하기 때문이라고 생각하지만, 메인 코드가 아닌 테스트 코드를 리팩토링하기 위해서 학습 등의 시간 비용을 소모하게 되면서 기능을 구현하는 데 있어 조금 더 많은 시간이 소모됐다고 생각했습니다
- 기능적인 부분 구현에서 실수가 발생하거나, 잘못 생각했을 경우 발생하는 비용이 많다는 생각을 했습니다. 예를 들어, 현재 작성한 구현 및 테스트 코드에서 기획이 조금 달라질 경우, 이를 위해서 연관되어 있는 모든 테스트 코드에 대해 변경점을 적용해야 하기 때문에 불필요한 작업이 많이 발생했습니다. 관리 포인트가 추가적으로 발생한 것이기 때문에 이 부분에 대해 어떻게 작성할 수 있을 지 고려하게 됐습니다. 이 부분은, 테스트 코드에 대해서 리팩토링이 부족했다고 생각하게 되었으며, 이후 진행하며 개선해볼 생각입니다

### 박주찬
- TDD를 처음 적용을 할때 이 테스트 코드에 대해 어디까지 테스트 코드를 써야 하는가 에 대해 생각이 많았습니다
- 그리고 머리로는 이 테스트 과정을 이해하는데 그 과정을 코드로 어떻게 담아내는가는 어려웠던거 같습니다
- 테스트 코드를 작성할 때 반복적으로 생성하는 부분을 `@BeforeEach` 로 따로 빼놨었는데 이부분이 다른 팀원들이 테스트 코드를 이해하는데 많이 방해를 했던거 같다
    - 그래서 중복되는 코드를 최소한 줄이면서 가독성 있게 하려면 어떻게 해야할까를 조금 오래 고민했다.??!?! 물론 결과물은 생각한 시간에 비해 좋은 결과물은 아니지만
- 테스트 이름에 대해 고민하는것이 어려웠다. 그리고 displayName을 제대로 이용하지 못했던거 같다. 코드리뷰에서 몇 번 리뷰로 올라왔었습니다
    - 중간에 영어이름 짓다가 너무 힘들어서 한글로 테스트 코드를 작성하다가도 그것도 힘들어서 영어로 했다가 뒤죽박죽이였던거 같았다

### 김한결
- JUnit5, AssertJ에 익숙하지 않아 기본 테스트 코드 작성부터 어려웠습니다
- given-when-then 패턴에서 given의 경계에 대해 헷갈렸습니다
- Red - Green - Refactor 사이클에서 Red 단계의 컴파일 에러부터 시작하는지 컴파일 에러가 없게 필요한 클래스나 메서드를 만들어 두고 하는지 헷갈렸습니다

### 이명규
- 테스트 메서드 명을 의도한 목적에 맞게 표현하는 것이 어려웠다 → 작성하면서 좀 더 추상적인 표현으로 테스트를 명시하게 됨
- 테스트 코드에서 given 절의 코드를 통해 다른 사람이 쉽게 테스트 목적을 인지하게 하는 것이 어려웠다
    - 선행 작업이 많은 테스트 코드의 경우 Fixture 혹은 private 메서드로 뽑지만 이렇게 함으로써 다른 사람들이 테스트 코드의 목적을 이해하기 쉬워질지는 모르겠다
- E2E 테스트의 자동화는 어떻게 해야할지 모르겠다
- 서비스 혹은 컨트롤러에 대한 TDD 사이클은 많이 익숙하지 않다
    - 자연스럽게 서비스, 컨트롤러의 구현 코드부터 작성하게 된다

### 김명진
- TDD를 적용할 때 테스트의 경계를 어디까지 설정해야 하는지에 대한 판단이 어려웠습니다.
- 처음 시작 시점에 어떤 단위부터 테스트를 작성해야 할지 감이 잡히지 않아, 초기 진입이 가장 힘들었습니다.
- @ParameterizedTest를 처음 사용하면서 공통 로직을 어떻게 재사용하고 구조화할지에 대해 여러 시행착오를 겪었습니다.
- 작성한 테스트가 의도한 목적을 명확하게 전달하고 있는지에 대해 고민이 많았고, 이를 표현하는 방식이 가장 어려웠습니다.


## 💻 테스트 작성이 개발에 미친 영향

### 박성민
- 테스트 코드에 대해 많이 작성해보지 못해서, 학습 시간인 초기 작성 시간에 대해 시간이 많이 소요되었습니다.
- 경계값을 세분화해서 꼼꼼하게 작성하다보니, 놓치게 된 부분을 빠르게 파악하고 수정할 수 있었습니다.
    - 놓칠 법한 부분까지 테스트 코드가 작성되지 않으면 여전히 놓칠 수 있는 위험성은 있어서, 초기 테스트 코드 작성 시 시나리오를 꼼꼼하게 체크해야할 것 같습니다.
- 코드의 변경사항이 존재할 때, 테스트 코드를 통해 올바르게 변경하고 있는지 빠르게 확인이 가능했습니다.
- 기능 구현하고 결과를 확인하는 것이 아닌, 결과를 미리 작성하고 기능을 구현하기 때문에 기능에 대한 검증을 확실하게 할 수 있었습니다

### 박주찬
- 테스트 코드에서 먼저 대략적인 시나리오를 작성하고 개발을 하니 개발 속도가 더 빨라진거 같다
- 그리고 개발하면서 빼먹은게 있다면 테스트 코드에서 아직 Red 상태기 때문에 빼먹은 기능을 찾기 굉장히 수월했습니다
- 무엇보다 TDD로 개발하니까 굉장히 재밌었다. 뭔가 레고를 차례차례 조립하는 기분이 들었다
- 은근 Green 작업할 때 돌리고 한번에 통과하면 기분이 좋았다👍🏻
- 앞으로 개발할 때도 TDD로 개발할 생각이다

### 김한결
- 테스트 코드 작성이 익숙하지 않아서 테스트 코드 작성하는 것도 공부해야 하다보니 평소 개발하던 것보다 시간이 오래 걸렸습니다
- 하지만 기능의 실패 케이스를 고민하는 과정을 통해 빼먹었던 검증 로직을 발견하고 수정할 수 있었습니다
- 기존의 개발하던 방식인 구현 → 테스트가 아닌 반대로 테스트 → 구현 순서로 개발을 하니 흥미로웠습니다
- TDD에 익숙해지면 완성도 높은 코드를 작성하는데 도움이 될 것 같습니다

### 이명규
- 직접 결과를 보고 예상했던대로 동작하니까 개발이 재밌다
- 테스트를 작성하면서 엣지 케이스, 책임 분리 등을 생각하는 과정을 겪게 된다 이로인해 좀 더 유지보수하기 쉽고 문제를 예상할 수 있게 되었다
- 독립적인 환경을 구축하고 테스트를 하게 되니 해당 객체가 가져야할 책임이 명확해졌다 → 또한 이를 테스트로 검증
- 짧은 사이클을 통해 커밋 단위가 좀 더 나뉘어졌다 → 리뷰시 쉽게 이해할 수 있음, 롤백이 쉽다

### 김명진
- 테스트 코드를 작성하면서 개발 속도가 다소 느려졌고, 예상보다 많은 시간을 소요했습니다.
- 코드를 작성하다 보면 흐름이 새는 경우가 많았지만, 테스트를 통해 코드의 흐름을 명확히 잡을 수 있었습니다.
- 테스트를 작성하고 실행하는 과정을 반복하면서 앞으로 어떤 방식으로 코드를 구조화하고 작성해야 할지 방향이 더욱 명확해졌습니다.


## 🔎 실무에서 적용할 수 있는 인사이트

### 박성민
- 테스트의 Displayname를 잘 작성하게 된다면, 테스트 코드 자체가 하나의 비즈니스 기획 문서가 될 수 있다는 생각을 했습니다. 별도의 문서를 읽지 않아도 회원가입 시 username 요구사항 확인, password 평문 저장 여부 확인 등 컨벤션을 잘 구성한다면, 하나의 문서로써 가치를 가질 수 있다는 생각을 하게 됐습니다
- 코드의 기능 추가, 기획 변경이 아닌 리팩토링 시에 안전하게 리팩토링을 진행할 수 있었습니다. 잘 작성된 테스트 코드는, 안전망과 같이 개발자의 실수를 방지할 수 있는 하나의 대비책이 될 수 있다고 생각했습니다
- Mocking 등을 통해, 실제 다른 서비스가 실행되지 않으면 테스트 할 수 없던 부분에 대해 테스트를 진행하며, 팀 사이에서 명세서만 확실하다면 내가 작성한 코드에 대해서 검증을 바로바로 할 수 있다는 것 또한 큰 장점이라고 생각합니다

### 박주찬
- 제일 많이 참고했던것은 우선 서동우 튜터님 예시 코드
- [TDD 서동우튜터님 노션](https://substantial-visage-888.notion.site/TDD-29f861c68fb18034a978d9a20e0b48cc?pvs=25)
- given when then 형식 → assertThat → assertThathThrownBy .isInstanceOf . hasMessage → given()

### 김한결
- 모든 메서드보다는 기능 시나리오 위주로 테스트
- 테스트가 어렵다는 건 설계의 문제점을 고민해봐야 한다는 것. 리팩토링을 고민해보자
- 테스트의 의도를 명확히 하자

### 이명규
- 각 테스트간 용어 차이를 이해하자 → 테스트 작성시 모호해지지 않는다
- 각 레이어별 테스트가 가지고 있는 역할을 이해하자 → 테스트하려는 것이 어떤 것인지 명확해진다
- 테스트에서 적용해야할 인사이트들
    - [Mock과 Stub의 차이](https://www.inflearn.com/community/questions/1257985/mock%EA%B3%BC-stub%EC%9D%98-%EC%B0%A8%EC%9D%B4%EA%B0%80-%EC%95%84%EC%A7%81-%EC%9E%98-%EA%B5%AC%EB%B6%84%EB%90%98%EC%A7%80-%EC%95%8A%EC%8A%B5%EB%8B%88%EB%8B%A4?srsltid=AfmBOor4bq6PMudigwNf5y6eY6Wk_-vRaLdGluHe0ZbpYUJjfvKKzkZ-)
    - [[Spring] @SpringBootTest 테스트 격리시키기(TestExecutionListener를 활용한 H2 데이터베이스 Truncate 초기화)](https://mangkyu.tistory.com/264)
    - [[Java] JUnit을 활용한 Java 단위 테스트 코드 작성법 (2/3)](https://mangkyu.tistory.com/144)
    - [[Spring] JUnit과 Mockito 기반의 Spring 단위 테스트 코드 작성법 (3/3)](https://mangkyu.tistory.com/145)
    - [[Spring] TDD로 멤버십 등록 API 구현 예제 - (3/5)](https://mangkyu.tistory.com/184)

### 김명진
- 테스트 코드를 통해 리팩토링 시 안정감을 느꼈고, 코드 수정 후 동작을 빠르게 검증할 수 있었습니다.
- 기능 요구사항을 테스트로 먼저 정의하니 코드 작성 방향이 명확해지고, 테스트 코드가 명세서처럼 작동해 팀원 간 이해를 높일 수 있었습니다.
- 실무에서도 핵심 로직만큼은 테스트를 먼저 작성해 안정적인 개발 흐름을 유지하려 합니다.