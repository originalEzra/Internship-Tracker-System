# 01 Register User A

```http
POST {{baseUrl}}/api/users
Content-Type: application/json
```

Body:

```json
{
  "username": "{{userAUsername}}",
  "email": "{{userAEmail}}",
  "password": "{{userAPassword}}"
}
```

Assertions:

```javascript
pm.test("register user A success", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.data.username).to.eql(pm.environment.get("userAUsername"));
});
```

