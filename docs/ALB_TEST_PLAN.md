## 테스트 1: 라우팅 검증

### 1. 테스트 목적
ALB가 경로에 따라 올바른 서비스로 라우팅하는지 확인한다.

### 2. 테스트 대상 엔드포인트
- Order 서비스: `POST /orders`
- Payment 서비스: `GET /payment`
- Product 서비스: `GET /product`

### 3. 테스트 방법 (curl 사용)

#### 3-1. Order 생성 (POST /orders)

```bash
ALB_URL="http://<terraform-output-alb-dns>"

curl -X POST "${ALB_URL}/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "supplierId": 1001,
    "customerId": 5001,
    "products": [
      { "productId": "9f8c1b1a-3f47-4c41-a26e-7b4d5aacd222", "quantity": 1, "price": 15000 }
    ],
    "comments": "빠른 배송 요청"
  }' \
  -w "HTTP %{http_code} | %{time_total}s\n"
```

### 3. 예상 결과
- /orders → 200 OK, Order Service 응답
- /payments → 200 OK, Payment Service 응답
- /products → 200 OK, Inventory Service 응답

### 4. 성공 기준
- 3개 엔드포인트 모두 정상 응답

---

### (2) 헬스체크 및 자동 복구 계획

## 테스트 2: 헬스체크 및 자동 복구

### 1. 테스트 목적
Order 서비스 Task 장애 발생 시 ALB와 ECS가 자동으로 트래픽 전환 및 재시작을 수행하는지 확인한다.

### 2. 사전 조건
- Order ECS Service의 Desired Count = 2
- Target Group에서 두 개의 IP(Target)가 모두 Healthy 상태

### 3. 테스트 방법 개요
1. 정상 상태에서 `/orders` 요청을 1초마다 계속 보낸다.
2. Order Service의 Task 하나를 강제로 중지(stop)한다.
3. 요청 실패 여부와 실패 비율을 기록한다.
4. ECS가 새 Task를 생성하고, ALB Target Group이 다시 Healthy가 되는 시점까지 관찰한다.

### 4. 상세 절차 (curl 기준)

#### 4-1. 요청 루프 실행 (터미널 A)

```bash
ALB_URL="http://<terraform-output-alb-dns>"

for i in {1..600}; do
  code=$(curl -s -o /dev/null -w "%{http_code}" \
    "${ALB_URL}/orders")
  echo "$(date '+%H:%M:%S') $code" | tee -a orders_health_test.log
  sleep 1
done
```

---

### (3) 무중단 배포 계획

## 테스트 3: 무중단 배포

### 1. 테스트 목적
Order 서비스 새 버전을 배포하는 동안에도 외부 사용자는 오류 없이 요청을 보낼 수 있는지 확인한다.

### 2. 사전 조건
- Order 서비스 Desired Count = 2
- Target Group Healthy Host Count = 2

### 3. 테스트 방법 개요
1. `/orders` 요청을 계속 보내며 HTTP 코드 로그를 남긴다.
2. `aws ecs update-service --force-new-deployment` 명령으로 새 배포를 강제 시작한다.
3. 배포 완료까지 요청 실패 여부를 기록한다.

### 4. 상세 절차

#### 4-1. 요청 루프 실행 (터미널 A)

```bash
ALB_URL="http://<terraform-output-alb-dns>"

while true; do
  code=$(curl -s -o /dev/null -w "%{http_code}" \
    "${ALB_URL}/orders")
  echo "$(date '+%H:%M:%S') $code" | tee -a orders_deploy_test.log
  sleep 1
done
```
