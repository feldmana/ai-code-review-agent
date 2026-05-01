package com.agentic.codereview.tool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Tool for scanning a project directory and finding code files
 */
public class FileScannerTool {
    private static final Set<String> CODE_EXTENSIONS = Set.of(
            ".java", ".py", ".js", ".ts", ".go", ".rust", ".cpp", ".c",
            ".cs", ".rb", ".php", ".scala", ".kt", ".swift"
    );

    private static final Set<String> EXCLUDE_DIRS = Set.of(
            ".git", ".gradle", "node_modules", "venv", ".venv", "dist", "build",
            "target", ".idea", "bin", "obj", "__pycache__"
    );

    /**
     * Scans a directory and returns all code files
     */
    public List<String> scanDirectory(String projectPath) throws IOException {
        List<String> codeFiles = new ArrayList<>();
        Path rootPath = Paths.get(projectPath);

        if (!Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Project path is not a directory: " + projectPath);
        }

        try (Stream<Path> paths = Files.walk(rootPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(this::isCodeFile)
                    .filter(this::isNotInExcludedDirectory)
                    .forEach(path -> codeFiles.add(path.toString()));
        }

        return codeFiles;
    }

    private boolean isCodeFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return CODE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    private boolean isNotInExcludedDirectory(Path path) {
        return !path.toString().contains("/.") ||
                EXCLUDE_DIRS.stream().noneMatch(excluded ->
                        path.toString().contains(File.separator + excluded + File.separator) ||
                        path.toString().contains("/" + excluded + "/"));
    }

    /**
     * Gets file count for a project
     */
    public int countCodeFiles(String projectPath) throws IOException {
        return scanDirectory(projectPath).size();
    }

    /**
     * Filters files by extension
     */
    public List<String> getFilesByExtension(String projectPath, String extension) throws IOException {
        List<String> files = scanDirectory(projectPath);
        return files.stream()
                .filter(f -> f.endsWith("." + extension.toLowerCase()))
                .toList();
    }
}

