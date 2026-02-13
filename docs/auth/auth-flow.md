# 인증 플로우 & 엔드포인트 정리(초안)

> 이 문서는 FE/BE가 구현해야 할 인증 플로우를 **하나로 고정**하기 위한 정리 문서입니다.
> 현재 코드베이스는 `password login(/auth/signin)`과 `toss oauth(/auth/toss/*)`가 공존합니다.

## 1) 현재 존재하는 엔드포인트

### Password login
- `POST /auth/signup`
- `POST /auth/signin` → 내부 JWT 발급(`LoginResponse.token`)

### Toss login
- `POST /auth/toss/signin` → 내부 JWT + toss refresh token(`LoginResponse.refreshToken`)
- `POST /auth/toss/refresh` → **현재 구현은 toss access token** 재발급(내부 JWT 재발급이 아님)
- `GET /auth/toss/userinfo`
- `POST /auth/toss/logout`
- `POST /auth/toss/logout/user-key`

## 2) 문제점(결정 필요)
- FE 입장: 내부 JWT가 필요한데 `/auth/toss/refresh`는 toss token을 재발급함
- refresh token 정책(access/refresh 만료/rotation) 미정
- 401 처리/refresh 흐름이 단일화돼 있지 않음

## 3) 제안 플로우(옵션)

### Option A. Toss only (권장: 제품 의사결정 필요)
- 로그인: `/auth/toss/signin` → **내부 access token(JWT)** + **내부 refresh token** 발급
- refresh: `POST /auth/refresh` (새 엔드포인트)로 내부 JWT 재발급
- `/auth/signin`(password)은 deprecate

### Option B. Password only (개발 편의)
- `/auth/signin`만 유지
- toss 관련 엔드포인트는 제거/비활성

### Option C. Mixed (비권장)
- 두 플로우 유지하되, refresh 정책을 통일(내부 refresh token 도입)

## 4) 토큰 정책(초안)
- access token(JWT): `JWT_EXP` (default 1h)
- refresh token: 서버 저장/회전(rotation) 필요 여부 결정
- 401 시 FE 동작:
  1) refresh 시도
  2) 실패 시 재로그인 유도

## 5) 결론(결정 체크)
- [ ] 최종 로그인 전략: Toss only / Password only / Mixed
- [ ] 내부 refresh token 도입 여부
- [ ] 유지할 엔드포인트 목록 확정
