# Apifox Regression Test Flow

Import this collection into Apifox:

```text
internship-tracker-apifox.postman_collection.json
```

Base URL:

```text
http://localhost:8080
```

Required collection variable:

```text
baseUrl = http://localhost:8080
```

The collection manages the rest of the variables automatically.

## Run Order

Run the whole collection from the first request:

```text
00 Initialize Test Data
```

Do not start directly from `01 Register User A`, because `00 Initialize Test Data` generates unique usernames and emails for the current run.

## Covered Flow

1. Initialize unique test data.
2. Register User A.
3. Login User A.
4. Get current user.
5. Update current user.
6. Confirm old token still works after username update.
7. Change password.
8. Verify old password login fails.
9. Login with new password.
10. Refresh access token with refresh token.
11. Create internship as current user.
12. Get current user's internships.
13. Find internship by id.
14. Update own internship.
15. Delete own internship.
16. Confirm deleted internship returns 404.
17. Confirm no-token request returns 401.
18. Confirm duplicate registration returns 400.
19. Confirm User B cannot access or update User A's internship.
20. Logout current user.
21. Confirm refresh token cannot be reused after logout.
22. Delete current user.

## Notes

- Negative cases such as 401, 404, and 400 are expected and are asserted in scripts.
- Logout deletes the refresh token. The current access token may still work until it expires because access JWTs are stateless.
- Test data uses timestamp-based usernames/emails, so repeated runs do not collide with previous database rows.
- Apifox script style uses Postman-compatible `pm.*` APIs.
