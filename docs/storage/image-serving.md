# 이미지 서빙 도메인(img.takealook.my) & 캐시 정책(초안)

## 목표
- 업로드된 이미지 URL이 안정적으로 열릴 것
- 트래픽/비용 최적화를 위해 브라우저/중간 캐시가 잘 동작할 것

## 구성(제안)
- Storage: Cloudflare R2
- Public image domain: `https://img.takealook.my/...`
  - R2 Public Bucket 또는 Cloudflare(Worker/Rules) 기반 프록시로 라우팅

## URL / Key 규칙
- 업로드 키: `chat/{roomId}/{timestamp}.{ext}`
  - (presigned upload 정책과 동일)

## Cache-Control/ETag 정책(제안)
### 원칙
- 채팅 이미지(스티커/첨부)는 **immutable**로 다루는 것을 기본으로 한다.
  - 동일 key에 대한 overwrite를 금지(또는 overwrite 시 새 key 발급)
  - 이 전제면 장기 캐시가 안전함

### 권장 헤더(예시)
- `Cache-Control: public, max-age=31536000, immutable`
- `ETag`: 객체 해시 기반(스토리지 제공 값 활용)

### 주의
- 만약 같은 key에 overwrite를 허용하면 `immutable`은 위험해짐
  - 이 경우 max-age를 짧게(예: 1h~1d) 또는 버저닝 쿼리/경로 필요

## 접근 권한 정책(초안)
- 기본: 채팅 이미지(스티커/첨부)는 공개 URL 허용
- 향후: 비공개 채팅/민감 콘텐츠는
  - 서명된 GET URL 또는
  - 앱 서버 프록시 + access token 검증
  중 하나로 전환 가능

## 운영 체크리스트
- [ ] DNS: `img.takealook.my` → Cloudflare
- [ ] R2 bucket public access 또는 proxy route 설정
- [ ] Response headers(Cache-Control/ETag) 적용 확인
- [ ] 업로드 후 바로 접근/캐시 히트 확인
