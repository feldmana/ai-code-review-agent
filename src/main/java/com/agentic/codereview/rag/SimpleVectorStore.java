package com.agentic.codereview.rag;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple in-memory vector store for RAG
 * Uses keyword-based similarity as fallback when embeddings aren't available
 */
public class SimpleVectorStore {

    private Map<String, Vector> vectors = new HashMap<>();

    public static class Vector {
        public String id;
        public String content;
        public Map<String, Float> terms;  // TF-IDF like term weights

        public Vector(String id, String content) {
            this.id = id;
            this.content = content;
            this.terms = new HashMap<>();
            computeTermWeights(content);
        }

        private void computeTermWeights(String content) {
            String[] words = content.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")
                    .split("\\s+");

            for (String word : words) {
                if (word.length() > 2) {  // Skip short words
                    terms.put(word, terms.getOrDefault(word, 0f) + 1f);
                }
            }

            // Normalize
            float max = terms.values().stream().max(Float::compare).orElse(1f);
            terms.replaceAll((k, v) -> v / max);
        }
    }

    /**
     * Add a vector (document/chunk) to the store
     */
    public void add(String id, String content) {
        vectors.put(id, new Vector(id, content));
    }

    /**
     * Find similar vectors using term overlap
     */
    public List<String> findSimilar(String query, int topK) {
        Vector queryVector = new Vector("query", query);

        Map<String, Float> similarities = new HashMap<>();

        for (Vector doc : vectors.values()) {
            float similarity = computeSimilarity(queryVector, doc);
            similarities.put(doc.id, similarity);
        }

        return similarities.entrySet()
                .stream()
                .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Compute similarity between two vectors (Jaccard + term overlap)
     */
    private float computeSimilarity(Vector q, Vector doc) {
        Set<String> queryTerms = q.terms.keySet();
        Set<String> docTerms = doc.terms.keySet();

        // Jaccard similarity
        Set<String> intersection = new HashSet<>(queryTerms);
        intersection.retainAll(docTerms);

        Set<String> union = new HashSet<>(queryTerms);
        union.addAll(docTerms);

        if (union.isEmpty()) return 0f;

        float jaccardSim = (float) intersection.size() / union.size();

        // Term overlap score
        float termOverlap = 0f;
        for (String term : intersection) {
            termOverlap += q.terms.get(term) * doc.terms.get(term);
        }

        // Combined score
        return (jaccardSim * 0.3f) + (termOverlap * 0.7f);
    }

    /**
     * Get vector by ID
     */
    public String get(String id) {
        Vector v = vectors.get(id);
        return v != null ? v.content : null;
    }

    /**
     * Clear all vectors
     */
    public void clear() {
        vectors.clear();
    }

    /**
     * Get store size
     */
    public int size() {
        return vectors.size();
    }
}

