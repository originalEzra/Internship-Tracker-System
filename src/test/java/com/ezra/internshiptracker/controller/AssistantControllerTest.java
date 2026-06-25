package com.ezra.internshiptracker.controller;

import com.ezra.internshiptracker.dto.assistant.AssistantAdviceResponse;
import com.ezra.internshiptracker.entity.InternshipStatus;
import com.ezra.internshiptracker.exception.GlobalExceptionHandler;
import com.ezra.internshiptracker.exception.InternshipNotFoundException;
import com.ezra.internshiptracker.service.AssistantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssistantControllerTest {

    private AssistantService assistantService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        assistantService = Mockito.mock(AssistantService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AssistantController(assistantService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getInternshipAdviceUsesCurrentUser() throws Exception {
        when(assistantService.getAdvice(10L, 1L)).thenReturn(advice());

        mockMvc.perform(get("/api/assistant/internships/10/advice")
                        .principal(new TestingAuthenticationToken("1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.internshipId").value(10))
                .andExpect(jsonPath("$.data.status").value("TECH_INTERVIEW"))
                .andExpect(jsonPath("$.data.suggestions[0]").value("Prepare Java and Spring Boot questions."));
    }

    @Test
    void missingInternshipReturns404ApiResponse() throws Exception {
        when(assistantService.getAdvice(10L, 2L))
                .thenThrow(new InternshipNotFoundException("Internship not found"));

        mockMvc.perform(get("/api/assistant/internships/10/advice")
                        .principal(new TestingAuthenticationToken("2", null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Internship not found"));
    }

    private AssistantAdviceResponse advice() {
        AssistantAdviceResponse response = new AssistantAdviceResponse();
        response.setInternshipId(10L);
        response.setStatus(InternshipStatus.TECH_INTERVIEW);
        response.setSummary("You are currently in the technical interview stage.");
        response.setSuggestions(List.of("Prepare Java and Spring Boot questions."));
        return response;
    }
}
