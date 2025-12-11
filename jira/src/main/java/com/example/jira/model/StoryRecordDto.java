package com.example.jira.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class StoryRecordDto {

    private String featureKey;
    private String storyKey;
    private List<String> subtasks;
    private Instant dateAdded;
    private Instant dateModified;

    public StoryRecordDto(String featureKey, String storyKey, List<String> subtasks,
                          Instant dateAdded, Instant dateModified) {
        this.featureKey = featureKey;
        this.storyKey = storyKey;
        this.subtasks = subtasks;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
    }
}

