# ResumeLint Backend — Java 22 / Spring Boot / MySQL

A drop-in replacement for the original Node/Express + Postgres backend
(`artifacts/api-server` + `lib/db` in the uploaded project), rebuilt in
**Java 22 + Spring Boot 3.3 + MySQL**. It implements the exact same
`openapi.yaml` contract, so the existing React frontend
(`artifacts/resume-analyzer`) works against it without any changes other
than pointing it at this server's URL.

## What's included

- All routes from `lib/api-spec/openapi.yaml`: `/api/healthz`,
  `/api/auth/*`, `/api/resumes/*`, `/api/dashboard/stats`.
- Same JWT bearer-token auth (HS256, 7 day expiry), same BCrypt(10)
  password hashing.
- The exact same resume-scoring heuristic ported line-for-line from
  `lib/analyze.ts` into `AnalysisService.java` (same regex patterns,
  weights, thresholds, and copy for suggestions/summary), including the
  original's pseudo-random keyword "detection" for keywords not literally
  present in the text.
- Same JSON field names/shapes for every response (`ResumeDto`,
  `AnalysisDto`, `DashboardStatsDto`, etc.) and the same error shape
  (`{"error": "..."}`).
- MySQL schema equivalent to the Drizzle Postgres schema (`users`,
  `resumes`, `analyses`), auto-created via Hibernate
  (`spring.jpa.hibernate.ddl-auto=update`) — or apply `schema-mysql.sql`
  by hand if you prefer to manage migrations yourself.

## Requirements

- JDK 22
- Maven 3.9+
- MySQL 8.0+ (or run it via the included `docker-compose.yml`)

## Quick start (local, no Docker)

1. Create the database:
   ```sql
   CREATE DATABASE resumelint CHARACTER SET utf8mb4;
   ```
2. Copy `.env.example` to `.env` and fill in real values, or just export
   the variables directly:
   ```bash
   export DB_HOST=localhost
   export DB_PORT=3306
   export DB_NAME=resumelint
   export DB_USER=root
   export DB_PASSWORD=your-mysql-password
   export SESSION_SECRET="$(openssl rand -base64 48)"
   export PORT=8080
   ```
3. Build and run:
   ```bash
   mvn clean package
   java -jar target/resumelint-backend.jar
   ```
   or simply `mvn spring-boot:run` during development.
4. Confirm it's up:
   ```bash
   curl http://localhost:8080/api/healthz
   # {"status":"ok"}
   ```

## Quick start (Docker Compose)

```bash
export SESSION_SECRET="$(openssl rand -base64 48)"
docker compose up --build
```

This starts MySQL 8.4 and the API on `http://localhost:8080`, with the
API waiting for MySQL's healthcheck before starting.

## Connecting the existing React frontend

The frontend's `custom-fetch.ts` reads a base URL you set via
`setBaseUrl(...)` and attaches `Authorization: Bearer <token>` from
`localStorage.getItem('auth_token')`. Point it at this server, e.g.:

```ts
import { setBaseUrl } from "@workspace/api-client-react";
setBaseUrl("http://localhost:8080");
```

No other frontend changes are required — every response shape matches
the generated Zod types in `lib/api-zod`.

## Environment variables

| Variable                | Required | Default          | Purpose                                   |
|--------------------------|:--------:|------------------|--------------------------------------------|
| `PORT`                   |    no    | `8080`           | HTTP port                                  |
| `DB_HOST`                |    no    | `localhost`      | MySQL host                                 |
| `DB_PORT`                |    no    | `3306`           | MySQL port                                 |
| `DB_NAME`                |    no    | `resumelint`     | MySQL database name                        |
| `DB_USER`                |    no    | `root`           | MySQL username                             |
| `DB_PASSWORD`            |    no    | `root`           | MySQL password                             |
| `SESSION_SECRET`         |  **yes** | —                | HS256 JWT signing secret (32+ chars)       |
| `CORS_ALLOWED_ORIGINS`   |    no    | `*`              | Comma-separated allowed origins            |
| `JWT_EXPIRATION_MS`      |    no    | `604800000` (7d) | Token lifetime in milliseconds             |

The app fails fast at startup with a clear error if `SESSION_SECRET` is
missing or shorter than 32 characters — the same fail-fast behavior the
Node backend had for a missing `SESSION_SECRET`/`DATABASE_URL`.

## Project layout

```
src/main/java/com/resumelint/
├── ResumelintApplication.java     entry point
├── config/                        CORS, interceptor wiring, JWT props, beans
├── security/                      JWT sign/verify, per-request auth holder
├── filter/                        AuthInterceptor (requireAuth equivalent)
├── controller/                    REST controllers (1:1 with routes/*.ts)
├── service/                       Business logic + analyzeResume() port
├── repository/                    Spring Data JPA repositories
├── entity/                        JPA entities + JSON column converters
├── dto/                           Request/response records (match OpenAPI)
└── exception/                     ApiException + global error mapping
```

## Notable behavior parity details

- `GET /api/resumes/{id}/analysis` returns the **oldest** analysis for a
  resume (ascending order, limit 1), because that is exactly what the
  original Drizzle query did (`.orderBy(analysesTable.createdAt).limit(1)`
  with no explicit direction defaults to ascending). This is preserved
  for parity — flip `AnalysisRepository` to `...OrderByCreatedAtDesc` if
  you'd rather it return the latest analysis.
- Validation error messages, 401/404/409 status codes, and the
  `{"error": "..."}` error shape all match the Node implementation.
- Dates are serialized exactly like JavaScript's `Date.toISOString()`
  (`IsoDates` helper) so existing frontend date parsing keeps working
  unchanged.

## Building a production jar

```bash
mvn clean package
java -jar target/resumelint-backend.jar
```

Switch `spring.jpa.hibernate.ddl-auto` from `update` to `validate` (or
`none`, managing the schema yourself via `schema-mysql.sql`) once your
schema is stable in production.
