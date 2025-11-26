package com.example.jira.service;

import com.example.jira.config.JiraConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class JiraService {

    private final JiraConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    public JiraService(JiraConfig config) {
        this.config = config;
    }

    public String createSubtask(String parentKey, String summary) throws JsonProcessingException {

        String projectKey = parentKey.split("-")[0];

        String authValue = config.getUsername() + ":" + config.getApiToken();
        String encodedAuth = Base64.getEncoder().encodeToString(authValue.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build JSON safely
        Map<String, Object> fields = Map.of(
                "summary", summary,
                "parent", Map.of("key", parentKey),
                "issuetype", Map.of("id", "10002"),   // Use your Sub-task issue type ID
                "project", Map.of("key", projectKey)
        );

        Map<String, Object> payload = Map.of("fields", fields);

        ObjectMapper mapper = new ObjectMapper();
        String bodyJson = mapper.writeValueAsString(payload);

        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                config.getBaseUrl() + "/rest/api/3/issue",
                entity,
                Map.class
        );

        return response.getBody().get("key").toString();
    }

}
