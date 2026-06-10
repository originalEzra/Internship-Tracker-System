# 14 Find Deleted Internship Should Return 404

```http
GET {{baseUrl}}/api/internships/{{internshipId}}
Authorization: Bearer {{token}}
```

Assertions:

```javascript
pm.test("deleted internship cannot be found", function () {
  pm.response.to.have.status(404);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(404);
});
```

