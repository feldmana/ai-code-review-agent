package com.agentic.codereview.rag;

import java.util.List;

/**
 * Abstraction for Retrieval-Augmented Generation (RAG) layer.
 * <p>
 * This allows switching implementations:
 * - file-based rule matching (v1)
 * - keyword-based retrieval (v1.1)
 * - embedding/vector DB retrieval (v2)
 * - hybrid retrieval (v3)
 */
public interface RagService {

    /**
     * Returns relevant context/rules for a given code snippet.
     *
     * @param code source code to analyze
     * @return list of relevant rule documents or context chunks
     */
    List<String> getRelevantRules(String code);

}