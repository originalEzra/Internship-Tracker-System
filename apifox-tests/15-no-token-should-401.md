# 15 No Token Should Return 401

```http
GET {{baseUrl}}/api/users/me
```

No `Authorization` header.

Assertions:

```javascript
pm.test("no token returns 401", function () {
  pm.response.to.have.status(401);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(401);
});
```

