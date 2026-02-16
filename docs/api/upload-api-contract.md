# Chat Image Upload API Contract

## 1) Strategy
- 현재 전략: **Presigned URL (PUT)** only
- Multipart upload는 미사용(미구현)
- FE는 파일 업로드용 바이너리를 `/storage/upload` 또는 `/uploads/presign`로 발급받은 URL에 직접 PUT

## 2) Validation
- 허용 MIME: `image/png`, `image/jpeg`, `image/jpg`, `image/webp`
- 허용 최대 크기: `cloud.r2.maxUploadBytes` (기본 10MB)
- 키 정책: `chat/{roomId}/{timestamp}.{ext}`
- `contentType`과 `key` 확장자가 다르면 400

## 3) Endpoints

### 3.1 `POST /uploads/presign`

Request:
```json
{ "roomId": 12, "contentType": "image/png", "sizeBytes": 1024 }
```

Response:
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

### 3.2 `GET /storage/upload`

Query:
- `key`: 업로드 객체 키
- `sizeBytes`(optional)
- `contentType`(optional, 권장: 타입 선검증)

Response: `UploadResponse` (위와 동일 구조)

## 4) FE Upload Flow
1. FE가 채팅방+타입으로 `/uploads/presign` 호출
2. 응답의 `url`로 `Content-Type` 헤더를 포함해 PUT 업로드
3. 업로드 완료 후 메시지 전송 시 `canonicalUrl` 사용

## 5) Security / notes
- Presigned URL은 TTL(expiry in seconds) 기반이므로 오래된 URL 재사용 제한
- key/size/type mismatch 및 허용 범위 초과는 서버에서 즉시 400 처리
