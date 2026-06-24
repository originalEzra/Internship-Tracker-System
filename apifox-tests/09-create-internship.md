# 09 Create Internship

```http
POST {{baseUrl}}/api/internships
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json
{
  "company": "Apifox Company",
  "position": "Backend Intern",
  "location": "Sydney",
  "status": "APPLIED",
  "applicationUrl": "https://example.com/job"
}
```

Post script:

```javascript
pm.test("create internship success", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.data.id).to.be.a("number");
  pm.expect(json.data.createdAt).to.be.a("string");
  pm.expect(json.data.updatedAt).to.be.a("string");
  pm.expect(json.data.userId).to.eql(undefined);
  pm.collectionVariables.set("internshipId", json.data.id);
  pm.collectionVariables.set("internshipCreatedAt", json.data.createdAt);
  pm.collectionVariables.set("internshipUpdatedAt", json.data.updatedAt);
});
```
