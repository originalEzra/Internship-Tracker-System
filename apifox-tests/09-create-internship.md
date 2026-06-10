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
  "status": "Applied",
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
  pm.expect(json.data.userId).to.eql(undefined);
  pm.environment.set("internshipId", json.data.id);
});
```

