package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.config.AssistantLlmProperties;
import com.ezra.internshiptracker.dto.assistant.AssistantAdviceResponse;
import com.ezra.internshiptracker.dto.assistant.AssistantAdviceSource;
import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.entity.Reminder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiAssistantLlmClient implements AssistantLlmClient {

    private final AssistantLlmProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAiAssistantLlmClient(AssistantLlmProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.restClient = RestClient.create();
    }

    @Override
    public AssistantAdviceResponse generateAdvice(
            Internship internship,
            List<Reminder> pendingReminders,
            AssistantAdviceResponse ruleBasedAdvice
    ) {
        OpenAiResponse openAiResponse = restClient.post()
                .uri(properties.getBaseUrl())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "model", properties.getModel(),
                        "input", buildPrompt(internship, pendingReminders, ruleBasedAdvice)
                ))
                .retrieve()
                .body(OpenAiResponse.class);

        if (openAiResponse == null || openAiResponse.output_text() == null) {
            throw new IllegalStateException("LLM response did not include output_text");
        }

        AssistantAdviceResponse response = parseAdvice(openAiResponse.output_text());
        response.setInternshipId(ruleBasedAdvice.getInternshipId());
        response.setStatus(ruleBasedAdvice.getStatus());
        response.setSource(AssistantAdviceSource.LLM);
        return response;
    }

    private String buildPrompt(
            Internship internship,
            List<Reminder> pendingReminders,
            AssistantAdviceResponse ruleBasedAdvice
    ) {
        return """
                You are an internship application assistant.
                Return strict JSON only, with this shape:
                {"summary":"short summary","suggestions":["suggestion 1","suggestion 2","suggestion 3"]}

                Keep suggestions practical, concise, and safe. Do not invent facts.

                Internship:
                - company: %s
                - position: %s
                - status: %s
                - updatedAt: %s
                - pending reminder count: %d

                Existing deterministic advice:
                - summary: %s
                - suggestions: %s
                """.formatted(
                internship.getCompany(),
                internship.getPosition(),
                internship.getStatus(),
                internship.getUpdatedAt(),
                pendingReminders.size(),
                ruleBasedAdvice.getSummary(),
                ruleBasedAdvice.getSuggestions()
        );
    }

    private AssistantAdviceResponse parseAdvice(String outputText) {
        try {
            JsonNode root = objectMapper.readTree(extractJsonObject(outputText));
            String summary = root.path("summary").asText(null);
            JsonNode suggestionsNode = root.path("suggestions");

            if (summary == null || summary.isBlank() || !suggestionsNode.isArray()) {
                throw new IllegalArgumentException("LLM advice JSON is missing required fields");
            }

            List<String> suggestions = new ArrayList<>();
            suggestionsNode.forEach(node -> {
                String suggestion = node.asText();
                if (!suggestion.isBlank()) {
                    suggestions.add(suggestion);
                }
            });

            if (suggestions.isEmpty()) {
                throw new IllegalArgumentException("LLM advice JSON has no suggestions");
            }

            AssistantAdviceResponse response = new AssistantAdviceResponse();
            response.setSummary(summary);
            response.setSuggestions(suggestions);
            return response;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse LLM advice JSON", e);
        }
    }

    private String extractJsonObject(String outputText) {
        int start = outputText.indexOf('{');
        int end = outputText.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("LLM response did not contain a JSON object");
        }
        return outputText.substring(start, end + 1);
    }

    private record OpenAiResponse(String output_text) {
    }
}
