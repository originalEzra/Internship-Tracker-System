# 12 Update Own Internship

```http
PUT {{baseUrl}}/api/internships/{{internshipId}}
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json
{
  "company": "Apifox Company Updated",
  "position": "Backend Intern Updated",
  "location": "Melbourne",
  "status": "ONLINE_ASSESSMENT",
  "applicationUrl": "https://example.com/job-updated"
}
```

Assertions:

```javascript
pm.test("update own internship success", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.data.id).to.eql(Number(pm.collectionVariables.get("internshipId")));
  pm.expect(json.data.company).to.eql("Apifox Company Updated");
  pm.expect(json.data.status).to.eql("ONLINE_ASSESSMENT");
  pm.expect(json.data.createdAt).to.eql(pm.collectionVariables.get("internshipCreatedAt"));
  pm.expect(json.data.updatedAt).to.be.a("string");
  pm.collectionVariables.set("internshipUpdatedAtAfterUpdate", json.data.updatedAt);
});
```
