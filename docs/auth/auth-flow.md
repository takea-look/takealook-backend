# 인증 플로우 & 엔드포인트 정리

## 목표 (MVP)
- **SNS OAuth-first** 고정
- 현재 클라이언트가 실제로 사용할 경로는 `POST /auth/google/signin` 하나로 통일
- 기존 비밀번호 기반 로그인/회원가입은 단계적으로 중단 (Backward compatibility만 유지)

## 지원 Provider 정의

### 실제 지원
- `google`

### 계획 중 (미지원)
- `apple` (501)
- `kakao` (501)

## 1) OAuth/토큰 계약

### 공통 규칙

- Authorization 헤더는 JWT 기반: `Authorization: Bearer <accessToken>`
- 레거시 호환: `accessToken` 헤더도 허용(폴백)
- 모든 에러는 `ErrorResponse`(
  `{ status, reason, message }`) 형식으로 반환
- 429 발생 시 `Retry-After` 헤더를 반환(초 단위)

### 요청 플로우(실제 동작)

#### Google 로그인

1. 클라이언트가 Google OAuth를 수행해서 `idToken` 획득
2. `POST /auth/google/signin`에 `GoogleLoginRequest` 전송
   - `{"idToken": "<google-id-token>"}`
3. 최초 로그인인 경우 계정 자동 생성 후 JWT 발급
4. 성공 시 access + refresh 한 번에 반환

#### OAuth Callback/리다이렉트 정렬

- 백엔드에는 OAuth callback 라우트가 없음
- 즉, 클라이언트(웹/앱)는 **OAuth IdP에서 발급한 idToken**을 받아 `/auth/google/signin`으로 전달해야 함
- Apple/Kakao 연동 시도는 현재 라우팅만 존재하고 실제 발급은 미지원

### 엔드포인트별 계약

#### `POST /auth/google/signin`
- Request: `GoogleLoginRequest`
  - `idToken: string`
- 성공(200): `LoginResponse`
  - `accessToken: string`
  - `refreshToken: string`
- 실패
  - Google 토큰 파싱/검증 실패: `INVALID_CREDENTIALS` (401)
  - 내부/네트워크 예외: 구현 규칙에 따라 5xx 또는 라우팅된 오류

#### `POST /auth/apple/signin`, `POST /auth/kakao/signin`
- Request: `Map<String,String>`(빈 payload 허용)
- 현재 상태: 미지원
- 성공: 없음(구현에서 501)
- 실패: `UNSUPPORTED_SOCIAL_PROVIDER` (501)

#### `POST /auth/refresh`
- Request: `RefreshTokenRequest`
  - `refreshToken: string`
- 성공(200): `LoginResponse`
  - `accessToken: string`
  - `refreshToken: null` (명시적으로 미반환)
- 실패: 토큰 무효/만료 시 `INVALID_CREDENTIALS` (401)

#### `POST /auth/signin`, `POST /auth/signup`
- 구현상 `410 GONE`
- `AUTH_FLOW_DEPRECATED`로 마이그레이션 유도
- FE 신규 연동에서 호출 금지

### access/refresh 동작 정렬

- `accessToken` / `refreshToken`은 동일 서명 및 동일 만료 정책(현재 코드: `jwt.expiration-time`)으로 발급
- `/auth/refresh`는 `refreshToken`의 subject를 검증해 새 `accessToken`을 재발급
- 토큰 갱신 API는 access 토큰만 갱신하고, refresh 토큰은 응답에서 `null`

## 2) 테스트 케이스(정렬 기준)

아래 케이스는 FE 연동/QA에서 우선 검증 권장 항목:

1. Google 로그인 성공: 유효 `idToken` -> `200` + `accessToken`,`refreshToken` 존재
2. Google 로그인 실패: 위조/만료 토큰 -> `401 INVALID_CREDENTIALS`
3. Apple/Kakao 로그인: 임의 idToken -> `501 UNSUPPORTED_SOCIAL_PROVIDER`
4. Refresh 토큰 갱신 성공: 기존 refresh로 `200` + `accessToken`만 반환
5. Refresh 토큰 만료/변조: `401 INVALID_CREDENTIALS`
6. Rate limit 초과 시나리오:
   - 동일 클라이언트 연속 호출 후 `429` + `Retry-After` 확인
7. Legacy endpoint 호출:
   - `/auth/signin`, `/auth/signup` → `410 AUTH_FLOW_DEPRECATED`

## 3) FE 연동 가이드

- 권장 호출 순서: `Google sign-in -> /auth/google/signin -> accessToken 저장 -> API 호출`
- 만료 토큰 탐지/재인증:
  - `401 INVALID_CREDENTIALS` 수신 시 login 화면로 이동
- `refreshToken`은 secure storage에 별도 관리하고, 백그라운드 갱신 실패 시 re-login

## API 정합성 체크리스트

- OAuth 경로: `/auth/google/signin`, `/auth/apple/signin`, `/auth/kakao/signin`
  - google만 200, apple/kakao는 501
- 토큰 갱신: `/auth/refresh`
  - `refreshToken` -> 새 `accessToken` 발급
- 비밀번호 경로:
  - `/auth/signin`, `/auth/signup`은 `410 GONE`로 동작
  - FE 신규 연동에서 호출 금지

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
