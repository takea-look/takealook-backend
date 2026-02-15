# 인증 플로우 & 엔드포인트 정리(초안)

> SNS OAuth-first 인증으로 통일하고, 비밀번호 기반 `/auth/signin`, `/auth/signup`은 **점진적으로 중단**합니다.

## 현재 지원되는 인증 흐름 (MVP)

- `POST /auth/google/signin` (request: `{ idToken }`) → 내부 JWT access + refresh 발급
- `POST /auth/refresh` (request: `{ refreshToken }`) → access token 재발급
- `POST /auth/apple/signin` / `POST /auth/kakao/signin`는 **현재 미지원(미구현)**. 엔드포인트는 존재하나 `501` 반환.

## 비밀번호 기반 인증

- `POST /auth/signin`
- `POST /auth/signup`

현재 두 API는 `410 GONE` + 공통 ErrorResponse(`AUTH_FLOW_DEPRECATED`)로 동작하며,
구현 의도상 새 클라이언트에서는 호출하면 안 됩니다.

## 에러 포맷

모든 인증 처리 실패는 `ErrorResponse`로 래핑합니다.

```json
{
  "status": 401,
  "reason": "INVALID_CREDENTIALS",
  "message": "Invalid username or password"
}
```


- `AUTH_FLOW_DEPRECATED`: 과거 로그인/회원가입 API 사용 시(410)
- `UNSUPPORTED_SOCIAL_PROVIDER`: Apple/Kakao 미지원 시(501)
- `INVALID_CREDENTIALS`: 토큰 유효성/토큰 재발급 실패(401)
