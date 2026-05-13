package com.agentic.codereview.model;

import java.util.List;

/**
 * Represents the aggregated summary of all code reviews
 */
public class Summary {
    private final List<ReviewResult> reviews;
    private final String generatedAt;
    private int totalIssues;
    private int highSeverityCount;
    private int mediumSeverityCount;
    private int lowSeverityCount;

    public Summary(List<ReviewResult> reviews) {
        this.reviews = reviews;
        this.generatedAt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        calculateMetrics();
    }

    private void calculateMetrics() {
        this.totalIssues = 0;
        this.highSeverityCount = 0;
        this.mediumSeverityCount = 0;
        this.lowSeverityCount = 0;

        for (ReviewResult review : reviews) {
            totalIssues += review.getIssues().size();
            switch (review.getSeverity()) {
                case HIGH -> highSeverityCount++;
                case MEDIUM -> mediumSeverityCount++;
                case LOW -> lowSeverityCount++;
            }
        }
    }

    // Getters
    public List<ReviewResult> getReviews() {
        return reviews;
    }

    public int getTotalIssues() {
        return totalIssues;
    }

    public int getHighSeverityCount() {
        return highSeverityCount;
    }

    public int getMediumSeverityCount() {
        return mediumSeverityCount;
    }

    public int getLowSeverityCount() {
        return lowSeverityCount;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    @Override
    public String toString() {
        return "Summary{" +
                "filesReviewed=" + reviews.size() +
                ", totalIssues=" + totalIssues +
                ", high=" + highSeverityCount +
                ", medium=" + mediumSeverityCount +
                ", low=" + lowSeverityCount +
                '}';
    }
}

