# chup-v3

CHUP 모노레포. 앱마다 `apps/` 아래 독립된 디렉터리로 관리한다.

## 구조

| 경로 | 내용 |
|------|------|
| `apps/server/` | 백엔드 (Kotlin + Spring Boot 4, Gradle 단일 모듈, 패키지 `team.themoment.thup`) |
| `.github/workflows/` | 앱별 CI/CD. `paths` 필터로 해당 앱이 바뀔 때만 실행 |
| `.claude/` `.agents/` `.codex/` `.gemini/` `.harness/` | AI 에이전트 설정 (harness 동기화 대상) |

## 경로 규칙

- Gradle 명령은 저장소 루트가 아니라 **`apps/server`에서** 실행한다.
  - 예: `cd apps/server && ./gradlew test`, `cd apps/server && ./gradlew ktlintFormat`
  - 에이전트·스킬 문서의 `./gradlew ...`, `src/main/kotlin/...`, `src/test/kotlin/...` 경로는 모두 `apps/server/` 기준으로 해석한다.
- 서버는 단일 모듈이므로 `:<module>:test` 대신 `:test`를 쓴다.
- 로컬 MySQL/Redis: `cd apps/server && docker compose up -d` (MySQL 3307, Redis 6379)
