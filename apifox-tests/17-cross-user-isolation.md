# 17 Cross-user Internship Isolation

This step uses several requests. You can create a subfolder named `17 Cross-user Internship Isolation` in Apifox.

## 17.1 Create Internship As User A

```http
POST {{baseUrl}}/api/internships
Authorization: Bearer {{token}}
Content-Type: application/json
```

Body:

```json
{
  "company": "User A Private Company",
  "position": "Private Internship",
  "location": "Sydney",
  "status": "APPLIED",
  "applicationUrl": "https://example.com/private"
}
```

Post script:

```javascript
const json = pm.response.json();
pm.environment.set("internshipId", json.data.id);
```

## 17.2 Register User B

```http
POST {{baseUrl}}/api/users
Content-Type: application/json
```

Body:

```json
{
  "username": "{{userBUsername}}",
  "email": "{{userBEmail}}",
  "password": "{{userBPassword}}"
}
```

Expected: `200`

## 17.3 Login User B

```http
POST {{baseUrl}}/api/users/login
Content-Type: application/json
```

Body:

```json
{
  "username": "{{userBUsername}}",
  "password": "{{userBPassword}}"
}
```

Post script:

```javascript
const json = pm.response.json();
pm.environment.set("userBToken", json.data.token);
```

## 17.4 User B Finds User A Internship

```http
GET {{baseUrl}}/api/internships/{{internshipId}}
Authorization: Bearer {{userBToken}}
```

Expected: `404`

## 17.5 User B Updates User A Internship

```http
PUT {{baseUrl}}/api/internships/{{internshipId}}
Authorization: Bearer {{userBToken}}
Content-Type: application/json
```

Body:

```json
{
  "company": "Should Not Update",
  "position": "Should Not Update",
  "location": "Sydney",
  "status": "APPLIED",
  "applicationUrl": "https://example.com/blocked"
}
```

Expected: `404`

