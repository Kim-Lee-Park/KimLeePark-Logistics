# KimLeePark-Logistics

![imag.png](assets/image.png)

B2B 물류 흐름을 기반으로 허브 간 배송 시스템을 설계하고, B2C 영역까지 확장하여 고객이 원하는 상품을 신속하게 주문·배송할 수 있는 서비스입니다.

## ERD

[erd 상세](https://https://www.erdcloud.com/d/aoTEHW2tn5jEtJ538)
![erd.png](assets/erd.png)

## 인프라 설계도

![Infra.png](assets/Infra.png)

## 배포 파이프라인

![Pipeline.png](assets/Pipeline.png)

## Observability

![Observability.png](assets/Observability.png)

## 주요 기술 스택

### 언어 및 프레임워크

<img src="https://img.shields.io/badge/java%2017-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white">
<img src="https://img.shields.io/badge/spring%20boot%203.5.8-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/spring%20data%20jpa-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/spring%20cloud%20gateway-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/spring%20cloud%20openfeign-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/spring%20security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
<img src="https://img.shields.io/badge/querydsl-6DB33F?style=for-the-badge&logo=spring&logoColor=white">

### 모니터링 스택

<img src="https://img.shields.io/badge/opentelemetry-000000?style=for-the-badge&logo=opentelemetry&logoColor=white">
<img src="https://img.shields.io/badge/loki-F46800?style=for-the-badge&logo=grafana&logoColor=white">
<img src="https://img.shields.io/badge/prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white">
<img src="https://img.shields.io/badge/tempo-F46800?style=for-the-badge&logo=grafana&logoColor=white">
<img src="https://img.shields.io/badge/grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white">

### 인프라 및 배포

<img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
<img src="https://img.shields.io/badge/github%20actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">
<img src="https://img.shields.io/badge/terraform-7B42BC?style=for-the-badge&logo=terraform&logoColor=white">
<img src="https://img.shields.io/badge/aws%20codedeploy-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white">
<img src="https://img.shields.io/badge/aws%20ecs-FF9900?style=for-the-badge&logo=amazon-ecs&logoColor=white">

### 데이터 및 이벤트

<img src="https://img.shields.io/badge/postgresql-4169E1?style=for-the-badge&logo=postgresql&logoColor=white">
<img src="https://img.shields.io/badge/amazon%20rds-527FFF?style=for-the-badge&logo=amazon-rds&logoColor=white">
<img src="https://img.shields.io/badge/redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
<img src="https://img.shields.io/badge/aws%20elasticache-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white">
<img src="https://img.shields.io/badge/flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white">
<img src="https://img.shields.io/badge/apache%20kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white">

### 테스트

<img src="https://img.shields.io/badge/junit%205-25A162?style=for-the-badge&logo=junit5&logoColor=white">
<img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white">

### 협업 툴 및 소스관리

<img src="https://img.shields.io/badge/slack-4A154B?style=for-the-badge&logo=slack&logoColor=white">
<img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">

## 패키지 구조

```
{service-name}/
├── presentation/          # 프레젠테이션 레이어
│   ├── controller/       # REST API 컨트롤러
│   └── dto/              # 요청/응답 DTO
├── application/          # 애플리케이션 레이어
│   ├── facade/           # Facade 패턴 (복잡한 비즈니스 로직 조율)
│   ├── service/          # 애플리케이션 서비스
│   ├── command/          # 명령 객체 (CQRS)
│   ├── query/            # 쿼리 서비스 (CQRS)
│   ├── cache/            # 캐시 관리
│   └── event/            # 이벤트 핸들러
├── domain/               # 도메인 레이어
│   ├── entity/           # 도메인 엔티티
│   ├── repository/       # 리포지토리 인터페이스
│   └── vo/               # 값 객체 (Value Object)
└── infrastructure/       # 인프라스트럭처 레이어
    ├── client/           # 외부 서비스 클라이언트 (Feign)
    ├── event/             # 이벤트 발행/구독
    └── repository/       # 리포지토리 구현체 (JPA)
```

### 레이어별 책임

- **Presentation Layer**: HTTP 요청/응답 처리, 유효성 검증
- **Application Layer**: 비즈니스 로직 조율, 트랜잭션 관리, 외부 서비스 호출
- **Domain Layer**: 핵심 비즈니스 로직, 도메인 규칙, 엔티티
- **Infrastructure Layer**: 외부 시스템 연동, 데이터 영속성, 기술적 세부사항

### 주요 기능

#### 🎁 상품 관리

판매자는 판매할 상품을 등록하고, 상품 정보를 관리하며, 상품 정보 변경 사항은 주문 및 재고 관리와 연동되어 반영됩니다.

- 상품 조회 시 **멀티 레벨 캐시(L1/L2)** 를 활용하여 빠른 응답 속도 제공
- **Negative Caching** 을 통해 존재하지 않는 상품에 대한 반복 조회 방지
- **RAG 기반 AI 상품 추천** 시스템을 통해 주문 생성 이벤트 수신 시 추천 생성 파이프라인 트리거
- 주문 상품명을 기준으로 **Vector Store 기반 유사 상품 검색** 수행
- 사용자 허브 정보, 주문 이력, 시간대, 날씨 등 **컨텍스트 정보 수집** 후 LLM 기반 추천 결과 생성

#### 🧾 주문 관리

고객은 원하는 상품을 선택하여 주문할 수 있으며, 주문 생성부터 결제 완료, 주문 상태 변경까지의 흐름을 관리합니다.

- **Saga 패턴(Choreography 방식)** 을 적용하여 주문·재고·결제·배송 간 분산 트랜잭션 관리
- 중간 단계 실패 시 **보상 트랜잭션**을 통해 이전 상태로 롤백
- **Outbox 패턴** 을 통해 DB 트랜잭션과 Kafka 이벤트 발행의 원자성 보장
- 스케줄러 기반으로 이벤트를 안정적으로 발행
- 주문 생성 이후의 재고 처리, 결제, 배송 생성은 **Kafka 기반 비동기 이벤트 흐름**으로 처리

#### 🏢 재고 관리

상품별 재고 수량을 관리하며, 주문 시 재고 수량을 동기화하여 조회할 수 있습니다.

- **재고 선점(Reservation) 패턴** 을 적용하여 결제 진행 중 재고가 소진되는 것을 방지
- TTL 기반(15분)으로 선점 시간 관리 하며 **Redis 분산락** 을 활용하여 동시성 환경에서도 재고 차감의 일관성 보장
- 재고 조회 시 **멀티 레벨 캐시** 를 활용하여 빠른 응답 제공
- **Probabilistic Early Refresh(PER)** 를 통해 캐시 스탬피드 방지

#### 🚚 배송 관리

주문 완료된 상품은 허브를 기준으로 배송이 진행되며, 배송 상태를 추적하며, 허브 간 이동을 고려한 배송 구조를 통해 효율적인 물류 흐름을 관리합니다.

- **다익스트라 알고리즘** 을 활용하여 최적의 배송 경로 생성
- 배송 생성 시 도착 허브의 업체 배송 담당자를 **랜덤 선택 알고리즘** 으로 자동 할당
- 트랜잭션을 활용한 멱등성 처리로 중복 배송 생성 방지
- **AI 기반 메시지 생성** 후 **Slack Webhook**을 통해 배송 담당자에게 전송

#### 🎫 고객 쿠폰

선착순 쿠폰, 회원가입 시 쿠폰을 발급하여 할인된 가격의 원하는 상품을 주문할 수 있습니다.

- 쿠폰 발급 시 **Redis Lua 스크립트** 를 활용한 원자적 재고 차감 로직으로 동시성 이슈 해결
- DB와 Redis를 이중 관리하여 빠른 재고 확인과 최종 상태 관리 분리
- 주문 생성 시 쿠폰 선점(Optimistic Lock) 및 할인 금액 계산 수행
- 결제 승인 시 쿠폰 사용 확정, 주문 실패 시 쿠폰 선점 해제
- Kafka 이벤트 기반으로 결제·주문·재고 서비스와 비동기 통신

## 팀원별 담당 기능

### 김명진

- 배송·쿠폰 기본 CRUD 설계·구현
- 쿠폰 발급 시 동시성 이슈를 고려해 Redis Lua 스크립트 기반의 원자적 재고 차감 로직 적용
- 중복 배송 요청 발생 가능성을 고려해 트랜잭션을 활용한 멱등성 처리 로직 구현
- 마이크로서비스 간 비동기 통신을 위해 Kafka 기반 이벤트 흐름 구성
- 서비스 내부 로직 분리를 위해 Spring Application Event를 활용한 도메인 이벤트 처리 적용

### 김한결

- 멀티 레벨 캐시(L1/L2) 설계 및 구현
- Observability 파이프라인 설계 및 구현
- 모니터링 환경 구성 및 지표 설계
- 서킷 브레이커 적용을 통한 장애 격리 및 안정성 확보
- 부하 테스트 시나리오 설계 및 수행

### 박성민

- Spring Gateway을 이용한 인증 및 회원 기능 구현
- Spring Cloud Config을 통한 설정파일 중앙 관리 시스템 구축
- RAG 기반 상품 추천 시스템 구현
- 결제 모킹, 리뷰 기능 CRUD 작성 및 이벤트 처리
- Gemini API 연동 및 Slack Webhook을 이용한 알림 시스템 구현
- 재고 기능 동시성 제어 및 Redis 기반 대용량 트래픽 처리

### 박주찬

- Event-Driven Architecture 기반의 주문 서비스 설계 및 구현
- Kafka를 활용한 비동기 메시징 파이프라인 구축 (주문 → 결제 → 쿠폰 → 재고 → 배송 → 알림)
- Saga 패턴을 적용한 분산 트랜잭션 및 보상 트랜잭션 로직 구현 (실패 시 롤백 처리)
- Transactional Outbox Pattern 도입을 통한 이벤트 발행의 신뢰성 및 데이터 일관성 보장
- OpenFeign을 활용한 마이크로서비스 간 동기 통신 구현

### 이명규

- 재고 Redis 분산락을 통산 멱등성 보장 구조 설계
- 테라폼을 이용한 인프라 IaC 구성
- AWS ALB 를 활용한 블루/그린 배포 파이프라인 구성
- AWS 인프라 환경 구성 및 운영상의 모니터링 환경 구성
- 부하테스트를 통한 주문, 허브 개선 작업 수행

## 의사결정

### 1. 재고 관리 전략: 재고 선점(Reservation) 패턴 채택

**고려한 방안**


| 방안 | 흐름                                             | 적합한 상황                  | 특징                             |
| ---- | ------------------------------------------------ | ---------------------------- | -------------------------------- |
| 1    | 주문 생성 → 결제 진행 → 재고 차감              | 구독 서비스, 디지털 상품     | 단순한 흐름, 재고 경합 없음      |
| 2 ⭐ | 주문 생성 → 재고 선점 → 결제 진행 → 재고 확정 | 기차 예매, 한정판 상품       | 결제 중 재고 보호, 공정한 선착순 |
| 3    | 주문 생성 → 재고 차감 + 결제 진행               | 라이브 커머스, 대규모 이벤트 | 빠른 응답, 복잡한 보상 로직      |

**선택 이유: 2번 (재고 선점 패턴)**

- 물류 시스템 특성상 **한정된 재고에 대한 동시 접근 경합** 발생
- 결제 진행 중 다른 사용자가 마지막 재고를 가져가는 상황 방지
- **공정한 선착순 처리**와 **사용자 경험** 개선
  - 결제 완료 직전 재고 부족으로 실패하는 최악의 UX 방지

**구현 방식**

- Redis Lua 스크립트로 원자적 재고 선점(Reserve) 처리
- TTL 기반 선점 시간 관리 (10분), 미결제 시 자동 복구
- 결제 성공 → 선점 확정(Confirm), 실패 → 즉시 복구(Release)

---

### 2. Kafka 이벤트 아키텍처

#### Choreography 패턴 채택


| 구분      | Orchestration                   | Choreography                            |
| --------- | ------------------------------- | --------------------------------------- |
| 제어 방식 | 중앙 오케스트레이터가 흐름 제어 | 각 서비스가 독립적으로 이벤트 구독/발행 |
| 결합도    | 중앙 서비스 의존성 높음         | 느슨한 결합                             |
| 확장성    | 오케스트레이터 병목 가능        | 서비스별 독립 확장                      |
| 장애 격리 | 중앙 서비스 장애 시 전체 영향   | 특정 서비스 장애 영향 최소화            |

**선택 이유**

- MSA 환경에서 **서비스 자율성 및 독립성** 최대화
- 특정 서비스만 독립적으로 스케일 아웃 가능
- 단일 장애 지점(SPOF) 제거

#### 다중 토픽-다중 이벤트 전략


| 방식                  | 장점                                 | 단점                                |
| --------------------- | ------------------------------------ | ----------------------------------- |
| 단일 토픽-다중 이벤트 | 순서 보장 용이, 관리 포인트 단순     | 이벤트 필터링 오버헤드, 확장성 제한 |
| 다중 토픽-다중 이벤트 | 도메인별 독립 확장, 명확한 책임 분리 | 토픽 관리 복잡도 증가               |

**선택 이유**

- 도메인(주문, 재고, 결제, 배송)별 **독립적 처리 및 확장**
- 토픽별 파티션 전략 최적화 (주문은 user_id, 재고는 product_id 등)
- Consumer Group 분리로 **병렬 처리 극대화**

**이벤트 흐름도**

[여기 사진 넣기]

---

### 3. 공통 라이브러리(common-library) 제거

**초기 아키텍처의 문제점**

```
common-module (공통 유틸, DTO, 예외 등)
↑ 의존
├── order-service
├── payment-service
├── delivery-service
└── ... (모든 서비스)
```

**발견된 문제**

1. **배포 파이프라인 병목**

   - 공통 라이브러리 변경 시 의존하는 **모든 서비스 재빌드 필요**
   - CI/CD 파이프라인 전체 실행으로 배포 시간 증가
   - 한 줄 수정에도 10개 서비스 재배포 발생
2. **확장성 제약**

   - 한 서비스만 필요한 기능도 전체 모듈에 포함
   - 서비스별 독립적 기술 스택 선택 제한
   - 불필요한 의존성 증가 (jar 크기 증가)
3. **버전 관리 복잡도**

   - 모든 서비스에서 동일한 공통 라이브러리 버전을 사용해야 하는 문제점 존재
   - 내용이 변경되어 서비스 간 통신이 호환되지 않을 시 서비스 전체적인 문제로 직결

**해결 방안**

- **DRY보다 독립성 우선**: 필요한 코드는 각 서비스에 복제
- 정말 공통적인 인프라 코드만 선택적 라이브러리로 분리
- 서비스 간 계약은 **API 스펙(OpenAPI)** 으로 관리

**결과**

- 서비스별 독립 배포 가능
- 빌드 시간 단축 (전체 재빌드 → 변경된 서비스만 빌드)
- 서비스별 최적화된 기술 스택 선택 가능

---

### 4. Observability: 모니터링 전략 및 핵심 지표

**모니터링 파이프라인**

[여기에 모니터링 사진넣기]

**부하 테스트 시 중점 확인 지표**

[지표 데이터 우리 대시보드에 맞게 수정 필요]

**1. RED 메트릭 (Request-centric)**


| 지표         | 측정 항목                 | 목표                   |
| ------------ | ------------------------- | ---------------------- |
| **Rate**     | 초당 요청 수 (QPS)        | 주문 API 1000 QPS 처리 |
| **Errors**   | 에러율 (5xx, 4xx)         | < 0.1%                 |
| **Duration** | 응답 시간 (P50, P95, P99) | P95 < 500ms, P99 < 1s  |

**2. USE 메트릭 (Resource-centric)**


| 리소스                 | 측정 항목                 | 임계치                       |
| ---------------------- | ------------------------- | ---------------------------- |
| **JVM**                | Heap 사용률, GC 빈도/시간 | Heap < 80%, GC Pause < 100ms |
| **DB Connection Pool** | Active/Idle 커넥션        | Active < 80%                 |
| **Redis**              | 커넥션 수, 메모리 사용률  | Memory < 80%                 |
| **Kafka**              | Consumer Lag, 처리량      | Lag < 1000                   |

**3. 비즈니스 메트릭**

- 주문 생성 성공/실패율
- 재고 선점/차감 성공률
- 결제 성공률
- 이벤트 처리 지연 시간 (Event End-to-End Latency)

**대시보드 구성**

- **Overview Dashboard**: 전체 서비스 상태 (RED 메트릭 중심)
- **Service Dashboard**: 서비스별 상세 메트릭 (Order, Payment, Inventory, Delivery)
- **Infrastructure Dashboard**: DB, Redis, Kafka 리소스 상태
- **Trace Dashboard**: 분산 트레이싱 기반 병목 구간 분석

## 트러블슈팅

### 1. 커스텀 헤더 강제 주입 보안 취약점

**문제 상황**

- Gateway에서 JWT 토큰을 검증한 후 `X-User-Id`, `X-User-Name`, `X-User-Role` 커스텀 헤더를 주입하여 각 서비스로 전달
- 각 서비스의 `AuthorizationFilter`는 이 커스텀 헤더를 받아 SecurityContext에 설정하여 권한을 부여
- 외부 사용자가 HTTP 요청에 `X-User-Role: MASTER` 같은 헤더를 임의로 주입하면 권한을 우회할 수 있는 보안 취약점 존재

**해결 방법**

- `/v1/internal/` 경로를 내부 서비스 간 통신 전용 API로 분리
- 내부 API는 Gateway를 거치지 않고 서비스 간 직접 호출(Feign Client 등)하여 헤더 강제 주입 공격 방지
- 이를 통해 외부에서 임의의 헤더를 주입하더라도 내부 API에 접근할 수 없도록 보안 강화

### 2. 재고 차감, 쿠폰 사용 시 Lost Update 문제

**문제 상황**

- 재고 차감/쿠폰 발급에 대해 동시 다발적인 요청 발생 시 **Lost Update 문제** 발생
- 예시: 재고 100개 상황에서 50명이 동시 주문 → 50개가 차감되어야 하나 실제로는 더 적게 차감됨
- 기존 **Read-Modify-Write 구조**에서 Update 전에 다른 요청이 Read하여 값이 덮어씌워지는 Race Condition 발생

**해결 방법**

- **Redis Lua 스크립트**를 활용한 원자적 연산 처리
  - 단순 Redis 명령어(`INCR`, `DECR`)로는 복잡한 재고 차감 로직 처리 불가
  - Lua 스크립트로 조건 검증 + 차감 연산을 하나의 원자적 작업으로 묶음
- **Fallback 전략**: Redis 장애 시 DB의 **원자적 쿼리**(`UPDATE ... SET stock = stock - ?`)로 2차 방어

### 3. 대용량 트래픽 환경의 Redis-DB 동기화

**문제 상황**

- 블랙 프라이데이, 선착순 쿠폰 발급 등 이벤트 발생 시 순간적인 대용량 트래픽이 DB에 부하를 줄 수 있음
- Redis-DB 간 데이터 동기화 방법을 **Write-Behind** 패턴으로 적용 (`Redis 저장 → Kafka → DB 동기화`)
- Kafka 장애 혹은 Redis 저장 이후 서버 다운 시 **메시지 유실 문제** 발생
- **Outbox 패턴** 도입으로 메시지 유실 방지 시도 → 요청 1건당 Outbox Insert 1회 발생하여 **새로운 병목 지점**이 됨
- Outbox Bulk Insert 고려 시 메모리 적재 중 서버 다운 시 메시지 유실 가능성 재발 (원점 회귀)

**해결 방법**

- Redis와 DB가 하나의 트랜잭션으로 묶일 수 없는 상황에서 **성능 vs 데이터 정합성** 트레이드오프 발생
- 이벤트는 상시가 아닌 순간적 트래픽 집중 현상이므로 **성능 우선** 전략 선택
- **Two-Phase 방식** 채택: Redis 저장 후 즉시 응답 → 백그라운드에서 비동기 DB 저장
- 일시적 데이터 불일치는 **Reconciliation 배치 스케줄러**로 주기적 동기화 및 보정

### 4. 모니터링 환경을 통한 트랜잭션 병목 현상 파악 및 해결

**문제 상황**

- Grafana/Prometheus를 통해 주문 생성 API의 응답 시간이 평균 XXms → 피크 시 XXXms로 증가하는 현상 발견
- OpenTelemetry 트레이스 분석 결과, 주문 생성 트랜잭션이 외부 서비스 호출로 인해 장시간 유지됨을 확인
- 주문 생성 트랜잭션 내부에서 동기 방식으로 외부 서비스 호출(고객 주소지 정보 조회, 쿠폰 및 등급 적용과 금액 계산)
- 트랜잭션이 외부 API 응답을 대기하는 동안 DB 커넥션과 락을 점유하여 DB 커넥션 풀 고갈 및 대기 시간 증가

**해결 방법**

- 트랜잭션 범위 최소화 및 분리
  - 외부 서비스 호출을 트랜잭션 밖으로 분리
  - 주문 생성 트랜잭션은 필수 데이터 저장에만 집중

## 🐋 Docker-compose 실행 방법

### 환경 변수 설정

프로젝트는 Spring Cloud Config Server를 통해 config-repo에서 애플리케이션 설정을 관리하며, Docker Compose를 통해 인프라 서비스를
구성합니다.
docker-compose.yml 파일에 아래 환경 변수를 설정해야 합니다.

#### Infra & Server 환경 변수

```yaml
# Database
{ POSTGRES_USER }
  { POSTGRES_PASSWORD }
  { REDIS_PASSWORD }

  # Spring Cloud Config
  { CONFIG_REPO_URI }
  { GIT_USERNAME }
  { GIT_TOKEN }

  #Kafka  
  { KAFKA_BOOTSTRAP_SERVERS }
```

### 실행

```bash
# 인프라 실행 (PostgreSQL, Redis, Kafka...)
docker-compose up -d

# (선택) 모니터링 실행
# telemetry 디렉토리로 이동
cd telemetry
docker-compose up -d
```

### 서비스 관리 명령어

```bash
# 실행 중인 모든 서비스 상태 확인
docker-compose ps

# 특정 서비스의 실시간 로그 확인
docker-compose logs -f [service-name]

# 전체 서비스 중지 및 컨테이너 제거
docker-compose down
```

### 접속 정보

- Grafana: http://{GRAFANA_HOST}:{GRAFANA_PORT}
- Kafka UI: http://{KAFKA_UI_HOST}:{KAFKA_UI_PORT}
- Swagger: http://{SWAGGER_HOST}:{SWAGGER_PORT}/swagger-ui.html
