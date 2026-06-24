# 19 Access Token After Logout Should Return 401

After logout, the same access token should be rejected because it has been added to the Redis blacklist.

```http
GET {{baseUrl}}/api/users/me
Authorization: Bearer {{token}}
```

Assertions:

```javascript
pm.test("blacklisted access token cannot access current user", function () {
  pm.response.to.have.status(401);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(401);
});
```
