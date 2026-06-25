package com.ezra.internshiptracker.integration;

import com.ezra.internshiptracker.entity.Role;
import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationIntegrationTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("internship_tracker_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7.2-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void realMysqlFlowCoversFlywayJwtInternshipAndRbac() throws Exception {
        assertFlywayAppliedMigrations();

        post("/api/users", """
                {
                  "username": "tc_user",
                  "email": "tc_user@example.com",
                  "password": "password123"
                }
                """, null, 200);

        JsonNode userLogin = post("/api/users/login", """
                {
                  "username": "tc_user",
                  "password": "password123"
                }
                """, null, 200);
        String userToken = userLogin.at("/data/token").asText();

        JsonNode createdInternship = post("/api/internships", """
                {
                  "company": "Testcontainers Company",
                  "position": "Backend Intern",
                  "location": "Sydney",
                  "status": "APPLIED",
                  "applicationUrl": "https://example.com/internship"
                }
                """, userToken, 200);
        long internshipId = createdInternship.at("/data/id").asLong();

        JsonNode internships = get("/api/internships", userToken, 200);
        assertThat(containsId(internships.at("/data/content"), internshipId)).isTrue();

        put("/api/internships/" + internshipId, """
                {
                  "company": "Testcontainers Company",
                  "position": "Backend Intern",
                  "location": "Sydney",
                  "status": "ONLINE_ASSESSMENT",
                  "applicationUrl": "https://example.com/internship",
                  "statusNote": "Received OA"
                }
                """, userToken, 200);

        JsonNode statusHistory = get("/api/internships/" + internshipId + "/status-history", userToken, 200);
        assertThat(statusHistory.at("/data/0/fromStatus").asText()).isEqualTo("APPLIED");
        assertThat(statusHistory.at("/data/0/toStatus").asText()).isEqualTo("ONLINE_ASSESSMENT");
        assertThat(statusHistory.at("/data/0/note").asText()).isEqualTo("Received OA");

        JsonNode createdReminder = post("/api/reminders", """
                {
                  "internshipId": %d,
                  "message": "OA due tomorrow",
                  "remindAt": "2027-01-02T10:00:00"
                }
                """.formatted(internshipId), userToken, 200);
        long reminderId = createdReminder.at("/data/id").asLong();
        assertThat(createdReminder.at("/data/status").asText()).isEqualTo("PENDING");

        JsonNode pendingReminders = get("/api/reminders?status=PENDING", userToken, 200);
        assertThat(containsId(pendingReminders.at("/data"), reminderId)).isTrue();

        JsonNode advice = get("/api/assistant/internships/" + internshipId + "/advice", userToken, 200);
        assertThat(advice.at("/data/status").asText()).isEqualTo("ONLINE_ASSESSMENT");
        assertThat(containsSuggestion(advice.at("/data/suggestions"), "online assessment")).isTrue();
        assertThat(containsSuggestion(advice.at("/data/suggestions"), "pending reminder")).isTrue();

        JsonNode cancelledReminder = put("/api/reminders/" + reminderId + "/cancel", "", userToken, 200);
        assertThat(cancelledReminder.at("/data/status").asText()).isEqualTo("CANCELLED");

        JsonNode forbiddenAdminAccess = get("/api/admin/users", userToken, 403);
        assertThat(forbiddenAdminAccess.path("code").asInt()).isEqualTo(403);

        post("/api/users", """
                {
                  "username": "tc_admin",
                  "email": "tc_admin@example.com",
                  "password": "password123"
                }
                """, null, 200);
        promoteToAdmin("tc_admin");

        JsonNode adminLogin = post("/api/users/login", """
                {
                  "username": "tc_admin",
                  "password": "password123"
                }
                """, null, 200);
        String adminToken = adminLogin.at("/data/token").asText();

        JsonNode adminUsers = get("/api/admin/users", adminToken, 200);
        assertThat(containsUsername(adminUsers.at("/data"), "tc_user")).isTrue();
        assertThat(containsUsername(adminUsers.at("/data"), "tc_admin")).isTrue();

        String userRefreshToken = userLogin.at("/data/refreshToken").asText();
        post("/api/users/logout", """
                {
                  "refreshToken": "%s"
                }
                """.formatted(userRefreshToken), userToken, 200);

        JsonNode blockedAfterLogout = get("/api/users/me", userToken, 401);
        assertThat(blockedAfterLogout.path("code").asInt()).isEqualTo(401);
    }

    private boolean containsId(JsonNode items, long id) {
        for (JsonNode item : items) {
            if (item.path("id").asLong() == id) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSuggestion(JsonNode suggestions, String text) {
        for (JsonNode suggestion : suggestions) {
            if (suggestion.asText().contains(text)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsUsername(JsonNode users, String username) {
        for (JsonNode user : users) {
            if (username.equals(user.path("username").asText())) {
                return true;
            }
        }
        return false;
    }

    private void assertFlywayAppliedMigrations() {
        Integer appliedMigrations = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1",
                Integer.class
        );

        assertThat(appliedMigrations).isGreaterThanOrEqualTo(8);
        assertThat(tableExists("users")).isTrue();
        assertThat(tableExists("internships")).isTrue();
        assertThat(tableExists("refresh_tokens")).isTrue();
        assertThat(tableExists("internship_status_history")).isTrue();
        assertThat(tableExists("reminders")).isTrue();
    }

    private boolean tableExists(String tableName) {
        Integer tableCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                """,
                Integer.class,
                tableName
        );

        return tableCount != null && tableCount == 1;
    }

    private void promoteToAdmin(String username) {
        User admin = userRepository.findByUsername(username).orElseThrow();
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }

    private JsonNode get(String path, String token, int expectedStatus) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path)).GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        return send(builder.build(), expectedStatus);
    }

    private JsonNode post(String path, String body, String token, int expectedStatus)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        return send(builder.build(), expectedStatus);
    }

    private JsonNode put(String path, String body, String token, int expectedStatus)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        return send(builder.build(), expectedStatus);
    }

    private JsonNode send(HttpRequest request, int expectedStatus) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(expectedStatus);
        return objectMapper.readTree(response.body());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
