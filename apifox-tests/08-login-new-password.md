# 08 Login New Password

```http
POST {{baseUrl}}/api/users/login
Content-Type: application/json
```

Body:

```json
{
  "username": "{{userANewUsername}}",
  "password": "{{userANewPassword}}"
}
```

Post script:

```javascript
pm.test("new password login succeeds", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.data.refreshToken).to.be.a("string");
  pm.environment.set("newToken", json.data.token);
  pm.environment.set("token", json.data.token);
  pm.environment.set("refreshToken", json.data.refreshToken);
});
```
