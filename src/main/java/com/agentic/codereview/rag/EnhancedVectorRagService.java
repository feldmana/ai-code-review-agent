package com.agentic.codereview.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enhanced Vector RAG Service v2.0
 * - Uses BM25 ranking instead of keyword matching
 * - Supports category-based filtering
 * - Provides relevance scoring
 * - Better chunk management with metadata
 */
public class EnhancedVectorRagService implements RagService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedVectorRagService.class);

    // Enable trace logging
    private static final boolean RAG_TRACE = true;

    private final String docsPath;
    private final int topK;
    private final EnhancedVectorStore vectorStore;
    private final Map<String, DocumentMetadata> documents;
    private boolean initialized = false;

    /**
     * Document metadata
     */
    public static class DocumentMetadata {
        public String id;
        public String fileName;
        public String title;
        public String category;
        public int chunkCount;
        public long fileSize;

        public DocumentMetadata(String id, String fileName, String title, String category, int chunkCount, long fileSize) {
            this.id = id;
            this.fileName = fileName;
            this.title = title;
            this.category = category;
            this.chunkCount = chunkCount;
            this.fileSize = fileSize;
        }
    }

    public EnhancedVectorRagService(String docsPath, int topK) {
        this.docsPath = docsPath;
        this.topK = topK;
        this.vectorStore = new EnhancedVectorStore();
        this.documents = new LinkedHashMap<>();
    }

    public EnhancedVectorRagService(String docsPath) {
        this(docsPath, 5);
    }

    /**
     * Initialize RAG service
     */
    public void initialize() {
        try {
            logger.info("Initializing Enhanced Vector RAG Service from: {}", docsPath);

            loadDocuments();

            this.initialized = true;

            logger.info("✓ Enhanced Vector RAG Service initialized");
            logger.info("  - Documents: {}", documents.size());
            logger.info("  - Total vectors: {}", vectorStore.size());
            logger.info("  - Stats: {}", vectorStore.getStats());

        } catch (Exception e) {
            logger.error("Failed to initialize Enhanced Vector RAG Service", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Load documents from disk with category detection
     */
    private void loadDocuments() throws IOException {
        Path docsDir = Paths.get(docsPath);

        if (!Files.exists(docsDir)) {
            logger.warn("Docs directory not found: {}", docsPath);
            Files.createDirectories(docsDir);
            return;
        }

        logger.info("Loading documents from: {}", docsDir);

        try (Stream<Path> paths = Files.walk(docsDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(this::isDocumentFile)
                    .forEach(this::loadDocument);
        }

        logger.info("✓ Loaded {} documents", documents.size());
    }

    private boolean isDocumentFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".md") || fileName.endsWith(".txt");
    }

    /**
     * Load single document with category detection
     */
    private void loadDocument(Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            String id = path.getFileName().toString();
            String title = extractTitle(id);
            String category = detectCategory(id, content);
            long fileSize = Files.size(path);

            List<String> chunks = splitIntoChunks(content);

            int chunkCount = 0;
            for (int i = 0; i < chunks.size(); i++) {
                String chunkId = id + "_chunk_" + i;
                vectorStore.add(chunkId, chunks.get(i), id, category);
                chunkCount++;
            }

            documents.put(id, new DocumentMetadata(id, id, title, category, chunkCount, fileSize));

            logger.debug("Loaded document: {} ({} chunks, category: {})", id, chunkCount, category);

        } catch (IOException e) {
            logger.error("Failed to load document: {}", path, e);
        }
    }

    /**
     * Detect category from filename and content
     */
    private String detectCategory(String fileName, String content) {
        String lower = fileName.toLowerCase();

        if (lower.contains("architecture")) return "ARCHITECTURE";
        if (lower.contains("naming")) return "NAMING";
        if (lower.contains("performance") || lower.contains("optimization")) return "PERFORMANCE";
        if (lower.contains("security")) return "SECURITY";
        if (lower.contains("testing") || lower.contains("test")) return "TESTING";
        if (lower.contains("service") || content.contains("@Service")) return "SERVICE_DESIGN";
        if (lower.contains("controller") || content.contains("@Controller")) return "CONTROLLER_DESIGN";
        if (lower.contains("bad") || lower.contains("anti")) return "ANTI_PATTERNS";
        if (lower.contains("example")) return "EXAMPLES";

        return "GENERAL";
    }

    private String extractTitle(String fileName) {
        return fileName.replace(".md", "")
                .replace(".txt", "")
                .replace("-", " ")
                .replace("_", " ");
    }

    /**
     * Split into intelligent chunks
     */
    private List<String> splitIntoChunks(String content) {
        List<String> chunks = new ArrayList<>();

        // Try to split by headers first (Markdown sections)
        String[] sections = content.split("(?=^#{1,6}\\s)", 0);

        for (String section : sections) {
            if (!section.trim().isEmpty()) {
                if (section.length() > 1000) {
                    // Further split large sections by paragraphs
                    String[] paragraphs = section.split("\n\n+");
                    StringBuilder chunk = new StringBuilder();

                    for (String para : paragraphs) {
                        if ((chunk.length() + para.length()) > 1000) {
                            if (chunk.length() > 0) {
                                chunks.add(chunk.toString().trim());
                                chunk = new StringBuilder();
                            }
                        }
                        chunk.append(para).append("\n\n");
                    }

                    if (chunk.length() > 0) {
                        chunks.add(chunk.toString().trim());
                    }
                } else {
                    chunks.add(section.trim());
                }
            }
        }

        return chunks.isEmpty() ? List.of(content) : chunks;
    }

    /**
     * 🔥 MAIN RAG RETRIEVAL METHOD (with scoring)
     */
    @Override
    public List<String> getRelevantRules(String code) {
        if (!initialized) {
            logger.warn("RAG not initialized");
            return Collections.emptyList();
        }

        try {
            if (RAG_TRACE) {
                logger.info("\n========== ENHANCED RAG QUERY ==========");
                logger.info("INPUT SIZE: {} chars", code.length());
                logger.info("TOP_K: {}", topK);
            }

            // Retrieve similar chunks using BM25
            List<EmbeddingVector> results = vectorStore.findSimilar(code, topK);

            if (RAG_TRACE) {
                logger.info("MATCHED CHUNKS: {}", results.size());
                for (EmbeddingVector v : results) {
                    logger.info("  - {} (score: {:.4f}, source: {}, category: {})",
                            v.getId(), v.getRelevanceScore(), v.getSource(), v.getCategory());
                }
            }

            // Extract content with ranking information
            List<String> rankedResults = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                EmbeddingVector v = results.get(i);
                String header = String.format("\n[RANK %d - %s - Score: %.4f]\n%s\n",
                        (i + 1), v.getCategory(), v.getRelevanceScore(), v.getSource());
                rankedResults.add(header + v.getContent());

                if (RAG_TRACE) {
                    logger.debug("Chunk {}: {}", i + 1, truncate(v.getContent()));
                }
            }

            if (RAG_TRACE) {
                logger.info("========================================\n");
            }

            return rankedResults;

        } catch (Exception e) {
            logger.error("RAG retrieval error", e);
            return Collections.emptyList();
        }
    }

    /**
     * Find rules by category (optional convenience method)
     */
    public List<String> getRulesByCategory(String category, int limit) {
        return vectorStore.findByCategory(category, limit)
                .stream()
                .map(EmbeddingVector::getContent)
                .collect(Collectors.toList());
    }

    /**
     * Get statistics
     */
    public Map<String, Object> getStats() {
        return vectorStore.getStats();
    }

    /**
     * List documents
     */
    public List<Map<String, Object>> listDocuments() {
        return documents.values().stream()
                .map(doc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", doc.id);
                    map.put("fileName", doc.fileName);
                    map.put("title", doc.title);
                    map.put("category", doc.category);
                    map.put("chunks", doc.chunkCount);
                    map.put("fileSize", doc.fileSize + " bytes");
                    return map;
                })
                .collect(Collectors.toList());
    }

    public void clear() {
        vectorStore.clear();
        documents.clear();
        initialized = false;
        logger.info("Enhanced RAG cleared");
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Debug dump
     */
    public void debugVectorStore() {
        vectorStore.debugDump();
    }

    private String truncate(String text) {
        if (text == null) return "null";
        return text.length() > 300 ? text.substring(0, 300) + "..." : text;
    }
}

