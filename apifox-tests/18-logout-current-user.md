# 18 Logout Current User

This deletes the current refresh token. The existing access token may still work until it expires because JWT access tokens are stateless.

```http
POST {{baseUrl}}/api/users/logout
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
pm.test("logout deletes refresh token", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.message).to.eql("Logged out");
});
```
