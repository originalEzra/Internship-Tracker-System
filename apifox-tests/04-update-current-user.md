# 04 Update Current User

```http
PUT {{baseUrl}}/api/users/me
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json
{
  "username": "{{userANewUsername}}",
  "email": "{{userANewEmail}}"
}
```

Assertions:

```javascript
pm.test("update current user success", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.data.username).to.eql(pm.environment.get("userANewUsername"));
  pm.expect(json.data.email).to.eql(pm.environment.get("userANewEmail"));
});
```

Important: because JWT subject is `userId`, the old token should still work after username changes.

