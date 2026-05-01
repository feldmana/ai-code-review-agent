package com.agentic.codereview.orchestrator;

import com.agentic.codereview.agent.*;
import com.agentic.codereview.config.AppConfig;
import com.agentic.codereview.llm.OllamaClient;
import com.agentic.codereview.model.Action;
import com.agentic.codereview.model.ReviewResult;
import com.agentic.codereview.model.Summary;
import com.agentic.codereview.model.Task;
import com.agentic.codereview.tool.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * AgentOrchestrator manages the execution of agents in sequence
 */
public class AgentOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(AgentOrchestrator.class);

    // Agents
    private final RouterAgent routerAgent;
    private final PlannerAgent plannerAgent;
    private final ReviewAgent reviewAgent;
    private final SummaryAgent summaryAgent;
    private final EmailAgent emailAgent;

    // Tools
    private final FileScannerTool fileScannerTool;
    private final FileReaderTool fileReaderTool;
    private final ReportWriterTool reportWriterTool;

    // Configuration
    private final AppConfig appConfig;
    private final OllamaClient ollamaClient;

    // State
    private List<ReviewResult> reviews;
    private Summary summary;

    public AgentOrchestrator(AppConfig appConfig, OllamaClient ollamaClient) {
        this.appConfig = appConfig;
        this.ollamaClient = ollamaClient;

        // Initialize agents
        this.routerAgent = new RouterAgent();
        this.plannerAgent = new PlannerAgent();
        this.reviewAgent = new ReviewAgent(ollamaClient, appConfig.getMaxRetries());
        this.summaryAgent = new SummaryAgent();
        this.emailAgent = new EmailAgent(appConfig);

        // Initialize tools
        this.fileScannerTool = new FileScannerTool();
        this.fileReaderTool = new FileReaderTool();
        this.reportWriterTool = new ReportWriterTool();

        // Initialize state
        this.reviews = new ArrayList<>();
    }

    /**
     * Main orchestration method - executes the full workflow
     */
    public void executeTask(String userInput, String projectPath) throws Exception {
        logger.info("=== Orchestrator: Starting task execution ===");
        logger.info("User input: {}", userInput);
        logger.info("Project path: {}", projectPath);

        // Step 1: Route task
        Task.TaskType taskType = routerAgent.routeTask(userInput, projectPath);
        Task task = new Task(UUID.randomUUID().toString(), taskType, projectPath, userInput);

        if (!routerAgent.validateTask(task)) {
            throw new IllegalArgumentException("Task validation failed: " + task);
        }

        logger.info("Task routed to: {}", taskType);

        // Step 2: Create execution plan
        List<Action> plan = plannerAgent.createPlan(userInput);
        if (!plannerAgent.validatePlan(plan)) {
            throw new IllegalArgumentException("Invalid plan generated");
        }

        logger.info("Execution plan created with {} actions", plan.size());
        logger.debug("Plan: {}", plannerAgent.planToJson(plan));

        // Step 3: Execute plan
        executePlan(plan, projectPath);

        logger.info("=== Orchestrator: Task execution completed ===");
    }

    /**
     * Executes the plan step by step
     */
    private void executePlan(List<Action> plan, String projectPath) throws Exception {
        for (Action action : plan) {
            logger.info("Executing action {}: {}", action.getSequenceNumber(), action.getAction());

            switch (action.getAction()) {
                case SCAN_FILES -> scanFiles(projectPath);
                case REVIEW_FILES -> reviewFiles(projectPath);
                case SUMMARIZE -> summarizeReviews();
                case WRITE_REPORT -> writeReport(projectPath);
                case SEND_EMAIL -> sendEmailReport();
                default -> logger.warn("Unknown action: {}", action.getAction());
            }
        }
    }

    /**
     * Scans project directory for code files
     */
    private void scanFiles(String projectPath) throws IOException {
        logger.info("Action: Scanning files in {}", projectPath);
        List<String> files = fileScannerTool.scanDirectory(projectPath);
        logger.info("Found {} code files", files.size());

        for (String file : files.stream().limit(Math.min(5, files.size())).toList()) {
            logger.debug("File: {}", file);
        }
        if (files.size() > 5) {
            logger.debug("... and {} more files", files.size() - 5);
        }
    }

    /**
     * Reviews all code files using ReviewAgent
     */
    private void reviewFiles(String projectPath) throws Exception {
        logger.info("Action: Reviewing files");

        List<String> files = fileScannerTool.scanDirectory(projectPath);
        logger.info("Reviewing {} files", files.size());

        reviews.clear();

        if (appConfig.isParallelProcessingEnabled()) {
            reviewFilesInParallel(files);
        } else {
            reviewFilesSequentially(files);
        }

        logger.info("Completed review of {} files", reviews.size());
    }

    /**
     * Reviews files in parallel using thread pool
     */
    private void reviewFilesInParallel(List<String> files) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(appConfig.getThreadPoolSize());
        List<Future<ReviewResult>> futures = new ArrayList<>();

        for (String filePath : files) {
            futures.add(executor.submit(() -> {
                try {
                    return reviewSingleFile(filePath);
                } catch (Exception e) {
                    logger.error("Failed to review file in parallel: {}", filePath, e);
                    return null;
                }
            }));
        }

        for (Future<ReviewResult> future : futures) {
            try {
                ReviewResult result = future.get(2, TimeUnit.MINUTES);
                if (result != null) {
                    reviews.add(result);
                }
            } catch (TimeoutException e) {
                logger.warn("Review timeout for file");
                future.cancel(true);
            } catch (Exception e) {
                logger.error("Error reviewing file in parallel", e);
            }
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
    }

    /**
     * Reviews files sequentially
     */
    private void reviewFilesSequentially(List<String> files) {
        for (String filePath : files) {
            try {
                ReviewResult result = reviewSingleFile(filePath);
                if (result != null) {
                    reviews.add(result);
                }
            } catch (Exception e) {
                logger.error("Failed to review file {}: {}", filePath, e.getMessage());
            }
        }
    }

    /**
     * Reviews a single file
     */
    private ReviewResult reviewSingleFile(String filePath) throws Exception {
        try {
            String fileName = new java.io.File(filePath).getName();

            if (fileReaderTool.isFileTooLarge(filePath)) {
                logger.warn("File too large, reading with limit: {}", fileName);
                String content = fileReaderTool.readFileWithLimit(filePath, 10000);
                return reviewAgent.reviewFileWithRetry(fileName, content);
            } else {
                String content = fileReaderTool.readFile(filePath);
                return reviewAgent.reviewFileWithRetry(fileName, content);
            }
        } catch (Exception e) {
            logger.error("Failed to review file {}: {}", filePath, e.getMessage());
            return null;
        }
    }

    /**
     * Summarizes all reviews
     */
    private void summarizeReviews() {
        logger.info("Action: Summarizing reviews");
        summary = summaryAgent.summarizeReviews(reviews);
        logger.info("Summary: {}", summary);
    }

    /**
     * Writes the markdown report
     */
    private void writeReport(String projectPath) throws IOException {
        logger.info("Action: Writing report");

        if (summary == null) {
            logger.warn("No summary available, skipping report generation");
            return;
        }

        String reportDir = "reports";
        new java.io.File(reportDir).mkdirs();

        String reportPath = reportWriterTool.writeReportToFile(summary, projectPath, reportDir);
        logger.info("Report written to: {}", reportPath);
    }

    /**
     * Sends the report via email if enabled
     */
    private void sendEmailReport() {
        logger.info("Action: Sending email report");

        if (!emailAgent.validateConfiguration()) {
            logger.warn("Email not properly configured, skipping");
            return;
        }

        if (summary == null) {
            logger.warn("No summary available, skipping email");
            return;
        }

        try {
            String reportContent = reportWriterTool.generateReport(summary, "project");
            String subject = "Code Review Report - " + new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

            if (emailAgent.sendReport(reportContent, subject)) {
                logger.info("Email sent successfully");
            } else {
                logger.error("Failed to send email report");
            }
        } catch (Exception e) {
            logger.error("Error generating or sending email report: {}", e.getMessage(), e);
        }
    }

    // Getters
    public List<ReviewResult> getReviews() { return reviews; }
    public Summary getSummary() { return summary; }
    public RouterAgent getRouterAgent() { return routerAgent; }
    public PlannerAgent getPlannerAgent() { return plannerAgent; }
}

