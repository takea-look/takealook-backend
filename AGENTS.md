# AGENTS.md (Codex / AI Agent Guide)

이 파일은 takealook-backend에서 Codex/에이전트가 **빠르게 온보딩**하고,
**일관된 Git 협업(브랜치→PR→CI→머지)** 으로 작업하도록 돕는 프로젝트 가이드입니다.

## TL;DR
- 작업은 항상 **브랜치에서** 하고, **PR 생성 후 CI 통과 확인**하고 머지한다.
- 병렬 작업/PR 다중 처리 시 **git worktree**를 기본으로 사용한다.

## Project Overview
- Kotlin + Spring Boot **WebFlux(reactive)** 기반 모듈러 모놀리스
- 주요 기능: 인증(JWT), 채팅(WebSocket), 스티커, 스토리지(Cloudflare R2)

## Repo Structure (high level)
```
app/                 # 엔트리포인트 + 공통 설정
core/                # shared layers (model/domain/data)
feature/             # vertical features (auth/chat/stickers/storage)
build-logic/         # Gradle convention plugins
 taskmaster/         # AI workflow rules (Cursor/Windsurf)
```

## Where to look (common tasks)
- App bootstrap: `app/src/main/kotlin/.../TakealookBackendApplication.kt`
- Bean wiring: `app/src/main/kotlin/.../TklBeanConfiguration.kt`
- Swagger/OpenAPI: `app/src/main/kotlin/.../SwaggerConfiguration.kt`
- DB schema: `app/src/main/resources/schema.sql`
- Auth (JWT/WebFlux security): `feature/auth/src/main/kotlin/com/takealook/auth/`
- Chat(WebSocket): `feature/chat/`
- R2: `feature/storage/`

## Local Dev Commands
```bash
# run
./gradlew :app:bootRun

# build
./gradlew build

# (optional) 특정 모듈만
./gradlew :feature:auth:test
```

## Database
- `schema.sql`을 초기화에 사용한다(로컬/테스트 환경).
- 스키마/마이그레이션 변경은 PR에서 **DDL 순서/참조 무결성**까지 함께 검증한다.

## Git / Collaboration Rules (must)
1) **Branch → Commit → PR → CI green → Merge**
2) PR은 가능한 작게(단일 의도) 유지
3) CI 실패 시: 원인 1줄 요약 + 최소 수정으로 green 복구

### git worktree (권장)
병렬 작업/충돌 회피를 위해 worktree를 기본으로 사용:
```bash
# 새 작업용 worktree
mkdir -p /tmp/takealook
cd <repo>
git fetch origin

git worktree add -b fix/<topic> /tmp/takealook/fix-<topic> origin/main
cd /tmp/takealook/fix-<topic>

# 작업 후
git push -u origin fix/<topic>
# PR 생성 후 머지되면
cd <repo>
git worktree remove /tmp/takealook/fix-<topic>
```

## Codex CLI Usage (suggested)
- 구현 작업: `codex exec --full-auto "..."` (repo 내부에서)
- 프롬프트에 항상 포함:
  - 목표/수용 기준(AC)
  - 금지사항(불필요한 리팩토링/무관 변경 금지)
  - 검증 커맨드
  - Git flow(브랜치→PR→CI→머지)

## Notes / Pitfalls
- WebFlux이므로 blocking 호출을 섞지 말 것(특히 security/filter에서).
- 시크릿/토큰은 절대 커밋 금지(GitHub Secrets/환경변수로).
