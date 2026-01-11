# WebSocket 티켓 인증 시스템

## 개요

브라우저 WebSocket API는 handshake 시 커스텀 HTTP 헤더를 지원하지 않습니다.
이 제약을 해결하기 위해 **일회용 티켓 기반 인증** 시스템을 도입했습니다.

## 왜 티켓 방식인가?

### 브라우저 WebSocket의 한계

| 방식 | 브라우저 지원 | 보안 |
|------|-------------|------|
| Authorization 헤더 | ❌ 불가 | - |
| 쿠키 | ⚠️ CSWSH 취약 | 낮음 |
| Query Param (JWT) | ✅ 가능 | ⚠️ 로그 노출 |
| **Query Param (Ticket)** | ✅ 가능 | ✅ 안전 |

### 티켓 방식의 장점

1. **일회용**: 사용 즉시 폐기되어 재사용 불가
2. **단기 TTL**: 30초 후 자동 만료
3. **JWT 미노출**: 실제 인증 토큰이 URL에 노출되지 않음
4. **로그 안전**: 서버 로그에 찍혀도 이미 무효화된 티켓

## 아키텍처

### 인증 플로우

```
┌─────────────┐                         ┌─────────────┐                    ┌───────┐
│   Client    │                         │   Server    │                    │ Redis │
└──────┬──────┘                         └──────┬──────┘                    └───┬───┘
       │                                       │                               │
       │ 1. POST /chat/ticket                  │                               │
       │    Header: accessToken: <JWT>         │                               │
       ├──────────────────────────────────────→│                               │
       │                                       │                               │
       │                                       │ 2. JWT 검증                   │
       │                                       │                               │
       │                                       │ 3. 티켓 생성 (UUID)           │
       │                                       │                               │
       │                                       │ 4. SET ws-ticket:{uuid}       │
       │                                       │    {userId, username}         │
       │                                       │    TTL=30s                    │
       │                                       ├──────────────────────────────→│
       │                                       │                               │
       │ 5. { ticket: "uuid", expiresIn: 30 }  │                               │
       │←──────────────────────────────────────┤                               │
       │                                       │                               │
       │ 6. WS /chat?ticket={uuid}             │                               │
       ├──────────────────────────────────────→│                               │
       │                                       │                               │
       │                                       │ 7. GETDEL ws-ticket:{uuid}    │
       │                                       ├──────────────────────────────→│
       │                                       │                               │
       │                                       │ 8. {userId, username}         │
       │                                       │←──────────────────────────────┤
       │                                       │                               │
       │                                       │ 9. 티켓 검증 성공             │
       │                                       │    세션 등록                  │
       │                                       │                               │
       │          10. WebSocket Connected ✅   │                               │
       │←─────────────────────────────────────→│                               │
```

### 컴포넌트 구조

```
feature/chat/
├── ticket/
│   ├── WsTicket.kt              # 응답 모델
│   ├── WsTicketService.kt       # 티켓 생성/검증 로직
│   └── WsTicketController.kt    # REST 엔드포인트
├── ChatHandler.kt               # WebSocket 핸들러 (티켓 검증)
└── ChatConfiguration.kt         # WebSocket 라우팅
```

## 보안 고려사항

### 구현된 보안 조치

| 보안 위협 | 대응 방안 |
|----------|----------|
| 티켓 재사용 | Redis GETDEL로 조회 즉시 삭제 |
| 티켓 탈취 | 30초 TTL로 공격 윈도우 최소화 |
| CSWSH | Origin 헤더 검증 |
| DoS | Rate limiting (향후 구현) |

### Origin 검증

```kotlin
private val allowedOrigins: Set<String> by lazy {
    allowedOriginsConfig.split(",").map { it.trim() }.toSet()
}

val origin = session.handshakeInfo.headers.origin
if (origin != null && origin !in allowedOrigins) {
    return session.close(CloseStatus.POLICY_VIOLATION)
}
```

## 설정

### application.properties

```properties
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
ws.ticket.ttl-seconds=30
ws.allowed-origins=https://takealook.app,http://localhost:3000
```

### 환경변수

| 변수 | 설명 | 기본값 |
|------|------|-------|
| REDIS_HOST | Redis 호스트 | localhost |
| REDIS_PORT | Redis 포트 | 6379 |
| WS_TICKET_TTL | 티켓 유효 시간 (초) | 30 |

## 관련 파일

- [WsTicketService.kt](../../feature/chat/src/main/kotlin/com/takealook/chat/ticket/WsTicketService.kt)
- [ChatHandler.kt](../../feature/chat/src/main/kotlin/com/takealook/chat/ChatHandler.kt)
- [SecurityConfig.kt](../../feature/auth/src/main/kotlin/com/takealook/auth/configuration/SecurityConfig.kt)
