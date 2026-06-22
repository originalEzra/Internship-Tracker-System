package com.ezra.internshiptracker.controller;

import com.ezra.internshiptracker.dto.PageResponse;
import com.ezra.internshiptracker.dto.internship.InternshipResponse;
import com.ezra.internshiptracker.entity.InternshipStatus;
import com.ezra.internshiptracker.exception.GlobalExceptionHandler;
import com.ezra.internshiptracker.service.InternshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InternshipControllerTest {

    private InternshipService internshipService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        internshipService = Mockito.mock(InternshipService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new InternshipController(internshipService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getMyInternshipsSupportsPaginationFilteringSearchAndSort() throws Exception {
        InternshipResponse internship = new InternshipResponse();
        internship.setId(10L);
        internship.setCompany("OpenAI");
        internship.setPosition("Backend Intern");
        internship.setStatus(InternshipStatus.APPLIED);

        PageResponse<InternshipResponse> pageResponse =
                new PageResponse<>(List.of(internship), 0, 10, 1, 1, true, true);

        when(internshipService.getMyInternships(
                1L,
                0,
                10,
                InternshipStatus.APPLIED,
                "open",
                "createdAt,desc"
        )).thenReturn(pageResponse);

        mockMvc.perform(get("/api/internships")
                        .principal(new TestingAuthenticationToken("1", null))
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "APPLIED")
                        .param("keyword", "open")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(10))
                .andExpect(jsonPath("$.data.content[0].status").value("APPLIED"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(internshipService).getMyInternships(
                1L,
                0,
                10,
                InternshipStatus.APPLIED,
                "open",
                "createdAt,desc"
        );
    }

    @Test
    void invalidStatusParameterReturns400ApiResponse() throws Exception {
        mockMvc.perform(get("/api/internships")
                        .principal(new TestingAuthenticationToken("1", null))
                        .param("status", "invalid-status"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request parameter"));
    }

    @Test
    void invalidPageParameterReturns400ApiResponse() throws Exception {
        mockMvc.perform(get("/api/internships")
                        .principal(new TestingAuthenticationToken("1", null))
                        .param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request parameter"));
    }
}
