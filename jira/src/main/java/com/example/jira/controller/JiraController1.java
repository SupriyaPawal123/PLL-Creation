package com.example.jira.controller;

import com.example.jira.service.ExcelService;
import com.example.jira.service.JiraService;
import com.example.jira.service.JiraService1;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/jira/v1")
public class JiraController1 {

    private final ExcelService excelService;
    private final JiraService1 jiraService;

    public JiraController1(ExcelService excelService, JiraService1 jiraService) {
        this.excelService = excelService;
        this.jiraService = jiraService;
    }

    @PostMapping(value = "/create-story-and-subtasks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createStoryAndSubtasks(
            @RequestPart("featureKey") String featureKey,
            @RequestPart("storyType") String storyType,
            @RequestPart(name = "storySummary", required = false) String storySummary,
            @RequestPart(name = "storyDescription", required = false) String storyDescription,
            @RequestPart("file") MultipartFile excelFile) {

        try {
            // read task names from excel (FullPLL -> 44, LightPLL -> 11)
            List<String> taskNames = excelService.readTasks(storyType, excelFile);

            Map<String, Object> result = jiraService.createStoryAndSubtasks(featureKey, storyType,storySummary, storyDescription, taskNames);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
