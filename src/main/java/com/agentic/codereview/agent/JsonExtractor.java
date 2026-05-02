package com.agentic.codereview.util;

public class JsonExtractor {

    public static String extractJson(String input) {
        if (input == null) return null;

        // remove markdown fences
        input = input.replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        // find first { and last }
        int start = input.indexOf("{");
        int end = input.lastIndexOf("}");

        if (start == -1 || end == -1 || end <= start) {
            throw new IllegalArgumentException("No valid JSON found in LLM response");
        }

        return input.substring(start, end + 1);
    }
}