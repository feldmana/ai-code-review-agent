package com.agentic.codereview.mcp;

import com.agentic.codereview.agent.ReviewAgent;
import com.agentic.codereview.config.AppConfig;
import com.agentic.codereview.llm.OllamaClient;
import com.agentic.codereview.orchestrator.AgentOrchestrator;
import com.agentic.codereview.rag.EnhancedVectorRagService;
import com.agentic.codereview.rag.RagService;
import com.agentic.codereview.tool.FileReaderTool;
import com.agentic.codereview.tool.FileScannerTool;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive chat that routes natural language to MCP tools.
 *
 * Supported intents:
 *   REVIEW  - scan + review files via MCP, save result to session + disk
 *   EMAIL   - email last review (session/saved) without re-running, or run first
 *   SUMMARY - show summary of last or saved review
 *   LIST    - list all saved review reports
 *   DATE    - ask MCP for current date/time
 *   TIP     - ask MCP for a Java programming tip (Ollama-backed)
 *   HELP    - print options
 *
 * Every MCP call prints its JSON request and response so you can see the protocol.
 */
public class ChatMCPClient {

    private static final Logger logger = LoggerFactory.getLogger(ChatMCPClient.class);
    private static final int    MCP_PORT_START = 9877; // will auto-pick next free port if busy
    private static final String STAR_LINE      = "★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★";
    private static final String RAG_PATH       = "rag-docs";
    private static final String REPORTS_DIR    = "chat-reports";
    private static final String DEFAULT_PATH   = "/Users/alexandrafeldman/Documents/Learning/OpenAI/testProject";

    // ── MCP wiring ────────────────────────────────────────────────────────────
    private final MCPServer    mcpServer;
    private final MCPClient    mcpClient;
    private final OllamaClient ollamaClient;
    private final AppConfig    config;
    private final int          mcpPort;
    private final Gson         gson = new Gson();

    // ── Session state — remembers the last completed review ───────────────────
    private String sessionProjectPath    = null;
    private String sessionReportContent  = null;
    private String sessionProjectName    = null;

    // ── Session transcript — full chat + MCP details saved to file ────────────
    private PrintWriter transcriptWriter = null;
    private String      transcriptPath   = null;

    public ChatMCPClient(MCPServer mcpServer, OllamaClient ollamaClient, AppConfig config, int mcpPort) {
        this.mcpServer    = mcpServer;
        this.mcpClient    = new MCPClient("localhost", mcpPort);
        this.ollamaClient = ollamaClient;
        this.config       = config;
        this.mcpPort      = mcpPort;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Entry point
    // ─────────────────────────────────────────────────────────────────────────

    public void start() throws Exception {
        initTranscript();

        mcpServer.start();
        info("MCP Server started on port " + mcpPort);

        mcpClient.connect();
        info("MCP Client connected");

        printRegisteredTools();
        printWelcome();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\nYou: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            logConversation("YOU", input);
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye!");
                logger.info(STAR_LINE);
                transcript("[" + timestamp() + "] Session ended.");
                break;
            }
            try {
                handleTurn(input, scanner);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                logger.error("Chat error", e);
            }
        }

        mcpClient.disconnect();
        mcpServer.stop();
        if (transcriptWriter != null) transcriptWriter.close();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Intent dispatch
    // ─────────────────────────────────────────────────────────────────────────

    private void handleTurn(String userInput, Scanner scanner) throws Exception {
        logger.info("Classifying intent for: {}", userInput);
        String intent = classifyIntent(userInput);
        logger.info("Intent classified: {}", intent);
        transcript("[AI] Intent: " + intent);

        switch (intent) {
            case "REVIEW"  -> handleReview(scanner);
            case "EMAIL"   -> handleEmail(scanner);
            case "SUMMARY" -> handleSummary(scanner);
            case "LIST"    -> handleList();
            case "DATE"    -> handleDate(userInput);
            case "TIP"     -> handleTip(userInput, scanner);
            case "HELP"    -> printWelcome();
            default        -> handleFreeChat(userInput); // any other question → Ollama
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REVIEW — scan project, review each file via MCP, save result
    // ─────────────────────────────────────────────────────────────────────────

    private void handleReview(Scanner scanner) throws Exception {
        String projectPath = askProjectPath(scanner);
        String projectName = Path.of(projectPath).getFileName().toString();

        System.out.println("\n══════════ Review via MCP ══════════");

        // ── Step 1: scan_files ───────────────────────────────────────────────
        JsonObject scanInput = new JsonObject();
        scanInput.addProperty("projectPath", projectPath);
        String scanResponse = callMCPTool("scan_files", scanInput);

        JsonObject scanResult = unwrapResult(scanResponse);
        if (scanResult == null) return;

        int fileCount = scanResult.get("filesFound").getAsInt();
        System.out.println("\n[Chat] Found " + fileCount + " files.");
        if (fileCount == 0) { System.out.println("[Chat] Nothing to review."); return; }

        JsonArray files = scanResult.getAsJsonArray("files");
        int limit = Math.min(3, files.size());
        System.out.println("[Chat] Reviewing first " + limit + " file(s)...");

        // ── Step 2: review_code per file ─────────────────────────────────────
        StringBuilder fullReport = new StringBuilder("# Code Review — " + projectName + "\n\n");

        for (int i = 0; i < limit; i++) {
            String filePath = files.get(i).getAsString();
            String fileName = Path.of(filePath).getFileName().toString();

            JsonObject reviewInput = new JsonObject();
            reviewInput.addProperty("fileName",    fileName);
            reviewInput.addProperty("fileContent", readFile(filePath));
            String reviewResponse = callMCPTool("review_code", reviewInput);

            JsonObject reviewResult = unwrapResult(reviewResponse);
            if (reviewResult != null) {
                fullReport.append("## ").append(fileName).append("\n");
                fullReport.append("Severity: ").append(reviewResult.get("severity").getAsString()).append("\n");
                fullReport.append("Issues: ").append(reviewResult.get("issuesCount").getAsInt()).append("\n\n");
            }
        }

        // ── Step 3: save_report ──────────────────────────────────────────────
        String report = fullReport.toString();
        JsonObject saveInput = new JsonObject();
        saveInput.addProperty("projectName",   projectName);
        saveInput.addProperty("projectPath",   projectPath);
        saveInput.addProperty("reportContent", report);
        callMCPTool("save_report", saveInput);

        // ── Update session ────────────────────────────────────────────────────
        sessionProjectPath   = projectPath;
        sessionProjectName   = projectName;
        sessionReportContent = report;
        System.out.println("\n[Chat] Review saved to session and disk. You can now 'email' or ask for a 'summary'.");
        System.out.println("══════════ Review Complete ══════════");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EMAIL — choose to send a custom message OR a saved review report
    // ─────────────────────────────────────────────────────────────────────────

    private void handleEmail(Scanner scanner) throws Exception {
        // Guard: check SMTP password is configured before going further
        String smtpPwd = config.getSmtpPassword();
        if (smtpPwd == null || smtpPwd.isEmpty() || smtpPwd.startsWith("${")) {
            System.out.println("\n[Chat] Email is not configured — SMTP_PASSWORD is missing.");
            System.out.println("[Chat] To fix this:");
            System.out.println("  1. Go to myaccount.google.com → Security → App Passwords");
            System.out.println("  2. Generate an App Password for 'Mail'");
            System.out.println("  3. Either:");
            System.out.println("       export SMTP_PASSWORD='your-app-password'  (then re-run)");
            System.out.println("       OR replace '${SMTP_PASSWORD}' in codereview.properties directly");
            return;
        }

        System.out.println("\n══════════ Email via MCP ══════════");
        System.out.println("[Chat] What would you like to email?");
        System.out.println("  [1] Type a custom message");
        System.out.println("  [2] Send a saved review report");
        System.out.print("Enter 1 or 2: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> emailCustomMessage(scanner);
            case "2" -> emailSavedReport(scanner);
            default  -> System.out.println("[Chat] Please enter 1 or 2.");
        }

        System.out.println("══════════ Done ══════════");
    }

    /** Option 1 — user types any message and we email it. */
    private void emailCustomMessage(Scanner scanner) throws Exception {
        System.out.println("[Chat] Type your message below (enter a blank line when finished):");
        StringBuilder msg = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if (line.isBlank()) break;
            msg.append(line).append("\n");
        }
        if (msg.isEmpty()) { System.out.println("[Chat] Nothing to send."); return; }

        System.out.print("[Chat] Subject (Enter = 'Message from Code Review Assistant'): ");
        String subject = scanner.nextLine().trim();
        if (subject.isEmpty()) subject = "Message from Code Review Assistant";

        JsonObject input = new JsonObject();
        input.addProperty("reportContent", msg.toString());
        input.addProperty("subject", subject);
        callMCPTool("email_report_content", input);
    }

    /** Option 2 — list all saved reports + session cache, pick one, email it. */
    private void emailSavedReport(Scanner scanner) throws Exception {
        // Fetch saved reports from disk via MCP
        String listResponse = callMCPTool("list_saved_reports", new JsonObject());
        JsonObject listResult = unwrapResult(listResponse);

        // Build a combined menu: session first (if exists), then disk reports
        List<String[]> menu = new java.util.ArrayList<>(); // [label, projectName/flag]

        if (sessionReportContent != null) {
            menu.add(new String[]{"★ Latest session review (" + sessionProjectName + ")", "__session__"});
        }

        if (listResult != null && listResult.get("count").getAsInt() > 0) {
            for (var el : listResult.getAsJsonArray("reports")) {
                JsonObject r = el.getAsJsonObject();
                String label = String.format("%-25s  saved: %s",
                    r.get("projectName").getAsString(), r.get("timestamp").getAsString());
                menu.add(new String[]{label, r.get("projectName").getAsString()});
            }
        }

        if (menu.isEmpty()) {
            System.out.println("[Chat] No saved reports found. Run a 'review' first.");
            return;
        }

        // Always add "run a new review" as the last option
        menu.add(new String[]{"Run a new review now and send it", "__new_review__"});

        System.out.println("\n[Chat] Available reports:");
        for (int i = 0; i < menu.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, menu.get(i)[0]);
        }

        // Loop until valid input — avoids "Invalid selection" when user types ahead
        int idx = -1;
        while (idx < 0) {
            System.out.print("Enter number (1-" + menu.size() + "): ");
            String pick = scanner.nextLine().trim();
            if (pick.isEmpty()) continue;   // skip accidental blank Enter presses
            try {
                int parsed = Integer.parseInt(pick) - 1;
                if (parsed >= 0 && parsed < menu.size()) idx = parsed;
                else System.out.println("[Chat] Please enter a number between 1 and " + menu.size());
            } catch (NumberFormatException e) {
                System.out.println("[Chat] Please enter a number between 1 and " + menu.size());
            }
        }

        String key = menu.get(idx)[1];

        // ── Option: run a fresh review first, then email the result ───────────
        if ("__new_review__".equals(key)) {
            System.out.println("[Chat] Running a new review first...");
            handleReview(scanner);
            if (sessionReportContent == null) {
                System.out.println("[Chat] Review did not produce a report, nothing to email.");
                return;
            }
            JsonObject emailInput = new JsonObject();
            emailInput.addProperty("reportContent", sessionReportContent);
            emailInput.addProperty("subject", "Code Review — " + sessionProjectName);
            callMCPTool("email_report_content", emailInput);
            return;
        }

        String content;
        String subject;

        if ("__session__".equals(key)) {
            content = sessionReportContent;
            subject = "Code Review — " + sessionProjectName;
        } else {
            JsonObject loadInput = new JsonObject();
            loadInput.addProperty("projectName", key);
            String loadResponse = callMCPTool("load_report", loadInput);
            JsonObject loaded   = unwrapResult(loadResponse);
            if (loaded == null) return;
            if (!loaded.has("reportContent")) {
                System.out.println("[Chat] Could not find report content for: " + key);
                return;
            }
            content = loaded.get("reportContent").getAsString();
            subject = "Code Review — " + key;
        }

        JsonObject emailInput = new JsonObject();
        emailInput.addProperty("reportContent", content);
        emailInput.addProperty("subject", subject);
        callMCPTool("email_report_content", emailInput);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUMMARY — display summary of last or saved review
    // ─────────────────────────────────────────────────────────────────────────

    private void handleSummary(Scanner scanner) throws Exception {
        System.out.println("\n══════════ Summary via MCP ══════════");

        // Use session report if available
        if (sessionReportContent != null) {
            System.out.println("[Chat] Summary of last review (" + sessionProjectName + "):\n");
            System.out.println(sessionReportContent);
            System.out.println("══════════ End of Summary ══════════");
            return;
        }

        // Otherwise list saved reports and let user pick
        String listResponse = callMCPTool("list_saved_reports", new JsonObject());
        JsonObject listResult = unwrapResult(listResponse);

        if (listResult == null || listResult.get("count").getAsInt() == 0) {
            System.out.println("[Chat] No saved reviews found. Run a 'review' first.");
            return;
        }

        JsonArray reports = listResult.getAsJsonArray("reports");
        System.out.println("[Chat] Available saved reviews:");
        for (int i = 0; i < reports.size(); i++) {
            JsonObject r = reports.get(i).getAsJsonObject();
            System.out.printf("  [%d] %s  (%s)%n",
                i + 1, r.get("projectName").getAsString(), r.get("timestamp").getAsString());
        }

        System.out.print("[Chat] Enter number to view: ");
        String pick = scanner.nextLine().trim();
        try {
            int idx = Integer.parseInt(pick) - 1;
            String projectName = reports.get(idx).getAsJsonObject().get("projectName").getAsString();
            JsonObject loadInput = new JsonObject();
            loadInput.addProperty("projectName", projectName);
            String loadResponse = callMCPTool("load_report", loadInput);
            JsonObject loaded   = unwrapResult(loadResponse);
            if (loaded != null && loaded.has("reportContent")) {
                System.out.println("\n" + loaded.get("reportContent").getAsString());
                transcript("[Summary displayed for: " + projectName + "]");
            }
        } catch (Exception e) {
            System.out.println("[Chat] Invalid selection.");
        }
        System.out.println("══════════ End of Summary ══════════");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LIST — list saved reports
    // ─────────────────────────────────────────────────────────────────────────

    private void handleList() throws Exception {
        System.out.println("\n══════════ Saved Reports via MCP ══════════");
        String response = callMCPTool("list_saved_reports", new JsonObject());
        JsonObject result = unwrapResult(response);
        if (result == null) return;

        int count = result.get("count").getAsInt();
        if (count == 0) {
            System.out.println("[Chat] No saved reports yet. Run a 'review' to create one.");
            return;
        }

        System.out.println("[Chat] Found " + count + " saved report(s):");
        JsonArray reports = result.getAsJsonArray("reports");
        for (int i = 0; i < reports.size(); i++) {
            JsonObject r = reports.get(i).getAsJsonObject();
            System.out.printf("  %d. %-25s  path: %s   saved: %s%n",
                i + 1,
                r.get("projectName").getAsString(),
                r.get("projectPath").getAsString(),
                r.get("timestamp").getAsString());
        }
        System.out.println("══════════════════════════════════════");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DATE — get_date_time MCP tool, then answer original question with context
    // ─────────────────────────────────────────────────────────────────────────

    private void handleDate(String originalQuestion) throws Exception {
        System.out.println("\n══════════ Date/Time via MCP ══════════");
        String response = callMCPTool("get_date_time", new JsonObject());
        JsonObject result = unwrapResult(response);
        if (result == null) return;

        String date = result.get("date").getAsString();
        String dow  = result.get("dayOfWeek").getAsString();
        String time = result.get("time").getAsString();
        System.out.printf("[MCP] Today is %s (%s), time: %s%n", date, dow, time);

        // If the user asked something more than just "what time/date", answer it
        String lower = originalQuestion.toLowerCase();
        boolean isJustTimeQuery = lower.matches(".*(what time|current time|what date|today'?s date).*");
        if (!isJustTimeQuery) {
            JsonObject chatInput = new JsonObject();
            chatInput.addProperty("question",    originalQuestion);
            chatInput.addProperty("dateContext", dow + " " + date);
            String chatResponse = callMCPTool("free_chat", chatInput);
            JsonObject chatResult = unwrapResult(chatResponse);
            if (chatResult != null && chatResult.has("answer")) {
                ollamaResponse(chatResult.get("answer").getAsString());
            }
        }
        System.out.println("═══════════════════════════════════════");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TIP — java_tip MCP tool (Ollama answers through MCP)
    // ─────────────────────────────────────────────────────────────────────────

    private void handleTip(String userInput, Scanner scanner) throws Exception {
        System.out.println("\n══════════ Java Tip via MCP ══════════");

        // Extract topic from user input or ask
        String topic = extractTopic(userInput);
        if (topic.equals("general Java")) {
            System.out.print("[Chat] Any specific topic? (e.g. streams, generics — or Enter for general): ");
            String typed = scanner.nextLine().trim();
            if (!typed.isEmpty()) topic = typed;
        }

        JsonObject input = new JsonObject();
        input.addProperty("topic", topic);
        String response = callMCPTool("java_tip", input);
        JsonObject result = unwrapResult(response);
        if (result != null) {
            String tip = "[Java Tip — " + result.get("topic").getAsString() + "]\n" + result.get("tip").getAsString();
            ollamaResponse(tip);
        }
        System.out.println("══════════════════════════════════════");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FREE CHAT — any unmatched question goes straight to Ollama
    // Date context is injected so "is it summer?" / "what day is it?" works.
    // Java questions should be caught by the TIP intent, but if they slip
    // through here Ollama still answers them correctly.
    // ─────────────────────────────────────────────────────────────────────────

    private void handleFreeChat(String userInput) throws Exception {
        String today = java.time.LocalDate.now().toString();
        String dow   = java.time.LocalDate.now().getDayOfWeek().toString();

        JsonObject input = new JsonObject();
        input.addProperty("question",    userInput);
        input.addProperty("dateContext", dow + " " + today);

        String response = callMCPTool("free_chat", input);
        JsonObject result = unwrapResult(response);
        if (result != null && result.has("answer")) {
            ollamaResponse(result.get("answer").getAsString());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MCP protocol helpers — every call is printed
    // ─────────────────────────────────────────────────────────────────────────

    private String callMCPTool(String toolName, JsonObject input) throws Exception {
        String requestJson = "{\"method\":\"invoke_tool\",\"toolName\":\"" + toolName + "\",\"input\":" + input + "}";
        logger.info("MCP request  → {}: {}", toolName, requestJson);
        transcript("\n[MCP] ──► " + toolName);
        transcript("      Request : " + requestJson);

        String response = mcpClient.invokeTool(toolName, input);

        String display = response.length() > 500 ? response.substring(0, 500) + "..." : response;
        logger.info("MCP response ← {}: {}", toolName, display);
        transcript("      Response: " + display);
        return response;
    }

    private JsonObject unwrapResult(String rawResponse) {
        try {
            JsonObject outer = JsonParser.parseString(rawResponse).getAsJsonObject();
            if (outer.has("error")) {
                System.out.println("[MCP] Error: " + outer.get("error").getAsString());
                return null;
            }
            return outer.getAsJsonObject("result");
        } catch (Exception e) {
            System.out.println("[MCP] Could not parse response: " + rawResponse);
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LLM intent classification
    // ─────────────────────────────────────────────────────────────────────────

    private String classifyIntent(String userInput) {
        // Fast-path: single-word or short commands don't need LLM
        String lower = userInput.toLowerCase().trim();
        if (lower.equals("review") || lower.startsWith("review "))   return "REVIEW";
        if (lower.equals("email")  || lower.startsWith("email ")
                                   || lower.equals("send email")
                                   || lower.startsWith("send report")) return "EMAIL";
        if (lower.equals("summary") || lower.startsWith("summary"))  return "SUMMARY";
        if (lower.equals("list")   || lower.equals("list reports"))   return "LIST";
        if (lower.equals("date")   || lower.equals("time")
                                   || lower.equals("what time")
                                   || lower.equals("what date"))      return "DATE";
        if (lower.equals("tip")    || lower.startsWith("tip "))       return "TIP";
        if (lower.equals("help")   || lower.equals("?")
                                   || lower.equals("options"))        return "HELP";

        String prompt = """
            You are a router for a code review chat assistant.
            Classify the user request into EXACTLY ONE intent from this list:

              REVIEW  - user explicitly wants to review, analyze, or check code quality
              EMAIL   - user explicitly wants to email or send a report
              SUMMARY - user wants to see a summary or view a past review
              LIST    - user wants to list or show saved reviews or reports
              DATE    - user asks about the current date, time, day, month, season, or year
              TIP     - user explicitly asks for a Java programming tip, trick, or knowledge
              HELP    - user asks to see the menu, commands list, or available options
                        (ONLY for: "show commands", "show options", "what can you do", "help menu")
              UNKNOWN - greetings, small talk, general knowledge questions, questions about
                        Java concepts without asking for a "tip", or anything that does not
                        clearly match one of the above

            IMPORTANT: Greetings like "how are you", "hello", "hey", "what's up" are UNKNOWN.
            Questions like "can you help me" or "can you help me with java" are UNKNOWN.
            Only classify as HELP when the user explicitly wants to see the list of commands/options.

            Return ONLY valid JSON: {"intent":"REVIEW"}

            User request: """ + userInput;
        try {
            String raw   = ollamaClient.generateResponse(prompt);
            int start    = raw.indexOf('{');
            int end      = raw.lastIndexOf('}');
            if (start >= 0 && end > start) {
                JsonObject json = JsonParser.parseString(raw.substring(start, end + 1)).getAsJsonObject();
                return json.get("intent").getAsString().toUpperCase();
            }
        } catch (Exception e) {
            logger.warn("Intent classification failed, defaulting to UNKNOWN", e);
        }
        return "UNKNOWN";
    }

    /** Try to pull a topic keyword from the user's message for java_tip. */
    private String extractTopic(String input) {
        String lower = input.toLowerCase();
        for (String kw : List.of("stream", "generic", "concurren", "lambda", "exception",
                                  "collection", "thread", "optional", "record", "interface")) {
            if (lower.contains(kw)) return kw;
        }
        return "general Java";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String askProjectPath(Scanner scanner) {
        System.out.print("\n[Chat] Project path (Enter = " + DEFAULT_PATH + "): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            path = DEFAULT_PATH;
            System.out.println("[Chat] Using: " + path);
        }
        return path;
    }

    private void printRegisteredTools() throws Exception {
        List<String> tools = mcpClient.listTools();
        System.out.println("\n[MCP] Registered tools: " + tools);
    }

    private void printWelcome() {
        System.out.println("""

            ╔═══════════════════════════════════════════════════════╗
            ║       MCP Chat Client — Code Review Assistant         ║
            ╚═══════════════════════════════════════════════════════╝

            Commands (just type naturally):
              review          → scan + AI review of a project
              email           → email a message or a saved report
              summary         → show summary of last or a saved review
              list            → list all saved review reports
              date            → ask MCP for today's date and time
              tip [topic]     → get a Java programming tip via MCP + Ollama
              help            → show this menu
              exit            → quit

            Anything else (e.g. "is it summer?", "who is Picasso?") →
              answered directly by Ollama
            """);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Session transcript — full chat + MCP protocol written to chat-logs/
    // ─────────────────────────────────────────────────────────────────────────

    private void initTranscript() {
        try {
            Files.createDirectories(Path.of("chat-logs"));
            String ts = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            transcriptPath = "chat-logs/session-" + ts + ".txt";
            transcriptWriter = new PrintWriter(new FileWriter(transcriptPath, true), true);
            transcriptWriter.println("=== MCP Chat Session " + ts + " ===\n");
            System.out.println("[INFO] Session transcript: " + transcriptPath);
        } catch (Exception e) {
            logger.warn("Could not create transcript file: {}", e.getMessage());
        }
    }

    private void transcript(String line) {
        logger.debug("{}", line);
        if (transcriptWriter != null) transcriptWriter.println(line);
    }

    private static String timestamp() {
        return java.time.LocalTime.now()
            .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private void chat(String msg) {
        System.out.println("[Chat] " + msg);
        logger.info("[Chat] {}", msg);
        transcript("[Chat] " + msg);
    }

    private void ollamaResponse(String msg) {
        System.out.println("\n[Ollama] " + msg);
        logger.info("[Ollama] {}", msg);
        transcript("[Ollama] " + msg);
    }

    private void logConversation(String role, String msg) {
        String ts = timestamp();
        transcript("\n[" + ts + "] " + role + ": " + msg);
        logger.info(STAR_LINE);
        logger.info("{} [{}] ► {}", role, ts, msg);
        logger.info(STAR_LINE);
    }

    private static void info(String msg) {
        System.out.println("\n[INFO] " + msg);
    }

    private static int findFreePort(int startPort) {
        for (int port = startPort; port < startPort + 20; port++) {
            try (java.net.ServerSocket s = new java.net.ServerSocket(port)) {
                return port;
            } catch (Exception ignored) {}
        }
        throw new RuntimeException("No free port found in range " + startPort + "–" + (startPort + 19));
    }

    private static String readFile(String filePath) {
        try {
            return Files.readString(Path.of(filePath));
        } catch (Exception e) {
            return "// Could not read: " + e.getMessage();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bootstrap
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        AppConfig config = AppConfig.getInstance();

        // RAG
        RagService ragService = new EnhancedVectorRagService(RAG_PATH, 5);
        if (ragService instanceof EnhancedVectorRagService enhanced) {
            enhanced.initialize();
        }

        // LLM
        OllamaClient ollamaClient = new OllamaClient(
                AppConfig.OLLAMA_HOST, AppConfig.OLLAMA_PORT, AppConfig.OLLAMA_MODEL);
        if (!ollamaClient.testConnection()) {
            System.err.println("Cannot connect to Ollama — run: ollama serve");
            System.exit(1);
        }

        // Orchestrator
        AgentOrchestrator orchestrator = new AgentOrchestrator(ragService, config, ollamaClient);

        // MCP tool registry — wire up all tool groups
        ReviewAgent reviewAgent = new ReviewAgent(ragService, ollamaClient, config.getMaxRetries());
        MCPToolRegistry registry = new MCPToolRegistry(
                reviewAgent, ragService, new FileScannerTool(), new FileReaderTool());
        registry.registerOrchestratorTools(orchestrator);   // run_full_review, send_email_report
        registry.registerLLMTools(ollamaClient);            // get_date_time, java_tip
        registry.registerReportStorageTools(REPORTS_DIR);   // save_report, load_report, list_saved_reports
        registry.registerEmailContentTool(config);          // email_report_content

        // MCP server (embedded) — auto-pick free port if default is busy
        int mcpPort = findFreePort(MCP_PORT_START);
        if (mcpPort != MCP_PORT_START) {
            System.out.println("[INFO] Port " + MCP_PORT_START + " busy, using " + mcpPort);
        }
        MCPServer mcpServer = new MCPServer(registry, mcpPort);

        new ChatMCPClient(mcpServer, ollamaClient, config, mcpPort).start();
    }
}