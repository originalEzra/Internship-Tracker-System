# Notification Manual Check

Automatic Apifox regression can verify notification query APIs, but reminder-to-notification generation depends on scheduler timing.

For manual verification:

1. Create a reminder with a near-future `remindAt`.
2. Wait until the scheduler processes it.
3. Call:

```http
GET /api/notifications?unreadOnly=true
Authorization: Bearer {{token}}
```

4. Pick a returned notification id and call:

```http
PUT /api/notifications/{{notificationId}}/read
Authorization: Bearer {{token}}
```

Expected:

- The notification has `type = REMINDER_DUE`.
- The notification has `sourceType = REMINDER`.
- `read` changes from `false` to `true`.
- `readAt` becomes non-null.

