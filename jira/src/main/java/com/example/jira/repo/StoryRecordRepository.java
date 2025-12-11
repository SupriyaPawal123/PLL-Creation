package com.example.jira.repo;

import com.example.jira.model.StoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRecordRepository extends JpaRepository<StoryRecord, Long> {

    StoryRecord findByStoryKey(String storyKey);
}
