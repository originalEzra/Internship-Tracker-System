# Manual RBAC Admin Test

The default regression collection creates normal `USER` accounts only. To test admin access, promote one local user in MySQL first:

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'your_admin_username';
```

Then log in as that user and call:

```http
GET {{baseUrl}}/api/admin/users
Authorization: Bearer {{token}}
```

Expected:

```text
200 OK
```

To verify forbidden access, log in as a normal `USER` and call the same endpoint.

Expected:

```text
403 Forbidden
```

If no token is sent, the expected response is:

```text
401 Unauthorized
```
