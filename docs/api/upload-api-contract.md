# Chat Image Upload API Contract

> 미디어 업로드(파이프라인/제한/오류/운영 지표) 기준을 v1로 정리한다.

## 1) 업로드 전략

- 기본 전략: **Presigned URL (PUT)** only
- Multipart upload 미지원
- 클라이언트 업로드 방식: `PUT` 바이너리 전송(직접 object key 사용)
- 지원 endpoint: `/uploads/presign`, `/storage/upload`

## 2) 허용 정책 (MIME/확장자/사이즈)

### 지원 MIME
- `image/png`
- `image/jpeg`
- `image/jpg`
- `image/webp`

### 경로/확장자
- key prefix: `chat/`
- key format(필수): `chat/{roomId}/{timestamp}.{ext}`
- 예시: `chat/12/1700000000000.png`
- 확장자-Content-Type 불일치 시 `400` 처리

### 사이즈
- 기본 최대: `cloud.r2.maxUploadBytes` (`10MB` 기본값)
- `sizeBytes`가 요청/쿼리에 있으면 사전 검증 후 초과면 `400`
- 기본적으로 클라이언트도 업로드 전에 미리 용량 검사 권장

## 3) Endpoint 계약

### 3.1 `POST /uploads/presign`

Request:
```json
{ "roomId": 12, "contentType": "image/png", "sizeBytes": 1024 }
```

Response (`200`):
```json
{
  "url": "https://<r2-presigned-url>",
  "key": "chat/12/1700000000000.png",
  "canonicalUrl": "https://img.takealook.my/chat/12/1700000000000.png",
  "headers": { "Content-Type": "image/png" },
  "maxUploadBytes": 10485760,
  "expiresInSeconds": 600
}
```

- `key`는 채팅방 단위 분산(key prefix-roomId)로 생성
- `expiresInSeconds`는 `cloud.r2.presignTtlMinutes * 60`
- 실패 응답 예시:

```json
{ "status": 400, "reason": "INVALID_REQUEST", "message": "Unsupported mime type: ..." }
```

### 3.2 `GET /storage/upload`

Query:
- `key`: 업로드 객체 키 (필수)
- `sizeBytes`(optional)
- `contentType`(optional)

Response: `UploadResponse`(`200`, `/uploads/presign`와 동일 구조)

- 실패 시 key/사이즈/MIME 제약 위반은 `400`, 권한/인증 문제는 `401/403`, rate-limit은 `429`

## 4) 클라이언트 업로드 플로우

1. 클라이언트가 roomId/contentType로 `/uploads/presign` 호출
2. 응답에 포함된 `url` + `headers.Content-Type`으로 PUT 업로드
3. 업로드 성공 후 채팅 메시지 전송시 `canonicalUrl` 사용

## 5) 업로드 실패/타임아웃/권한 에러 스펙

아래 케이스는 구현/운영에서 구분 권장:

| 실패 유형 | 원인 | 권장 응답 | 프론트 대응 |
|---|---|---:|---|
| MIME/확장자 불일치 | `contentType`-`key` mismatch, 지원 안 하는 MIME | `400 INVALID_REQUEST` | 사용자에게 파일 형식 재선택 안내 |
| 허용 크기 초과 | `sizeBytes > 10MB`(설정값) | `400 INVALID_REQUEST` | 파일 압축/사이즈 축소 후 재시도 |
| 업로드 URL 만료 | Presign TTL 경과 후 PUT 요청 | `403 Forbidden` / `401`(서버 구현에 따라) | 키/URL 재발급 후 재업로드 |
| 업로드 타임아웃/네트워크 오류 | 클라이언트 PUT 실패 | 네트워크 레벨 오류 or `5xx` | 지수 백오프로 동일 키 재시도(지속 실패 시 재발급) |
| 권한/토큰 문제 | 인증 실패, 만료 | `401/403` | accessToken 갱신 or 재로그인 후 presign 재요청 |

## 6) S3/R2 경로 전략

- Prefix: `chat/{roomId}/...`
- 파일명: `{epochMillis}.{ext}`
- 특징:
  - room 단위 파티셔닝으로 핫스팟 분산
  - 동일 room/키 재사용을 피해 overwrite 우회
  - 동일 key overwrite 불허 정책 권장(멱등성/캐시 오류 방지)
- canonical URL: `${publicBaseUrl}/{key}`

### Retention (권장)
- 운영 초안: 업로드 원본 이미지 메타데이터 및 객체 저장은 **최소 90일** 보존, 삭제 정책은 채팅 정책 확정 시 정식 반영
- 장기 보존 필요시 외부 아카이브/TTL 계층 분리 권장

## 8) 모니터링 지표(초안)

- 업로드 파이프라인 지표:
  - `takealook_upload_presign_requests_total`
  - `takealook_upload_url_requests_total`
  - `takealook_abuse_rate_limited_total{scope="upload",endpoint=...}`
- 성능/신뢰 지표(권장 추가):
  - `takealook_upload_presign_latency_ms`
  - `takealook_upload_upload_seconds_count`(업로드 결과/실패율 이벤트)
  - `takealook_upload_put_fail_total`

## 9) 운영 체크리스트

- Presigned URL TTL 재발급 경로 테스트
- 업로드 실패 시나리오(크기초과/타임아웃/권한) E2E 검증
- `img.takealook.my/{roomId}/...` URL 캐시/접근 정책 점검
