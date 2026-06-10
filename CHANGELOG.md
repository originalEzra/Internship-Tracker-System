# Changelog

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

- Add JUnit and Spring Boot integration tests.
- Add Flyway migrations.
- Add pagination, filtering, and sorting for internship lists.
- Add production-ready configuration profiles.
- Add README request/response examples.
