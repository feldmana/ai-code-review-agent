package com.agentic.codereview.tool;

import com.agentic.codereview.model.Issue;
import com.agentic.codereview.model.ReviewResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Tool for writing markdown reports
 */
public class ReportWriterTool {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Writes a markdown report from the summary
     */
    public String generateReport(List<ReviewResult> reviews) {

        StringBuilder report = new StringBuilder();

        report.append("# Code Review Report\n\n");
        report.append("Generated at: ").append(new java.util.Date()).append("\n\n");

        int totalIssues = 0;
        int high = 0, medium = 0, low = 0;

        for (ReviewResult review : reviews) {

            report.append("## File: ")
                    .append(review.getFileName())
                    .append("\n\n");

            // -------------------------
            // Issues
            // -------------------------
            List<Issue> issues = review.getIssues();

            if (issues != null && !issues.isEmpty()) {
                report.append("### Issues\n");

                for (Issue issue : issues) {
                    report.append("- [")
                            .append(issue.getSeverity())
                            .append("] ")
                            .append(issue.getType())
                            .append(": ")
                            .append(issue.getMessage());

                    if (issue.getSuggestion() != null && !issue.getSuggestion().isEmpty()) {
                        report.append(" → ")
                                .append(issue.getSuggestion());
                    }

                    report.append("\n");

                    totalIssues++;

                    // severity stats
                    if (issue.getSeverity() != null) {
                        switch (issue.getSeverity()) {
                            case HIGH -> high++;
                            case MEDIUM -> medium++;
                            case LOW -> low++;
                        }
                    }
                }

                report.append("\n");
            } else {
                report.append("No issues found.\n\n");
            }

            // -------------------------
            // Suggestions
            // -------------------------
            List<String> suggestions = review.getSuggestions();

            if (suggestions != null && !suggestions.isEmpty()) {
                report.append("### Suggestions\n");

                for (String suggestion : suggestions) {
                    report.append("- ").append(suggestion).append("\n");
                }

                report.append("\n");
            }

            report.append("---\n\n");
        }

        // -------------------------
        // Summary section
        // -------------------------
        report.append("# Summary\n\n");
        report.append("- Total files: ").append(reviews.size()).append("\n");
        report.append("- Total issues: ").append(totalIssues).append("\n");
        report.append("- High: ").append(high).append("\n");
        report.append("- Medium: ").append(medium).append("\n");
        report.append("- Low: ").append(low).append("\n");

        return report.toString();
    }

    /**
     * Writes the report to a file
     */


    /**
     * Writes markdown report to file
     */
    public String writeReportToFile(String reportContent, String reportDir) throws IOException {

        // Ensure directory exists
        File dir = new File(reportDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Create timestamped filename
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "code_review_report_" + timestamp + ".md";

        File reportFile = new File(dir, fileName);

        // Write file
        try (FileWriter writer = new FileWriter(reportFile)) {
            writer.write(reportContent);
        }

        return reportFile.getAbsolutePath();
    }


    /**
     * Appends to an existing report file
     */
    public void appendToReport(String filePath, String content) throws IOException {
        Files.writeString(Paths.get(filePath), content, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }
}

