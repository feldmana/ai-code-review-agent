package com.agentic.codereview.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents the review result for a single file
 */
public class ReviewResult {

    private final String fileName;
    private final long reviewedAt;
    private List<Issue> issues;
    private List<String> suggestions;
    private Severity severity;
    private String fileContent;

    public ReviewResult(String fileName,
                        List<Issue> issues,
                        List<String> suggestions,
                        Severity severity) {

        this.fileName = Objects.requireNonNull(fileName, "File name cannot be null");
        this.issues = Objects.requireNonNull(issues, "Issues list cannot be null");
        this.suggestions = Objects.requireNonNull(suggestions, "Suggestions list cannot be null");
        this.severity = Objects.requireNonNull(severity, "Severity cannot be null");
        this.reviewedAt = System.currentTimeMillis();
    }

    public String getFileName() {
        return fileName;
    }

    // Getters and Setters

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public long getReviewedAt() {
        return reviewedAt;
    }

    @Override
    public String toString() {
        return "ReviewResult{" +
                "fileName='" + fileName + '\'' +
                ", severity=" + severity +
                ", issuesCount=" + (issues == null ? 0 : issues.size()) +
                ", suggestionsCount=" + (suggestions == null ? 0 : suggestions.size()) +
                '}';
    }

    public enum Severity {
        LOW, MEDIUM, HIGH
    }
}