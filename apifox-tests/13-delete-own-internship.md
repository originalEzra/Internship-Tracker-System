# 13 Delete Own Internship

```http
DELETE {{baseUrl}}/api/internships/{{internshipId}}
Authorization: Bearer {{token}}
```

Assertions:

```javascript
pm.test("delete own internship success", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
});
```

