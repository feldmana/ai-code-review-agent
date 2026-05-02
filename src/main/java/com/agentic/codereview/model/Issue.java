package com.agentic.codereview.model;

public class Issue {

    private String type;
    private ReviewResult.Severity severity;
    private String message;
    private String suggestion;

    public Issue() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ReviewResult.Severity getSeverity() {
        return severity;
    }

    public void setSeverity(ReviewResult.Severity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}