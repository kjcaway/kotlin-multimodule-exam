# Courtboard API

농구 전술 보드(Courtboard) 서비스의 백엔드 API. `multimodule-exam` 멀티모듈의 하위 모듈(`ma-courtboard-api`)이며 `module-common`에 의존한다.

## Tech Stack

- **Kotlin / Spring Boot** (Web, JPA, Validation, Mail, Actuator)
- **DB**: PostgreSQL (Casbin 룰도 `tbl_casbin_rule`에 저장)
- **Auth**: JWT (jjwt) + Casbin RBAC + Google OAuth 로그인, 비밀번호 jBCrypt
- **기타**: Log4j2(Logback 제외), Thumbnailator(아바타 리사이즈)

## Project Structure

```
src/main/kotlin/me/courtboard/api/
├── aop/          # @CheckPerm(권한) + @CheckLogin(로그인 필수) + AOP
├── api/
│   ├── board/    # 게시판: BoardController(글+이미지업로드), BoardCommentController(댓글), util/BoardHtmlSanitizer
│   ├── casbin/   # Casbin 정책 관리 admin (CasbinAdminController, CasbinService)
│   ├── internal/ # loopback 전용 운영 엔드포인트
│   ├── member/   # 회원: MemberController(가입/로그인/구글로그인/이메일인증), MemberAdminController,
│   │             #   service(MemberService, MemberMailService, GoogleAuthService, MemberAvatarService)
│   ├── my/       # 내 정보(전술/정보/비번/탈퇴/아바타)
│   ├── quicktactics/ # 퀵보드: QuickTacticsController(GET/PUT /api/quick-tactics), 사용자별 마지막 작전판 상태 1행 upsert
│   └── tactics/  # 전술: TacticsController + TacticsAdminController(템플릿 toggle)
├── component/    # JwtProvider, CustomMailSender
├── config/       # WebConfig(CORS + /uploads 정적 핸들러), BeanConfig
├── filter/       # AuthFilter(JWT→CourtboardContext), UploadsHeaderFilter(nosniff/CSP)
├── global/       # CourtboardContext, Constants(역할), dto/ApiResult, error/
├── listener/     # StartupListener, ShutdownListener
└── util/PasswordUtil.kt

src/main/resources/  # application.yml, application-dev.yml, casbin/, log4j2/, mail/signup.html
```

- `*-prod.yml`은 gitignore 대상이라 리포에 없음(배포 시 별도 제공).
- DDL은 운영 DB에 수동 적용하며 리포에 보관하지 않는다.

## Architecture

**Controller → Service → Repository**. Spring Security 미사용, 인증/인가 직접 구현.

### 인증

- `AuthFilter`가 `Authorization: Bearer <token>` 파싱 → JWT 유효 시 Casbin 역할 조회 후 `CourtboardContext`(ThreadLocal)에 `RequestContext(memberId, role)` 저장. 토큰 없으면 guest, 만료 시 401.
- **Google 로그인**(`POST /api/member/google-login`): `GoogleAuthService`가 `credential`(ID 토큰) 검증 → 이메일 기준 회원 조회/생성(`provider=google`) 후 자체 access/refresh 발급.

### 인가

- `@CheckPerm` → `CheckPermAop`가 Casbin Enforcer로 권한 검사.
- `@CheckLogin` → `CheckLoginAop`가 로그인 여부만 검사(미로그인 401). `@CheckPerm`과 병용 가능.
- 역할 계층: `admin` > `user` > `guest`. 새 보호 엔드포인트는 Casbin 정책 `(role, courtboard, <path>, <method>)` 등록 필요.

### API 응답

모든 응답은 `ApiResult<T>`로 감쌈:

```json
{ "success": true, "data": { ... } }
{ "success": false, "message": "error message" }
```

## 파일 업로드 / 정적 서빙

로컬 파일시스템 + DB 메타데이터. `${storage.path}` 하위에 저장하고 `/uploads/...`로 서빙.

- **게시판 이미지** (`POST /api/board/images`, `BoardImageService`): 빈 파일/5MB 초과 거부, MIME 화이트리스트(`png|jpeg|webp|gif`), `verifyMagicBytes`로 시그니처 검증(Content-Type 위장 차단). 응답 URL을 본문 `<img src>`에 임베드.
- **아바타** (`POST/DELETE /api/my/avatar`, `MemberAvatarService`): 동일 검증 후 Thumbnailator로 `256x256` png 저장(`avatar/{memberId}.png`). `avatarUrl` 갱신 후 access token 재발급(claim에 `avatarUrl` 포함). Google 계정은 수정/삭제 불가.

```yaml
spring.servlet.multipart: { max-file-size: 5MB, max-request-size: 6MB }
storage.path: /tmp/courtboard-uploads   # dev
```

## Key Conventions

- **DTO**: `*ReqDto`(요청) / `*ResDto`(응답).
- **Controller 분리**: 일반 vs admin 별도 파일 (예: `TacticsController` vs `TacticsAdminController`).
- **전술 데이터**: `formations`, `playerInfo`를 JSON으로 DB `states` 컬럼에 저장.
- **퀵보드 데이터**(`tbl_quick_tactics`): 로그인 사용자별 마지막 작전판 상태를 `member_id` PK 1행으로 `states`(JSON: `players`, `ball`, `playerInfo`, `isHalfCourt`)에 upsert 저장. 단일 정지 상태(포메이션 1개)만 다룸. `@CheckLogin`으로 로그인 필수(Casbin 정책 불필요).
- **게시판 본문**: `BoardHtmlSanitizer`(jsoup)로 XSS sanitize 후 저장. `data:` 프로토콜은 기존 base64 게시물 호환용으로 유지.
- **게시판 수정/삭제**: 작성자 본인만(author-only 체크).
- **공통 모듈**: `module-common` 유틸은 `me.multimoduleexam.util` 패키지로 임포트.

## Build & Deploy

```bash
./gradlew :ma-courtboard-api:build   # 멀티모듈 루트에서 실행
./deploy.sh 1.9.5                    # 빌드 → Docker(linux/amd64) → tar → SCP 업로드
```

서버에서 수동으로 `docker load` / `docker run`.

## Production Tuning (application-prod.yml)

서버 스펙 **1 vCPU / 1.5GB RAM** 기준:

```yaml
server.tomcat:
  threads: { max: 20, min-spare: 5 }   # 기본 200/10 → 컨텍스트 스위칭 최소화
  accept-count: 30                      # 요청 큐 (기본 100)
  connection-timeout: 10000
spring.datasource.hikari:
  maximum-pool-size: 5   # PgBouncer가 외부 풀링 → 앱 단 5개면 충분
  minimum-idle: 2
  max-lifetime: 115000   # PgBouncer 기본(120s)보다 짧게
  leak-detection-threshold: 2000
```

## Testing

루트에서 테스트는 기본 비활성(`tasks.withType<Test> { enabled = false }`)이지만, 이 모듈은
`build.gradle.kts`에서 `tasks.test`를 다시 활성화한다.

```bash
./gradlew :ma-courtboard-api:test                 # UP-TO-DATE면 스킵됨
./gradlew :ma-courtboard-api:test --rerun-tasks --console=plain   # 매번 실행 + 깔끔한 로그
```

- 서비스/정책: `MemberServiceTest`(Mockito Kotlin), `CasbinPolicyTest`.
- 컨트롤러: `Board`, `BoardComment`, `Member`, `MemberAdmin`, `My`, `QuickTactics`, `Tactics`, `TacticsAdmin` `*ControllerTest`.

### 콘솔 출력 (`build.gradle.kts`)

`tasks.test`에 설정이 들어 있어 실행 시 콘솔에 결과가 표시된다.

- **테스트 케이스별 로그**: `testLogging`으로 `passed`/`skipped`/`failed` 이벤트 출력
  (`showStandardStreams = false` — MockMvc 덤프는 숨김, 실패 시 스택트레이스는 `FULL`).
- **요약**: `TestListener.afterSuite`가 루트 스위트에서 `N tests, N passed, N failed, N skipped` 출력.

### 커버리지 (JaCoCo)

`jacoco` 플러그인 적용. `test`가 끝나면 `finalizedBy(jacocoTestReport)`로 리포트가 이어지고,
`jacocoTestReport`의 `doLast`가 XML을 파싱해 커버리지 요약(Instructions/Branches/Lines/Methods/Classes)을
콘솔에 출력한다. HTML 리포트는 `build/reports/jacoco/test/html/index.html`.

- **측정 제외**: `**/dto/**`(모든 `*ReqDto`/`*ResDto`), `**/config/**`, `**/*Application*`.
  data class 자동 생성 코드 등이 수치를 부풀리지 않도록 비즈니스 로직 위주로만 집계.
- UP-TO-DATE로 스킵되면 콘솔 출력도 안 나오므로 매번 보려면 `--rerun-tasks` 사용.
