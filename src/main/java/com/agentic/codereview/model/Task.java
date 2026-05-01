package com.agentic.codereview.model;

import java.util.Objects;

/**
 * Represents a user task/request to be executed by the agent system
 */
public class Task {
    public enum TaskType {
        REVIEW_CODE,
        SUMMARIZE,
        SEND_EMAIL
    }

    private String id;
    private TaskType type;
    private String projectPath;
    private String description;
    private long createdAt;

    public Task(String id, TaskType type, String projectPath, String description) {
        this.id = Objects.requireNonNull(id, "Task ID cannot be null");
        this.type = Objects.requireNonNull(type, "Task type cannot be null");
        this.projectPath = Objects.requireNonNull(projectPath, "Project path cannot be null");
        this.description = description;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getId() { return id; }
    public TaskType getType() { return type; }
    public String getProjectPath() { return projectPath; }
    public String getDescription() { return description; }
    public long getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", projectPath='" + projectPath + '\'' +
                '}';
    }
}

