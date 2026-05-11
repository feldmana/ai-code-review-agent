package com.agentic.codereview.rag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class KeyRagService implements RagService {

    private static final String RULES_PATH = "rag-docs/rules";

    /**
     * Simple RAG v1:
     * - loads markdown files
     * - selects relevant ones using keywords
     */
    public List<String> getRelevantRules(String code) {

        List<String> rules = new ArrayList<>();

        try {
            // Load all rule files
            List<Path> files = Files.list(Path.of(RULES_PATH))
                    .filter(p -> p.toString().endsWith(".md"))
                    .toList();

            for (Path file : files) {
                String content = Files.readString(file);

                if (isRelevant(file.getFileName().toString(), code)) {
                    rules.add(content);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load RAG rules", e);
        }

        return rules;
    }

    /**
     * VERY SIMPLE relevance logic (we'll upgrade later to embeddings)
     */
    private boolean isRelevant(String fileName, String code) {

        fileName = fileName.toLowerCase();

        if (fileName.contains("naming")) {
            return code.contains("class") || code.contains("void");
        }

        if (fileName.contains("architecture")) {
            return code.contains("Controller") ||
                    code.contains("Service") ||
                    code.contains("Repository");
        }

        if (fileName.contains("quality")) {
            return true; // always relevant
        }

        if (fileName.contains("bad-examples")) {
            return true;
        }

        return false;
    }
}