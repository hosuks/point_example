# AWS Architecture

## Architecture Diagram

```
                                    ┌─────────────────────────────────────────────────────────────┐
                                    │                        AWS Cloud                            │
                                    │                                                             │
    ┌─────────┐                     │  ┌─────────────────────────────────────────────────────┐    │
    │         │                     │  │                    VPC                              │    │
    │  Users  │                     │  │                                                     │    │
    │         │                     │  │  ┌─────────────┐    ┌─────────────┐                 │    │
    └────┬────┘                     │  │  │ Public      │    │ Private     │                 │    │
         │                          │  │  │ Subnet      │    │ Subnet      │                 │    │
         │                          │  │  │             │    │             │                 │    │
         ▼                          │  │  │ ┌─────────┐ │    │ ┌─────────┐ │  ┌───────────┐  │    │
    ┌─────────┐     ┌─────────┐     │  │  │ │   ALB   │ │    │ │   ECS   │ │  │   RDS     │  │    │
    │  Route  │────▶│ Cloud   │     │  │  │ │         │─┼────┼▶│ Fargate │─┼─▶│ (Aurora   │  │    │
    │   53    │     │ Front   │──── ┼──┼──┼▶│         │ │    │ │         │ │  │  MySQL)   │  │    │
    └─────────┘     └─────────┘     │  │  │ └─────────┘ │    │ └─────────┘ │  └───────────┘  │    │
                                    │  │  │             │    │             │                 │    │
                                    │  │  │             │    │ ┌─────────┐ │  ┌───────────┐  │    │
                                    │  │  │             │    │ │ElastiC- │ │  │Parameter  │  │    │
                                    │  │  │             │    │ │ache     │ │  │ Store     │  │    │
                                    │  │  │             │    │ │(Redis)  │ │  │           │  │    │
                                    │  │  │             │    │ └─────────┘ │  └───────────┘  │    │
                                    │  │  └─────────────┘    └─────────────┘                 │    │
                                    │  │                                                     │    │
                                    │  └─────────────────────────────────────────────────────┘    │
                                    │                                                             │
                                    │  ┌─────────────────────────────────────────────────────┐    │
                                    │  │                  Monitoring                         │    │
                                    │  │  ┌───────────┐  ┌───────────┐  ┌───────────┐        │    │
                                    │  │  │CloudWatch │  │CloudWatch │  │  X-Ray    │        │    │
                                    │  │  │  Logs     │  │  Metrics  │  │           │        │    │
                                    │  │  └───────────┘  └───────────┘  └───────────┘        │    │
                                    │  └─────────────────────────────────────────────────────┘    │
                                    │                                                             │
                                    └─────────────────────────────────────────────────────────────┘
```

## Components

### 1. DNS & CDN Layer
- **Route 53**: DNS 관리 및 헬스 체크
- **CloudFront**: CDN 및 정적 콘텐츠 캐싱 (필요시)

### 2. Load Balancing
- **Application Load Balancer (ALB)**
  - HTTPS 터미네이션
  - 경로 기반 라우팅
  - 헬스 체크

### 3. Compute Layer
- **ECS Fargate**
  - 서버리스 컨테이너 실행
  - Auto Scaling 설정
  - 최소 2개 이상의 태스크로 고가용성 확보

### 4. Data Layer
- **Amazon Aurora (MySQL Compatible)**
  - Multi-AZ 배포로 고가용성
  - Read Replica로 읽기 성능 향상
  - 자동 백업 및 Point-in-time Recovery

- **ElastiCache (Redis)**
  - 포인트 잔액 캐싱
  - 분산 락 구현 (동시성 제어)
  - 세션 관리

### 5. Configuration Management
- **AWS Systems Manager Parameter Store**
  - 포인트 설정값 관리 (최대 적립금액, 최대 보유금액 등)
  - 암호화된 비밀 관리
  - 애플리케이션 재시작 없이 설정 변경 가능

### 6. Monitoring & Logging
- **CloudWatch Logs**: 애플리케이션 로그 수집
- **CloudWatch Metrics**: 시스템 메트릭 모니터링
- **CloudWatch Alarms**: 임계값 알림
- **X-Ray**: 분산 트레이싱

## Security Considerations

1. **Network Security**
   - VPC 내 Private Subnet에 애플리케이션 및 데이터베이스 배치
   - Security Group을 통한 접근 제어
   - NAT Gateway를 통한 아웃바운드 인터넷 접근

2. **Data Security**
   - RDS 암호화 (at rest)
   - SSL/TLS 통신 (in transit)
   - IAM 기반 접근 제어

3. **Application Security**
   - WAF (Web Application Firewall) 적용
   - Secrets Manager를 통한 민감 정보 관리

## Scalability

1. **Horizontal Scaling**
   - ECS Auto Scaling: CPU/Memory 기반 자동 확장
   - Aurora Read Replica: 읽기 부하 분산

2. **Performance Optimization**
   - ElastiCache를 통한 자주 조회되는 데이터 캐싱
   - Connection Pooling (HikariCP)

## High Availability

1. **Multi-AZ Deployment**
   - ECS 태스크를 여러 AZ에 분산
   - Aurora Multi-AZ 자동 장애 조치
   - ElastiCache 클러스터 모드

2. **Health Checks**
   - ALB 헬스 체크
   - ECS 태스크 헬스 체크
   - Route 53 헬스 체크 (옵션)