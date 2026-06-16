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
  "status": "INTERVIEW",
  "applicationUrl": "https://example.com/job-updated"
}
```

Assertions:

```javascript
pm.test("update own internship success", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.data.id).to.eql(Number(pm.environment.get("internshipId")));
  pm.expect(json.data.company).to.eql("Apifox Company Updated");
});
```

