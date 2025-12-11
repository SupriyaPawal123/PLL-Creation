package com.example.jira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "story_records")
@Getter
@Setter
public class StoryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String featureKey;
    private String storyKey;

    private Instant dateAdded;
    private Instant dateModified;
    @Column(columnDefinition = "CLOB")   // store JSON string
    private String subtasksJson;

    public StoryRecord() {}

    public StoryRecord(String featureKey, String storyKey, List<String> subtasks) {
        this.featureKey = featureKey;
        this.storyKey = storyKey;
        this.dateAdded = Instant.now();
        this.dateModified = Instant.now();
        this.subtasksJson = toJson(subtasks);
    }

    public StoryRecord(String featureKey, String storyKey, List<String> strings, Instant dateAdded, Instant dateModified) {
        this.featureKey = featureKey;
        this.storyKey = storyKey;
        this.subtasksJson = toJson(strings);
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
    }

    public void updateSubtasks(List<String> subtasks) {
        this.subtasksJson = toJson(subtasks);
        this.dateModified = Instant.now();
    }

    private String toJson(List<String> list) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
