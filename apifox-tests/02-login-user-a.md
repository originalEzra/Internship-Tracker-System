# 02 Login User A

```http
POST {{baseUrl}}/api/users/login
Content-Type: application/json
```

Body:

```json
{
  "username": "{{userAUsername}}",
  "password": "{{userAPassword}}"
}
```

Post script:

```javascript
pm.test("login user A success", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.data.token).to.be.a("string");

  pm.environment.set("token", json.data.token);
  pm.environment.set("userId", json.data.user.id);
});
```

