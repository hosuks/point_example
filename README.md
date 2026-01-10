# 무료 포인트 시스템 API

무료 포인트 적립, 사용, 취소를 관리하는 REST API 시스템입니다.

## 기술 스택

- **Java 17+**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **H2 Database** (In-Memory)
- **Gradle**
- **Lombok**

## 빌드 및 실행

### 요구사항

- JDK 17 이상
- Gradle 8.x (Gradle Wrapper 포함)

### 빌드

```bash
./gradlew build
```

### 실행

```bash
./gradlew bootRun
```

또는 JAR 파일로 실행:

```bash
./gradlew build
java -jar build/libs/point-0.0.1-SNAPSHOT.jar
```

### 테스트 실행

```bash
./gradlew test
```

### H2 Console 접속

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:pointdb`
- Username: `sa`
- Password: (빈 값)

## API 명세

### 기본 URL

```
http://localhost:8080/api/v1
```

### 포인트 API

#### 1. 포인트 적립

```http
POST /points/earn
Content-Type: application/json

{
    "memberId": 1,
    "amount": 1000,
    "manual": false,
    "expiryDays": 365
}
```

**응답:**
```json
{
    "pointKey": "A1B2C3D4",
    "memberId": 1,
    "amount": 1000,
    "manual": false,
    "expirationDate": "2026-01-08T10:00:00",
    "balance": 1000
}
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| memberId | Long | Yes | 회원 ID |
| amount | Long | Yes | 적립 금액 (1 ~ 100,000) |
| manual | Boolean | No | 수기 지급 여부 (기본: false) |
| expiryDays | Integer | No | 만료일 (1 ~ 1824일, 기본: 365일) |

#### 2. 적립 취소

```http
POST /points/earn/cancel
Content-Type: application/json

{
    "pointKey": "A1B2C3D4"
}
```

**응답:**
```json
{
    "pointKey": "E5F6G7H8",
    "originalPointKey": "A1B2C3D4",
    "memberId": 1,
    "cancelledAmount": 1000,
    "balance": 0
}
```

> **Note:** 일부라도 사용된 포인트는 적립 취소할 수 없습니다.

#### 3. 포인트 사용

```http
POST /points/use
Content-Type: application/json

{
    "memberId": 1,
    "amount": 500,
    "orderId": "ORDER-001"
}
```

**응답:**
```json
{
    "pointKey": "I9J0K1L2",
    "memberId": 1,
    "usedAmount": 500,
    "orderId": "ORDER-001",
    "balance": 500,
    "usageDetails": [
        {
            "pointId": 1,
            "usedAmount": 500
        }
    ]
}
```

**사용 우선순위:**
1. 수기 지급 포인트 우선
2. 만료일이 짧은 순서

#### 4. 사용 취소

```http
POST /points/use/cancel
Content-Type: application/json

{
    "pointKey": "I9J0K1L2",
    "amount": 300
}
```

**응답:**
```json
{
    "pointKey": "M3N4O5P6",
    "originalPointKey": "I9J0K1L2",
    "memberId": 1,
    "cancelledAmount": 300,
    "remainingCancellableAmount": 200,
    "balance": 800,
    "cancelDetails": [
        {
            "originalPointId": 1,
            "cancelledAmount": 300,
            "expired": false
        }
    ],
    "newEarns": []
}
```

> **Note:** 만료된 포인트를 사용 취소할 경우 신규 적립 처리됩니다.

#### 5. 잔액 조회

```http
GET /points/balance/{memberId}
```

**응답:**
```json
{
    "memberId": 1,
    "balance": 1500,
    "points": [
        {
            "pointId": 1,
            "originalAmount": 1000,
            "remainingAmount": 500,
            "manual": true,
            "expirationDate": "2026-01-08T10:00:00"
        },
        {
            "pointId": 2,
            "originalAmount": 1000,
            "remainingAmount": 1000,
            "manual": false,
            "expirationDate": "2026-06-08T10:00:00"
        }
    ]
}
```

#### 6. 거래 내역 조회

```http
GET /points/transactions/{memberId}
```

**응답:**
```json
[
    {
        "pointKey": "A1B2C3D4",
        "type": "EARN",
        "amount": 1000,
        "orderId": null,
        "cancelledAmount": 0,
        "createdAt": "2025-01-08T10:00:00"
    },
    {
        "pointKey": "I9J0K1L2",
        "type": "USE",
        "amount": 500,
        "orderId": "ORDER-001",
        "cancelledAmount": 0,
        "createdAt": "2025-01-08T11:00:00"
    }
]
```

### 설정 API

#### 1. 전체 설정 조회

```http
GET /configs
```

**응답:**
```json
[
    {
        "id": 1,
        "configKey": "MAX_EARN_AMOUNT",
        "configValue": "100000",
        "description": "1회 최대 적립 가능 포인트"
    },
    {
        "id": 2,
        "configKey": "MIN_EARN_AMOUNT",
        "configValue": "1",
        "description": "1회 최소 적립 가능 포인트"
    },
    {
        "id": 3,
        "configKey": "MAX_BALANCE_PER_MEMBER",
        "configValue": "1000000",
        "description": "개인별 최대 보유 가능 포인트"
    }
]
```

#### 2. 설정 변경

```http
PUT /configs/{key}
Content-Type: application/json

{
    "value": "200000"
}
```

**설정 키 목록:**

| Key | Default | Description |
|-----|---------|-------------|
| MAX_EARN_AMOUNT | 100000 | 1회 최대 적립 가능 포인트 |
| MIN_EARN_AMOUNT | 1 | 1회 최소 적립 가능 포인트 |
| MAX_BALANCE_PER_MEMBER | 1000000 | 개인별 최대 보유 가능 포인트 |
| DEFAULT_EXPIRY_DAYS | 365 | 기본 만료일 (일) |
| MIN_EXPIRY_DAYS | 1 | 최소 만료일 (일) |
| MAX_EXPIRY_DAYS | 1825 | 최대 만료일 (5년 미만) |

## 에러 응답

```json
{
    "code": "POINT_001",
    "message": "적립 금액이 유효하지 않습니다: Amount must be between 1 and 100000",
    "timestamp": "2025-01-08T10:00:00"
}
```

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| POINT_001 | 400 | 적립 금액이 유효하지 않습니다 |
| POINT_002 | 400 | 최대 보유 가능 포인트를 초과합니다 |
| POINT_003 | 400 | 만료일이 유효하지 않습니다 |
| POINT_004 | 404 | 트랜잭션을 찾을 수 없습니다 |
| POINT_005 | 404 | 포인트를 찾을 수 없습니다 |
| POINT_006 | 400 | 사용된 포인트는 적립 취소할 수 없습니다 |
| POINT_007 | 400 | 유효하지 않은 트랜잭션 타입입니다 |
| POINT_008 | 400 | 잔액이 부족합니다 |
| POINT_009 | 400 | 사용 금액이 유효하지 않습니다 |
| POINT_010 | 400 | 주문번호가 필요합니다 |
| POINT_011 | 400 | 취소 가능 금액을 초과합니다 |
| POINT_012 | 400 | 취소 금액이 유효하지 않습니다 |

## 프로젝트 구조

```
src/main/java/com/point/
├── PointApplication.java          # 메인 애플리케이션
├── config/
│   ├── PointConfigKey.java        # 설정 키 상수
│   └── PointProperties.java       # 설정 프로퍼티
├── controller/
│   ├── PointController.java       # 포인트 API 컨트롤러
│   └── ConfigController.java      # 설정 API 컨트롤러
├── domain/
│   ├── Point.java                 # 포인트 엔티티
│   ├── PointConfig.java           # 설정 엔티티
│   ├── PointStatus.java           # 포인트 상태 enum
│   ├── PointTransaction.java      # 트랜잭션 엔티티
│   ├── PointUsageDetail.java      # 사용 상세 엔티티
│   └── TransactionType.java       # 트랜잭션 타입 enum
├── dto/                           # 요청/응답 DTO
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── PointErrorCode.java
│   └── PointException.java
├── repository/                    # JPA 리포지토리
└── service/
    ├── PointConfigService.java    # 설정 서비스
    └── PointService.java          # 포인트 서비스

src/main/resources/
├── application.yml                # 애플리케이션 설정
└── docs/
    ├── ERD.md                     # ERD 문서
    └── AWS_Architecture.md        # AWS 아키텍처 문서
```

## 설계 특징

### 1. 포인트 추적

- 각 적립 단위(Point)별로 원래 금액과 잔액을 관리
- PointUsageDetail을 통해 어떤 주문에서 어떤 포인트가 사용되었는지 1원 단위로 추적 가능

### 2. 사용 우선순위

1. 수기 지급 포인트(manual=true) 우선
2. 만료일이 짧은 순서로 사용

### 3. 설정 관리

- DB 기반 설정으로 하드코딩 없이 설정 변경 가능
- 애플리케이션 재시작 없이 설정 변경 적용

### 4. 사용 취소 시 만료 처리

- 만료된 포인트를 사용 취소하면 신규 포인트로 적립 처리
- 만료되지 않은 포인트는 원래 포인트에 복원

## 문서

- [ERD](src/main/resources/docs/ERD.md)
- [AWS Architecture](src/main/resources/docs/AWS_Architecture.md)
