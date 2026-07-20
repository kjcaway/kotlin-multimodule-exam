# CLAUDE.md

Guidance for working in this repository.

## Overview

A personal multi-module project for learning Spring Boot + Kotlin. It collects small,
self-contained examples of Spring features (Spring Batch, custom validators, domain
layer, Kubernetes readiness/liveness, JdbcTemplate, Testcontainers, etc.). See
`README.md` for the feature list. Servlet stack only.

## Tech Stack

- Spring Boot 3.1
- Kotlin 1.8 / JDK 17
- Gradle 8.x (Kotlin DSL)

Versions are centralized in `gradle.properties`.

## Module Conventions

Two module prefixes, with different roles:

- **`ma-*`** — Runnable **server applications**. Each has its own Spring Boot
  `*Application.kt` entry point and can be started on its own.
- **`module-*`** — **Library modules**. They cannot run on their own; they exist to be
  referenced (via `implementation(project(":module-..."))`) by `ma-*` apps and by other
  `module-*` modules.

### Modules

Applications (`ma-*`):
- `ma-courtboard-api` — main API app (board / tactics / member / etc.).
- `ma-performance-test-api` — API app for performance testing.

Libraries (`module-*`):
- `module-common` — shared utilities (validators, cache, util). Used by every app.
- `module-domain` — domain layer / entities shared across apps.
- `module-api` — example API module.
- `module-api-exam` — **boilerplate / starter template** for new API apps (see below).
- `module-batch` — Spring Batch example.

Membership is declared in `settings.gradle.kts`.

## `module-api-exam` — API Boilerplate

This module is the **starter template**: when spinning up a new Spring Boot API app,
copy this module (rename it to a `ma-*` module) and build from there. It is intentionally
minimal and only carries the wiring every API app needs. Keep it lean — do not add
feature-specific code here.

What it ships out of the box:

- **Entry point** — `ApiExamApplication.kt`, a bare `@SpringBootApplication`.
- **Dependency baseline** (`build.gradle.kts`): Spring Web + WebFlux, Actuator, Log4j2
  (the default `spring-boot-starter-logging` is excluded repo-wide), Jackson Kotlin
  module, Kotlin reflect, and Micrometer/Prometheus. Depends on `module-common`.
- **Sample endpoint** — `api/HelloController.kt`: `GET /api/hello` and
  `GET /api/hello/check/k8sconfig` (echoes a value injected from `k8sConfig.app.*`,
  demonstrating config/secret injection).
- **Lifecycle listeners** — `listener/StartupListener` (`ApplicationReadyEvent`) and
  `listener/ShutdownListener` (`ContextClosedEvent`) as hook points for startup/graceful
  shutdown logic.
- **HTTP client** — `util/WebClientUtil.kt`, a reusable `WebClient` helper (per-timeout
  pooled clients, GET/POST/PUT/DELETE, 10MB buffer, centralized error handling).
- **Config** — `application.yml` (port `8081`, Log4j2) and `application-dev.yml`
  (`dev` profile: graceful shutdown, Actuator `health`/`prometheus`, Kubernetes
  liveness/readiness probes, `k8sConfig.app.*` values from env vars).
- **Kubernetes / Docker** — `Dockerfile` (Amazon Corretto 17) and `buildNPush.sh`
  (build image and load into a local minikube cluster).
- **Testing** — tests are **enabled** for this module (unlike the repo default) and run
  only `@Tag("unit")` / `@Tag("api")` classes. Includes a custom `@EnabledIfReachable`
  JUnit condition (skip tests when a host/port is unreachable) and `WebClientUtilTest`.

### Using it as a template

1. Copy `module-api-exam` to a new `ma-<name>-api` directory and rename the package /
   `*Application.kt`.
2. Register it in `settings.gradle.kts`.
3. Adjust `server.port`, profiles, and `k8sConfig` values as needed.
4. Update the image name/tag in `Dockerfile` / `buildNPush.sh`.

## Build & Run

```bash
# Build everything
./gradlew build

# Build a single module
./gradlew :ma-courtboard-api:build

# Run a server app
./gradlew :ma-courtboard-api:bootRun
```

Note: tests are disabled by default at the root (`tasks.withType<Test> { enabled = false }`).

## Notes

- `db/` (SQL scripts) and `.github/` (CI workflows) can generally be ignored when working
  on application code.
