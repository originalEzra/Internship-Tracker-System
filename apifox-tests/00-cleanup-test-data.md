# 00 Initialize Test Data

Run this before the full regression flow.

This request calls:

```http
GET {{baseUrl}}/api/health
```

Then its test script generates unique test data for the current run:

- `userAUsername`
- `userAEmail`
- `userANewUsername`
- `userANewEmail`
- `userBUsername`
- `userBEmail`

The generated values include a timestamp suffix, so repeated Apifox runs do not collide with old rows left in the database.

Passwords stay stable:

- `userAPassword`: `Password123`
- `userANewPassword`: `NewPassword123`
- `userBPassword`: `Password123`

After this initialization step finishes, run `01` through `18`.
