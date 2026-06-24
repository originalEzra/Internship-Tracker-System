# 20 Refresh After Logout Should Return 401

After logout, the same refresh token should no longer be usable.

```http
POST {{baseUrl}}/api/users/refresh-token
Content-Type: application/json
```

Body:

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

Assertions:

```javascript
pm.test("refresh token cannot be used after logout", function () {
  pm.response.to.have.status(401);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(401);
});
```
