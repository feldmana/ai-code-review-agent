package com.agentic.codereview.mcp;

import com.agentic.codereview.agent.EmailAgent;
import com.agentic.codereview.agent.ReviewAgent;
import com.agentic.codereview.config.AppConfig;
import com.agentic.codereview.llm.OllamaClient;
import com.agentic.codereview.orchestrator.AgentOrchestrator;
import com.agentic.codereview.rag.RagService;
import com.agentic.codereview.tool.FileScannerTool;
import com.agentic.codereview.tool.FileReaderTool;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of MCP Tools
 * Exposes CodeReviewAgent functionality as Claude/AI-accessible tools
 */
public class MCPToolRegistry {
    private static final Logger logger = LoggerFactory.getLogger(MCPToolRegistry.class);
    private final Map<String, MCPTool> tools = new HashMap<>();
    private final Gson gson = new Gson();

    private final ReviewAgent reviewAgent;
    private final RagService ragService;
    private final FileScannerTool fileScannerTool;
    private final FileReaderTool fileReaderTool;

    public MCPToolRegistry(ReviewAgent reviewAgent, RagService ragService, 
                         FileScannerTool fileScannerTool, FileReaderTool fileReaderTool) {
        this.reviewAgent = reviewAgent;
        this.ragService = ragService;
        this.fileScannerTool = fileScannerTool;
        this.fileReaderTool = fileReaderTool;
        
        registerTools();
    }

    /**
     * Register all available tools
     */
    private void registerTools() {
        registerReviewCodeTool();
        registerScanFilesTool();
        registerGetRulesTool();
        registerAnalyzeCodeTypeTool();
        
        logger.info("✅ MCP Tool Registry initialized with {} tools", tools.size());
    }

    /**
     * Tool: Review a single file
     */
    private void registerReviewCodeTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
            "fileName", Map.of("type", "string", "description", "Name of the file to review"),
            "fileContent", Map.of("type", "string", "description", "Source code content"),
            "projectPath", Map.of("type", "string", "description", "Project root path (optional)")
        ));
        schema.put("required", List.of("fileName", "fileContent"));

        MCPTool tool = new MCPTool.Builder()
            .name("review_code")
            .description("Perform AI-powered code review on a single file using RAG and LLM analysis")
            .inputSchema(schema)
            .handler(input -> {
                try {
                    String fileName = input.get("fileName").getAsString();
                    String fileContent = input.get("fileContent").getAsString();
                    
                    logger.info("🔍 MCP Tool: Reviewing file: {}", fileName);
                    
                    var reviewResult = reviewAgent.reviewFileWithRetry(fileName, fileContent);
                    
                    return gson.toJson(Map.of(
                        "fileName", reviewResult.getFileName(),
                        "issuesCount", reviewResult.getIssues().size(),
                        "severity", reviewResult.getSeverity().toString(),
                        "issues", reviewResult.getIssues(),
                        "suggestions", reviewResult.getSuggestions()
                    ));
                } catch (Exception e) {
                    logger.error("❌ Review failed", e);
                    return gson.toJson(Map.of("error", e.getMessage()));
                }
            })
            .build();
        
        tools.put("review_code", tool);
        logger.debug("✓ Registered tool: review_code");
    }

    /**
     * Tool: Scan directory for Java files
     */
    private void registerScanFilesTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
            "projectPath", Map.of("type", "string", "description", "Path to project directory to scan")
        ));
        schema.put("required", List.of("projectPath"));

        MCPTool tool = new MCPTool.Builder()
            .name("scan_files")
            .description("Scan a project directory and find all Java source files")
            .inputSchema(schema)
            .handler(input -> {
                try {
                    String projectPath = input.get("projectPath").getAsString();
                    
                    logger.info("📁 MCP Tool: Scanning project: {}", projectPath);
                    
                    List<String> files = fileScannerTool.scanDirectory(projectPath);
                    
                    return gson.toJson(Map.of(
                        "projectPath", projectPath,
                        "filesFound", files.size(),
                        "files", files
                    ));
                } catch (Exception e) {
                    logger.error("❌ Scan failed", e);
                    return gson.toJson(Map.of("error", e.getMessage()));
                }
            })
            .build();
        
        tools.put("scan_files", tool);
        logger.debug("✓ Registered tool: scan_files");
    }

    /**
     * Tool: Get relevant rules for code
     */
    private void registerGetRulesTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
            "code", Map.of("type", "string", "description", "Code snippet to analyze for relevant rules")
        ));
        schema.put("required", List.of("code"));

        MCPTool tool = new MCPTool.Builder()
            .name("get_rules")
            .description("Retrieve relevant coding rules for a code snippet using RAG system")
            .inputSchema(schema)
            .handler(input -> {
                try {
                    String code = input.get("code").getAsString();
                    
                    logger.info("📚 MCP Tool: Getting relevant rules");
                    
                    List<String> rules = ragService.getRelevantRules(code);
                    
                    return gson.toJson(Map.of(
                        "rulesCount", rules.size(),
                        "rules", rules
                    ));
                } catch (Exception e) {
                    logger.error("❌ Get rules failed", e);
                    return gson.toJson(Map.of("error", e.getMessage()));
                }
            })
            .build();
        
        tools.put("get_rules", tool);
        logger.debug("✓ Registered tool: get_rules");
    }

    /**
     * Tool: Analyze code type
     */
    private void registerAnalyzeCodeTypeTool() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
            "code", Map.of("type", "string", "description", "Java code to analyze")
        ));
        schema.put("required", List.of("code"));

        MCPTool tool = new MCPTool.Builder()
            .name("analyze_code_type")
            .description("Detect the type of Java code (Service, Controller, Repository, Entity, etc.)")
            .inputSchema(schema)
            .handler(input -> {
                try {
                    String code = input.get("code").getAsString();
                    
                    logger.info("🔎 MCP Tool: Analyzing code type");
                    
                    // Detect code type
                    String codeType = "UNKNOWN";
                    if (code.contains("@Service")) codeType = "SERVICE";
                    else if (code.contains("@Controller") || code.contains("@RestController")) codeType = "CONTROLLER";
                    else if (code.contains("@Repository")) codeType = "REPOSITORY";
                    else if (code.contains("@Entity")) codeType = "ENTITY";
                    else if (code.contains("@Configuration")) codeType = "CONFIGURATION";
                    
                    return gson.toJson(Map.of(
                        "codeType", codeType,
                        "applicableRules", getApplicableRules(codeType)
                    ));
                } catch (Exception e) {
                    logger.error("❌ Code type analysis failed", e);
                    return gson.toJson(Map.of("error", e.getMessage()));
                }
            })
            .build();
        
        tools.put("analyze_code_type", tool);
        logger.debug("✓ Registered tool: analyze_code_type");
    }

    private List<String> getApplicableRules(String codeType) {
        return switch(codeType) {
            case "SERVICE" -> List.of(
                "Microservices Design",
                "Single Responsibility Principle",
                "Dependency Injection",
                "Exception Handling",
                "Null Safety"
            );
            case "CONTROLLER" -> List.of(
                "REST API Design",
                "HTTP Method Usage",
                "Validation",
                "Error Handling",
                "Authorization"
            );
            case "REPOSITORY" -> List.of(
                "Data Access Patterns",
                "Query Optimization",
                "Entity Mapping",
                "Transaction Management"
            );
            case "ENTITY" -> List.of(
                "Domain Model Design",
                "Annotations",
                "Relationships",
                "Serialization"
            );
            default -> List.of("General Best Practices");
        };
    }

    /**
     * Register orchestrator-level tools that run the full review pipeline.
     * Call this after constructing the registry if you want email/full-review tools.
     */
    public void registerOrchestratorTools(AgentOrchestrator orchestrator) {
        registerRunFullReviewTool(orchestrator);
        registerSendEmailReportTool(orchestrator);
        logger.info("✅ Orchestrator tools registered (run_full_review, send_email_report)");
    }

    private void registerRunFullReviewTool(AgentOrchestrator orchestrator) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
            "projectPath", Map.of("type", "string", "description", "Root path of the project to review")
        ));
        schema.put("required", List.of("projectPath"));

        MCPTool tool = new MCPTool.Builder()
            .name("run_full_review")
            .description("Scan all Java files in a project and run a full AI code review. Returns the complete report.")
            .inputSchema(schema)
            .handler(input -> {
                try {
                    String projectPath = input.get("projectPath").getAsString();
                    logger.info("🔍 MCP Tool: Running full review on: {}", projectPath);
                    String report = orchestrator.runReviewAndGetReport(projectPath);
                    return gson.toJson(Map.of(
                        "status", "success",
                        "projectPath", projectPath,
                        "report", report
                    ));
                } catch (Exception e) {
                    logger.error("❌ Full review failed", e);
                    return gson.toJson(Map.of("error", e.getMessage()));
                }
            })
            .build();

        tools.put("run_full_review", tool);
        logger.debug("✓ Registered tool: run_full_review");
    }

    private void registerSendEmailReportTool(AgentOrchestrator orchestrator) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
            "projectPath", Map.of("type", "string", "description", "Root path of the project to review and email")
        ));
        schema.put("required", List.of("projectPath"));

        MCPTool tool = new MCPTool.Builder()
            .name("send_email_report")
            .description("Run a full code review and send the report via email.")
            .inputSchema(schema)
            .handler(input -> {
                try {
                    String projectPath = input.get("projectPath").getAsString();
                    logger.info("📧 MCP Tool: Running review + email for: {}", projectPath);
                    orchestrator.runReviewAndSendEmail(projectPath);
                    return gson.toJson(Map.of(
                        "status", "success",
                        "message", "Review completed and email sent for: " + projectPath
                    ));
                } catch (Exception e) {
                    logger.error("❌ Review + email failed", e);
                    return gson.toJson(Map.of("error", e.getMessage()));
                }
            })
            .build();

        tools.put("send_email_report", tool);
        logger.debug("✓ Registered tool: send_email_report");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LLM tools — date/time + Java tips via Ollama
    // ─────────────────────────────────────────────────────────────────────────

    public void registerLLMTools(OllamaClient ollama) {
        // get_date_time — trivial demo: MCP tool that just returns system time
        tools.put("get_date_time", new MCPTool.Builder()
            .name("get_date_time")
            .description("Returns the current date, time, and day of week")
            .inputSchema(Map.of("type", "object", "properties", Map.of()))
            .handler(input -> {
                LocalDateTime now = LocalDateTime.now();
                return gson.toJson(Map.of(
                    "date",      now.toLocalDate().toString(),
                    "time",      now.toLocalTime().withNano(0).toString(),
                    "dayOfWeek", now.getDayOfWeek().toString(),
                    "timestamp", System.currentTimeMillis()
                ));
            })
            .build());

        // java_tip — calls Ollama from inside an MCP tool (LLM-backed tool demo)
        Map<String, Object> tipSchema = new HashMap<>();
        tipSchema.put("type", "object");
        tipSchema.put("properties", Map.of(
            "topic", Map.of("type", "string", "description", "Optional topic, e.g. streams, generics, concurrency")
        ));
        tools.put("java_tip", new MCPTool.Builder()
            .name("java_tip")
            .description("Get a Java best-practice tip from the AI (Ollama-backed)")
            .inputSchema(tipSchema)
            .handler(input -> {
                try {
                    String topic = input.has("topic") ? input.get("topic").getAsString() : "general Java";
                    String prompt = "Give ONE concise, practical Java programming tip about: " + topic +
                        ". Keep it to 2-3 sentences. Be specific and actionable.";
                    String tip = ollama.generateResponse(prompt);
                    return gson.toJson(Map.of("topic", topic, "tip", tip.trim()));
                } catch (Exception e) {
                    return gson.toJson(Map.of("error", e.getMessage()));
                }
            })
            .build());

        // free_chat — general-purpose Ollama tool; any question goes here
        Map<String, Object> chatSchema = new HashMap<>();
        chatSchema.put("type", "object");
        chatSchema.put("properties", Map.of(
            "question",    Map.of("type", "string", "description", "Any natural language question"),
            "dateContext", Map.of("type", "string", "description", "Optional: today's date to inject into context")
        ));
        chatSchema.put("required", List.of("question"));
        tools.put("free_chat", new MCPTool.Builder()
            .name("free_chat")
            .description("Forward any question to Ollama and return the answer. Date context is injected automatically.")
            .inputSchema(chatSchema)
            .handler(input -> {
                try {
                    String question    = input.get("question").getAsString();
                    String dateContext = input.has("dateContext") ? input.get("dateContext").getAsString() : "";
                    String prompt      = dateContext.isEmpty()
                        ? "Answer concisely (2-3 sentences): " + question
                        : "Today is " + dateContext + ".\nAnswer concisely (2-3 sentences): " + question;
                    String answer = ollama.generateResponse(prompt);
                    return gson.toJson(Map.of("answer", answer.trim()));
                } catch (Exception e) {
                    return gson.toJson(Map.of("error", e.getMessage()));
                }
            })
            .build());

        logger.info("✅ LLM tools registered (get_date_time, java_tip, free_chat)");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Report storage tools — save/load/list JSON reports per project
    // ─────────────────────────────────────────────────────────────────────────

    public void registerReportStorageTools(String reportsDir) {
        new File(reportsDir).mkdirs();

        // save_report
        Map<String, Object> saveSchema = new HashMap<>();
        saveSchema.put("type", "object");
        saveSchema.put("properties", Map.of(
            "projectName",   Map.of("type", "string"),
            "projectPath",   Map.of("type", "string"),
            "reportContent", Map.of("type", "string")
        ));
        saveSchema.put("required", List.of("projectName", "reportContent"));
        tools.put("save_report", new MCPTool.Builder()
            .name("save_report")
            .description("Persist a review report to disk, keyed by project name")
            .inputSchema(saveSchema)
            .handler(input -> {
                try {
                    String projectName   = input.get("projectName").getAsString();
                    String reportContent = input.get("reportContent").getAsString();
                    String projectPath   = input.has("projectPath") ? input.get("projectPath").getAsString() : "";
                    String timestamp     = LocalDateTime.now().toString().replaceAll("[:\\.]", "-").substring(0, 19);

                    JsonObject json = new JsonObject();
                    json.addProperty("projectName",   projectName);
                    json.addProperty("projectPath",   projectPath);
                    json.addProperty("timestamp",     timestamp);
                    json.addProperty("reportContent", reportContent);

                    String filePath = reportsDir + "/" + sanitize(projectName) + ".json";
                    Files.writeString(Path.of(filePath), gson.toJson(json));
                    logger.info("✓ Report saved: {}", filePath);
                    return gson.toJson(Map.of("status", "saved", "filePath", filePath));
                } catch (Exception e) {
                    return gson.toJson(Map.of("error", e.getMessage()));
                }
            })
            .build());

        // load_report
        Map<String, Object> loadSchema = new HashMap<>();
        loadSchema.put("type", "object");
        loadSchema.put("properties", Map.of(
            "projectName", Map.of("type", "string", "description", "Project name as returned by list_saved_reports")
        ));
        loadSchema.put("required", List.of("projectName"));
        tools.put("load_report", new MCPTool.Builder()
            .name("load_report")
            .description("Load a saved review report from disk by project name")
            .inputSchema(loadSchema)
            .handler(input -> {
                try {
                    String projectName = input.get("projectName").getAsString();
                    String filePath    = reportsDir + "/" + sanitize(projectName) + ".json";
                    if (!Files.exists(Path.of(filePath))) {
                        return gson.toJson(Map.of("error", "No saved report for: " + projectName));
                    }
                    String content = Files.readString(Path.of(filePath));
                    logger.info("✓ Report loaded: {}", filePath);
                    return content; // already JSON
                } catch (Exception e) {
                    return gson.toJson(Map.of("error", e.getMessage()));
                }
            })
            .build());

        // list_saved_reports
        tools.put("list_saved_reports", new MCPTool.Builder()
            .name("list_saved_reports")
            .description("List all projects that have a saved review report")
            .inputSchema(Map.of("type", "object", "properties", Map.of()))
            .handler(input -> {
                try {
                    File dir = new File(reportsDir);
                    File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
                    List<Map<String, String>> reports = new ArrayList<>();
                    if (files != null) {
                        for (File f : files) {
                            try {
                                String raw = Files.readString(f.toPath());
                                JsonObject json = com.google.gson.JsonParser.parseString(raw).getAsJsonObject();
                                reports.add(Map.of(
                                    "projectName", json.get("projectName").getAsString(),
                                    "projectPath", json.has("projectPath") ? json.get("projectPath").getAsString() : "",
                                    "timestamp",   json.has("timestamp")   ? json.get("timestamp").getAsString()   : ""
                                ));
                            } catch (Exception ignored) {}
                        }
                    }
                    return gson.toJson(Map.of("count", reports.size(), "reports", reports));
                } catch (Exception e) {
                    return gson.toJson(Map.of("error", e.getMessage()));
                }
            })
            .build());

        logger.info("✅ Report storage tools registered (save_report, load_report, list_saved_reports)");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Email-content tool — sends an already-computed report string by email
    // ─────────────────────────────────────────────────────────────────────────

    public void registerEmailContentTool(AppConfig config) {
        EmailAgent emailAgent = new EmailAgent(config);

        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
            "reportContent", Map.of("type", "string", "description", "Full report text to email"),
            "subject",       Map.of("type", "string", "description", "Email subject line")
        ));
        schema.put("required", List.of("reportContent"));

        tools.put("email_report_content", new MCPTool.Builder()
            .name("email_report_content")
            .description("Send an already-computed review report via email without re-running the review")
            .inputSchema(schema)
            .handler(input -> {
                try {
                    String content = input.get("reportContent").getAsString();
                    String subject = input.has("subject")
                        ? input.get("subject").getAsString()
                        : "Code Review Report - " + LocalDateTime.now().toLocalDate();
                    boolean sent = emailAgent.sendReport(content, subject);
                    return gson.toJson(Map.of(
                        "status",  sent ? "sent" : "failed",
                        "subject", subject
                    ));
                } catch (Exception e) {
                    return gson.toJson(Map.of("error", e.getMessage()));
                }
            })
            .build());

        logger.info("✅ Email content tool registered (email_report_content)");
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }

    /**
     * Get a tool by name
     */
    public MCPTool getTool(String name) {
        return tools.get(name);
    }

    /**
     * Get all tools
     */
    public Map<String, MCPTool> getAllTools() {
        return new HashMap<>(tools);
    }

    /**
     * Get list of tool names
     */
    public List<String> getToolNames() {
        return List.copyOf(tools.keySet());
    }

    /**
     * Invoke a tool with comprehensive error handling
     */
    public String invokeTool(String toolName, JsonObject input) throws Exception {
        if (toolName == null || toolName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tool name cannot be null or empty");
        }
        
        MCPTool tool = tools.get(toolName);
        if (tool == null) {
            logger.error("❌ Tool not found: {}", toolName);
            throw new IllegalArgumentException("Tool not found: " + toolName);
        }
        
        if (input == null) {
            logger.error("❌ Input cannot be null for tool: {}", toolName);
            throw new IllegalArgumentException("Input cannot be null");
        }
        
        try {
            logger.debug("⏳ Invoking tool: {} with input: {}", toolName, input);
            String result = tool.invoke(input);
            logger.debug("✓ Tool {} completed successfully", toolName);
            return result;
        } catch (Exception e) {
            logger.error("❌ Tool execution failed for {}: {}", toolName, e.getMessage(), e);
            throw new Exception("Tool execution failed: " + e.getMessage(), e);
        }
    }
}

