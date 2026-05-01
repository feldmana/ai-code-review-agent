package com.agentic.codereview.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents the review result for a single file
 */
public class ReviewResult {
    public enum Severity {
        LOW, MEDIUM, HIGH
    }

    private String fileName;
    private List<String> issues;
    private List<String> suggestions;
    private Severity severity;
    private String fileContent;
    private long reviewedAt;

    public ReviewResult(String fileName, List<String> issues, List<String> suggestions, Severity severity) {
        this.fileName = Objects.requireNonNull(fileName, "File name cannot be null");
        this.issues = Objects.requireNonNull(issues, "Issues list cannot be null");
        this.suggestions = Objects.requireNonNull(suggestions, "Suggestions list cannot be null");
        this.severity = Objects.requireNonNull(severity, "Severity cannot be null");
        this.reviewedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getFileName() { return fileName; }
    public List<String> getIssues() { return issues; }
    public List<String> getSuggestions() { return suggestions; }
    public Severity getSeverity() { return severity; }
    public String getFileContent() { return fileContent; }
    public void setFileContent(String fileContent) { this.fileContent = fileContent; }
    public long getReviewedAt() { return reviewedAt; }

    @Override
    public String toString() {
        return "ReviewResult{" +
                "fileName='" + fileName + '\'' +
                ", severity=" + severity +
                ", issuesCount=" + issues.size() +
                ", suggestionsCount=" + suggestions.size() +
                '}';
    }
}

