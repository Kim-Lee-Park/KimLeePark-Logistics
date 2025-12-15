# 7주차 과제: 분산 트랜잭션 구현 및 회고

## 1. 프로젝트 개요

### 선택 시나리오

**시나리오 A: 주문 취소 & 환불**

KimLeePark-Logistics 프로젝트에서 주문 생성부터 취소까지의 분산 트랜잭션을 구현했습니다.

### 시스템 아키텍처

- **이벤트 브로커**: Kafka
- **패턴**: 오케스트레이션 + 코레오그래피 기반 이벤트 드리븐 아키텍처
- **참여 서비스**: Order, User, Hub(Inventory), Promotion, Payment, Delivery

### 핵심 이벤트 플로우

### 주문 생성

```jsx
주문
요청 → 회원 / 상품
조회 → 재고
선점 → 쿠폰
선점
및
할인
계산
→ 결제
처리 → 쿠폰
확정 → 재고
차감 → 배송
생성 → 배송
완료
```

### 주문 취소

```jsx
주문
취소
요청 → 결제
취소 → 쿠폰
복원
이벤트 → 재고
복원
이벤트 
```

---

## STEP 1: 비즈니스 로직 구현

### 1-1. 정방향 트랜잭션: 주문 생성

### 1️⃣ 주문 생성 및 재고 선점 (동기)

**OrderFacade.java - createOrder()**

```jsx
@Transactional
public
Order
createOrder(CreateOrderCommand
command
)
{
  try {
  *// 1. 유저 정보 조회 (grade, email, username)*
    UserProfile
    userProfile = userQueryService.getUserProfile(command.userId());

  *// 2. 배송지 정보 조회*
    UserAddressHubId
    userAddressHubId = userClient.getUserAddressHubIdByAddressId(
        command.addressId());

  *// 3. 주문 생성 (PENDING 상태)*
    Order
    order = orderService.createOrder(command);
    log.info("주문 생성 완료 - orderId: {}", order.getOrderId());

  *// 4. 멱등성 키 생성 (재고용, 배송용)*
    String
    InventoryIdempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
        order.getOrderId(),
        Target.INVENTORY,
        OperationType.DECREASE
    );

    String
    DeliveryIdempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
        order.getOrderId(),
        Target.DELIVERY,
        OperationType.MAKING
    );

  *// 5. 상품 존재 확인 및 재고 선점*
    List < OrderItemCommand > orderItems = command.items();
    int
    originalPriceTotal = 0;
    List < InventoryReservationRequest.ReservationItemRequest > reservationItems = new ArrayList <
        > ();

    for (OrderItemCommand orderItem : orderItems
  )
    {
      productQueryService.getProductById(orderItem.productId());
    *// 상품 존재 확인*
      originalPriceTotal += orderItem.getTotalPrice();
      reservationItems.add(
          new InventoryReservationRequest.ReservationItemRequest(
              orderItem.productId(),
              orderItem.hubId(),
              orderItem.quantity()
          )
      );
    }

  *// 6. 재고 선점 요청 (동기)*
    InventoryReservationRequest
    reservationRequest = new InventoryReservationRequest(
        order.getOrderId(),
        InventoryIdempotencyKey,
        reservationItems
    );
    InventoryReservationResponse
    inventoryResponse = inventoryService.reserveProduct(
        reservationRequest);

    validateInventoryReservation(inventoryResponse, order.getOrderId(), reservationItems.size());

  *// 7. 할인 금액 업데이트*
    orderService.updateDiscountPrice(order, 0, 1, originalPriceTotal - 1);

  *// 8. OrderCreatedEvent 발행 (Outbox에 저장)*
    OrderCreatedEvent
    event = OrderCreatedEvent.from(
        order,
        userProfile.email(),
        userProfile.username(),
        userAddressHubId.address(),
        InventoryIdempotencyKey,
        DeliveryIdempotencyKey,
        userAddressHubId.userAddressHubId()
    );

    orderOutboxEventService.saveEvent(order.getOrderId(), "ORDER_CREATED", event);

    log.info("=== 주문 생성 완료: orderId={} ===", order.getOrderId());
    return order;

  } catch (Exception
  e
)
  {
    log.error("=== 주문 생성 실패 - 전체 롤백: {} ===", e.getMessage(), e);
    throw new BusinessException(
        OrderErrorCode.ORDER_CREATION_FAILED,
        "주문 생성 중 오류 발생: " + e.getMessage()
    );
  }
}
```

**핵심 포인트**:

- **동기 처리 구간**: 사용자 정보 조회, 재고 선점까지는 동기로 처리
- **실패 시 롤백**: 예외 발생 시 `@Transactional`로 전체 롤백
- **Outbox 패턴**: 이벤트는 DB에 먼저 저장 후 별도 발행

---

### 1-2. 보상 트랜잭션: 주문 취소

### 1️⃣ 주문 취소 요청

**OrderFacade.java - cancelOrder()**

```jsx
@Transactional
public
Order
cancelOrder(CancelOrderCommand
command
)
{
  log.info("=== 주문 취소 시작: orderId={} ===", command.orderId());

  try {
  *// 1. 주문 조회 및 검증*
    orderService.findById(command.orderId());

  *// 2. 주문 취소 처리 (상태 변경: CANCELLED)*
    Order
    order = orderService.cancelOrder(command);
    log.info("주문 취소 완료 - orderId: {}", command.orderId());

  *// 3. 보상 트랜잭션용 멱등성 키 생성*
    String
    InventoryIdempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
        order.getOrderId(),
        Target.INVENTORY,
        OperationType.INCREASE *// 재고 증가*
    );

    String
    DeliveryIdempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
        order.getOrderId(),
        Target.DELIVERY,
        OperationType.CANCEL
    );

  *// 4. OrderCancelledEvent 발행 (Outbox에 저장)*
    OrderCancelledEvent
    event = OrderCancelledEvent.from(
        order,
        order.getUserCouponId(),
        InventoryIdempotencyKey,
        DeliveryIdempotencyKey
    );

    orderOutboxEventService.saveEvent(order.getOrderId(), "ORDER_CANCELLED", event);

    log.info("=== 주문 취소 완료: orderId={} ===", command.orderId());
    return order;

  } catch (Exception
  e
)
  {
    log.error("=== 주문 취소 실패 - 전체 롤백: orderId={}, error={} ===",
        command.orderId(), e.getMessage(), e);
    throw new BusinessException(
        OrderErrorCode.ORDER_CREATION_FAILED,
        "주문 취소 중 오류 발생: " + e.getMessage()
    );
  }
}
```

**핵심 포인트**:

- **상태 변경**: 취소 가능 여부 검증 후 `CANCELLED` 상태로 변경
- **보상 이벤트 발행**: 결제 취소, 쿠폰 복원, 재고 복구 순으로 진행

### 1-3. 이벤트 발행/구독 구조

### Outbox 패턴 구현

**OrderOutboxEvent.java**

java

```jsx
@Entity
@Table(name = "p_order_outbox_events")
public

class OrderOutboxEvent extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID
  id;

  @Column(nullable = false)
  private UUID
  orderId;

  @Column(nullable = false)
  private String
  eventType;
*// ORDER_CREATED, ORDER_CANCELLED*

  @Column(columnDefinition = "TEXT", nullable = false)
  private String
  payload;
*// JSON 직렬화된 이벤트 데이터*

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderOutboxStatus
  status;
*// PENDING, PUBLISHING, PUBLISHED, FAILED*

  @Column(nullable = false)
  private Integer
  retryCount = 0;

  private LocalDateTime
  publishedAt;
  private LocalDateTime
  lastRetryAt;

  *// 지수 백오프 계산 (AWS/Google Cloud 방식)*
  public

  long

  getBackoffMillis() {
    long
    exponentialBackoff = INITIAL_BACKOFF_MILLIS * (1
    L << (this.retryCount - 1)
  )
    ;
    long
    cappedBackoff = Math.min(exponentialBackoff, MAX_BACKOFF_MILLIS);
    double
    jitterFactor = 0.8 + (Math.random() * 0.4);
  *// ±20% Jitter*
    return (long)(cappedBackoff * jitterFactor);
  }

  public boolean

  shouldRetryNow() {
    if (!canRetry()) {
      return false;
    }

    if (this.lastRetryAt == null) {
      return true;
    *// 첫 시도*
    }

    long
    elapsed = Duration.between(this.lastRetryAt, LocalDateTime.now()).toMillis();
    return elapsed >= getBackoffMillis();
  }
}
```

**OrderOutboxEventPublisher.java**

java

```jsx
@Slf4j
@Component
@RequiredArgsConstructor
public

class OrderOutboxEventPublisher {

  private final
  OrderOutboxEventRepository
  orderOutboxEventRepository;
  private final
  OutboxEventTransactionManager
  transactionManager;

*// 5초마다 PENDING 이벤트 발행 시도*
  @Scheduled(fixedDelay = 5000)
  public void

  publishPendingEvents() {
    List < OrderOutboxEvent > pendingEvents =
        orderOutboxEventRepository.findPendingEvents();

    if (pendingEvents.isEmpty()) {
      return;
    }

    log.info("발행 대기 중인 이벤트 {}건 처리 시작", pendingEvents.size());

    for (OrderOutboxEvent event : pendingEvents
  )
    {
      if (!event.shouldRetryNow()) {
        continue;
      *// 백오프 기간이 지나지 않음*
      }
      transactionManager.publishEvent(event);
    }

    log.info("발행 대기 이벤트 처리 완료");
  }

*// 30초마다 PUBLISHING 상태 복구*
  @Scheduled(fixedDelay = 30000)
  @Transactional
  public void

  recoverStuckPublishingEvents() {
    try {
      List < OrderOutboxEvent > stuckEvents =
          orderOutboxEventRepository.findStuckPublishingEvents();

      if (stuckEvents.isEmpty()) {
        return;
      }

      LocalDateTime
      threshold = LocalDateTime.now().minusMinutes(3);

      int
      recoveredCount = 0;
      for (OrderOutboxEvent event : stuckEvents
    )
      {
        if (event.getLastRetryAt() != null
            && event.getLastRetryAt().isBefore(threshold)) {

          event.resetToPending();
          orderOutboxEventRepository.saveAndFlush(event);
          recoveredCount++;

          log.warn("PUBLISHING 상태 이벤트 복구: eventId={}, retryCount={}",
              event.getId(), event.getRetryCount());
        }
      }

      if (recoveredCount > 0) {
        log.info("총 {}건의 PUBLISHING 이벤트 복구 완료", recoveredCount);
      }

    } catch (Exception
    e
  )
    {
      log.error("PUBLISHING 상태 이벤트 복구 중 오류 발생", e);
    }
  }
}
```

**핵심 포인트**:

1. **Outbox 저장**: 이벤트를 먼저 DB에 저장 (트랜잭션 보장)
2. **별도 발행**: 스케줄러가 주기적으로 PENDING 이벤트를 Kafka에 발행
3. **장애 복구**: PUBLISHING 상태로 멈춘 이벤트를 PENDING으로 복구

---

## STEP 2: 실무 패턴 적용

### 2-1. 멱등성 (Idempotency)

### 멱등성이 필요한 이유

네트워크는 완벽하지 않습니다. Kafka는 "적어도 한 번 전달(at-least-once)"을 보장하므로, 같은 메시지가 2번 이상 도착할 수 있습니다. 예를 들어:

- 재고 차감 이벤트가 2번 도착 → 재고가 2번 차감됨
- 환불 이벤트가 2번 도착 → 고객에게 돈이 2번 들어감

### 구현 방법 1: 이벤트 ID 기반 중복 체크

**PaymentEventListener.java의 멱등성 체크**

```jsx
@KafkaHandler
@Transactional
public
void handlePaymentApproved(@Payload
PaymentApprovedEvent
event,
...)
{
  Order
  order = orderService.findById(event.orderId());

*// 멱등성 체크: 이미 PAID 상태면 무시*
  if (order.getOrderStatus() == OrderStatus.PAID) {
    log.info("이미 처리된 결제 완료 이벤트 - orderId: {}", event.orderId());
    acknowledgment.acknowledge();
    return;
  *// 중복 처리 방지*
  }

  order.changeStatus(OrderStatus.PAID);
  orderRepository.save(order);
}
```

### 구현 방법 2: 멱등성 키 기반 외부 API 호출

**OrderOutboundRequest.java**

```jsx
@Entity
@Table(name = "p_order_outbound_request")
public

class OrderOutboundRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID
  requestId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order
  order;

  @Column(name = "idempotency_key", nullable = false, unique = true)
  private String
  idempotencyKey;
*// UNIQUE 제약조건으로 중복 방지*

  @Enumerated(EnumType.STRING)
  private Target
  target;
*// INVENTORY, DELIVERY*

  @Enumerated(EnumType.STRING)
  private OperationType
  operation;
*// INCREASE, DECREASE, MAKING, CANCEL*
}
```

**멱등성 키 생성 규칙**

```jsx
public
String
generateIdempotencyKey(
    UUID
orderId,
    Target
target,
    OperationType
operationType
)
{
  long
  timestamp = System.currentTimeMillis();

  return String.format("%s-%s-%s-%d",
      orderId,
      target.name(),
      operationType.name(),
      timestamp
  );
}

*// 예시: 550e8400-e29b-41d4-a716-446655440000-INVENTORY-DECREASE-1703001234567*
```

**멱등성 키 사용 예시 (재고 선점)**

```jsx
@Transactional
public
void reserveProduct(Order
order
)
{
*// 1. 멱등성 키 생성*
  String
  idempotencyKey = orderOutboundRequestService.generateIdempotencyKey(
      order.getOrderId(),
      Target.INVENTORY,
      OperationType.DECREASE
  );

*// 2. 이미 처리했는지 확인*
  if (orderOutboundRequestService.existsByIdempotencyKey(idempotencyKey)) {
    log.info("해당 주문의 재고 차감 멱등키 존재. orderId: {}", order.getOrderId());
    return;
  *// 중복 요청 무시*
  }

*// 3. 실제 재고 차감 API 호출*
  InventoryReservationRequest
  request = new InventoryReservationRequest(
      order.getOrderId(),
      idempotencyKey,
      reservationItems
  );
  inventoryClient.reserveProduct(request);

*// 4. 멱등성 키 저장*
  orderOutboundRequestService.save(new CreateOrderOutboundRequestCommand(
      order.getOrderId(),
      idempotencyKey,
      Target.INVENTORY,
      OperationType.DECREASE
  ));
}
```

**핵심 포인트**:

1. **DB 제약조건**: `idempotency_key`에 UNIQUE 제약 → 중복 INSERT 차단
2. **트랜잭션**: `@Transactional`로 조회-저장을 원자적으로 처리
3. **타임스탬프 포함**: 같은 주문의 여러 작업(재고 차감/복원)을 구분

---

### 2-2. 재시도 & 지수 백오프

### 왜 필요한가?

외부 API(결제 게이트웨이, 재고 서비스)는 일시적으로 실패할 수 있습니다:

- 네트워크 일시적 단절
- 서버 과부하
- DB 락 대기 타임아웃

이런 "일시적 실패"는 잠시 후 재시도하면 성공하는 경우가 많습니다.

### 지수 백오프 전략

실패할 때마다 재시도 간격을 2배씩 늘려가는 방식입니다:

```jsx
1
초 → 2
초 → 4
초 → 8
초 → ... → 최대
5
분
```

**OrderOutboxEvent.java - 백오프 계산**

```jsx
private
static
final
int
MAX_RETRY_COUNT = 20;
*// 최대 20회*
private
static
final
long
INITIAL_BACKOFF_MILLIS = 1000
L;
*// 초기 1초*
private
static
final
long
MAX_BACKOFF_MILLIS = 300000
L;
*// 최대 5분*

public
long
getBackoffMillis()
{
*// Exponential: 1초 * 2^(retryCount-1)*
  long
  exponentialBackoff = INITIAL_BACKOFF_MILLIS * (1
  L << (this.retryCount - 1)
)
  ;

*// 최댓값 제한*
  long
  cappedBackoff = Math.min(exponentialBackoff, MAX_BACKOFF_MILLIS);

*// Jitter 추가 (0~20% 랜덤 변동)*
  double
  jitterFactor = 0.8 + (Math.random() * 0.4);
*// 0.8 ~ 1.2*
  return (long)(cappedBackoff * jitterFactor);
}
```

**재시도 판단 로직**

```jsx
public
boolean
shouldRetryNow()
{
  if (!canRetry()) {
    return false;
  *// PUBLISHED 또는 FAILED 상태면 재시도 안 함*
  }

*// 첫 시도이거나 backoff 시간이 지났으면 재시도*
  if (this.lastRetryAt == null) {
    return true;
  }

  long
  elapsed = Duration.between(this.lastRetryAt, LocalDateTime.now()).toMillis();
  return elapsed >= getBackoffMillis();
}
```

### Kafka 레벨 재시도

**KafkaConfig.java**

```jsx
@Bean
public
ConcurrentKafkaListenerContainerFactory < String, Object > kafkaListenerContainerFactory()
{
  ConcurrentKafkaListenerContainerFactory < String, Object > factory =
      new ConcurrentKafkaListenerContainerFactory < > ();

  factory.setConsumerFactory(consumerFactory());
  factory.setConcurrency(3);
*// 동시 처리 쓰레드 3개*
  factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
  factory.setCommonErrorHandler(errorHandler());
*// 재시도 핸들러 등록*

  return factory;
}

@Bean
public
DefaultErrorHandler
errorHandler()
{
*// 최대 3번 재시도, 초기 1초 간격*
  FixedBackOff
  fixedBackOff = new FixedBackOff(1000
  L, 3
  L
)
  ;
  return new DefaultErrorHandler(fixedBackOff);
}
```

**@RetryableTopic 어노테이션**

java

```java
@RetryableTopic(
    attempts = "3",  *// 최대 3회 재시도*
backoff =@Backoff(delay = 1000L, multiplier = 2.0, maxDelay = 4000L),*// 1초 → 2초 → 4초*
autoCreateTopics ="true",
include =Exception .class,
topicSuffixingStrategy =TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
)

@KafkaListener(
    topics = "payment.topic",
    groupId = "order-service-group"
)
public class PaymentEventListener {
    *// ...*
}
```

**재시도 흐름**

```jsx
1
차
시도
실패(payment.topic)
    ↓ 1
초
대기
2
차
시도
실패(payment.topic - retry - 0)
    ↓ 2
초
대기
3
차
시도
실패(payment.topic - retry - 1)
    ↓
DLT
이동(payment.topic - dlt)
```

### DLT(Dead Letter Topic) 처리

모든 재시도가 실패하면 DLT로 이동합니다.

**PaymentEventListener.java - DLT Handler**

```jsx
@DltHandler
public
void handlePaymentDlt(
    @Payload
Object
event,
@Header(KafkaHeaders.RECEIVED_TOPIC)
String
topic,
@Header(KafkaHeaders.EXCEPTION_MESSAGE)
String
exceptionMessage
)
{

  log.error("========================================");
  log.error("⚠️ DLT 도착: Payment Event");
  log.error("⚠️ 수동 처리가 필요합니다!");
  log.error("========================================");
  log.error("eventType={}, error={}", event.getClass().getSimpleName(), exceptionMessage);

*// 실무에서는:// 1. Slack/이메일 알림 발송// 2. 관리자 대시보드에 알림 표시// 3. 수동 재처리 대기 큐에 적재*
}
```

**재시도 vs DLT 전략**

| 오류 유형                              | 재시도 전략           | 이유              |
|------------------------------------|------------------|-----------------|
| **일시적 장애** (네트워크, 서버 과부하)          | 많이 재시도 (20회)     | 시간이 지나면 복구됨     |
| **비즈니스 로직 오류** (재고 부족, 유효하지 않은 쿠폰) | 적게 재시도 (3회)      | 재시도해도 똑같이 실패    |
| **데이터 오류** (잘못된 JSON, 필수 필드 누락)    | 재시도 안 함 (즉시 DLT) | 재시도해도 절대 성공 안 함 |

---

### 2-3. 트랜잭션 범위 설정

### 문제: 이벤트 발행은 트랜잭션 안에 포함되어야 하나?

**안티패턴 (트랜잭션 커밋 후 이벤트 발행)**

java

```jsx
@Transactional
public
void createOrder()
{
  Order
  order = new Order();
  orderRepository.save(order);
*// 트랜잭션 커밋 ← 여기까지만 보장*
}
*// 트랜잭션 밖에서 이벤트 발행 ← 실패하면?*
kafkaTemplate.send("order.created", new OrderCreatedEvent(order.getId()));
```

**문제점**:

- 주문은 DB에 저장됐는데 이벤트 발행 실패 → 데이터 불일치
- 이벤트는 발행됐는데 트랜잭션 롤백 → 존재하지 않는 주문 ID

### 해결: Transactional Outbox Pattern

**OrderOutboxEventService.java**

```jsx
@Transactional
public
void saveEvent(UUID
orderId, String
eventType, Object
eventData
)
{
  try {
  *// 1. 이벤트 데이터를 JSON으로 직렬화*
    String
    payload = objectMapper.writeValueAsString(eventData);

  *// 2. Outbox 테이블에 저장 (트랜잭션 내)*
    OrderOutboxEvent
    outboxEvent = OrderOutboxEvent.create(
        orderId,
        eventType,
        payload
    );

    orderOutboxEventRepository.save(outboxEvent);

    log.info("Outbox 이벤트 저장 성공: eventType={}, orderId={}", eventType, orderId);

  } catch (JsonProcessingException
  e
)
  {
    log.error("이벤트 직렬화 실패: eventType={}, orderId={}", eventType, orderId, e);
    throw new BusinessException(
        OrderErrorCode.ORDER_CREATION_FAILED,
        "이벤트 데이터 직렬화 실패: " + e.getMessage()
    );
  }
}
```

**OutboxEventTransactionManager.java**

```jsx
@Transactional(propagation = Propagation.REQUIRES_NEW)
public
void publishEvent(OrderOutboxEvent
event
)
{
  try {
  *// 1단계: PUBLISHING으로 상태 변경 및 즉시 커밋*
    event.markAsPublishing();
    orderOutboxEventRepository.saveAndFlush(event);

  *// 2단계: Kafka 발행*
    kafkaPublisher.publishToKafka(event);

  *// 3단계: PUBLISHED로 상태 변경 및 즉시 커밋*
    event.markAsPublished();
    orderOutboxEventRepository.saveAndFlush(event);

  } catch (Exception
  e
)
  {
    log.error("이벤트 발행 실패: eventId={}", event.getId(), e);
    handlePublishFailure(event, e);
  }
}
```

**핵심 포인트**:

1. **이벤트를 먼저 DB에 저장** (비즈니스 로직과 같은 트랜잭션)
2. **별도 스케줄러가 발행** (트랜잭션 밖)
3. **REQUIRES_NEW**: 각 발행 시도를 독립적인 트랜잭션으로 처리

---

## STEP 3: 테스트 및 회고

### 3-1. 테스트 시나리오

### 테스트 환경 구성

**1. User DB 데이터**

```jsx
*
-1.
사용자
생성(userId
:
11, GOLD
등급
)*
INSERT
INTO
p_users(user_id, name, email, role, status, ...)
VALUES(11, 'user1', 'customer1@example.com', 'CUSTOMER', 'APPROVED', ...);
*
-2.
배송지
주소 *
INSERT
INTO
p_user_address(user_address_id, user_id, hub_id, address, ...)
VALUES('00000000-0000-0000-0009-000000000007', 11, '00000000-0000-0000-0002-000000000001',
    '서울특별시 마포구 월드컵북로', ...);
*
-3.
사용자
등급(GOLD - 10 % 할인) *
INSERT
INTO
p_user_grades(user_grade_id, user_id, grade_name, ...)
VALUES('00000000-0000-0000-0010-000000000001', 11, 'GOLD', ...);
```

**2. Promotion DB 데이터**

```jsx
*
-1.
등급별
할인율 *
INSERT
INTO
p_grades(grade_id, grade_name, benefit_discount_rate, ...)
VALUES('...', 'NONE', 0, ...), ('...', 'BRONZE', 4,
...),
('...', 'SILVER', 7,
...),
('...', 'GOLD', 10,
...)
;
*
-2.
쿠폰
생성(정액
10, 000
원
할인
)*
INSERT
INTO
p_coupons(coupon_id, name, discount_type, discount_value, ...)
VALUES('550e8400-e29b-41d4-a716-446655440029', '테스트 10,000원 할인 쿠폰', 'FIXED', 10000, ...);
*
-3.
사용자
쿠폰
발급 *
INSERT
INTO
p_user_coupons(user_coupon_id, coupon_id, user_id, status, ...)
VALUES('550e8400-e29b-41d4-a716-446655440030', '550e8400-e29b-41d4-a716-446655440029', 11, 'READY',
    ...);
```

**3. Hub DB 데이터**

```jsx
*
-1.
서울
허브 *
INSERT
INTO
p_hubs(hub_id, name, address, ...)
VALUES('00000000-0000-0000-0002-000000000001', '서울 허브', '서울시 강남구 테헤란로 123', ...);
*
-2.
공급업체(삼성전자) *
INSERT
INTO
p_companies(company_id, hub_id, type, name, ...)
VALUES('00000000-0000-0000-0004-000000000001', '00000000-0000-0000-0002-000000000001', 'SUPPLIER',
    '삼성전자', ...);
*
-3.
상품
2
개 *
INSERT
INTO
p_products(product_id, company_id, name, ...)
VALUES('00000000-0000-0000-0005-000000000001', '00000000-0000-0000-0004-000000000001',
    'Galaxy S24 Ultra',
    ...), ('00000000-0000-0000-0005-000000000002', '00000000-0000-0000-0004-000000000001', 'QLED TV 75인치',
...)
;
*
-4.
재고
데이터 *
INSERT
INTO
p_inventory(inventory_id, product_id, hub_id, quantity, ...)
VALUES(gen_random_uuid(), '00000000-0000-0000-0005-000000000001',
    '00000000-0000-0000-0002-000000000001', 100,
    ...), (gen_random_uuid(), '00000000-0000-0000-0005-000000000002', '00000000-0000-0000-0002-000000000001', 50,
...)
;
```

---

### 테스트 1: 주문 생성 (정상 흐름)

- 로그인된 사용자 (user1 / 1234)
- GOLD 등급 (10% 할인)
- 10,000원 할인 쿠폰 보유
- 충분한 재고 (Galaxy S24 Ultra: 100개, QLED TV: 50개)

```jsx
POST / v1 / orders
Authorization: Bearer
{
  JWT_TOKEN
}

{
  "userId"
:
  11,
      "supplierId"
:
  "00000000-0000-0000-0004-000000000001",
      "userCouponId"
:
  "550e8400-e29b-41d4-a716-446655440030",
      "comment"
:
  "빠른 배송 부탁드립니다.",
      "addressId"
:
  "00000000-0000-0000-0009-000000000007",
      "deliveryLatitude"
:
  37.5665,
      "deliveryLongitude"
:
  126.9016,
      "orderItems"
:
  [
    {
      "productId": "00000000-0000-0000-0005-000000000001",
      "productName": "Galaxy S24 Ultra",
      "hubId": "00000000-0000-0000-0002-000000000001",
      "quantity": 2,
      "price": 1500000
    },
    {
      "productId": "00000000-0000-0000-0005-000000000002",
      "productName": "QLED TV 75인치",
      "hubId": "00000000-0000-0000-0002-000000000001",
      "quantity": 1,
      "price": 2500000
    }
  ]
}
```

**Then**:

**1단계: 주문 생성 (동기)**

```jsx
✅ Order
테이블에
레코드
생성(PENDING
상태
)
✅ 재고
선점
요청
성공(Inventory
서비스
)
✅ 원가
계산: 1, 500, 000 * 2 + 2, 500, 000 * 1 = 5, 500, 000
원
✅ 할인
금액
업데이트(쿠폰
:
10, 000
원, 등급
:
1
원
)
✅ 최종
금액: 5, 489, 999
원
✅ OrderCreatedEvent를
Outbox에
저장
```

**2단계: 비동기 이벤트 체인**

```jsx
✅ OrderOutboxEventPublisher가
5
초
이내에
Kafka에
발행
✅ Payment
서비스가
결제
처리 → PaymentApprovedEvent
발행
✅ Order
서비스가
상태
변경: PENDING → PAID
✅ Promotion
서비스가
쿠폰
확정 → CouponUsedEvent
발행
✅ Order
서비스가
상태
변경: PAID → COUPON_CONFIRMED
✅ Inventory
서비스가
재고
차감 → InventoryDeductedEvent
발행
✅ Order
서비스가
상태
변경: COUPON_CONFIRMED → STOCK_CONFIRMED1
```

**실제 로그 확인**:

```jsx
2024 - 12 - 16
10
:
30
:
00 [OrderFacade]
주문
생성
완료 - orderId
:
550e8400 -
...
2024 - 12 - 16
10
:
30
:
00 [OrderOutboxEventPublisher]
발행
대기
중인
이벤트
1
건
처리
시작
2024 - 12 - 16
10
:
30
:
01 [OutboxEventKafkaPublisher]
Kafka
발행
성공: topic = order.topic, partition = 0, offset = 123
2024 - 12 - 16
10
:
30
:
02 [PaymentEventListener]
결제
완료
이벤트
수신: orderId = 550e8400 -
...
2024 - 12 - 16
10
:
30
:
03 [InventoryEventListener]
재고
처리
이벤트
수신: orderId = 550e8400 -
...
```

---

### 테스트 2: 주문 취소 (보상 트랜잭션)

- 재고 차감까지 완료된 주문 (STOCK_CONFIRMED 상태)
- 배송 생성 이벤트 발행 코드를 주석 처리하여 흐름 중단

**배송 생성 차단 코드 (InventoryEventListener.java)**:

```jsx
*// 배송 생성 이벤트 발행 주석 처리// Delivery->Order: 재고 차감 이벤트// deliveryEventPublisher.publishInventoryDeducted(event);*
```

주문 취소 API 호출

```jsx
POST / v1 / orders / {orderId}
/cancel
Authorization: Bearer
{
  JWT_TOKEN
}
X - User - Id
:
11

{
  "cancelReason"
:
  "고객 변심",
      "cancelType"
:
  "USER_REQUEST"
}
```

**1단계: 주문 취소 처리**

```jsx
✅ OrderCancellation
테이블에
레코드
생성
✅ Order
상태
변경: STOCK_CONFIRMED → CANCELLED
✅ OrderCancelledEvent를
Outbox에
저장
```

**2단계: 보상 트랜잭션 체인**

```jsx
✅ Inventory
서비스가
재고
복원 → InventoryReplenishedEvent
발행
✅ Order
서비스가
재고
복원
확인
로그
출력
✅ Promotion
서비스가
쿠폰
복원 → CouponCancelledEvent
발행
✅ Order
서비스가
쿠폰
복원
확인
로그
출력
✅ Payment
서비스가
결제
취소 → PaymentCancelledEvent
발행
✅ Order
서비스가
결제
취소
확인
로그
출력
```

**실제 로그 확인**:

```jsx
2024 - 12 - 16
10
:
35
:
00 [OrderFacade]
주문
취소
시작: orderId = 550e8400 -
...
2024 - 12 - 16
10
:
35
:
00 [OrderFacade]
주문
취소
완료 - orderId
:
550e8400 -
...
2024 - 12 - 16
10
:
35
:
01 [InventoryEventListener]
결제
취소
이벤트
수신: orderId = 550e8400 -
...
2024 - 12 - 16
10
:
35
:
01 [InventoryEventListener]
결제
취소
확인
완료: orderId = 550e8400 -
...
2024 - 12 - 16
10
:
35
:
02 [CouponEventListener]
쿠폰
취소
이벤트
수신: orderId = 550e8400 -
...
2024 - 12 - 16
10
:
35
:
02 [CouponEventListener]
쿠폰
취소
확인
완료: orderId = 550e8400 -
...
```

---

### 3-2. 6주차 설계 vs 실제 구현

### 설계 단계에서 계획한 것

**1. 이벤트 기반 코레오그래피 패턴**

- 중앙 오케스트레이터 없이 각 서비스가 이벤트를 듣고 다음 동작 수행
- Order → Payment → Inventory → Delivery 순서로 이벤트 체인 구성

**2. 보상 트랜잭션은 역순으로 실행**

```jsx
정방향: Order → Payment → Promotion → Inventory → Delivery
역방향: Delivery → Inventory → Payment → Order
```

**3. 멱등성 키로 중복 처리 방지**

- 각 외부 API 호출 시 고유한 idempotency key 전달
- 같은 키로 2번 호출해도 1번만 처리

---

### 실제 구현에서 달라진 점

**1. Outbox 패턴 도입**

**설계 시 생각**:

```jsx
@Transactional
public
void createOrder()
{
  Order
  order = orderRepository.save(new Order(...));
  kafkaTemplate.send("order.created", new OrderCreatedEvent(order.getId()));
*// 트랜잭션 커밋과 이벤트 발행이 원자적이지 않음 ❌*
}
```

**실제 구현**:

```jsx
@Transactional
public
void createOrder()
{
  Order
  order = orderRepository.save(new Order(...));

  orderOutboxEventService.saveEvent(
      order.getOrderId(),
      "ORDER_CREATED",
      new OrderCreatedEvent(...)
  );
}
```

**왜 바뀌었나**:

- **원자성 보장 불가**: 트랜잭션 커밋과 Kafka 발행 사이에 장애 발생 가능
- **메시지 손실 위험**: 트랜잭션은 커밋됐는데 Kafka 발행 실패하면?
- **해결**: Outbox 테이블에 먼저 저장 → 스케줄러가 안전하게 발행

---

**4. 지수 백오프 전략 구체화**

**설계 시 생각**:

```jsx
실패하면
재시도한다(구체적인
전략
없음
)
```

**실제 구현**:

```jsx
public
long
getBackoffMillis()
{
*// Exponential: 1초 * 2^(retryCount-1)*
  long
  exponentialBackoff = INITIAL_BACKOFF_MILLIS * (1
  L << (this.retryCount - 1)
)
  ;

*// Cap: 최대 5분*
  long
  cappedBackoff = Math.min(exponentialBackoff, MAX_BACKOFF_MILLIS);

*// Jitter: ±20% 랜덤 변동*
  double
  jitterFactor = 0.8 + (Math.random() * 0.4);
  return (long)(cappedBackoff * jitterFactor);
}

*// 재시도 간격: 1초 → 2초 → 4초 → 8초 → ... → 5분(최대)*
```

**왜 이렇게 했나**:

- **참고**: AWS, Google Cloud의 권장 재시도 전략
- **Exponential Backoff**: 재시도 간격을 점점 늘려서 서버 부하 감소
- **Cap (최댓값 제한)**: 무한정 늘어나는 것 방지
- **Jitter (지터)**: 여러 클라이언트가 동시에 재시도하는 것 방지 (Thundering Herd 문제)

---

### 3-3. 구현하면서 어려웠던 점

### 1. 트랜잭션 범위 설정

**문제**: 이벤트 발행은 트랜잭션 안에 포함되어야 하나?

**시행착오**:

```jsx
*// 첫 번째 시도: 트랜잭션 안에서 Kafka 발행*
@Transactional
public
void createOrder()
{
  orderRepository.save(order);
  kafkaTemplate.send("order.created", event);
*// ❌ 트랜잭션이 커밋되기 전에 발행됨*
}

*// 두 번째 시도: 트랜잭션 밖에서 Kafka 발행*
public
void createOrder()
{
  saveOrderInTransaction(order);
*// 트랜잭션 커밋*
  kafkaTemplate.send("order.created", event);
*// ❌ 발행 실패 시 메시지 손실*
}
```

**해결 과정**:

1. "Kafka와 트랜잭션" 관련 자료 검색
2. Transactional Outbox Pattern 발견
3. 이벤트를 먼저 DB에 저장하는 방식으로 변경

**최종 구현**:

java

```jsx
@Transactional
public
void createOrder()
{
  orderRepository.save(order);

*// Outbox 테이블에 이벤트 저장 (같은 트랜잭션)*
  orderOutboxEventService.saveEvent(order.getId(), "ORDER_CREATED", event);

*// 트랜잭션 커밋 후 별도 스케줄러가 발행 ✅*
}
```

**배운 점**:

- **트랜잭션과 메시징을 함께 사용할 때는 Outbox 패턴 필수**
- **at-least-once 전달 보장**: 이벤트는 중복될 수 있지만 손실되지 않음

---

### 2. 재시도 전략 설계

**문제**:

- 재시도를 너무 많이 하면? → 장애 중인 서버에 부하만 증가
- 재시도를 너무 적게 하면? → 일시적 장애에서 복구 실패

**시행착오**:

- 재시도 100번 시도
- 전 시간 x2로 최대 2시간 넘는 대기시간을 가짐

**해결 과정**:

1. "retry backoff strategy" 검색
2. AWS SDK의 지수 백오프 구현 발견
3. AWS 문서에서 "Exponential Backoff + Jitter" 권장사항 확인
4. Google Cloud 문서에서도 동일한 전략 확인

**최종 구현**:

```jsx
public
long
getBackoffMillis()
{
*// 1초 → 2초 → 4초 → 8초 → 16초 → ... → 최대 5분*
  long
  exponentialBackoff = INITIAL_BACKOFF_MILLIS * (1
  L << (this.retryCount - 1)
)
  ;
  long
  cappedBackoff = Math.min(exponentialBackoff, MAX_BACKOFF_MILLIS);

*// Jitter: 여러 클라이언트가 동시에 재시도하는 것 방지*
  double
  jitterFactor = 0.8 + (Math.random() * 0.4);
  return (long)(cappedBackoff * jitterFactor);
}
```

**배운 점**:

- **지수 백오프**: 재시도 간격을 점진적으로 늘려서 서버 부하 감소
- **Jitter**: 모든 클라이언트가 동시에 재시도하는 "Thundering Herd" 문제 방지
- **최댓값 제한**: 무한정 대기하지 않도록 상한선 설정 (5분)

---

---

### 3. Kafka 메시지 역직렬화 실패

**문제**:

```jsx
Caused
by: com.fasterxml.jackson.databind.exc.InvalidDefinitionException
:
Cannot
construct
instance
of`PaymentApprovedEvent`
(no
Creators, like
default
constructor, exist
)
```

**원인**:

```jsx
*// Java Record는 기본 생성자가 없음*
public
record
PaymentApprovedEvent(
    UUID
orderId,
    int
paidAmount,
    String
paymentMethod
)
{
}
```

**해결 과정**:

1. Jackson 설정 검색 → `ParameterNamesModule` 발견
2. Kafka Consumer 설정에 추가

**최종 구현**:

```jsx
@Configuration
public

class JacksonConfig {

  @Bean
  @Primary
  public ObjectMapper

  objectMapper() {
    ObjectMapper
    mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
  *// LocalDateTime 지원*
    mapper.registerModule(new ParameterNamesModule());
  *// Record 지원 ← 추가*
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }
}
```

**배운 점**:

- **Java Record 역직렬화**: `ParameterNamesModule` 필수
- **컴파일 옵션**: `parameters` 플래그 필요 (Spring Boot는 기본 활성화)
- **Jackson 모듈**: 다양한 모듈로 확장 가능 (JavaTimeModule, Jdk8Module 등)

---

### 3-4. 실무에서 진짜 사용한다면?

### 1. Outbox 테이블 무한 증가 문제

**현재 상태**:

```jsx
*// 모든 이벤트를 영구 보관*
@Entity
@Table(name = "p_order_outbox_events")
public

class OrderOutboxEvent {
*// PUBLISHED 상태가 되어도 삭제하지 않음*
}
```

**문제점**:

- Outbox 테이블이 계속 커짐 (월 100만 건 발행 시 연간 1,200만 건)
- 조회 성능 저하 (PENDING 이벤트 조회 시 PUBLISHED도 함께 스캔)
- 스토리지 비용 증가

**실무 개선안**:

**1) TTL(Time To Live) 적용**:

```jsx
@Scheduled(cron = "0 0 3 * * *")  *// 매일 새벽 3시*
@Transactional
public
void cleanupOldEvents()
{
  LocalDateTime
  threshold = LocalDateTime.now().minusDays(30);

*// PUBLISHED 상태이고 30일 지난 이벤트 삭제*
  int
  deleted = orderOutboxEventRepository.deleteByStatusAndPublishedAtBefore(
      OrderOutboxStatus.PUBLISHED,
      threshold
  );

  log.info("Outbox 정리 완료: {}건 삭제", deleted);
}
```

---

### 2. DLT 모니터링 자동화

**현재 상태**:

```jsx
@DltHandler
public
void handlePaymentDlt(@Payload
Object
event,
...)
{
  log.error("⚠️ DLT 도착: Payment Event");
*// 로그만 출력 ❌*
}
```

**문제점**:

- 운영팀이 직접 로그를 확인해야 함
- 실시간 알림 없음
- 재처리 프로세스가 수동

**실무 개선안**:

**1) Slack 알림 자동 발송**:

```jsx
@DltHandler
public
void handlePaymentDlt(@Payload
Object
event,
...)
{
  log.error("========================================");
  log.error("⚠️ DLT 도착: Payment Event");
  log.error("========================================");

*// Slack 알림 발송 로직 존재*
}
```

**2) 관리자 대시보드**:

```jsx
@RestController
@RequestMapping("/admin/dlt")
public

class DltManagementController {

*// DLT 이벤트 목록 조회*
  @GetMapping
  public List<DltEventResponse>

  getDltEvents() {
    return dltEventRepository.findAll().stream()
    .map(DltEventResponse::from)
    .toList();
  }

*// 수동 재처리*
  @PostMapping("/{eventId}/retry")
  public void

  retryEvent(@PathVariable UUID

  eventId
) {
  DltEvent
  event = dltEventRepository.findById(eventId)
  .orElseThrow();

  *// Outbox에 다시 저장 (PENDING 상태로)*
  OrderOutboxEvent

  retryEvent = OrderOutboxEvent.create(
      event.getOrderId(),
      event.getEventType(),
      event.getPayload()
  );
  orderOutboxEventRepository
.

  save(retryEvent);

  *// DLT에서 제거*
  dltEventRepository

.

  delete(event);
}
}
```

**3) 자동 재처리 큐**:

```jsx
@Scheduled(fixedDelay = 3600000)  *// 1시간마다*
@Transactional
public
void autoRetryDltEvents()
{
  List < DltEvent > retryableEvents = dltEventRepository
  .findByRetryCountLessThan(3);
*// 3회 미만만 자동 재시도*

  for (DltEvent event : retryableEvents
)
  {
    try {
    *// 자동 재처리 시도*
      eventProcessor.process(event);

    *// 성공 시 DLT에서 제거*
      dltEventRepository.delete(event);

    } catch (Exception
    e
  )
    {
    *// 실패 시 재시도 횟수 증가*
      event.incrementRetryCount();
      dltEventRepository.save(event);
    }
  }
}
```

**효과**:

- 실시간 장애 감지 (Slack 알림)
- 빠른 대응 (관리자 대시보드)
- 일부 이벤트 자동 복구

---

### 마무리

- kafka를 처음 머리로만 이해했을 때는 이벤트 발행자 - 이벤트 모음집 - 이벤트 구독자 정도로만 생각했는데 생각보다 복잡했다.
- 설정해줘야 할것이 많지만 초반에 설정을 잘해준다면 뒤에는 편하게 사용할 수 있는 것 같다.
- 가장 문제가 많이 생겼던것은 역직렬화 문제였다.
    - JacksonConfig를 추가하고 TypeMapping에 조금 더 신경을 썻더니 모두 진행되었다.
    - 그리고 이벤트 메세지 형식을 발행자와 구독자 같은 형식을 가지도록 설정했다.
- DLT에 대한 처리가 조금 아쉽지만 남은 기간동안 DLT에 대해 조금 더 개선해볼 예정이다.