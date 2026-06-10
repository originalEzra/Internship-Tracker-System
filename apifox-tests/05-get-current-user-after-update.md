# 05 Get Current User After Update

```http
GET {{baseUrl}}/api/users/me
Authorization: Bearer {{token}}
```

Assertions:

```javascript
pm.test("old token still works after username update", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.data.id).to.eql(Number(pm.environment.get("userId")));
  pm.expect(json.data.username).to.eql(pm.environment.get("userANewUsername"));
});
```

