package com.ezra.internshiptracker.controller;

import com.ezra.internshiptracker.entity.Role;
import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.exception.GlobalExceptionHandler;
import com.ezra.internshiptracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminControllerTest {

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllUsersReturnsUserResponsesWithoutPasswords() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                user(1L, "ezra", "ezra@example.com", Role.USER),
                user(2L, "admin", "admin@example.com", Role.ADMIN)
        ));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].username").value("ezra"))
                .andExpect(jsonPath("$.data[0].role").value("USER"))
                .andExpect(jsonPath("$.data[0].password").doesNotExist())
                .andExpect(jsonPath("$.data[1].username").value("admin"))
                .andExpect(jsonPath("$.data[1].role").value("ADMIN"))
                .andExpect(jsonPath("$.data[1].password").doesNotExist());
    }

    private User user(Long id, String username, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
