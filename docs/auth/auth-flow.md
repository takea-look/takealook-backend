# 인증 플로우 & 엔드포인트 정리

## 목표 (MVP)
- **SNS OAuth-first** 고정
- 현재 클라이언트가 실제로 사용할 경로는 `POST /auth/google/signin` 하나로 통일
- 기존 비밀번호 기반 로그인/회원가입은 단계적으로 중단 (Backward compatibility만 유지)

## 지원 Provider 정의

### 실제 지원
- `google`

### 계획 중 (실패 응답)
- `apple` (501)
- `kakao` (501)

`/auth/google/signin`은 `GoogleLoginRequest`를 받아 구글 ID token을 검증하고 내부 JWT(`accessToken`, `refreshToken`)를 발급합니다.

## 공통 스키마

### Request / `POST /auth/google/signin`

```json
{ "idToken": "<google-id-token>" }
```

### Response 성공

```json
{
  "accessToken": "<jwt access>",
  "refreshToken": "<jwt refresh>"
}
```

### Response 실패(미지원 Provider)

```json
{
  "status": 501,
  "reason": "UNSUPPORTED_SOCIAL_PROVIDER",
  "message": "Apple provider is planned. Current MVP supported provider: google. Use /auth/google/signin for sign-in."
}
```

## API 정합성 체크리스트

- 비밀번호 경로: `POST /auth/signin`, `POST /auth/signup`
  - 구현상 `410 GONE` (AUTH_FLOW_DEPRECATED)로 동작
  - FE 신규 연동에서 호출하면 안 됨
- OAuth 경로: `POST /auth/google/signin`, `POST /auth/apple/signin`, `POST /auth/kakao/signin`
  - google만 유효(200), apple/kakao는 501
- 토큰 갱신: `POST /auth/refresh`
  - `refreshToken`을 받아 새로운 `accessToken` 발급

## 에러 포맷

모든 인증 처리 실패는 공통 `ErrorResponse` 반환

```json
{
  "status": 401,
  "reason": "INVALID_CREDENTIALS",
  "message": "Invalid username or password"
}
```

지원 코드:
- `AUTH_FLOW_DEPRECATED`: 과거 로그인/회원가입 API 사용 시(410)
- `UNSUPPORTED_SOCIAL_PROVIDER`: Apple/Kakao 미지원 시(501)
- `INVALID_CREDENTIALS`: 토큰 유효성/재발급 실패(401)
