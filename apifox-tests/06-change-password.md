# 06 Change Password

```http
PUT {{baseUrl}}/api/users/me/password
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json
{
  "oldPassword": "{{userAPassword}}",
  "newPassword": "{{userANewPassword}}"
}
```

Assertions:

```javascript
pm.test("change password success", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.message).to.eql("Password updated");
});
```

