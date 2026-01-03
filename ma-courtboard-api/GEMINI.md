# Project: Courtboard API

## 1. 프로젝트 개요
- **설명**: 농구 전술 보드(Courtboard) 서비스의 백엔드 API 서버
- **Tech Stack**: Kotlin, Spring Boot, Spring Data JPA
- **Architecture**: Layered Architecture (Controller -> Service -> Repository), MVC Pattern
- **Database**: PostgreSQL (Entity 정의 기반 추론)

## 2. 패키지 구조 (@src/main/kotlin/me/courtboard/api)

```text
me.courtboard.api
├── aop/                  # AOP 관련 (권한 체크)
│   └── CheckPermAop.kt   # @CheckPerm 어노테이션 기반 Casbin 권한 검사 수행
├── api/                  # 도메인별 비즈니스 로직
│   ├── member/           # 회원 관리 도메인 (Entity, DTO, Repository, Service, Controller)
│   ├── my/               # 내 정보/계정 관리 API Controller
│   └── tactics/          # 전술 관리 도메인 (Entity, DTO, Repository, Service, Controller)
├── component/            # 주요 컴포넌트
│   ├── JwtProvider.kt    # JWT 토큰 생성 및 검증
│   └── CustomMailSender.kt # 이메일 발송
├── config/               # 설정 클래스 (WebMvc, Bean 등)
├── filter/               # 서블릿 필터
│   └── AuthFilter.kt     # JWT 인증 및 SecurityContext(CourtboardContext) 설정
├── global/               # 전역 공통 모듈
│   ├── CourtboardContext.kt # ThreadLocal을 이용한 요청별 유저 컨텍스트 관리
│   ├── dto/              # 공통 응답 포맷 (ApiResult)
│   └── error/            # 전역 예외 처리 (CustomExceptionHandler)
├── listener/             # 애플리케이션 라이프사이클 이벤트 리스너
└── util/                 # 유틸리티 클래스 (PasswordUtil 등)
```

## 3. 주요 특징 및 로직

### 인증 및 인가 (Auth)
- **Authentication (인증)**:
  - `JwtProvider`를 통해 Access/Refresh Token 발급 및 검증.
  - `AuthFilter`에서 요청 헤더의 JWT를 파싱하여 유저 정보를 추출.
  - 추출된 정보는 `CourtboardContext`(ThreadLocal)에 저장되어 전역적으로 접근 가능.
- **Authorization (인가)**:
  - **Casbin** 라이브러리를 사용하여 RBAC(Role-Based Access Control) 구현.
  - `@CheckPerm` 어노테이션이 붙은 메서드는 `CheckPermAop`에서 Casbin Enforcer를 통해 권한을 검사함.

### 도메인 로직
- **Tactics (전술)**:
  - 전술 데이터(`formations`, `playerInfo`)는 JSON 형태로 DB(`states` 컬럼)에 저장됨.
  - `TacticsService`에서 생성, 조회(공개/비공개), 수정, 삭제 로직 처리.
  - `admin`용 전체 조회와 일반 유저용 조회 로직이 분리되어 있음.
- **Member (회원)**:
  - 이메일 인증 코드 발송 및 검증 로직 포함.
  - 비밀번호는 BCrypt로 해싱하여 저장 (`PasswordUtil`).
