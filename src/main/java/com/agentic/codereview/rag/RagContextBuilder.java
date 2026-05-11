package com.agentic.codereview.rag;

import java.util.List;
import java.util.StringJoiner;

/**
 * RagContextBuilder - Formats RAG results into well-structured prompt context
 * This ensures RAG results are properly integrated into LLM prompts
 */
public class RagContextBuilder {

    /**
     * Build structured context for review prompt
     */
    public static String buildReviewContext(List<String> ragResults, String codeSnippet) {
        if (ragResults == null || ragResults.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();

        context.append("=== RELEVANT CODING RULES AND GUIDELINES ===\n");
        context.append("(Retrieved from knowledge base based on code similarity)\n\n");

        for (int i = 0; i < ragResults.size(); i++) {
            context.append("--- GUIDELINE ").append(i + 1).append(" ---\n");
            context.append(ragResults.get(i)).append("\n\n");
        }

        context.append("=== END OF GUIDELINES ===\n\n");
        context.append("Now review the following code against these guidelines:\n\n");

        return context.toString();
    }

    /**
     * Build category-based context
     */
    public static String buildCategoryContext(String category, List<String> rules) {
        StringJoiner context = new StringJoiner("\n");

        context.add("=== CATEGORY: " + category + " ===");
        context.add("");

        for (int i = 0; i < rules.size(); i++) {
            context.add("Rule " + (i + 1) + ":");
            context.add(rules.get(i));
            context.add("");
        }

        return context.toString();
    }

    /**
     * Detect code type from snippet
     */
    public static String detectCodeType(String code) {
        String lower = code.toLowerCase();

        if (lower.contains("@controller") || lower.contains("@restcontroller")) {
            return "CONTROLLER";
        }
        if (lower.contains("@service")) {
            return "SERVICE";
        }
        if (lower.contains("@repository") || lower.contains("@dao")) {
            return "REPOSITORY";
        }
        if (lower.contains("@entity") || lower.contains("@table")) {
            return "ENTITY";
        }
        if (lower.contains("public interface")) {
            return "INTERFACE";
        }
        if (lower.contains("@configuration") || lower.contains("@bean")) {
            return "CONFIGURATION";
        }
        if (lower.contains("test") && lower.contains("void")) {
            return "TEST";
        }

        return "GENERAL";
    }

    /**
     * Create recommendation prompt based on detected issues
     */
    public static String buildRecommendationContext(String codeType) {
        return switch (codeType) {
            case "CONTROLLER" -> """
                    This is a Controller class. Pay special attention to:
                    - HTTP method mapping correctness
                    - Request/Response handling
                    - Error handling and status codes
                    - Validation of incoming data
                    - Proper exception mapping
                    """;

            case "SERVICE" -> """
                    This is a Service class. Pay special attention to:
                    - Business logic correctness
                    - Transaction handling
                    - Dependency injection
                    - Separation of concerns
                    - Error handling and logging
                    """;

            case "REPOSITORY" -> """
                    This is a Repository/DAO class. Pay special attention to:
                    - Database query correctness
                    - Connection management
                    - SQL injection prevention
                    - Performance optimization
                    - Transaction boundaries
                    """;

            case "ENTITY" -> """
                    This is an Entity/Model class. Pay special attention to:
                    - Proper annotations and mapping
                    - Field access modifiers
                    - Serialization/Deserialization
                    - Proper relationships definition
                    - Validation constraints
                    """;

            case "CONFIGURATION" -> """
                    This is a Configuration class. Pay special attention to:
                    - Bean definitions correctness
                    - Dependency resolution
                    - Configuration priority
                    - Property injection
                    - Conditional configurations
                    """;

            case "TEST" -> """
                    This is a Test class. Pay special attention to:
                    - Test coverage and completeness
                    - Proper setup and teardown
                    - Assertions and verifications
                    - Test isolation
                    - Use of mocks and stubs
                    """;

            default -> """
                    General Java code review.
                    """;
        };
    }
}

