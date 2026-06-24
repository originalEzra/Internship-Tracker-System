# 22 Delete Current User

Use this at the very end because it deletes the test user.

```http
DELETE {{baseUrl}}/api/users/me
Authorization: Bearer {{token}}
```

Assertions:

```javascript
pm.test("delete current user success", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
});
```

Optional cleanup:

- Login as user B and delete user B too.
- Delete the private internship created in step 17 first if your database does not cascade user deletion.
