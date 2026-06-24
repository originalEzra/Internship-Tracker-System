# Changelog

## 2026-06-25 - Internship Status Machine Milestone

### Added

- Expanded `InternshipStatus` values: `DRAFT`, `ONLINE_ASSESSMENT`, `TECH_INTERVIEW`, `HR_INTERVIEW`, and `WITHDRAWN`.
- Backend-owned status transition rules through `InternshipStatus.canTransitionTo(...)`.
- `InvalidInternshipStatusTransitionException` mapped to unified `400` API responses.
- Flyway migration `V6__normalize_internship_status_machine.sql`.
- Unit tests for valid transitions, invalid transitions, and terminal statuses.
- Controller test proving invalid transitions return `400`.
- Apifox regression step for invalid status transition rejection.

### Changed

- Updating an internship now validates status transitions before saving.
- Existing `INTERVIEW` rows are migrated to `TECH_INTERVIEW`.
- Apifox update flow now moves an internship from `APPLIED` to `ONLINE_ASSESSMENT`.
- README documents the status machine and rejected transition examples.

### Verified

- `./mvnw -Dtest=InternshipServiceTest,InternshipControllerTest test` passes.
- `./mvnw test` passes.
- 57 backend tests run successfully with 0 failures.

## 2026-06-25 - Internship updatedAt Milestone

### Added

- Flyway migration `V5__add_updated_at_to_internships.sql`.
- `updatedAt` field on internship records.
- `updatedAt` in `InternshipResponse`.
- `updatedAt` support in internship list sorting.
- Apifox regression assertions for `createdAt` and `updatedAt`.

### Changed

- Creating an internship now sets both `createdAt` and `updatedAt`.
- Updating an internship now refreshes `updatedAt`.
- Existing rows are backfilled with `created_at` or the current timestamp during migration.

### Verified

- `./mvnw -Dtest=InternshipServiceTest test` passes.
- `./mvnw test` passes.
- 53 backend tests run successfully with 0 failures.

## 2026-06-24 - Redis Auth Hardening Milestone

### Added

- Spring Data Redis dependency.
- Docker Compose Redis service for local development.
- Redis-backed access token blacklist for logout.
- Redis-backed login failure rate limiting.
- `TokenBlacklistService` for hashing and storing logged-out access tokens with JWT-based TTL.
- `LoginRateLimitService` for tracking failed login attempts by username.
- `TooManyLoginAttemptsException` mapped to unified `429` API responses.
- Unit tests for token blacklist and login rate limiting.
- Testcontainers Redis container in the integration test.
- Apifox regression step proving old access token returns `401` after logout.

### Changed

- Logout now deletes the refresh token and blacklists the current access token when `Authorization: Bearer <token>` is provided.
- JWT authentication filter now rejects blacklisted access tokens before creating `Authentication`.
- Login clears failed-attempt counters after a successful login.
- README and Obsidian notes document Redis as authentication state, not ordinary caching.

### Verified

- `./mvnw test` passes.
- 52 backend tests run successfully with 0 failures.

## 2026-06-17 - Lightweight RBAC Milestone

### Added

- `Role` enum with `USER` and `ADMIN`.
- `users.role` database column through `V4__add_user_role.sql`.
- Role field in `User` entity and `UserResponse`.
- Admin-only endpoint: `GET /api/admin/users`.
- `@EnableMethodSecurity` and `@PreAuthorize("hasRole('ADMIN')")` for method-level authorization.
- `JwtAuthenticationFilter` now loads the current user's role and creates `ROLE_USER` or `ROLE_ADMIN` authorities.
- JSON `403 Forbidden` response for authenticated users without enough authority.
- Tests for role authorities and admin user response shape.

### Changed

- New users default to the `USER` role.
- JWT still stores only `userId`; role is loaded from the database during authentication.
- README documents the lightweight RBAC choice and how to promote a local user to `ADMIN`.

### Verified

- `./mvnw test` passes.
- 36 backend tests run successfully with 0 failures.

## 2026-06-16 - Refresh Token and Logout Milestone

### Added

- Refresh token entity and repository.
- `refresh_tokens` table migration: `V3__create_refresh_tokens_table.sql`.
- `AuthService` for access token creation, refresh token creation, refresh flow, and logout.
- `POST /api/users/refresh-token` endpoint.
- `POST /api/users/logout` endpoint.
- `JWT_REFRESH_EXPIRATION_MS` and `TEST_JWT_REFRESH_EXPIRATION_MS` configuration.
- Controller and service tests for refresh token and logout behavior.

### Changed

- Login response now returns both `token` and `refreshToken`.
- Refresh tokens are generated as random opaque tokens.
- Only SHA-256 refresh token hashes are stored in the database.
- User deletion now removes refresh tokens before deleting the user.
- Apifox regression collection now covers refresh-token reuse and logout.

### Fixed

- Prevented user deletion from being blocked by future refresh token foreign key references.

### Verified

- `./mvnw test` passes.
- 33 backend tests run successfully with 0 failures.

## 2026-06-16 - Internship Query Enhancement Milestone

### Added

- `InternshipStatus` enum with `APPLIED`, `INTERVIEW`, `OFFER`, and `REJECTED`.
- `PageResponse<T>` DTO for stable paginated API responses.
- Pagination, status filtering, keyword search, and sorting for `GET /api/internships`.
- Repository query for current-user internship search by status and keyword.
- Controller tests for internship query parameters and invalid enum parameters.
- Flyway migration `V2__normalize_internship_status_and_add_query_indexes.sql`.

### Changed

- Internship `status` changed from free-form `String` to enum.
- Create/update internship requests now require enum status values.
- Internship list API now returns a paginated response instead of a raw list.
- Apifox regression tests now use uppercase enum status values and assert `data.content`.
- User deletion now loads owned internships through the query repository method.

### Fixed

- Inconsistent status text such as `Applied` and `Interview` is normalized for enum compatibility.
- Invalid request parameters and unreadable JSON bodies now return unified `ApiResponse` error bodies.

### Verified

- `./mvnw test` passes.
- 27 backend tests run successfully with 0 failures.

## 2026-06-10 - Database Migration Milestone

### Added

- Flyway dependencies for database migration management.
- Initial migration: `V1__create_users_and_internships_tables.sql`.
- Explicit `users` table schema with unique constraints for username and email.
- Explicit `internships` table schema with `user_id` foreign key.
- Index on `internships.user_id`.

### Changed

- Development profile now uses `spring.jpa.hibernate.ddl-auto=validate`.
- Test profile now uses `spring.jpa.hibernate.ddl-auto=validate`.
- Enabled `spring.flyway.baseline-on-migrate=true` for smoother adoption on existing local schemas.
- Updated README database section with Flyway migration notes.

### Verified

- `./mvnw test` passes.
- 24 backend tests run successfully with 0 failures.

## 2026-06-10 - Configuration Profile Milestone

### Added

- `application-dev.properties` for local MySQL development settings.
- `application-test.properties` for test-focused settings.
- `JwtProperties` to bind JWT configuration through `@ConfigurationProperties`.
- `JwtPropertiesTest` to verify JWT property binding.

### Changed

- Simplified `application.properties` so it only keeps common shared configuration.
- Replaced scattered JWT `@Value` injection with centralized `JwtProperties`.
- Updated README with profile usage and environment variable tables.

### Verified

- `./mvnw test` passes.
- 24 backend tests run successfully with 0 failures.

## 2026-06-10 - Backend Test Milestone

### Added

- `UserServiceTest` for user registration, duplicate checks, login, password updates, and account deletion.
- `InternshipServiceTest` for current-user scoped internship access, update, and delete behavior.
- `JwtUtilTest` for token generation, validation, and userId extraction.
- `JwtAuthenticationFilterTest` for verifying that valid tokens populate `SecurityContextHolder`.
- `UserControllerTest` for user API response structure and exception mapping.

### Verified

- `./mvnw test` passes.
- 23 backend tests run successfully with 0 failures.

## 2026-06-10

Authentication and user-scoped internship milestone.

### Added

- User registration endpoint: `POST /api/users`.
- User login endpoint: `POST /api/users/login`.
- JWT-based stateless authentication.
- Current-user endpoints:
  - `GET /api/users/me`
  - `PUT /api/users/me`
  - `PUT /api/users/me/password`
  - `DELETE /api/users/me`
- BCrypt password hashing.
- Password update flow with old password verification.
- `JwtAuthenticationFilter` for token parsing and authentication setup.
- `SecurityContextHolder` integration through `UsernamePasswordAuthenticationToken`.
- Health endpoint: `GET /api/health`.
- Global exception handling with unified `ApiResponse`.
- Apifox regression test collection.

### Changed

- JWT subject changed from username to userId.
- User APIs changed from admin-style user management to current-user-only APIs.
- Internship APIs no longer accept frontend-provided `userId`.
- Internship queries now use current authenticated user id.
- Cross-user internship access is blocked through `findByIdAndUserId(...)`.
- User deletion now deletes the current user's internships before deleting the user.
- Apifox tests now generate unique usernames/emails per run.
- Database and JWT configuration now use environment-backed properties.

### Removed

- Public `GET /api/users`.
- Public `GET /api/users/{id}`.
- User update/delete by arbitrary path id.
- Frontend-controlled internship ownership.

### Fixed

- Login endpoint returning 403 because it was not properly permitted.
- Missing token returning 403 instead of 401.
- Duplicate username/email handling.
- User deletion failing because of internship foreign key references.
- Apifox false failures for expected 401/404/400 negative cases.
- Repeated Apifox runs failing because old test data reused the same usernames.

### Verified

- `./mvnw test` passes.
- Apifox regression suite passes 23/23.

## Next Planned Milestones

- Add Flyway migrations.
- Add pagination, filtering, and sorting for internship lists.
- Add production-ready configuration profiles.
- Add README request/response examples.
