# Courtboard API

농구 전술 보드(Courtboard) 서비스의 백엔드 API 서버. `multimodule-exam` 멀티모듈 프로젝트의 하위 모듈(`ma-courtboard-api`)이며, `module-common`에 의존한다.

## Tech Stack

- **Language**: Kotlin
- **Framework**: Spring Boot (Web, JPA, Validation, Mail, Actuator)
- **Database**: PostgreSQL (Casbin 룰도 DB에 저장 — `tbl_casbin_rule`)
- **Auth**: JWT (jjwt 0.12.5) + Casbin RBAC
- **Logging**: Log4j2 (Logback 제외됨)
- **Password Hashing**: jBCrypt

## Project Structure

```
src/main/kotlin/me/courtboard/api/
├── aop/                  # @CheckPerm 어노테이션 + AOP 권한 검사
├── api/
│   ├── board/            # 게시판 도메인
│   │   ├── BoardController.kt    # CRUD + POST /api/board/images (이미지 업로드)
│   │   ├── entity/               # BoardEntity, BoardImageEntity
│   │   ├── dto/                  # BoardReqDto, BoardResDto, BoardListResDto, BoardImageResDto
│   │   ├── repository/           # BoardRepository, BoardImageRepository
│   │   ├── service/              # BoardService, BoardImageService
│   │   └── util/BoardHtmlSanitizer.kt
│   ├── internal/                 # InternalController (loopback 전용 운영 엔드포인트)
│   ├── member/                   # 회원 도메인
│   ├── my/                       # 내 정보 API
│   └── tactics/                  # 전술 도메인
├── component/
│   ├── JwtProvider.kt    # Access/Refresh Token 발급·검증
│   └── CustomMailSender.kt
├── config/               # WebConfig (CORS + /uploads 정적 리소스 핸들러), BeanConfig
├── filter/
│   ├── AuthFilter.kt          # JWT 파싱 → CourtboardContext(ThreadLocal) 세팅
│   └── UploadsHeaderFilter.kt # /uploads 응답에 nosniff/CSP/Content-Disposition 강제
├── global/
│   ├── CourtboardContext.kt  # ThreadLocal 기반 요청별 유저 컨텍스트
│   ├── Constants.kt          # 역할 상수: admin / user / guest
│   ├── dto/ApiResult.kt      # 통일된 API 응답 포맷
│   └── error/                # CustomExceptionHandler, CustomRuntimeException
├── listener/             # StartupListener, ShutdownListener
└── util/PasswordUtil.kt

src/main/resources/
├── application.yml / application-dev.yml / application-prod.yml
├── casbin/                # Casbin 모델/정책
└── db/V01__board_image.sql # tbl_board_image DDL (운영 DB에 수동 적용)
```

## Architecture

**Controller → Service → Repository** 레이어드 아키텍처. Spring Security 미사용 — 인증/인가는 직접 구현.

### 인증 흐름

1. `AuthFilter`가 `Authorization: Bearer <token>` 헤더를 파싱
2. JWT 유효 시 → Casbin에서 역할 조회 → `CourtboardContext`에 `RequestContext(memberId, role)` 저장
3. JWT 없으면 → `RequestContext(GUEST_ID, ROLE_GUEST)` 저장
4. JWT 만료 시 → 401 + `ApiResult.error("expired jwt token")` 응답

### 인가 흐름

- `@CheckPerm` 어노테이션이 붙은 메서드 → `CheckPermAop`가 Casbin Enforcer로 권한 검사
- 역할 계층: `admin` > `user` > `guest`

### API 응답 포맷

모든 응답은 `ApiResult<T>`로 감싸서 반환:

```json
{ "success": true, "data": { ... } }
{ "success": false, "message": "error message" }
```

## Build & Deploy

```bash
# 빌드 (멀티모듈 루트에서 실행)
./gradlew :ma-courtboard-api:build

# 배포 스크립트 (버전 인자 필수)
./deploy.sh 1.9.5
# → Gradle 빌드 → Docker 이미지 빌드 (linux/amd64) → tar 저장 → SCP 업로드
```

- Docker 이미지는 `linux/amd64` 플랫폼으로 빌드 (서버가 x86_64)
- 서버에서 수동으로 `docker load` 및 `docker run` 실행 필요

## Configuration

- `application.yml` — 기본값 (로컬 개발용 더미 설정 포함)
- `application-dev.yml` — 개발 환경
- `application-prod.yml` — 운영 환경
- Casbin 모델/정책: `src/main/resources/casbin/`

## Production Tuning (application-prod.yml)

서버 스펙이 **1 vCPU / 1.5GB RAM**이므로 아래 값을 반영해둔다.

### Tomcat Thread Pool

```yaml
server:
  tomcat:
    threads:
      max: 20        # 기본 200 → 1 CPU 기준 컨텍스트 스위칭 최소화
      min-spare: 5   # 기본 10
    accept-count: 30 # 요청 큐 크기 (기본 100)
    connection-timeout: 10000
```

### HikariCP Connection Pool

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 5   # 기본 10 → Supabase PgBouncer가 외부 풀링하므로 앱 단 5개 충분
      minimum-idle: 2        # 기본 10 → idle 커넥션 비용 절감
      connection-timeout: 30000
      max-lifetime: 115000   # PgBouncer 기본값(120s)보다 짧게 유지
      leak-detection-threshold: 2000
```

## Key Conventions

- **DTO 네이밍**: `*ReqDto` (요청), `*ResDto` (응답)
- **Controller 분리**: 일반 사용자용과 admin/manage용 Controller가 별도 파일로 분리됨 (예: `TacticsController` vs `TacticsAdminController`)
- **전술 데이터**: `formations`, `playerInfo`를 JSON으로 DB `states` 컬럼에 저장
- **게시판 본문 HTML**: `BoardHtmlSanitizer`(jsoup 기반)로 XSS 방지를 위한 sanitize 후 저장. `data:` 프로토콜은 기존 base64 인라인 게시물 호환을 위해 한동안 유지(이전 방식의 잔존물).
- **게시판 수정/삭제 권한**: 작성자 본인만 가능 (PUT/DELETE `/api/board/{id}`에서 author-only 체크)
- **공통 모듈**: `module-common`의 `JsonUtil` 등 유틸을 `me.multimoduleexam.util` 패키지로 임포트

## 게시판 이미지 업로드 / 정적 서빙

게시판 이미지는 **로컬 파일 시스템 + DB 메타데이터** 구조로 저장한다.

### 업로드 파이프라인 (`POST /api/board/images`)

1. `BoardController.uploadImage` — `@CheckPerm` + `@RequestPart("file")` (multipart). Casbin 정책에 `(role, courtboard, /api/board/images, POST)` 등록 필요.
2. `BoardImageService.upload`:
   - **검증**: 빈 파일/`5MB` 초과 거부, MIME 화이트리스트(`image/png|jpeg|webp|gif`), `verifyMagicBytes`로 파일 시그니처가 헤더와 일치하는지 직접 확인 (Content-Type 위장 차단).
3. 프론트가 응답 URL을 게시물 본문 `<img src>`에 임베드.

### Storage 설정

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 6MB

storage:
  path: /tmp/courtboard-uploads   # dev (application-dev.yml)
```

## Testing

```bash
./gradlew :ma-courtboard-api:test
```

- `MemberServiceTest` — Mockito Kotlin 기반 서비스 단위 테스트
- `CasbinPolicyTest` — Casbin 정책 검증 (`src/test/resources/casbin/` 정책 파일 사용)
