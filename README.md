# Internship Tracker Backend

Spring Boot backend for tracking internship applications with JWT authentication, current-user authorization, and user-scoped internship APIs.

## Tech Stack

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Security
- Spring Data JPA
- MySQL
- JWT with `jjwt`
- Bean Validation
- Maven
- Apifox regression tests

## Current Milestone

This version completes the authentication, refresh token, logout, user isolation, database migration, and internship query milestone.

Implemented:

- User registration and login
- BCrypt password hashing
- JWT stateless authentication
- JWT subject based on `userId`
- Refresh token issuing, storage, refresh, and logout
- `/api/users/me` current-user endpoints
- Password update with old password verification
- Internship CRUD scoped to the authenticated user
- No frontend-controlled `userId` in internship APIs
- Duplicate username/email checks
- Database unique constraints for username and email
- Unified API response format
- Global exception handling
- 401 response for unauthenticated requests
- Apifox regression collection covering the full login and internship flow
- JUnit and Mockito backend tests for authentication, services, controllers, and JWT filter behavior
- Environment-based configuration with Spring profiles for development and testing
- Flyway database migrations for users and internships schema management
- Paginated, searchable, filterable, and sortable internship list API
- Enum-based internship status values: `APPLIED`, `INTERVIEW`, `OFFER`, `REJECTED`

## Authentication Flow

1. User registers with username, email, and password.
2. Backend validates duplicate username/email.
3. Backend hashes the password with BCrypt.
4. User logs in with username and password.
5. Backend validates the password with `passwordEncoder.matches(...)`.
6. Backend generates an access JWT with `userId` as the subject.
7. Backend generates a random refresh token.
8. Backend stores only the SHA-256 hash of the refresh token.
9. Frontend stores the access token and refresh token.
10. Frontend sends protected requests with:

```http
Authorization: Bearer <token>
```

11. `JwtAuthenticationFilter` parses and validates the access token.
12. The filter creates an `Authentication` object and writes it into `SecurityContextHolder`.
13. Controllers read the authenticated user from `Authentication`, not from request body or URL parameters.

When the access token expires, the frontend can call:

```http
POST /api/users/refresh-token
```

with the refresh token to receive a new access token.

Logout calls:

```http
POST /api/users/logout
```

and deletes the stored refresh token hash. The existing access token may still work until it expires because JWT access tokens are stateless.

## API Overview

### Health

| Method | Endpoint | Description | Auth |
| --- | --- | --- | --- |
| GET | `/api/health` | Check whether the app is running | No |

### Users

| Method | Endpoint | Description | Auth |
| --- | --- | --- | --- |
| POST | `/api/users` | Register user | No |
| POST | `/api/users/login` | Login and receive access/refresh tokens | No |
| POST | `/api/users/refresh-token` | Use refresh token to receive a new access token | No |
| POST | `/api/users/logout` | Delete refresh token so it cannot be reused | No |
| GET | `/api/users/me` | Get current user | Yes |
| PUT | `/api/users/me` | Update current user's username/email | Yes |
| PUT | `/api/users/me/password` | Update current user's password | Yes |
| DELETE | `/api/users/me` | Delete current user | Yes |

### Internships

| Method | Endpoint | Description | Auth |
| --- | --- | --- | --- |
| GET | `/api/internships` | Get current user's internships with pagination/filter/search/sort | Yes |
| GET | `/api/internships/{id}` | Get current user's internship by id | Yes |
| POST | `/api/internships` | Create internship for current user | Yes |
| PUT | `/api/internships/{id}` | Update current user's internship | Yes |
| DELETE | `/api/internships/{id}` | Delete current user's internship | Yes |

`GET /api/internships` supports:

| Query Parameter | Description | Example |
| --- | --- | --- |
| `page` | Zero-based page number | `0` |
| `size` | Page size, capped at 100 | `10` |
| `status` | Optional internship status filter | `APPLIED` |
| `keyword` | Optional search keyword for company or position | `google` |
| `sort` | Sort field and direction | `createdAt,desc` |

Allowed sort fields:

- `createdAt`
- `company`
- `position`
- `status`

Example:

```http
GET /api/internships?page=0&size=10&status=APPLIED&keyword=backend&sort=createdAt,desc
Authorization: Bearer <token>
```

## Response Format

Successful response:

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

Error response:

```json
{
  "code": 401,
  "message": "Unauthorized",
  "data": null
}
```

Paginated internship response:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true
  }
}
```

## Security Notes

- Passwords are never returned in API responses.
- Passwords are stored as BCrypt hashes.
- JWT uses `userId` as subject because `username` can change.
- Refresh tokens are generated as random opaque tokens.
- Only refresh token hashes are stored in the database.
- Logout invalidates the refresh token, not already-issued access tokens.
- Protected endpoints do not trust frontend-provided `userId`.
- Internship access uses `findByIdAndUserId(id, currentUserId)` to prevent cross-user access.
- Missing or invalid token returns `401 Unauthorized`.
- Cross-user internship access returns `404 Not Found`.

## Database Design

Database schema is managed by Flyway migrations under:

```text
src/main/resources/db/migration
```

Initial migration:

```text
V1__create_users_and_internships_tables.sql
```

Query enhancement migration:

```text
V2__normalize_internship_status_and_add_query_indexes.sql
```

`V2` normalizes old text status values to uppercase enum values and adds indexes for current-user internship queries by `user_id`, `status`, and `created_at`.

Refresh token migration:

```text
V3__create_refresh_tokens_table.sql
```

`V3` creates `refresh_tokens` with a hashed token value, expiration time, creation time, and `user_id` foreign key.

Hibernate is configured with `ddl-auto=validate` in dev/test profiles. This means Flyway creates or migrates the schema, and Hibernate validates that the entity mappings match the database.

### users

| Column | Description |
| --- | --- |
| id | Primary key |
| username | Unique username |
| email | Unique email |
| password | BCrypt-hashed password |
| created_at | User creation time |

### internships

| Column | Description |
| --- | --- |
| id | Primary key |
| company | Company name |
| position | Internship position |
| location | Location |
| status | Application status |
| application_url | Application link |
| created_at | Creation time |
| user_id | Foreign key to users.id |

## Configuration

The project uses environment variables and Spring profiles so the same code can run in different environments.

Common JWT settings:

| Variable | Description | Default |
| --- | --- | --- |
| `JWT_SECRET` | Secret used to sign JWT tokens | `internship-tracker-dev-secret-key-32bytes` |
| `JWT_EXPIRATION_MS` | Access token expiration time in milliseconds | `3600000` |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh token expiration time in milliseconds | `604800000` |

Development profile database settings:

| Variable | Description | Default |
| --- | --- | --- |
| `DB_URL` | MySQL connection URL | `jdbc:mysql://localhost:3306/internship_tracker...` |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | empty |

Test profile database settings:

| Variable | Description | Default |
| --- | --- | --- |
| `TEST_DB_URL` | Test database URL | `jdbc:mysql://localhost:3306/internship_tracker_test...` |
| `TEST_DB_USERNAME` | Test database username | falls back to `DB_USERNAME` |
| `TEST_DB_PASSWORD` | Test database password | falls back to `DB_PASSWORD` |
| `TEST_JWT_SECRET` | Test JWT secret | `test-secret-key-with-at-least-32-bytes` |
| `TEST_JWT_EXPIRATION_MS` | Test access token expiration | `3600000` |
| `TEST_JWT_REFRESH_EXPIRATION_MS` | Test refresh token expiration | `604800000` |

Profiles:

- `dev`: local MySQL development configuration
- `test`: test-focused configuration

## Local Setup

Create a MySQL database:

```sql
CREATE DATABASE internship_tracker;
```

Set environment variables:

```bash
export DB_URL='jdbc:mysql://localhost:3306/internship_tracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true'
export DB_USERNAME='root'
export DB_PASSWORD='your_mysql_password'
export JWT_SECRET='replace-with-at-least-32-byte-secret-key'
export JWT_EXPIRATION_MS='3600000'
export JWT_REFRESH_EXPIRATION_MS='604800000'
```

Run the app:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

For an existing local database that was created before Flyway was introduced, `spring.flyway.baseline-on-migrate=true` is enabled in the dev profile. This lets Flyway create its schema history table without trying to recreate existing tables.

Run compile/tests:

```bash
./mvnw test
```

Current backend test coverage includes:

- `UserServiceTest`: registration, duplicate users, login, password update, account deletion
- `InternshipServiceTest`: current-user scoped internship CRUD, pagination, filtering, and keyword search
- `JwtUtilTest`: JWT generation, validation, and userId extraction
- `JwtAuthenticationFilterTest`: SecurityContext authentication setup
- `JwtPropertiesTest`: JWT property binding
- `UserControllerTest`: user endpoint responses and exception mapping
- `InternshipControllerTest`: internship query parameters and invalid request parameter handling
- `AuthServiceTest`: refresh token hashing, access token refresh, expiration handling, and logout

## Apifox Regression Tests

The importable collection is here:

```text
apifox-tests/internship-tracker-apifox.postman_collection.json
```

Run from:

```text
00 Initialize Test Data
```

The initialization step generates unique usernames and emails for each run, so repeated test runs do not collide with old database rows.

The collection covers:

- register
- login
- refresh access token
- get current user
- update current user
- change password
- old password login failure
- create internship
- get current user's internships with pagination, status filter, keyword search, and sorting
- update own internship
- delete own internship
- no-token 401
- duplicate register 400
- cross-user internship isolation
- logout and refresh-token reuse failure
- delete current user

## Important Problems Solved

- Login was blocked by Spring Security before `/api/users/login` was properly permitted.
- Missing token originally returned 403 instead of 401.
- JWT parsing alone was not enough until `Authentication` was written into `SecurityContextHolder`.
- JWT subject was changed from username to userId because username can be updated.
- Frontend-controlled `userId` was removed to prevent users from modifying other users' internships.
- Public user-listing endpoints were replaced with `/api/users/me`.
- Database unique constraints were added to protect against duplicate username/email under concurrency.
- User deletion was updated to remove the user's internships first to avoid foreign key failures.
- Apifox tests were changed to generate unique test data per run.
- Database schema management was moved from Hibernate auto-update to Flyway migrations.
- Internship list queries were upgraded from returning a raw list to returning a paginated `PageResponse`.
- Internship status was changed from free-form text to an enum to avoid inconsistent values such as `Applied` and `Interview`.
- Refresh tokens were added so access tokens can stay short-lived while users can still continue a session.
- Logout was implemented by deleting the stored refresh token hash.

## Next Improvements

- Add `updatedAt` fields.
- Add stronger password validation.
- Add role-based authorization.
- Improve README with screenshots and request examples as the frontend grows.
