# 로깅/모니터링 최소 셋업(초안)

## 목표
- 운영 장애 시 최소한의 탐지(에러 로그)와 추적이 가능할 것

## 로깅
- 기본 정책: **표준 출력(JSON은 추후)**
- 중요 포인트(이미 로그 존재):
  - 인증 실패/예외(GlobalExceptionHandler)
  - WebSocket handshake 실패(ticket/roomId 누락, origin 불일치)
  - 업로드 key/size 정책 위반(IllegalArgumentException)

## 모니터링(제안)
- 1차: infra 로그 수집 + 알림
- 2차: Spring Actuator 도입 후
  - `/actuator/health`, `/actuator/metrics` 노출(내부망/인증 필수)

## 체크리스트
- [ ] error log rate 알림
- [ ] WS 연결 실패율(Policy violation/Bad data) 추적
- [ ] presigned url 발급 실패율 추적
