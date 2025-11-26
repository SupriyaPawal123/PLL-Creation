package com.example.jira.controller;

import com.example.jira.service.ExcelService;
import com.example.jira.service.JiraService;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jira")
public class JiraController {

    private final ExcelService excelService;
    private final JiraService jiraService;

    public JiraController(ExcelService excelService, JiraService jiraService) {
        this.excelService = excelService;
        this.jiraService = jiraService;
    }

    @PostMapping(value = "/create-subtasks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createSubtasks(
            @RequestPart("storyKey") String storyKey,
            @RequestPart("storyType") String storyType,
            @RequestPart("file") MultipartFile excelFile) throws JsonProcessingException {

        List<String> taskNames = excelService.readTasks(storyType, excelFile);

        List<String> created = new ArrayList<>();

        for (String name : taskNames) {
            String subtaskKey = jiraService.createSubtask(storyKey, name);
            created.add(subtaskKey);
        }

        return ResponseEntity.ok(created);
    }
    

}
