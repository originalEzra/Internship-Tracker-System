package com.ezra.internshiptracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "assistant.llm")
public class AssistantLlmProperties {

    private boolean enabled = false;

    private String provider = "openai";

    private String apiKey = "";

    private String model = "gpt-4.1-mini";

    private String baseUrl = "https://api.openai.com/v1/responses";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
