package com.example.jira.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JiraConfig {

    @Value("${jira.baseUrl}")
    private String baseUrl;

    @Value("${jira.username}")
    private String username;

    @Value("${jira.apiToken}")
    private String apiToken;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getApiToken() {
        return apiToken;
    }
}
