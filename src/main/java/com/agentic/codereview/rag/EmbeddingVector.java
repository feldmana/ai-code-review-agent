package com.agentic.codereview.rag;

import java.util.*;

/**
 * Enhanced Vector representation with metadata and similarity scoring
 */
public class EmbeddingVector {
    private final String id;
    private final String content;
    private final String source;           // e.g., "architecture.md", "naming.md"
    private final String category;         // e.g., "ARCHITECTURE", "NAMING", "PERFORMANCE"
    private final Map<String, Float> terms;  // Term-frequency map
    private final int length;
    private float relevanceScore;          // Set after similarity computation

    public EmbeddingVector(String id, String content, String source, String category) {
        this.id = id;
        this.content = content;
        this.source = source;
        this.category = category;
        this.terms = new HashMap<>();
        this.length = computeTermWeights(content);
        this.relevanceScore = 0.0f;
    }

    /**
     * Compute TF-IDF-like term weights
     */
    private int computeTermWeights(String content) {
        String[] words = content.toLowerCase()
                .replaceAll("[^a-z0-9\\s_]", " ")
                .split("\\s+");

        for (String word : words) {
            if (word.length() > 2 && !isStopWord(word)) {
                terms.put(word, terms.getOrDefault(word, 0f) + 1f);
            }
        }

        // Normalize weights
        if (!terms.isEmpty()) {
            float max = terms.values().stream().max(Float::compare).orElse(1f);
            terms.replaceAll((k, v) -> v / max);
        }

        return words.length;
    }

    /**
     * Filter common stop words
     */
    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of(
                "the", "a", "an", "and", "or", "if", "then", "else", "for", "in", "of",
                "to", "is", "are", "was", "be", "being", "have", "has", "do", "does",
                "not", "no", "yes", "that", "this", "from", "with", "by", "as", "on", "at"
        );
        return stopWords.contains(word);
    }

    // Getters
    public String getId() { return id; }
    public String getContent() { return content; }
    public String getSource() { return source; }
    public String getCategory() { return category; }
    public Map<String, Float> getTerms() { return terms; }
    public int getLength() { return length; }
    public float getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(float score) { this.relevanceScore = score; }

    @Override
    public String toString() {
        return "EmbeddingVector{" +
                "id='" + id + '\'' +
                ", source='" + source + '\'' +
                ", category='" + category + '\'' +
                ", score=" + relevanceScore +
                ", length=" + length +
                '}';
    }
}

