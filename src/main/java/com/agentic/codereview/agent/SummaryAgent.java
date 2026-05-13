package com.agentic.codereview.agent;

import com.agentic.codereview.model.ReviewResult;
import com.agentic.codereview.model.Summary;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * SummaryAgent aggregates multiple code reviews into a single summary
 */
public class SummaryAgent {
    private static final Logger logger = LoggerFactory.getLogger(SummaryAgent.class);
    private static final Gson gson = new Gson();

    /**
     * Creates a summary from multiple review results
     */
    public Summary summarizeReviews(List<ReviewResult> reviews) {
        logger.info("Summarizing {} reviews", reviews.size());

        Summary summary = new Summary(reviews);

        logger.info("Summary created: {} files, {} total issues",
                reviews.size(), summary.getTotalIssues());
        logger.info("Severity breakdown - High: {}, Medium: {}, Low: {}",
                summary.getHighSeverityCount(),
                summary.getMediumSeverityCount(),
                summary.getLowSeverityCount());

        return summary;
    }

    /**
     * Gets high-severity files that need attention
     */
    public List<ReviewResult> getHighSeverityReviews(Summary summary) {
        return summary.getReviews().stream()
                .filter(r -> r.getSeverity() == ReviewResult.Severity.HIGH)
                .toList();
    }

    /**
     * Gets the most problematic files based on issue count
     */
    public List<ReviewResult> getMostProblematicFiles(Summary summary, int topN) {
        return summary.getReviews().stream()
                .sorted((a, b) -> Integer.compare(b.getIssues().size(), a.getIssues().size()))
                .limit(topN)
                .toList();
    }

    /**
     * Generates a JSON representation of the summary
     */
    public String summaryToJson(Summary summary) {
        JsonObject json = new JsonObject();

        json.addProperty("filesReviewed", summary.getReviews().size());
        json.addProperty("totalIssues", summary.getTotalIssues());
        json.addProperty("highSeverity", summary.getHighSeverityCount());
        json.addProperty("mediumSeverity", summary.getMediumSeverityCount());
        json.addProperty("lowSeverity", summary.getLowSeverityCount());
        json.addProperty("generatedAt", summary.getGeneratedAt());

        return gson.toJson(json);
    }

    /**
     * Generates a brief executive summary
     */
    public String generateExecutiveSummary(Summary summary) {
        int totalFiles = summary.getReviews().size();
        int totalIssues = summary.getTotalIssues();
        int avgIssuesPerFile = totalFiles > 0 ? totalIssues / totalFiles : 0;

        return String.format("""
                        Code Review Executive Summary
                        ==============================
                        Total Files Reviewed: %d
                        Total Issues Found: %d
                        Average Issues per File: %d
                        
                        Severity Distribution:
                        - High: %d files
                        - Medium: %d files
                        - Low: %d files
                        
                        Generated: %s
                        """,
                totalFiles, totalIssues, avgIssuesPerFile,
                summary.getHighSeverityCount(),
                summary.getMediumSeverityCount(),
                summary.getLowSeverityCount(),
                summary.getGeneratedAt()
        );
    }
}

