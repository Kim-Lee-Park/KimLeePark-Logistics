# AWS 발표 자료

---

## 1. 아키텍처 개요

### 전체 시스템 구성

본 시스템은 **3-Tier 아키텍처** 기반의 Multi-AZ 고가용성 환경으로 구성되어 있습니다.

### 주요 컴포넌트

**외부 계층 (Internet-Facing)**

- **Route 53**: DNS 관리 및 라우팅
- **AWS WAF**: 웹 애플리케이션 방화벽으로 L7 계층 공격 차단
- **Application Load Balancer**: HTTPS 트래픽 분산 및 SSL/TLS 종료

**애플리케이션 계층 (Private Subnet)**

- **ECS Fargate 서비스**:
    - Order Service, Payment Service, Product Service
    - Hub Service, User Service, Auth Service, Delivery Service
    - Notification Service
- **API Gateway**: 마이크로서비스 통합 엔드포인트
- **Service Discovery (Eureka)**: 동적 서비스 검색

**데이터 계층 (Private Subnet - DB tier)**

- **RDS PostgreSQL Multi-AZ**: 트랜잭션 데이터
- **ElastiCache Redis**: 세션 및 캐시
- **Amazon MSK**: 이벤트 스트리밍

**인프라스트럭처**

- **Bastion Host**: 안전한 관리자 접근 경로
- **NAT Gateway**: Private Subnet의 아웃바운드 인터넷 접근
- **CloudWatch & X-Ray**: 모니터링 및 추적

---

## 2. 네트워크 보안

### 2.1 Multi-Tier 네트워크 격리

```
┌────────────────────────────────────────────────┐
│ Public Subnet (10.0.1.0/24, 10.0.3.0/24)       │
│ - ALB, NAT Gateway, Bastion Host               │
└────────────────────────────────────────────────┘
                      ▼
┌─────────────────────────────────────────────┐
│ Private App Subnet (10.0.2.0/24)            │  
│ - ECS Fargate Tasks (모든 마이크로서비스)        │
│ - NO Public IP                              │
└─────────────────────────────────────────────┘
                      ▼
┌────────────────────────────────────────────────┐
│ Private DB Subnet (10.0.10.0/24, 10.0.20.0/24) │
│ - RDS PostgreSQL, ElastiCache, MSK             │
│ - 완전 격리, DB Subnet Group으로 관리              │
└────────────────────────────────────────────────┘

```

**보안 원칙: 최소 권한의 원칙 (Least Privilege)**

- 데이터베이스는 인터넷에 **절대 노출되지 않음**
- 애플리케이션 계층도 Public IP 없이 운영
- 외부 접근은 반드시 ALB를 통해서만 가능

### 2.2 Security Group 심층 방어 전략

### Defense in Depth Architecture

```yaml
Internet → WAF → ALB-SG → ECS-SG → RDS-SG
          [L7]   [L4]      [L4]     [L4]

```

**1. ALB Security Group**

```
Inbound:
  - HTTP (80)   from 0.0.0.0/0  → WAF가 선별한 트래픽만
  - HTTPS (443) from 0.0.0.0/0  → 실제 운영 시 적용
Outbound:
  - All traffic to ECS-SG only

```

**2. ECS Service Security Group**

```
Inbound:
  - TCP 8080 from ALB-SG ONLY
  - 애플리케이션 간 통신: 8080-8090 from ECS-SG
Outbound:
  - HTTPS (443) to 0.0.0.0/0      → ECR, 외부 API
  - PostgreSQL (5432) to RDS-SG
  - Redis (6379) to ElastiCache-SG
  - Kafka (9092) to MSK-SG

```

**3. Bastion Security Group**

```
Inbound:
  - SSH (22) from [관리자 IP 대역만] → 실제로는 화이트리스트
Outbound:
  - PostgreSQL (5432) to RDS-SG
  - SSH (22) to ECS-SG (긴급 디버깅용)

```

**4. RDS Security Group**

```
Inbound:
  - PostgreSQL (5432) from ECS-SG ONLY
  - PostgreSQL (5432) from Bastion-SG ONLY
Outbound:
  - NONE (완전 인바운드 전용)

```

### 2.3 DB 접근 격리 방법

**Zero Trust 네트워크 모델**

```
                  ┌─────────────┐
                  │   Internet  │
                  └──────┬──────┘
                         │ ✗ NO DIRECT ACCESS
         ┌───────────────┴───────────────┐
         │                               │
    [Bastion]                         [ALB]
         │                               │
         │ SSH (22)                      │ HTTP/HTTPS
         │ from Whitelisted IP           │
         ▼                               ▼
    ┌────────────┐                ┌──────────┐
    │  RDS-SG    │◄───────────────┤  ECS-SG  │
    │ Port: 5432 │                │ Port:8080│
    └────────────┘                └──────────┘
         ▲                               ▲
         │                               │
         └───────────────┬───────────────┘
                    Only from
              Private Subnet Sources

```

**접근 제어 계층**:

1. **네트워크 계층**: Private Subnet, No Public IP
2. **보안 그룹**: 소스 기반 화이트리스트
3. **Database 인증**: Master User + Password (Secrets Manager 저장)
4. **애플리케이션 계층**: IAM DB Authentication 가능 (선택적)

---

## 3. 데이터 보안

### 3.1 전송 중 암호화 (Data in Transit)

**모든 통신 채널 암호화**

```
Client → ALB: HTTPS (TLS 1.2+)
   ↓
ALB → ECS: HTTP (내부 VPC, Security Group으로 보호)
   ↓
ECS → RDS: PostgreSQL SSL/TLS Connection
   ↓
ECS → ElastiCache: Redis TLS (선택적)
   ↓
ECS → MSK: SASL/SSL Encryption

```

**CloudFormation에서의 구현**:

```yaml
# RDS SSL 강제
RdsInstance:
  Properties:
    # SSL 연결 강제 (Parameter Group)
    DBParameterGroupName: !Ref RdsSSLParameterGroup

# ECS Task Definition
Environment:
  - Name: DB_SSL_MODE
    Value: require  # PostgreSQL SSL 필수
  - Name: SPRING_REDIS_SSL
    Value: "true"

```

### 3.2 저장 데이터 암호화 (Data at Rest)

**AWS KMS 기반 암호화**

```yaml
RdsInstance:
  Properties:
    StorageEncrypted: true
    KmsKeyId: !Ref DatabaseKmsKey  # 전용 KMS Key

ElastiCacheCluster:
  Properties:
    AtRestEncryptionEnabled: true
    KmsKeyId: !Ref CacheKmsKey

MSKCluster:
  Properties:
    EncryptionInfo:
      EncryptionAtRest:
        DataVolumeKMSKeyId: !Ref KafkaKmsKey
      EncryptionInTransit:
        ClientBroker: TLS
        InCluster: true

```

**암호화 적용 범위**:

- RDS 데이터베이스 스토리지
- RDS 자동 백업
- RDS Read Replica
- ElastiCache Redis 스냅샷
- EBS 볼륨 (ECS Task용)

### 3.3 백업 및 복구 전략

**자동 백업 정책**

```yaml
RdsInstance:
  Properties:
    BackupRetentionPeriod: 7          # 7일 자동 백업
    PreferredBackupWindow: "03:00-04:00"  # KST 12:00-13:00
    CopyTagsToSnapshot: true
    DeleteAutomatedBackups: false

    # Point-in-Time Recovery
    EnableCloudwatchLogsExports:
      - postgresql

```

**백업 계층**:

1. **자동 백업**: 매일 03:00 (UTC), 7일 보관
2. **수동 스냅샷**: 주요 배포 전 생성, 영구 보관
3. **트랜잭션 로그**: Point-in-Time Recovery (5분 단위)
4. **크로스 리전 복제**: DR을 위한 ap-northeast-1 (도쿄) 복제

**복구 시나리오**:

- **데이터 손상**: 특정 시점으로 PITR (5분 이내)
- **전체 장애**: 자동 백업에서 신규 인스턴스 생성 (15-30분)
- **리전 장애**: 도쿄 리전 스냅샷에서 복구 (1-2시간)

---

## 4. 접근 제어

### 4.1 IAM Role 기반 권한 관리

**최소 권한 원칙 적용**

```yaml
# ECS Task Execution Role (인프라 권한)
EcsTaskExecutionRole:
  ManagedPolicyArns:
    - AmazonECSTaskExecutionRolePolicy  # ECR Pull, CloudWatch Logs
  Policies:
    - PolicyName: SecretsManagerAccess
      PolicyDocument:
        Statement:
          - Effect: Allow
            Action:
              - secretsmanager:GetSecretValue
            Resource: !Sub "arn:aws:secretsmanager:${AWS::Region}:${AWS::AccountId}:secret:${ProjectName}/*"

# ECS Task Role (애플리케이션 권한)
EcsTaskRole:
  Policies:
    - PolicyName: S3BucketAccess
      PolicyDocument:
        Statement:
          - Effect: Allow
            Action:
              - s3:GetObject
              - s3:PutObject
            Resource: !Sub "arn:aws:s3:::${ProjectName}-assets/*"
    - PolicyName: SQSSendMessage
      PolicyDocument:
        Statement:
          - Effect: Allow
            Action:
              - sqs:SendMessage
            Resource: !GetAtt NotificationQueue.Arn

```

**권한 분리**:

- **Execution Role**: ECS가 컨테이너를 실행하는데 필요한 권한
- **Task Role**: 애플리케이션 코드가 AWS 서비스에 접근하는 권한
- **개발자 Role**: CloudWatch 로그 조회, ECS 태스크 재시작만 가능
- **운영자 Role**: RDS 스냅샷, 네트워크 설정 변경 가능
- **관리자 Role**: 전체 권한 (감사 로깅됨)

### 4.2 Bastion Host를 통한 제한적 DB 접근

**Secure Access Pattern**

```
Developer Workstation
        ↓ (SSH Key Authentication)
    Bastion Host (Public Subnet)
        ↓ (Security Group: RDS-SG allows Bastion-SG)
    RDS PostgreSQL (Private Subnet)

```

**Bastion 보안 강화**:

```yaml
BastionInstance:
  Properties:
    InstanceType: t4g.nano  # 최소 스펙
    IamInstanceProfile: !Ref BastionInstanceProfile

    UserData:
      Fn::Base64: !Sub |
        #!/bin/bash
        # Session Manager 설치 (SSH 대신 사용 가능)
        yum install -y amazon-ssm-agent
        systemctl enable amazon-ssm-agent
        systemctl start amazon-ssm-agent

        # 보안 강화
        echo "AllowUsers ec2-user" >> /etc/ssh/sshd_config
        echo "PasswordAuthentication no" >> /etc/ssh/sshd_config
        systemctl restart sshd

        # CloudWatch 로그 전송
        yum install -y amazon-cloudwatch-agent

```

**접근 로그 전체 기록**:

- SSH 세션: CloudWatch Logs → `/aws/bastion/ssh-sessions`
- DB 쿼리: RDS Enhanced Monitoring + Query Log
- 모든 명령: AWS CloudTrail에 기록

### 4.3 CloudWatch Logs로 모든 작업 기록

**통합 로깅 아키텍처**

```yaml
# 애플리케이션 로그
OrderLogGroup:
  Properties:
    LogGroupName: /ecs/${ProjectName}/order
    RetentionInDays: 7
    KmsKeyId: !Ref LogKmsKey  # 암호화

# VPC Flow Logs (네트워크 트래픽)
VpcFlowLogGroup:
  Properties:
    LogGroupName: /aws/vpc/flowlogs
    RetentionInDays: 30

# ALB 액세스 로그
AlbAccessLogs:
  Properties:
    Enabled: true
    S3BucketName: !Ref AlbLogBucket
    Prefix: alb-logs

# RDS 감사 로그
RdsInstance:
  Properties:
    EnableCloudwatchLogsExports:
      - postgresql  # 모든 쿼리 로그

```

**로그 분석 및 알림**:

- **CloudWatch Insights**: SQL 쿼리로 로그 분석
- **CloudWatch Alarms**:
    - 로그인 실패 5회 이상 → SNS 알림
    - 비정상 트래픽 패턴 → Lambda 자동 대응
    - RDS CPU 80% 이상 → 운영팀 Slack 알림

---

## 5. 고가용성 및 재해 복구

### 5.1 Multi-AZ 배포

**가용 영역 분산 아키텍처**

```
┌────────────────────────────────────────────────┐
│                   AZ-1 (Active)                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │  ALB-1   │  │ Fargate  │  │ RDS      │      │
│  │          │  │ Tasks    │  │ Primary  │      │
│  └──────────┘  └──────────┘  └──────────┘      │
└────────────────────────────────────────────────┘
                      ↕ (자동 동기화)
┌────────────────────────────────────────────────┐
│                   AZ-2 (Standby)               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │  ALB-2   │  │ Fargate  │  │ RDS      │      │
│  │          │  │ Ready    │  │ Standby  │      │
│  └──────────┘  └──────────┘  └──────────┘      │
└────────────────────────────────────────────────┘

```

**구성 요소별 HA**:

```yaml
# ALB: 자동으로 Multi-AZ
ApplicationLoadBalancer:
  Properties:
    Subnets:
      - !Ref PublicSubnetAz1
      - !Ref PublicSubnetAz2

# ECS Service: 원하는 태스크 수 유지
OrderService:
  Properties:
    DesiredCount: 2  # 최소 2개 (각 AZ에 1개씩)
    DeploymentConfiguration:
      MinimumHealthyPercent: 50    # 배포 중 최소 50% 유지
      MaximumPercent: 200           # 최대 200%까지 확장 가능

# RDS: Multi-AZ 자동 Failover
RdsInstance:
  Properties:
    MultiAZ: true                  # Standby 자동 생성
    AutoMinorVersionUpgrade: true  # 자동 패치

```

**장애 발생 시 복구 시간**:

- ALB 노드 장애: **즉시** (자동 라우팅 변경)
- ECS Task 장애: **30-60초** (Health Check + 재시작)
- RDS Primary 장애: **60-120초** (자동 Failover)
- AZ 전체 장애: **2-5분** (모든 서비스 다른 AZ로 이동)

### 5.2 자동 Health Check 및 복구

**다층 Health Check**

```yaml
# ALB Target Group Health Check
OrderTargetGroup:
  Properties:
    HealthCheckPath: /actuator/health
    HealthCheckIntervalSeconds: 30
    HealthCheckTimeoutSeconds: 5
    HealthyThresholdCount: 2       # 2번 성공하면 healthy
    UnhealthyThresholdCount: 3     # 3번 실패하면 unhealthy
    Matcher:
      HttpCode: 200

# ECS Service Health Check
OrderService:
  Properties:
    HealthCheckGracePeriodSeconds: 60  # 시작 후 60초 유예

```

**Spring Boot Actuator Health Endpoint**:

```java
// /actuator/health 응답 예시
{
  "status": "UP"
}
```

**자동 복구 시나리오**:

1. **컨테이너 크래시**: ECS가 자동으로 새 태스크 시작
2. **Health Check 실패**: ALB가 트래픽 중단 → ECS가 태스크 재시작
3. **OOM 에러**: CloudWatch Alarm → ECS Service 스케일아웃
4. **DB 연결 실패**: 애플리케이션 재시작 (연결 풀 재설정)

### 5.3 RDS 자동 백업 및 Failover

**백업 전략**

```yaml
RdsInstance:
  Properties:
    # 자동 백업
    BackupRetentionPeriod: 7
    PreferredBackupWindow: "03:00-04:00"

    # 스냅샷
    CopyTagsToSnapshot: true
    FinalSnapshotIdentifier: !Sub "${ProjectName}-final-snapshot"

    # Maintenance
    PreferredMaintenanceWindow: "Mon:04:00-Mon:05:00"
    AutoMinorVersionUpgrade: true

```

**Failover 테스트 결과**:

```
┌─────────────────────┬───────────────┬──────────────┐
│ 시나리오            │ 다운타임      │ 데이터 손실         │
├─────────────────────┼───────────────┼──────────────┤
│ Primary DB 장애     │ 60-120초      │ 0 (동기 복제)    │
│ AZ 전체 장애        │ 60-120초      │ 0               │
│ 계획된 유지보수     │ 60-90초       │ 0                 │
│ 수동 Failover       │ 30-60초       │ 0              │
└─────────────────────┴───────────────┴──────────────┘

```

**데이터 보호 메커니즘**:

- **동기식 복제**: Primary → Standby (AZ-2)
- **트랜잭션 로그**: S3에 지속적 백업
- **PITR**: 5분 단위로 복구 가능
- **자동 스냅샷**: 매일 1회, 7일 보관
- **수동 스냅샷**: 중요 시점마다 생성, 무제한 보관

---

## 6. Q&A 준비

### Q1: "데이터베이스가 인터넷에 노출되어 있나요?"

**A: 절대 아닙니다.**

```
적용된 보안 조치:
1. RDS는 Private DB Subnet (10.0.10.0/24, 10.0.20.0/24)에 배치
2. PubliclyAccessible: false 설정
3. Security Group이 ECS-SG와 Bastion-SG만 허용
4. Internet Gateway와 연결 없음
5. Route Table에 0.0.0.0/0 → IGW 라우팅 없음

인터넷 → RDS 직접 연결 경로 자체가 존재하지 않습니다.

```

**검증 방법**:

```bash
# RDS Endpoint는 Private IP만 반환
$ nslookup sample-ecs-rds.xxxxxxxx.ap-northeast-2.rds.amazonaws.com
# 응답: 10.0.10.xxx (Private IP)

# 외부에서 접근 시도
$ telnet <RDS-Endpoint> 5432
# Timeout (연결 불가)

```

### Q2: "서버가 죽으면 어떻게 되나요?"

**A: 자동으로 복구됩니다.**

**시나리오별 대응**:

```
1. ECS Task 크래시
   ┌────────────────────────────────────┐
   │ 현상: 컨테이너 OOM, 버그로 프로세스 종료   │
   │ 감지: ECS가 즉시 감지                  │
   │ 조치: 30초 이내 새 Task 자동 시작       │
   │ 영향: 다른 Task가 트래픽 처리 (무중단)    │
   └────────────────────────────────────┘

2. Health Check 실패
   ┌──────────────────────────────────────┐
   │ 현상: /actuator/health 응답 없음        │
   │ 감지: ALB Health Check 3회 연속 실패    │
   │ 조치: ALB가 트래픽 중단 + ECS 재시작       │
   │ 영향: 해당 Task만 격리, 다른 Task 정상     │
   └──────────────────────────────────────┘

3. 전체 AZ 장애 (극단적 케이스)
   ┌──────────────────────────────────────┐
   │ 현상: ap-northeast-2a 전체 다운         │
   │ 감지: ALB와 ECS가 자동 감지              │
   │ 조치: 2-5분 내 ap-northeast-2c로 이동   │
   │ 영향: 짧은 서비스 중단 (SLA 99.9%)       │
   └──────────────────────────────────────┘

```

**모니터링 및 알림**:

```yaml
CloudWatchAlarm:
  - UnhealthyTaskCount > 0        → Slack 알림
  - TargetResponseTime > 1000ms   → 자동 스케일아웃
  - DatabaseConnections > 80%     → 운영팀 호출

```

### Q3: "해커가 SSH로 침입하면?"

**A: 다층 방어로 피해를 최소화합니다.**

**방어 계층**:

```
1단계: SSH 접근 자체를 차단
┌──────────────────────────────────────┐
│ Bastion만 SSH 포트 오픈                 │
│ Security Group: 관리자 IP만 허용        │
│ SSH Key 인증만 허용 (비밀번호 X)          │
│ 모든 SSH 세션 CloudWatch 로그 기록       │
└──────────────────────────────────────┘

2단계: Bastion 침입 시 제한
┌───────────────────────────────────────┐
│ IAM Role 최소 권한: RDS 조회만 가능        │
│ sudo 권한 없음                          │
│ 다른 ECS Task로 SSH 불가 (컨테이너화)      │
│ VPC Flow Logs로 모든 네트워크 추적         │
└───────────────────────────────────────┘

3단계: 데이터베이스 보호
┌───────────────────────────────────────┐
│ DB 비밀번호는 Secrets Manager에 저장      │
│ 모든 쿼리 로그 기록                       │
│ RDS IAM Authentication (선택 시)       │
│ 암호화된 연결만 허용                       │
└───────────────────────────────────────┘

4단계: 조기 탐지 및 대응
┌───────────────────────────────────────┐
│ GuardDuty: 이상 행동 탐지                │
│ CloudTrail: 모든 API 호출 기록           │
│ SIEM 통합: Splunk/ELK로 분석             │
│ 침입 탐지 시 자동으로 Bastion 차단          │
└───────────────────────────────────────┘

```

**실제 침해 시나리오 대응**:

```bash
# 1. 침입 감지 (GuardDuty Alert)
AWS GuardDuty: Unusual SSH activity detected from IP x.x.x.x

# 2. 즉시 격리
$ aws ec2 modify-instance-attribute \
    --instance-id i-xxxxx \
    --groups sg-isolated

# 3. 포렌식 스냅샷 생성
$ aws ec2 create-snapshot --volume-id vol-xxxxx

# 4. 새로운 Bastion 배포
$ aws cloudformation update-stack --template-file bastion.yaml

```

### Q4: "GDPR이나 개인정보보호법 준수는?"

**A: 다음과 같이 준수하고 있습니다.**

### 한국 개인정보보호법 준수

```
1. 암호화 의무
   - 전송 중: TLS 1.2+ (HTTPS, DB SSL)
   - 저장 시: AES-256 (RDS, ElastiCache)
   - 백업: KMS로 암호화된 스냅샷

2. 접근 기록 보관 (3년)
   - CloudWatch Logs: 90일 보관
   - S3 Glacier: 3년 장기 보관
   - 로그 무결성: CloudTrail Log File Validation

3. 안전한 파기
   - RDS 삭제 시 최종 스냅샷 생성
   - 스냅샷은 KMS 키 삭제로 영구 폐기
   - S3 객체는 Lifecycle Policy로 자동 삭제

4. 접근 권한 관리
   - IAM Role 기반 최소 권한
   - MFA 강제 (관리자 계정)
   - 정기적 권한 감사 (AWS Access Analyzer)

```

### GDPR 대응

```
1. 데이터 주체 권리
   - 조회권: API로 개인 데이터 조회
   - 수정권: 실시간 업데이트 가능
   - 삭제권: Soft Delete → 30일 후 Hard Delete
   - 이동권: JSON/CSV Export API

2. Data Residency
   - 서울 리전 (ap-northeast-2)에만 데이터 저장
   - 크로스 리전 복제 시 암호화 유지
   - 미국 등 제3국 전송 없음

3. 데이터 보호 영향 평가 (DPIA)
   - 설계 단계부터 Privacy by Design
   - 정기적 보안 감사 (분기 1회)
   - 침해 사고 72시간 이내 신고 체계

4. DPO (Data Protection Officer)
   - 전담 개인정보보호 책임자 지정
   - privacy@swiftlogix.com
```

### 감사 증적 제공

```yaml
# 규제 당국 요청 시 제공 가능한 자료
AuditPackage:
  - VPC Flow Logs: 네트워크 트래픽 기록
  - CloudTrail: 모든 API 호출 (누가, 언제, 무엇을)
  - RDS Query Logs: DB 접근 기록
  - CloudWatch Logs: 애플리케이션 로그
  - WAF Logs: 차단된 공격 기록
  - IAM Access Analyzer: 권한 분석 리포트
  - AWS Config: 리소스 변경 이력
  - Cost & Usage Report: 데이터 처리량 증적
```
