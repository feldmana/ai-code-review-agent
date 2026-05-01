package com.agentic.codereview.tool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.agentic.codereview.model.ReviewResult;
import com.agentic.codereview.model.Summary;

/**
 * Tool for writing markdown reports
 */
public class ReportWriterTool {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Writes a markdown report from the summary
     */
    public String generateReport(Summary summary, String projectPath) throws IOException {
        StringBuilder report = new StringBuilder();

        // Header
        report.append("# Code Review Report\n\n");
        report.append("**Generated:** ").append(summary.getGeneratedAt()).append("\n");
        report.append("**Project Path:** ").append(projectPath).append("\n\n");

        // Summary Statistics
        report.append("## Summary Statistics\n\n");
        report.append("- **Files Reviewed:** ").append(summary.getReviews().size()).append("\n");
        report.append("- **Total Issues:** ").append(summary.getTotalIssues()).append("\n");
        report.append("- **High Severity:** ").append(summary.getHighSeverityCount()).append("\n");
        report.append("- **Medium Severity:** ").append(summary.getMediumSeverityCount()).append("\n");
        report.append("- **Low Severity:** ").append(summary.getLowSeverityCount()).append("\n\n");

        // Detailed Reviews
        report.append("## Detailed Reviews\n\n");

        for (ReviewResult review : summary.getReviews()) {
            report.append("### File: `").append(review.getFileName()).append("`\n\n");
            report.append("**Severity:** ").append(review.getSeverity()).append("\n\n");

            if (!review.getIssues().isEmpty()) {
                report.append("#### Issues\n");
                for (String issue : review.getIssues()) {
                    report.append("- ").append(issue).append("\n");
                }
                report.append("\n");
            }

            if (!review.getSuggestions().isEmpty()) {
                report.append("#### Suggestions\n");
                for (String suggestion : review.getSuggestions()) {
                    report.append("- ").append(suggestion).append("\n");
                }
                report.append("\n");
            }

            report.append("---\n\n");
        }

        return report.toString();
    }

    /**
     * Writes the report to a file
     */
    public String writeReportToFile(Summary summary, String projectPath, String outputDir) throws IOException {
        String reportContent = generateReport(summary, projectPath);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = outputDir + "/code_review_report_" + timestamp + ".md";

        Files.writeString(Paths.get(fileName), reportContent, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return fileName;
    }

    /**
     * Appends to an existing report file
     */
    public void appendToReport(String filePath, String content) throws IOException {
        Files.writeString(Paths.get(filePath), content, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
}

