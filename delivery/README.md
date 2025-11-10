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

