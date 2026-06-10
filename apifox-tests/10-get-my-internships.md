# 10 Get My Internships

```http
GET {{baseUrl}}/api/internships
Authorization: Bearer {{token}}
```

Assertions:

```javascript
pm.test("get my internships contains created internship", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);

  const internshipId = Number(pm.environment.get("internshipId"));
  const found = json.data.some(item => item.id === internshipId);
  pm.expect(found).to.eql(true);
});
```

