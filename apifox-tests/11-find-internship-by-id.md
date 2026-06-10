# 11 Find Internship By Id

```http
GET {{baseUrl}}/api/internships/{{internshipId}}
Authorization: Bearer {{token}}
```

Assertions:

```javascript
pm.test("find own internship by id success", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.data.id).to.eql(Number(pm.environment.get("internshipId")));
});
```

