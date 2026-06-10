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

This version completes the authentication and user isolation milestone.

Implemented:

- User registration and login
- BCrypt password hashing
- JWT stateless authentication
- JWT subject based on `userId`
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

## Authentication Flow

1. User registers with username, email, and password.
2. Backend validates duplicate username/email.
3. Backend hashes the password with BCrypt.
4. User logs in with username and password.
5. Backend validates the password with `passwordEncoder.matches(...)`.
6. Backend generates a JWT with `userId` as the subject.
7. Frontend stores the token.
8. Frontend sends protected requests with:

```http
Authorization: Bearer <token>
```

9. `JwtAuthenticationFilter` parses and validates the token.
10. The filter creates an `Authentication` object and writes it into `SecurityContextHolder`.
11. Controllers read the authenticated user from `Authentication`, not from request body or URL parameters.

## API Overview

### Health

| Method | Endpoint | Description | Auth |
| --- | --- | --- | --- |
| GET | `/api/health` | Check whether the app is running | No |

### Users

| Method | Endpoint | Description | Auth |
| --- | --- | --- | --- |
| POST | `/api/users` | Register user | No |
| POST | `/api/users/login` | Login and receive JWT | No |
| GET | `/api/users/me` | Get current user | Yes |
| PUT | `/api/users/me` | Update current user's username/email | Yes |
| PUT | `/api/users/me/password` | Update current user's password | Yes |
| DELETE | `/api/users/me` | Delete current user | Yes |

### Internships

| Method | Endpoint | Description | Auth |
| --- | --- | --- | --- |
| GET | `/api/internships` | Get current user's internships | Yes |
| GET | `/api/internships/{id}` | Get current user's internship by id | Yes |
| POST | `/api/internships` | Create internship for current user | Yes |
| PUT | `/api/internships/{id}` | Update current user's internship | Yes |
| DELETE | `/api/internships/{id}` | Delete current user's internship | Yes |

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

## Security Notes

- Passwords are never returned in API responses.
- Passwords are stored as BCrypt hashes.
- JWT uses `userId` as subject because `username` can change.
- Protected endpoints do not trust frontend-provided `userId`.
- Internship access uses `findByIdAndUserId(id, currentUserId)` to prevent cross-user access.
- Missing or invalid token returns `401 Unauthorized`.
- Cross-user internship access returns `404 Not Found`.

## Database Design

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
| `JWT_EXPIRATION_MS` | JWT expiration time in milliseconds | `3600000` |

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
| `TEST_JWT_EXPIRATION_MS` | Test JWT expiration | `3600000` |

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
```

Run the app:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Run compile/tests:

```bash
./mvnw test
```

Current backend test coverage includes:

- `UserServiceTest`: registration, duplicate users, login, password update, account deletion
- `InternshipServiceTest`: current-user scoped internship CRUD
- `JwtUtilTest`: JWT generation, validation, and userId extraction
- `JwtAuthenticationFilterTest`: SecurityContext authentication setup
- `JwtPropertiesTest`: JWT property binding
- `UserControllerTest`: user endpoint responses and exception mapping

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
- get current user
- update current user
- change password
- old password login failure
- create internship
- get current user's internships
- update own internship
- delete own internship
- no-token 401
- duplicate register 400
- cross-user internship isolation
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

## Next Improvements

- Add Flyway database migrations.
- Add pagination, filtering, and sorting for internships.
- Add `updatedAt` fields.
- Add stronger password validation.
- Add refresh token or role-based authorization.
- Improve README with screenshots and request examples as the frontend grows.
