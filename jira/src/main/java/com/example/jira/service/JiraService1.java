package com.example.jira.service;

import com.example.jira.config.JiraConfig;
import com.example.jira.model.StoryRecordDto;
import com.example.jira.repo.StoryRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class JiraService1 {

    private JiraConfig config;
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    // Issue type ids chosen from your issuetype list
    private static final String STORY_ISSUE_TYPE_ID = "10004"; // Story (scoped to project)
    private static final String SUBTASK_ISSUE_TYPE_ID = "10002"; // Subtask
    private final StoryRecordService storyRecordService;
    private StoryRecordRepository storyRecordRepository;

    public JiraService1(JiraConfig config, StoryRecordService storyRecordService) {
        this.config = config;
        this.storyRecordService = storyRecordService;
        this.storyRecordRepository = storyRecordRepository;
    }

    private HttpHeaders authHeaders() {
        String cred = config.getUsername() + ":" + config.getApiToken();
        String encoded = Base64.getEncoder().encodeToString(cred.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Create a Story under the same project as the featureKey and return the new story key.
     */
    public String createStory(String featureKey, String summary, String description) throws Exception {
        String projectKey = featureKey.split("-")[0];

        Map<String, Object> descriptionADF = Map.of(
                "type", "doc",
                "version", 1,
                "content", List.of(
                        Map.of(
                                "type", "paragraph",
                                "content", List.of(
                                        Map.of(
                                                "type", "text",
                                                "text", description == null ? "" : description
                                        )
                                )
                        )
                )
        );

        Map<String, Object> fields = Map.of(
                "summary", summary,
                "description", descriptionADF,
                "issuetype", Map.of("id", STORY_ISSUE_TYPE_ID),
                "project", Map.of("key", projectKey)
        );

        Map<String, Object> payload = Map.of("fields", fields);
        String json = mapper.writeValueAsString(payload);

        HttpEntity<String> req = new HttpEntity<>(json, authHeaders());
        ResponseEntity<Map> resp =
                rest.postForEntity(config.getBaseUrl() + "/rest/api/3/issue", req, Map.class);

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            return resp.getBody().get("key").toString();
        }
        throw new RuntimeException("Failed to create story: " + resp.getStatusCode());
    }
    /**
     * Create an issue link between feature and story. Uses link type "Relates".
     * If you have a specific link type (e.g. "Parent/Child") change "name" accordingly.
     */
    public void createIssueLink(String inwardIssueKey, String outwardIssueKey) throws Exception {
        // The "type.name" value must match one of your issue link types (Relates, Blocks, etc.)
        Map<String, Object> linkPayload = Map.of(
                "type", Map.of("name", "Relates"),
                "inwardIssue", Map.of("key", inwardIssueKey),
                "outwardIssue", Map.of("key", outwardIssueKey)
        );
        String json = mapper.writeValueAsString(linkPayload);
        HttpEntity<String> req = new HttpEntity<>(json, authHeaders());

        rest.postForEntity(config.getBaseUrl() + "/rest/api/3/issueLink", req, Void.class);
    }

    /**
     * Create a subtask under the given storyKey with the provided summary. Returns the created key.
     */
    public String createSubtask(String storyKey, String summary) throws Exception {
        String projectKey = storyKey.split("-")[0];

        Map<String, Object> fields = Map.of(
                "summary", summary,
                "parent", Map.of("key", storyKey),
                "issuetype", Map.of("id", SUBTASK_ISSUE_TYPE_ID),
                "project", Map.of("key", projectKey)
        );

        Map<String, Object> payload = Map.of("fields", fields);
        String json = mapper.writeValueAsString(payload);

        HttpEntity<String> req = new HttpEntity<>(json, authHeaders());
        ResponseEntity<Map> resp = rest.postForEntity(config.getBaseUrl() + "/rest/api/3/issue", req, Map.class);

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            return resp.getBody().get("key").toString();
        }
        throw new RuntimeException("Failed to create subtask: " + resp.getStatusCode());
    }

    /**
     * Create a subtask under the given storyKey with the provided summary. Returns the created key in single call.
     */
    public List<String> createSubtasksInBulk(String parentStoryKey, List<String> taskNames)
            throws Exception {

        // derive project key from parent (e.g. SCRUM-123 → SCRUM)
        String projectKey = parentStoryKey.split("-")[0];

        List<Map<String, Object>> issueUpdates = new ArrayList<>();

        for (String taskName : taskNames) {
            Map<String, Object> fields = Map.of(
                    "project", Map.of("key", projectKey), // 🔥 REQUIRED
                    "summary", taskName,
                    "issuetype", Map.of("id", SUBTASK_ISSUE_TYPE_ID),
                    "parent", Map.of("key", parentStoryKey)
            );

            issueUpdates.add(Map.of("fields", fields));
        }

        Map<String, Object> payload = Map.of("issueUpdates", issueUpdates);

        String json = mapper.writeValueAsString(payload);
        HttpEntity<String> req = new HttpEntity<>(json, authHeaders());

        ResponseEntity<Map> resp = rest.postForEntity(
                config.getBaseUrl() + "/rest/api/3/issue/bulk",
                req,
                Map.class
        );

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new RuntimeException("Bulk subtask creation failed: " + resp);
        }

        List<Map<String, Object>> issues =
                (List<Map<String, Object>>) resp.getBody().get("issues");

        return issues.stream()
                .map(i -> i.get("key").toString())
                .toList();
    }

    /**
     * High-level operation: create a story under the feature and create multiple subtasks under that story.
     * Returns a map containing newStoryKey and createdSubtaskKeys.
     */
    public Map<String, Object> createStoryAndSubtasks(String featureKey, String storyType, String storyName, String storyDescription, List<String> taskNames) throws Exception {
        // Build summary and description for the new story
        String description =  (storyDescription == null || storyDescription.isBlank())
        ? "Auto Story for " + featureKey + " - " + Instant.now().toString()
        : storyDescription;
        
        String finalSummary = (storyName == null || storyName.isBlank())
                ? "Auto Story for " + featureKey + " - " + Instant.now().toString()
                : storyName;

        // 1) create Jira story
        String storyKey = createStory(featureKey, finalSummary, description);

        /*// 2) create subtasks under the story
        List<String> createdSubtasks = new ArrayList<>();
        for (String t : taskNames) {
            createdSubtasks.add(createSubtask(storyKey, t));
        }*/

        // 2) Bulk create subtasks (ONE HTTP CALL)
        List<String> createdSubtasks = createSubtasksInBulk(storyKey, taskNames);

        // 3) store in SAME TABLE as JSON
        storyRecordService.saveStory(featureKey, storyKey, createdSubtasks);

        // 4) link
        createIssueLink(featureKey, storyKey);

        return Map.of("storyKey", storyKey, "subtasks", createdSubtasks);
    }

    public List<StoryRecordDto> getStoryRecords(){
        return storyRecordService.getAllStories();
    }
}

