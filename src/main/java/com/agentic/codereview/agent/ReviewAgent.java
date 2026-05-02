package com.agentic.codereview.agent;

import com.agentic.codereview.llm.OllamaClient;
import com.agentic.codereview.model.Issue;
import com.agentic.codereview.model.ReviewResult;
import com.agentic.codereview.util.JsonExtractor;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ReviewAgent {

    private static final Logger logger = LoggerFactory.getLogger(ReviewAgent.class);

    private final OllamaClient ollamaClient;
    private final int maxRetries;
    private final Gson gson = new Gson();

    public ReviewAgent(OllamaClient ollamaClient, int maxRetries) {
        this.ollamaClient = ollamaClient;
        this.maxRetries = maxRetries;
    }

    public ReviewResult reviewFileWithRetry(String fileName, String content) {

        int attempt = 0;

        while (attempt < maxRetries) {
            attempt++;

            try {
                logger.info("Attempting review of {} (attempt {}/{})",
                        fileName, attempt, maxRetries);

                String prompt = buildReviewPrompt(fileName, content);
                String llmResponse = ollamaClient.generateResponse(prompt);

                logger.info("=== RAW LLM RESPONSE [{}] ===\n{}", fileName, llmResponse);

                String cleaned = JsonExtractor.extractJson(llmResponse);

                JsonObject obj;

                try {
                    obj = gson.fromJson(cleaned, JsonObject.class);
                } catch (Exception e) {
                    logger.error("❌ Invalid JSON:\n{}", cleaned);
                    return fallbackResult(fileName);
                }

                return parseReviewResult(obj, fileName);

            } catch (Exception e) {

                logger.warn("Failed attempt {}/{} for {}: {}",
                        attempt, maxRetries, fileName, e.getMessage());

                if (attempt == maxRetries) {
                    return fallbackResult(fileName);
                }
            }
        }

        return fallbackResult(fileName);
    }

    private String buildReviewPrompt(String fileName, String content) {
        return """
            You are a senior Java software architect and strict code reviewer.

            RULES:
            - RETURN ONLY VALID JSON
            - NEVER TRUNCATE OUTPUT
            - MAX 2 ISSUES ONLY

            FORMAT:
            {
              "issues": [
                {
                  "type": "string",
                  "severity": "LOW|MEDIUM|HIGH",
                  "message": "string",
                  "suggestion": "string"
                }
              ],
              "suggestions": ["string"],
              "severity": "LOW|MEDIUM|HIGH"
            }

            FILE: %s

            CODE:
            ```
            %s
            ```
            """.formatted(fileName, content);
    }

    private ReviewResult parseReviewResult(JsonObject obj, String fileName) {

        List<Issue> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        ReviewResult.Severity severity = ReviewResult.Severity.LOW;

        // ---------------- issues ----------------
        if (obj != null && obj.has("issues") && obj.get("issues").isJsonArray()) {

            for (JsonElement el : obj.getAsJsonArray("issues")) {

                if (!el.isJsonObject()) continue;

                JsonObject issueObj = el.getAsJsonObject();

                Issue issue = new Issue();

                String type = fallback(issueObj, "type", "code");
                String message = fallback(issueObj, "message", "description");

                issue.setType(type != null ? type : "UNKNOWN");
                issue.setMessage(message != null ? message : "No message");
                issue.setSeverity(normalizeSeverity(getAsString(issueObj, "severity")));
                issue.setSuggestion(getAsString(issueObj, "suggestion"));

                issues.add(issue);
            }
        }

        // ---------------- suggestions ----------------
        if (obj != null && obj.has("suggestions") && obj.get("suggestions").isJsonArray()) {

            for (JsonElement el : obj.getAsJsonArray("suggestions")) {
                if (el.isJsonPrimitive()) {
                    suggestions.add(el.getAsString());
                }
            }
        }

        // ---------------- severity ----------------
        if (obj != null && obj.has("severity")) {
            severity = normalizeSeverity(obj.get("severity").getAsString());
        }

        return new ReviewResult(fileName, issues, suggestions, severity);
    }

    private ReviewResult fallbackResult(String fileName) {
        return new ReviewResult(
                fileName,
                new ArrayList<>(),
                new ArrayList<>(),
                ReviewResult.Severity.LOW
        );
    }

    private ReviewResult.Severity normalizeSeverity(String value) {
        if (value == null || value.isBlank()) {
            return ReviewResult.Severity.LOW;
        }

        String cleaned = value.trim().toUpperCase();

        // handle "HIGH|MEDIUM"
        if (cleaned.contains("|")) {
            cleaned = cleaned.split("\\|")[0].trim();
        }

        // handle "SEVERITY: HIGH"
        if (cleaned.contains(":")) {
            cleaned = cleaned.split(":")[1].trim();
        }

        // remove weird characters
        cleaned = cleaned.replaceAll("[^A-Z]", "");

        try {
            return ReviewResult.Severity.valueOf(cleaned);
        } catch (Exception e) {
            return ReviewResult.Severity.LOW;
        }
    }

    private String getAsString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return null;
        return obj.get(key).getAsString();
    }

    private String fallback(JsonObject obj, String primary, String fallback) {
        String v = getAsString(obj, primary);
        if (v != null) return v;
        return getAsString(obj, fallback);
    }
}