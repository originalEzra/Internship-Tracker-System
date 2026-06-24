# 10 Get My Internships

```http
GET {{baseUrl}}/api/internships?page=0&size=10&status=APPLIED&keyword=Apifox&sort=updatedAt,desc
Authorization: Bearer {{token}}
```

Assertions:

```javascript
pm.test("get my internships contains created internship", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.data.content).to.be.an('array');

  const internshipId = Number(pm.collectionVariables.get("internshipId"));
  const item = json.data.content.find(item => item.id === internshipId);
  pm.expect(item).to.not.eql(undefined);
  pm.expect(item.updatedAt).to.be.a("string");
});
```
