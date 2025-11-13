## 이벤트 객체 규칙

- 불변성을 보장하는 `record` 로 이벤트 객체를 만들어야 한다

</br>

## 이벤트 네이밍 규칙

- 과거형 사용 (ex. OrderCreatedEvent, PaymentCompletedEvent)
  - 이미 완료된 이벤트로 표현하도록 해야한다
- `{도메인명}{과거형동작}{Event}`

</br>

## 이벤트 리스너 작성 가이드

- Class 네이밍
  - `{도메인}EventListener`
    - 도메인 : 해당 이벤트를 구독하려는 도메인 명
- 메서드 명
  - 각 도메인이 수행하는 행위 명
    - ex. `pay()`, `deduct()`

</br>

## 비동기 처리 가이드

- 언제 비동기 처리를 수행할지
  - 순서와 상관없이 수행되어야 할때
  - 즉각적인 응답이 필요없을때
  - 대용량 데이터를 처리해야할 때
- 비동기에 대한 예외처리

  - 스프링에서 제공해주는 `AsyncUncaughtExceptionHandler`로 비동기 예외 처리

  ```java
  @Configuration
  @EnableAsync
  public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        // 비동기 설정
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
  }
  ```

- 비동기 스레드풀 측정 (이론상)
  - 실제 외부 API 의 대기 시간을 측정해야함
  - 만들어진 `PerformanceMonitor` 를 사용하여 계산할 수 있음
    - CPU 코어 수 \* (1 + (대기 시간 / 작업 시간))
    - ex) ((3.42/1.11) + 1) \* 12 → 12 코어 / 3.42 ms 대기시간 / 1.11 ms 작업시간
      ```java
      : ========== 성능 통계 ==========
      2025-11-12T21:38:59.414+09:00  INFO 62156 --- [order-service] [    Test worker] c.klp.logistics.util.PerformanceMonitor  : 메서드: OrderService.createOrder
      2025-11-12T21:38:59.414+09:00  INFO 62156 --- [order-service] [    Test worker] c.klp.logistics.util.PerformanceMonitor  :   호출 횟수: 100
      2025-11-12T21:38:59.415+09:00  INFO 62156 --- [order-service] [    Test worker] c.klp.logistics.util.PerformanceMonitor  :   평균 전체 시간: 4.53ms
      2025-11-12T21:38:59.415+09:00  INFO 62156 --- [order-service] [    Test worker] c.klp.logistics.util.PerformanceMonitor  :   평균 CPU 시간: 1.11ms
      2025-11-12T21:38:59.415+09:00  INFO 62156 --- [order-service] [    Test worker] c.klp.logistics.util.PerformanceMonitor  :   평균 대기 시간: 3.42ms
      2025-11-12T21:38:59.415+09:00  INFO 62156 --- [order-service] [    Test worker] c.klp.logistics.util.PerformanceMonitor  :   스레드 풀 권장 크기: 49
      ```

</br>

## 테스트 가이드

### 이벤트 객체 테스트

- 이벤트 객체는 올바른 값에 대한 검증을 해야한다
  - 필수값 혹은 유효한 필드값

### 이벤트 발행 객체의 테스트

- 단위 테스트 최소 2개 필요하다
  - 최소 예상가능한 작동이 되는지 확인을 위해 존재함
  - `verify` 를 통해 이벤트가 실제로 발행되는지 검증해야한다
    - 정상 시나리오의 이벤트 발행 테스트
    - 실패 시나리오의 이벤트 미발행 테스트

### EventListener 테스트

- 통합 테스트 최소 1개 필요하다
  - 이벤트 발행을 통해 실제 예상했던 메서드가 호출이 되었는지 테스트한다

### TransactionalEventListener 테스트

- 트랜잭션과 관련된 이벤트 발행(`TransactionalEventListener`)은 통합 테스트 최소 2개 이상이 필요하다
  - 상위 트랜잭션의 커밋 성공 시나리오
  - 상위 트랜잭션의 커밋 실패 시나리오

### Async 테스트

- `Awaitility` 를 사용한다

### Event 발행 객체 테스트 예시

```java
@Test
@DisplayName("주문을 생성하면 OrderCreatedEvent 를 발행한다")
void publishOrderCreatedEvent() {
    OrderCreateCommand command = new OrderCreateCommand(
      supplierId,
      customerId,
      List.of(product),
      comments
    );
    Order order = mock(Order.class);
    when(orderRepository.save(any())).thenReturn(order);
    when(order.getOrderId()).thenReturn(UUID.randomUUID());

    orderService.createOrder(command);

    verify(eventPublisher, times(1)).publishEvent(any(OrderCreatedEvent.class));
}
```

### @EventListener 테스트 예시

```java
@Test
@DisplayName("주문 생성 이벤트를 구독하여 deduct() 를 수행할 수 있다")
void subscribeOrderCreatedEvent() {
    OrderCreateCommand command = new OrderCreateCommand(
        supplierId,
        customerId,
        List.of(product),
        comments
    );

    orderService.createOrder(command);

    verify(productEventListener, times(1)).deduct(any(OrderCreatedEvent.class));
}
```

### @TransactionalEventListener 테스트 예시

```java
@Test
@DisplayName("주문 생성에 실패한 경우 재고 차감이 시도되지 않는다")
void throwOrderCreatedEvent() {
    OrderCreateCommand invalidCommand = new OrderCreateCommand(
        supplierId,
        customerId,
        List.of(),
        comments
    );

    assertThatThrownBy(
        () -> orderService.createOrder(invalidCommand)
    ).isInstanceOf(RuntimeException.class);
    verify(productEventListener, never()).deduct(any(OrderCreatedEvent.class));
}

@Test
@DisplayName("주문 생성에 성공한 경우 재고차감이 시도한다")
void tryPaySuccess() {
    OrderCreateCommand command = new OrderCreateCommand(
        supplierId,
        customerId,
        List.of(product),
        comments
    );

    orderService.createOrder(command);

    verify(productEventListener, times(1)).deduct(any(OrderCreatedEvent.class));
}
```
