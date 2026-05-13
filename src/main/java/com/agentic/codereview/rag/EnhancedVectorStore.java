package com.agentic.codereview.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced Vector Store with BM25-like ranking and metadata
 */
public class EnhancedVectorStore {
    private static final Logger logger = LoggerFactory.getLogger(EnhancedVectorStore.class);

    private final Map<String, EmbeddingVector> vectors = new HashMap<>();
    private final Map<String, Integer> documentFrequency = new HashMap<>();  // IDF computation
    private int totalDocuments = 0;

    /**
     * Add a vector with metadata
     */
    public void add(String id, String content, String source, String category) {
        EmbeddingVector vector = new EmbeddingVector(id, content, source, category);
        vectors.put(id, vector);
        totalDocuments++;

        // Update document frequency for IDF calculation
        for (String term : vector.getTerms().keySet()) {
            documentFrequency.put(term, documentFrequency.getOrDefault(term, 0) + 1);
        }

        logger.debug("Added vector: {} (category: {})", id, category);
    }

    /**
     * Find similar vectors with BM25-like scoring
     */
    public List<EmbeddingVector> findSimilar(String query, int topK) {
        if (vectors.isEmpty()) {
            logger.warn("Vector store is empty");
            return Collections.emptyList();
        }

        EmbeddingVector queryVector = new EmbeddingVector("query", query, "query", "QUERY");

        // Score all vectors
        List<EmbeddingVector> scored = new ArrayList<>();
        for (EmbeddingVector doc : vectors.values()) {
            float bm25Score = computeBM25(queryVector, doc);
            doc.setRelevanceScore(bm25Score);
            scored.add(doc);
        }

        // Sort by score and return top-k
        return scored.stream()
                .filter(v -> v.getRelevanceScore() > 0)
                .sorted((a, b) -> Float.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * BM25 ranking algorithm (simplified)
     * - Better than simple Jaccard similarity
     * - Factors in term frequency and document frequency
     */
    private float computeBM25(EmbeddingVector query, EmbeddingVector doc) {
        final float k1 = 1.5f;    // Term frequency saturation
        final float b = 0.75f;    // Length normalization
        final float avgDocLength = (float) vectors.values().stream()
                .mapToInt(EmbeddingVector::getLength)
                .average()
                .orElse(100);

        float score = 0.0f;
        Set<String> queryTerms = query.getTerms().keySet();

        for (String term : queryTerms) {
            if (doc.getTerms().containsKey(term)) {
                float tf = doc.getTerms().get(term);
                int df = documentFrequency.getOrDefault(term, 1);
                float idf = (float) Math.log((totalDocuments - df + 0.5f) / (df + 0.5f) + 1.0f);

                float normLength = doc.getLength() / avgDocLength;
                float bm25Term = idf * ((k1 + 1) * tf) / (k1 * (1 - b + b * normLength) + tf);

                score += bm25Term;
            }
        }

        return Math.max(0, score);
    }

    /**
     * Find by category filter
     */
    public List<EmbeddingVector> findByCategory(String category, int limit) {
        return vectors.values().stream()
                .filter(v -> v.getCategory().equalsIgnoreCase(category))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Find by source (document name)
     */
    public List<EmbeddingVector> findBySource(String source) {
        return vectors.values().stream()
                .filter(v -> v.getSource().equalsIgnoreCase(source))
                .collect(Collectors.toList());
    }

    /**
     * Get vector by ID
     */
    public EmbeddingVector get(String id) {
        return vectors.get(id);
    }

    /**
     * Get all vectors
     */
    public Collection<EmbeddingVector> getAll() {
        return vectors.values();
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStats() {
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (EmbeddingVector v : vectors.values()) {
            categoryCounts.merge(v.getCategory(), 1, Integer::sum);
        }

        return Map.of(
                "totalVectors", vectors.size(),
                "totalDocuments", totalDocuments,
                "uniqueTerms", documentFrequency.size(),
                "categoryCounts", categoryCounts
        );
    }

    public void clear() {
        vectors.clear();
        documentFrequency.clear();
        totalDocuments = 0;
        logger.info("Vector store cleared");
    }

    public int size() {
        return vectors.size();
    }

    /**
     * Debug output
     */
    public void debugDump() {
        logger.info("\n========== ENHANCED VECTOR STORE DUMP ==========");
        for (EmbeddingVector v : vectors.values()) {
            logger.info("ID: {}, Source: {}, Category: {}, Terms: {}",
                    v.getId(), v.getSource(), v.getCategory(), v.getTerms().size());
        }
        logger.info("TOTAL VECTORS: {}", vectors.size());
        logger.info("STATS: {}", getStats());
        logger.info("===============================================\n");
    }
}

