# 21 Login After Logout For Cleanup

This step logs in again only to get a fresh access token for deleting the test user.

The old access token should stay unusable after logout. This new token belongs to a new session.

```http
POST {{baseUrl}}/api/users/login
Content-Type: application/json
```

Body:

```json
{
  "username": "{{userANewUsername}}",
  "password": "{{userANewPassword}}"
}
```

Assertions:

```javascript
pm.test("login after logout succeeds for cleanup", function () {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.code).to.eql(200);
  pm.expect(json.data.token).to.be.a("string");
  pm.collectionVariables.set("token", json.data.token);
  pm.collectionVariables.set("refreshToken", json.data.refreshToken);
});
```
