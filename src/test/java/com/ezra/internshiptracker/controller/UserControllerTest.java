package com.ezra.internshiptracker.controller;

import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.exception.GlobalExceptionHandler;
import com.ezra.internshiptracker.exception.LoginFailedException;
import com.ezra.internshiptracker.service.AuthService;
import com.ezra.internshiptracker.service.UserService;
import com.ezra.internshiptracker.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private UserService userService;
    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        authService = Mockito.mock(AuthService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService, authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registerReturnsUserResponseWithoutPassword() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(user(1L, "ezra", "ezra@example.com"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ezra",
                                  "email": "ezra@example.com",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("User created"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("ezra"))
                .andExpect(jsonPath("$.data.email").value("ezra@example.com"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void loginReturnsTokenAndUser() throws Exception {
        User user = user(1L, "ezra", "ezra@example.com");

        when(userService.login(any())).thenReturn(user);
        when(authService.createAccessToken(user)).thenReturn("jwt-token");
        when(authService.createRefreshToken(user)).thenReturn("refresh-token");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ezra",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.user.role").value("USER"))
                .andExpect(jsonPath("$.data.user.id").value(1));
    }

    @Test
    void refreshTokenReturnsNewAccessToken() throws Exception {
        when(authService.refreshAccessToken("refresh-token")).thenReturn("new-jwt-token");

        mockMvc.perform(post("/api/users/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Token refreshed"))
                .andExpect(jsonPath("$.data.token").value("new-jwt-token"));
    }

    @Test
    void logoutDeletesRefreshToken() throws Exception {
        mockMvc.perform(post("/api/users/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Logged out"));

        verify(authService).logout("refresh-token");
    }

    @Test
    void loginFailureReturns401ApiResponse() throws Exception {
        when(userService.login(any()))
                .thenThrow(new LoginFailedException("Username or password is incorrect"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ezra",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Username or password is incorrect"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getCurrentUserReadsUserIdFromAuthentication() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user(1L, "ezra", "ezra@example.com"));

        mockMvc.perform(get("/api/users/me")
                        .principal(new TestingAuthenticationToken("1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("ezra"));

        verify(userService).getUserById(1L);
    }

    @Test
    void updateCurrentUserUsesAuthenticatedUserId() throws Exception {
        when(userService.updateUser(any(Long.class), any(User.class)))
                .thenReturn(user(1L, "ezra-updated", "updated@example.com"));

        mockMvc.perform(put("/api/users/me")
                        .principal(new TestingAuthenticationToken("1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ezra-updated",
                                  "email": "updated@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("User updated"))
                .andExpect(jsonPath("$.data.username").value("ezra-updated"));

        verify(userService).updateUser(any(Long.class), any(User.class));
    }

    private User user(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
