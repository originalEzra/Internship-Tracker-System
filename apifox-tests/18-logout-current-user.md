# 18 Logout Current User

This deletes the current refresh token and blacklists the current access token in Redis.

```http
POST {{baseUrl}}/api/users/logout
Content-Type: application/json
Authorization: Bearer {{token}}
```

Body:

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

Assertions:

```javascript
pm.test("logout deletes refresh token and blacklists access token", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.message).to.eql("Logged out");
});
```
