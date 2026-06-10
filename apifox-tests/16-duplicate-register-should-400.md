# 16 Duplicate Register Should Return 400

```http
POST {{baseUrl}}/api/users
Content-Type: application/json
```

Body:

```json
{
  "username": "{{userANewUsername}}",
  "email": "{{userANewEmail}}",
  "password": "{{userANewPassword}}"
}
```

Assertions:

```javascript
pm.test("duplicate register returns 400", function () {
  pm.response.to.have.status(400);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(400);
});
```

