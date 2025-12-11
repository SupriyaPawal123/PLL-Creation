package com.example.jira.service;

import com.example.jira.model.StoryRecord;
import com.example.jira.model.StoryRecordDto;
import com.example.jira.repo.StoryRecordRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoryRecordService {

    private final StoryRecordRepository repo;

    public StoryRecordService(StoryRecordRepository repo) {
        this.repo = repo;
    }

    public StoryRecord saveStory(String featureKey, String storyKey, List<String> subtasks) {
        StoryRecord record = new StoryRecord(featureKey, storyKey, subtasks);
        return repo.save(record);
    }

    public StoryRecord updateStory(String storyKey, List<String> updatedSubtasks) {
        StoryRecord record = repo.findByStoryKey(storyKey);

        if (record == null) {
            throw new RuntimeException("Story not found: " + storyKey);
        }

        record.updateSubtasks(updatedSubtasks);
        return repo.save(record);
    }

    public List<StoryRecordDto> getAllStories() {
        return repo.findAll().stream()
                .map(record -> new StoryRecordDto(
                        record.getFeatureKey(),
                        record.getStoryKey(),
                        fromJson(record.getSubtasksJson()),
                        record.getDateAdded(),
                        record.getDateModified()
                ))
                .collect(Collectors.toList());
    }

    private List<String> fromJson(String json) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json,
                            new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to List", e);
        }
    }
}