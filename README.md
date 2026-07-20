# Multi Module Example
This is multi-module-example with kotlin.
<br>
It's example for next features.
<br>
It's only for spring servlet stack.

## Environment
- Springboot 3.1
- JDK 17
- Gradle 8.4
- Kotlin 1.8
- ...

## Features
- Example for SpringBatch 5 without MetaTables.
- Example for custom validator and handler.
- Example for domain layer module.
- Example for readiness, liveness state using in Kubernetes.
  - Include environments in yml
  - Dockerfile
- Example for JdbcTemplate.
- Example for integration test with TestContainers

## Modules

- `ma-*` — runnable Spring Boot **server applications** (each has its own `main`).
- `module-*` — **library modules** that cannot run alone; they are referenced by `ma-*`
  apps (and by other `module-*` modules).

## `module-api-exam` (API boilerplate)

`module-api-exam` is the **starter template** for new API applications. When starting a
new Spring Boot API app, copy this module (rename it to a `ma-*` module) and build on top
of it. It stays intentionally minimal — just the common wiring every API app needs.

Included out of the box:

- `ApiExamApplication` — a bare `@SpringBootApplication` entry point.
- Dependency baseline — Spring Web + WebFlux, Actuator, Log4j2 (default logging excluded),
  Jackson Kotlin module, Kotlin reflect, Micrometer/Prometheus; depends on `module-common`.
- `HelloController` — sample endpoints `GET /api/hello` and `GET /api/hello/check/k8sconfig`
  (the latter demonstrates config/secret injection via `k8sConfig.app.*`).
- `StartupListener` / `ShutdownListener` — application lifecycle hook points.
- `WebClientUtil` — reusable pooled `WebClient` helper (per-timeout clients, GET/POST/PUT/
  DELETE, 10MB buffer, centralized error handling).
- Config — `application.yml` (port `8081`, Log4j2) and `application-dev.yml`
  (`dev` profile: graceful shutdown, Actuator `health`/`prometheus`, K8s liveness/readiness,
  env-based `k8sConfig.app.*`).
- `Dockerfile` (Amazon Corretto 17) and `buildNPush.sh` (build + load image into local
  minikube).
- Tests — enabled for this module, running only `@Tag("unit")` / `@Tag("api")` classes;
  includes a custom `@EnabledIfReachable` JUnit condition.

### Use as a template

1. Copy `module-api-exam` to `ma-<name>-api` and rename the package / `*Application.kt`.
2. Register it in `settings.gradle.kts`.
3. Adjust `server.port`, profiles, and `k8sConfig` values.
4. Update the image name/tag in `Dockerfile` / `buildNPush.sh`.
