package com.agentic.codereview.tool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Tool for reading file content
 */
public class FileReaderTool {

    /**
     * Reads the content of a file
     */
    public String readFile(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
    }

    /**
     * Reads file content with size limit (useful for very large files)
     */
    public String readFileWithLimit(String filePath, int maxChars) throws IOException {
        String content = readFile(filePath);
        if (content.length() > maxChars) {
            return content.substring(0, maxChars) + "\n... [Content truncated]";
        }
        return content;
    }

    /**
     * Gets file size in bytes
     */
    public long getFileSize(String filePath) throws IOException {
        return Files.size(Paths.get(filePath));
    }

    /**
     * Checks if file is too large to process (> 100KB)
     */
    public boolean isFileTooLarge(String filePath) throws IOException {
        return getFileSize(filePath) > 100_000;
    }
}

