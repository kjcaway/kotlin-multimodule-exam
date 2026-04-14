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
│   ├── member/           # 회원 도메인 (Entity, DTO, Repository, Service, Controller)
│   ├── my/               # 내 정보 API
│   └── tactics/          # 전술 도메인 (Entity, DTO, Repository, Service, Controller)
├── component/
│   ├── JwtProvider.kt    # Access/Refresh Token 발급·검증
│   └── CustomMailSender.kt
├── config/               # WebConfig (CORS 등), BeanConfig
├── filter/
│   └── AuthFilter.kt     # JWT 파싱 → CourtboardContext(ThreadLocal) 세팅
├── global/
│   ├── CourtboardContext.kt  # ThreadLocal 기반 요청별 유저 컨텍스트
│   ├── Constants.kt          # 역할 상수: admin / user / guest
│   ├── dto/ApiResult.kt      # 통일된 API 응답 포맷
│   └── error/                # CustomExceptionHandler, CustomRuntimeException
├── listener/             # StartupListener, ShutdownListener
└── util/PasswordUtil.kt
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

## Key Conventions

- **DTO 네이밍**: `*ReqDto` (요청), `*ResDto` (응답)
- **Controller 분리**: 일반 사용자용과 admin/manage용 Controller가 별도 파일로 분리됨 (예: `TacticsController` vs `TacticsAdminController`)
- **전술 데이터**: `formations`, `playerInfo`를 JSON으로 DB `states` 컬럼에 저장
- **공통 모듈**: `module-common`의 `JsonUtil` 등 유틸을 `me.multimoduleexam.util` 패키지로 임포트

## Testing

```bash
./gradlew :ma-courtboard-api:test
```

- `MemberServiceTest` — Mockito Kotlin 기반 서비스 단위 테스트
- `CasbinPolicyTest` — Casbin 정책 검증 (`src/test/resources/casbin/` 정책 파일 사용)
