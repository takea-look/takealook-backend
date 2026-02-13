# 푸시 서버(초안)

## 목표
- 서버 측에서 "푸시 발송"을 트리거할 수 있는 최소 API를 제공
- 실제 provider(Firebase/APNS 등) 연동은 추후 단계로 분리

## 현재 구현
- Endpoint: `POST /push/send`
- Provider: `push.provider` 설정값(기본 `noop`)
- 동작: 로그만 남기고 `accepted=true` 반환

## 다음 단계
- [ ] provider 선정(FCM/APNS)
- [ ] device token 저장/갱신 API
- [ ] 발송 실패 재시도/레이트리밋
- [ ] 운영 모니터링(발송 성공/실패율)
