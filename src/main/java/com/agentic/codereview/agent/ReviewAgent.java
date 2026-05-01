package com.agentic.codereview.agent;

import com.agentic.codereview.llm.OllamaClient;
import com.agentic.codereview.model.ReviewResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ReviewAgent sends code to Ollama LLM for review
 */
public class ReviewAgent {
    private static final Logger logger = LoggerFactory.getLogger(ReviewAgent.class);
    private final OllamaClient ollamaClient;
    private int maxRetries = 3;

    public ReviewAgent(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    public ReviewAgent(OllamaClient ollamaClient, int maxRetries) {
        this.ollamaClient = ollamaClient;
        this.maxRetries = maxRetries;
    }

    /**
     * Reviews a single file content
     */
    public ReviewResult reviewFile(String fileName, String fileContent) throws IOException {
        logger.info("Reviewing file: {}", fileName);

        String prompt = buildReviewPrompt(fileName, fileContent);
        String response = ollamaClient.generateResponse(prompt);

        return parseReviewResponse(fileName, response);
    }

    /**
     * Builds a detailed prompt for code review
     */
    private String buildReviewPrompt1(String fileName, String fileContent) {
        return """
                You are an expert code reviewer. Analyze the following code file and provide:
                1. A list of issues (bugs, performance problems, security concerns)
                2. A list of suggestions for improvement
                3. An overall severity rating (LOW, MEDIUM, or HIGH)
                
                File: """ + fileName + """
                
                Code:
                ```
                """ + fileContent + """
                ```
                
                Respond in JSON format with keys: "issues" (array), "suggestions" (array), "severity" (string)
                """;
    }

    private String buildReviewPrompt(String fileName, String fileContent) {

        return """
            You are an expert Java code reviewer.

            IMPORTANT RULES:
            - Only review Java production code.
            - If the file is a TEST file (name contains "Test", "Tests", or is under test/), respond ONLY with:
              {"skip": true, "reason": "test file"}
            - If the file is NOT Java code, respond ONLY with:
              {"skip": true, "reason": "non-java file"}
            - Do NOT analyze test classes or test logic.
            - Do NOT include explanations outside JSON.

            TASK (ONLY if valid production Java code):
            1. List issues (bugs, performance problems, security concerns)
            2. List suggestions for improvement
            3. Give severity: LOW, MEDIUM, or HIGH

            File Name:
            """ + fileName + """

            Code:
            ```
            """ + fileContent + """
            ```

            OUTPUT RULES (STRICT):
            - Return ONLY valid JSON
            - No markdown
            - No code fences
            - No extra text

            JSON format:
            {
              "issues": [],
              "suggestions": [],
              "severity": "LOW|MEDIUM|HIGH"
            }
            """;
    }

    /**
     * Parses the LLM response into a ReviewResult
     */
    private ReviewResult parseReviewResponse(String fileName, String response) {
        logger.debug("Parsing review response for: {}", fileName);

        List<String> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        ReviewResult.Severity severity = ReviewResult.Severity.LOW;

        try {
            // Try to extract JSON from response
            int jsonStart = response.indexOf('{');
            int jsonEnd = response.lastIndexOf('}');

            if (jsonStart != -1 && jsonEnd != -1) {
                String jsonString = response.substring(jsonStart, jsonEnd + 1);
                JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

                // Parse issues
                if (json.has("issues") && json.get("issues").isJsonArray()) {
                    json.getAsJsonArray("issues").forEach(item ->
                            issues.add(item.getAsString())
                    );
                }

                // Parse suggestions
                if (json.has("suggestions") && json.get("suggestions").isJsonArray()) {
                    json.getAsJsonArray("suggestions").forEach(item ->
                            suggestions.add(item.getAsString())
                    );
                }

                // Parse severity
                if (json.has("severity")) {
                    String severityStr = json.get("severity").getAsString().toUpperCase();
                    try {
                        severity = ReviewResult.Severity.valueOf(severityStr);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid severity: {}, defaulting to LOW", severityStr);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse JSON response for {}: {}", fileName, e.getMessage());
            // Fallback: treat entire response as a single issue
            issues.add("Review response could not be parsed: " + response.substring(0, Math.min(100, response.length())));
        }

        ReviewResult result = new ReviewResult(fileName, issues, suggestions, severity);
        logger.info("Review result for {}: {} issues, {} suggestions, severity={}", 
                fileName, issues.size(), suggestions.size(), severity);

        return result;
    }

    /**
     * Reviews a file with retry logic for handling LLM failures
     */
    public ReviewResult reviewFileWithRetry(String fileName, String fileContent) throws IOException {
        int attempts = 0;

        while (attempts < maxRetries) {
            try {
                attempts++;
                logger.info("Attempting review of {} (attempt {}/{})", fileName, attempts, maxRetries);
                return reviewFile(fileName, fileContent);
            } catch (IOException e) {
                logger.warn("Failed to review {} (attempt {}): {}", fileName, attempts, e.getMessage());
                if (attempts >= maxRetries) {
                    throw new IOException("Failed to review " + fileName + " after " + maxRetries + " attempts", e);
                }
                // Wait before retry
                try {
                    Thread.sleep(1000 * attempts); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry", ie);
                }
            }
        }

        throw new IOException("Could not review file " + fileName);
    }
}

