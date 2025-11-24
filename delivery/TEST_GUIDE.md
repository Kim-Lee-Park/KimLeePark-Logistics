# 테스트 가이드

## 테스트 네이밍 규칙

### 1. 메서드명 규칙

**핵심 원칙:**

- **한국어 메서드명 사용**: 테스트 의도를 명확하게 표현하기 위해 **모든 테스트 메서드명은 한글로 작성**합니다.
- **언더스코어(_) 사용**: 단어 구분을 위해 언더스코어를 사용합니다.
- **형식**: `기대하는동작_조건_결과` 또는 `동작_조건_검증내용`

**메서드명 작성 규칙:**

- 테스트하고자 하는 기능을 명확하게 표현
- 예외 상황은 `예외발생`, `실패` 등의 키워드 사용
- 검증 내용은 `검증`, `설정`, `변경` 등의 키워드 사용

**예시:**

```java

@Test
void 배송_생성시_배송상태는_CREATED_설정_검증() {
  // 테스트 코드
}

@Test
void 배송_생성시_배송담당자가없으면_예외발생() {
  // 테스트 코드
}

@Test
void 배송_생성시_주소가null이면_예외발생() {
  // 테스트 코드
}

@Test
void 배송중일때_상태가_ARRIVED_AT_FINAL_HUB로_변경() {
  // 테스트 코드
}

@Test
void 배송ID로_배송_조회_성공() {
  // 테스트 코드
}
```

### 2. given-when-then 패턴

**핵심 원칙:**

- **모든 테스트는 given-when-then 패턴을 따릅니다.**
- **각 단계에 명확한 주석을 작성**하여 무엇을 하는지 명시합니다.
- **given**: 테스트 데이터 준비 및 Mock 설정
- **when**: 테스트 대상 메서드 실행
- **then**: 결과 검증

**given-when-then 주석 작성 규칙:**

- `// given: [무엇을 준비하는지]` - 예: "배송 등록 데이터 준비", "Mock 설정"
- `// when: [무엇을 실행하는지]` - 예: "배송 생성", "배송 조회"
- `// then: [무엇을 검증하는지]` - 예: "생성 검증", "상태값 검증", "예외 발생 검증"

**예시:**

```java

@Test
void 배송_생성시_배송상태는_CREATED_설정_검증() {
  // given: 배송 등록 데이터 준비
  UUID vendorDriverId = UUID.randomUUID();
  UUID orderId = UUID.randomUUID();
  UUID departureId = UUID.randomUUID();
  UUID arrivalId = UUID.randomUUID();
  UUID receiverId = UUID.randomUUID();
  String receiverName = "홍길동";
  String address = "서울특별시 강남구 테헤란로 123";
  String receiverSlackId = "U123456";

  // when: 배송 생성
  Delivery delivery = Delivery.create(
      vendorDriverId,
      orderId,
      departureId,
      arrivalId,
      receiverId,
      receiverName,
      address,
      receiverSlackId
  );

  // then: 배송 상태값 검증
  assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CREATED);
}

@Test
void 배송_생성시_배송담당자가없으면_예외발생() {
  // given: 배송 담당자가 null인 데이터
  UUID vendorDriverId = null;
  UUID orderId = UUID.randomUUID();
  // ... 기타 필드

  // when & then: 예외 발생 검증
  assertThatThrownBy(() -> Delivery.create(
      vendorDriverId,
      orderId,
      // ... 기타 파라미터
  ))
      .isInstanceOf(BusinessException.class)
      .satisfies(exception -> {
        BusinessException businessException = (BusinessException) exception;
        assertThat(businessException.getErrorCode()).isEqualTo(
            DeliveryErrorCode.INVALID_DELIVERY_DATA);
      });
}

@Test
void 배송_생성_성공() {
  // given: 배송 등록 데이터 준비
  DeliveryCommand command = new DeliveryCommand(...);
  Delivery delivery = createDelivery();
  when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);

  // when: 배송 생성
  Delivery result = deliveryService.registerDelivery(command);

  // then: 생성 검증
  assertThat(result).isNotNull();
  assertThat(result.getStatus()).isEqualTo(DeliveryStatus.CREATED);
  verify(deliveryRepository).save(any(Delivery.class));
}
```

**주요 포인트:**

- `given`: 테스트에 필요한 데이터를 준비하고, Mock 객체의 동작을 설정합니다.
- `when`: 테스트하고자 하는 메서드를 실행합니다.
- `then`: 실행 결과를 검증합니다. 예외 테스트의 경우 `when & then`으로 합쳐서 작성할 수 있습니다.

---

## Fixture 사용법

### 1. Fixture 클래스 위치

- **위치**: `src/test/java/com/klp/delivery/delivery/fixture/DeliveryFixture.java`
- **목적**: 테스트 데이터를 공통화하여 중복 코드를 제거하고 유지보수를 용이하게 합니다.

### 2. Fixture 메서드 명명 규칙

**상수 명명 규칙:**

- `DEFAULT_*`: 기본 상수 값 (예: `DEFAULT_ORDER_ID`, `DEFAULT_COMPANY_NAME`)
- 모든 테스트에서 공통으로 사용하는 값을 상수로 정의합니다.

**Factory 메서드 명명 규칙:**

- `create*()`: 기본 엔티티 생성 (예: `createDelivery()`, `createCompany()`)
- `create*(파라미터)`: 파라미터를 받아 엔티티 생성 (예: `createDelivery(UUID deliveryId)`)
- `create*With*()`: 특정 조건으로 엔티티 생성 (예: `createDeliveryWithStatus(DeliveryStatus status)`)

**실제 Fixture 예시:**

```java
public class DeliveryFixture {

  // 상수 정의
  public static UUID DEFAULT_ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static UUID DEFAULT_DELIVERY_ID = UUID.fromString("00000000-0000-0000-0000-000000000006");
  public static String DEFAULT_COMPANY_NAME = "테스트업체";
  public static String DEFAULT_COMPANY_ADDRESS = "서울특별시 강남구 테헤란로 123";
  public static Long DEFAULT_CUSTOMER_ID = 2L;
  public static String DEFAULT_IDEMPOTENCY_KEY = "멱등키123";

  // 기본 엔티티 생성
  public static Delivery createDelivery() {
    return Delivery.create(
        DEFAULT_VENDOR_DRIVER_ID,
        DEFAULT_ORDER_ID,
        DEFAULT_DEPARTURE_ID,
        DEFAULT_ARRIVAL_ID,
        DEFAULT_RECEIVER_ID,
        DEFAULT_COMPANY_NAME,
        DEFAULT_COMPANY_ADDRESS,
        DEFAULT_RECEIVER_SLACK_ID
    );
  }

  // 파라미터를 받아 엔티티 생성
  public static Delivery createDelivery(UUID deliveryId) {
    Delivery delivery = createDelivery();
    // 리플렉션을 사용하여 deliveryId 설정
    // ...
    return delivery;
  }

  // 특정 조건으로 엔티티 생성
  public static Delivery createDeliveryWithStatus(DeliveryStatus status) {
    Delivery delivery = createDelivery();
    delivery.updateStatus(status);
    return delivery;
  }

  // Company 생성
  public static Company createCompany() {
    return new Company(
        DEFAULT_RECEIVER_ID.toString(),
        DEFAULT_HUB_ID,
        "CUSTOMER",
        DEFAULT_COMPANY_NAME,
        DEFAULT_COMPANY_ADDRESS
    );
  }

  // Driver 생성
  public static Driver createDriver() {
    return new Driver(DEFAULT_VENDOR_DRIVER_ID_STR, DEFAULT_RECEIVER_SLACK_ID);
  }
}
```

### 3. Fixture 사용 예시

**기본 사용:**

```java

@Test
void 배송_생성시_상태가_CREATED_설정_검증() {
  // given: Fixture를 사용하여 배송 데이터 준비
  Delivery delivery = DeliveryFixture.createDelivery();

  // when: 상태 확인
  DeliveryStatus status = delivery.getStatus();

  // then: 상태값 검증
  assertThat(status).isEqualTo(DeliveryStatus.CREATED);
}
```

**상수 사용:**

```java

@Test
void 배송_생성_성공() {
  // given: Fixture 상수를 사용하여 테스트 데이터 준비
  UUID orderId = DeliveryFixture.DEFAULT_ORDER_ID;
  UUID customerId = DeliveryFixture.DEFAULT_CUSTOMER_ID;
  Company company = DeliveryFixture.createCompany();

  when(companyApiClient.findCompany(DEFAULT_CUSTOMER_ID.toString()))
      .thenReturn(company);

  // when: 배송 생성
  Delivery result = deliveryService.registerDelivery(command);

  // then: 생성 검증
  assertThat(result).isNotNull();
}
```

**static import 사용 (권장):**

```java
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ORDER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_CUSTOMER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDelivery;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createCompany;

@Test
void 배송_생성_성공() {
  // given: static import를 사용하여 Fixture 사용
  UUID orderId = DEFAULT_ORDER_ID;
  Company company = createCompany();

  // when & then: ...
}
```

### 4. Fixture 사용 장점

- **중복 코드 제거**: 테스트 데이터 생성 로직을 한 곳에서 관리
- **가독성 향상**: 테스트 코드가 간결해지고 의도가 명확해짐
- **유지보수 용이**: 테스트 데이터 변경 시 Fixture 클래스만 수정하면 됨
- **일관성 보장**: 모든 테스트에서 동일한 테스트 데이터를 사용

### 5. Fixture 사용 시 주의사항

- **테스트 간 격리**: Fixture는 테스트 데이터를 생성하는 것일 뿐, 상태를 공유하지 않습니다.
- **파라미터 활용**: 필요에 따라 파라미터를 받아 다양한 테스트 데이터를 생성할 수 있습니다.
- **static import 권장**: 코드 가독성을 위해 static import를 사용하는 것을 권장합니다.

---

## Mock 사용 가이드

### 1. Mock 사용 시점

**Mock을 사용해야 하는 경우:**

- **외부 의존성 격리**: 외부 API, DB 등 외부 시스템과의 통신을 차단
- **복잡한 의존성 단순화**: Facade, Service 등 복잡한 의존성을 단순화
- **테스트 격리**: 다른 레이어와 독립적으로 테스트하고자 할 때
- **테스트 속도 향상**: 실제 외부 시스템 호출 없이 빠른 테스트 실행
- **예외 상황 시뮬레이션**: 외부 시스템 오류 등을 시뮬레이션

**Mock을 사용하지 않아도 되는 경우:**

- **도메인 모델 테스트**: 순수 Java 객체이므로 Mock 불필요
- **Repository 테스트**: `@DataJpaTest`를 사용하여 실제 DB 사용
- **E2E 테스트**: 실제 시스템을 사용하는 통합 테스트

### 2. Mock 생성 방법

#### @Mock + @InjectMocks (Mockito - 단위 테스트)

**사용 시점**: 순수한 단위 테스트, Spring Context 불필요

```java

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

  @Mock
  private DeliveryRepository deliveryRepository;

  @Mock
  private CompanyApiClient companyApiClient;

  @Mock
  private DriverApiClient driverApiClient;

  @InjectMocks
  private DeliveryService deliveryService;
}
```

**실제 사용 예시:**

```java
public class DeliveryServiceTest extends MockTest {

  @InjectMocks
  DeliveryService deliveryService;

  @Mock
  DeliveryRepository deliveryRepository;

  @Mock
  CompanyApiClient companyApiClient;

  @Mock
  DriverApiClient driverApiClient;

  @Test
  void 배송_생성_성공() {
    // given: Mock 설정
    Delivery delivery = createDelivery();
    when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);

    // when: 배송 생성
    Delivery result = deliveryService.registerDelivery(command);

    // then: 생성 검증
    assertThat(result).isNotNull();
    verify(deliveryRepository).save(any(Delivery.class));
  }
}
```

#### @MockitoBean (Spring Boot Test - Controller 테스트)

**사용 시점**: `@WebMvcTest`를 사용하는 Controller 테스트

```java

@WebMvcTest(controllers = DeliveryController.class)
class DeliveryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private DeliveryFacade deliveryFacade;

  @MockitoBean
  private DeliveryService deliveryService;
}
```

**실제 사용 예시:**

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
    // given: Mock 설정
    DeliveryCreateRequest request = new DeliveryCreateRequest(...);
    DeliveryResponse response = new DeliveryResponse(...);
    when(deliveryFacade.createDelivery(any(), any())).thenReturn(response);

    // when: 배송 생성 요청
    mockMvc.perform(post("/api/deliveries")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    // then: Facade 호출 검증
    verify(deliveryFacade).createDelivery(any(), any());
  }
}
```

#### @TestConfiguration (Spring Boot Test - E2E 테스트)

**사용 시점**: `@SpringBootTest`를 사용하는 E2E 테스트에서 외부 API만 Mock

```java

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DeliveryIntegrationTest {

  @TestConfiguration
  static class TestConfig {

    @Bean
    @Primary
    public CompanyApiClient companyApiClient() {
      return companyId -> DeliveryFixture.createCompany();
    }

    @Bean
    @Primary
    public DriverApiClient driverApiClient() {
      return receiverId -> DeliveryFixture.createDriver();
    }
  }
}
```

### 3. Mock 설정 예시

#### when().thenReturn() - 정상 동작 시뮬레이션

**사용 시점**: Mock 객체가 특정 값을 반환해야 할 때

```java

@Test
void 배송_생성_성공() {
  // given: Mock 설정 - Company 조회 시 Fixture로 생성한 Company 반환
  Company company = createCompany();
  when(companyApiClient.findCompany(DEFAULT_CUSTOMER_ID.toString()))
      .thenReturn(company);

  Driver driver = createDriver();
  when(driverApiClient.findDriver(DEFAULT_CUSTOMER_ID.toString()))
      .thenReturn(driver);

  Delivery delivery = createDelivery();
  when(deliveryRepository.save(any(Delivery.class)))
      .thenReturn(delivery);

  // when: 테스트 대상 메서드 실행
  Delivery result = deliveryService.registerDelivery(command);

  // then: 생성 검증
  assertThat(result).isNotNull();
  assertThat(result.getStatus()).isEqualTo(DeliveryStatus.CREATED);
  verify(companyApiClient).findCompany(DEFAULT_CUSTOMER_ID.toString());
  verify(driverApiClient).findDriver(DEFAULT_CUSTOMER_ID.toString());
  verify(deliveryRepository).save(any(Delivery.class));
}
```

#### when().thenThrow() - 예외 상황 시뮬레이션

**사용 시점**: Mock 객체가 예외를 던져야 할 때

```java

@Test
void 외부_API_호출_실패시_예외발생() {
  // given: Mock 설정 - Company 조회 시 RuntimeException 발생
  when(companyApiClient.findCompany(anyString()))
      .thenThrow(new RuntimeException("외부 API 호출 실패"));

  // when & then: 예외 발생 검증
  assertThatThrownBy(() -> deliveryService.findCompany(DEFAULT_CUSTOMER_ID.toString()))
      .isInstanceOf(BusinessException.class)
      .satisfies(exception -> {
        BusinessException businessException = (BusinessException) exception;
        assertThat(businessException.getErrorCode()).isEqualTo(
            DeliveryErrorCode.EXTERNAL_API_ERROR);
      });

  // then: 예외가 발생했는지 검증
  verify(companyApiClient).findCompany(DEFAULT_CUSTOMER_ID.toString());
}
```

#### doNothing().when() - void 메서드 Mock 설정

**사용 시점**: 반환값이 없는 void 메서드를 Mock할 때

```java

@Test
void 배송_상태변경_성공() {
  // given: Mock 설정 - void 메서드는 doNothing() 사용
  doNothing().when(deliveryService).updateDeliveryStatus(any(), any());

  // when: 테스트 대상 메서드 실행
  deliveryService.updateDeliveryStatus(deliveryId, DeliveryStatus.DELIVERED);

  // then: 메서드 호출 검증
  verify(deliveryService).updateDeliveryStatus(deliveryId, DeliveryStatus.DELIVERED);
}
```

#### doThrow().when() - void 메서드 예외 시뮬레이션

**사용 시점**: void 메서드가 예외를 던져야 할 때

```java

@Test
void 배송_상태변경_실패() {
  // given: Mock 설정 - void 메서드가 예외를 던질 때
  doThrow(new BusinessException(DeliveryErrorCode.DELIVERY_NOT_FOUND))
      .when(deliveryService).updateDeliveryStatus(any(), any());

  // when & then: 예외 발생 검증
  assertThatThrownBy(
      () -> deliveryService.updateDeliveryStatus(deliveryId, DeliveryStatus.DELIVERED))
      .isInstanceOf(BusinessException.class);
}
```

#### when().thenAnswer() - 동적 응답 생성

**사용 시점**: 파라미터에 따라 다른 값을 반환해야 할 때

```java

@Test
void 배송생성_성공_여러아이템_다른출발지() {
  // given: Mock 설정 - 파라미터에 따라 다른 Delivery 반환
  when(deliveryService.registerDelivery(any(DeliveryCommand.class)))
      .thenAnswer(invocation -> {
        DeliveryCommand cmd = invocation.getArgument(0);
        if (cmd.departureId().equals(hubId1)) {
          return delivery1;
        } else if (cmd.departureId().equals(hubId2)) {
          return delivery2;
        }
        return delivery1;
      });

  // when & then: ...
}
```

### 4. Mock 검증 (Verify)

**메서드 호출 여부 확인:**

```java
// 기본 검증 - 메서드가 호출되었는지 확인
verify(companyApiClient).

findCompany(DEFAULT_CUSTOMER_ID.toString());
```

**메서드 호출 횟수 확인:**

```java
// 정확히 1번 호출되었는지 확인
verify(companyApiClient, times(1)).

findCompany(anyString());

// 여러 번 호출되었는지 확인
verify(deliveryService, times(2)).

registerDelivery(any(DeliveryCommand.class));
```

**메서드 호출 안 함 확인:**

```java
// 메서드가 호출되지 않았는지 확인
verify(companyApiClient, never()).

findCompany(anyString());

// 특정 상황에서 호출되지 않았는지 확인
verify(deliveryService, never()).

registerDelivery(any(DeliveryCommand.class));
```

**실제 사용 예시:**

```java

@Test
void 배송_생성_성공() {
  // given: Mock 설정
  Company company = createCompany();
  when(companyApiClient.findCompany(DEFAULT_CUSTOMER_ID.toString()))
      .thenReturn(company);

  Driver driver = createDriver();
  when(driverApiClient.findDriver(DEFAULT_CUSTOMER_ID.toString()))
      .thenReturn(driver);

  Delivery delivery = createDelivery();
  when(deliveryRepository.save(any(Delivery.class)))
      .thenReturn(delivery);

  // when: 배송 생성
  Delivery result = deliveryService.registerDelivery(command);

  // then: 생성 검증
  assertThat(result).isNotNull();

  // then: Mock 메서드 호출 검증
  verify(companyApiClient).findCompany(DEFAULT_CUSTOMER_ID.toString());
  verify(driverApiClient).findDriver(DEFAULT_CUSTOMER_ID.toString());
  verify(deliveryRepository).save(any(Delivery.class));
}
```

### 5. Mock 사용 시 주의사항

- **과도한 Mock 사용 지양**: 실제 구현과 동작이 달라질 수 있음
- **Mock 설정 명확성**: Mock이 반환하는 값이 명확해야 함
- **Mock 검증**: Mock이 예상대로 호출되었는지 검증
- **실제 객체 사용**: 가능하면 실제 객체를 사용하는 것이 좋음

---

## 테스트 레이어별 가이드

### 1. 도메인 모델 테스트 (Domain Model Tests)

- **목표**: 도메인 모델의 비즈니스 규칙 검증
- **Mock 사용**: 불필요 (순수 Java 객체)
- **예시**: `DeliveryTest`, `IdempotencyKeyTest`

```java

@Test
void 배송_생성시_상태가_CREATED_설정() {
  // given
  UUID vendorDriverId = UUID.randomUUID();
  // ... 기타 필드

  // when
  Delivery delivery = Delivery.create(
      vendorDriverId,
      orderId,
      // ... 기타 파라미터
  );

  // then
  assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CREATED);
}
```

### 2. 서비스 레이어 테스트 (Service Layer Tests)

- **목표**: 비즈니스 로직 및 외부 의존성 처리
- **Mock 사용**: 외부 API, Repository
- **예시**: `DeliveryServiceTest`, `IdempotencyServiceTest`

```java

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

  @Mock
  private DeliveryRepository deliveryRepository;

  @Mock
  private CompanyApiClient companyApiClient;

  @InjectMocks
  private DeliveryService deliveryService;

  @Test
  void 배송_생성_성공() {
    // given
    Company company = DeliveryFixture.createCompany();
    when(companyApiClient.findCompany(anyString())).thenReturn(company);

    // when
    Delivery delivery = deliveryService.registerDelivery(command);

    // then
    assertThat(delivery).isNotNull();
    verify(deliveryRepository).save(any(Delivery.class));
  }
}
```

### 3. Repository 레이어 테스트 (Repository Layer Tests)

- **목표**: 데이터 영속성 검증
- **Mock 사용**: 불필요 (`@DataJpaTest` 사용)
- **예시**: `DeliveryRepositoryImplTest`, `IdempotencyRepositoryImplTest`

```java

@DataJpaTest
@ActiveProfiles("test")
@Import({DeliveryRepositoryImpl.class, JpaAuditingConfig.class})
class DeliveryRepositoryImplTest {

  @Autowired
  private DeliveryRepositoryImpl deliveryRepository;

  @Test
  void 배송_등록_성공() {
    // given
    Delivery delivery = DeliveryFixture.createDelivery();

    // when
    Delivery result = deliveryRepository.save(delivery);

    // then
    assertThat(result.getDeliveryId()).isNotNull();
    assertThat(result.getStatus()).isEqualTo(DeliveryStatus.CREATED);
  }
}
```

### 4. Controller 레이어 테스트 (Controller Layer Tests)

- **목표**: HTTP API 계층 검증
- **Mock 사용**: Service, Facade
- **예시**: `DeliveryControllerTest`

```java

@WebMvcTest(controllers = DeliveryController.class)
class DeliveryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private DeliveryFacade deliveryFacade;

  @Test
  void 배송생성_성공_201Created() throws Exception {
    // given
    DeliveryCreateRequest request = new DeliveryCreateRequest(...);
    DeliveryResponse response = new DeliveryResponse(...);
    when(deliveryFacade.createDelivery(any(), any())).thenReturn(response);

    // when & then
    mockMvc.perform(post("/api/deliveries")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.deliveries").isArray());
  }
}
```

### 5. E2E 통합 테스트 (End-to-End Integration Tests)

- **목표**: 전체 시스템 통합 검증
- **Mock 사용**: 외부 API만 Mock
- **예시**: `DeliveryIntegrationTest`

```java

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
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
      return companyId -> DeliveryFixture.createCompany();
    }
  }

  @Test
  void 배송생성_조회_E2E() {
    // given
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
  }
}
```

