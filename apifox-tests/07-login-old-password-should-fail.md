# 07 Login Old Password Should Fail

```http
POST {{baseUrl}}/api/users/login
Content-Type: application/json
```

Body:

```json
{
  "username": "{{userANewUsername}}",
  "password": "{{userAPassword}}"
}
```

Assertions:

```javascript
pm.test("old password login fails", function () {
  pm.response.to.have.status(401);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(401);
});
```

