package com.agentic.codereview;

import com.agentic.codereview.config.AppConfig;
import com.agentic.codereview.llm.OllamaClient;
import com.agentic.codereview.mcp.MCPServer;
import com.agentic.codereview.mcp.MCPServerManager;
import com.agentic.codereview.mcp.MCPToolRegistry;
import com.agentic.codereview.mcp.MCPTestClient;
import com.agentic.codereview.orchestrator.AgentOrchestrator;
import com.agentic.codereview.rag.*;
import com.agentic.codereview.rag.RagService;
import com.agentic.codereview.agent.ReviewAgent;
import com.agentic.codereview.tool.FileScannerTool;
import com.agentic.codereview.tool.FileReaderTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Main entry point for the CodeReviewAgent CLI application
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    static String ragPath="/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/rag-docs";
    private static RagService ragService = new EnhancedVectorRagService(ragPath, 5);//Enhanced v2 with BM25
    private static MCPServerManager mcpServerManager;



    public static void main(String[] args) {
        logger.info("=================================");
        logger.info("  CodeReviewAgent v1.0");
        logger.info("  AI-Powered Code Review System");
        logger.info("=================================");

        // Load configuration
        AppConfig config = AppConfig.getInstance();
        logger.info("Configuration loaded: {}", config);
        
        // Initialize Enhanced RAG Service
        if (ragService instanceof EnhancedVectorRagService) {
            EnhancedVectorRagService enhancedRag = (EnhancedVectorRagService) ragService;
            enhancedRag.initialize();
            enhancedRag.debugVectorStore();
            logger.info("📚 Documents loaded: {}", enhancedRag.listDocuments());
        }

        // Initialize Ollama client
        OllamaClient ollamaClient = new OllamaClient(
                AppConfig.OLLAMA_HOST,
                AppConfig.OLLAMA_PORT,
                AppConfig.OLLAMA_MODEL
        );

        // Test connection to Ollama
        logger.info("Testing connection to Ollama at {}:{}", AppConfig.OLLAMA_HOST, AppConfig.OLLAMA_PORT);
        if (!ollamaClient.testConnection()) {
            logger.error("❌ Failed to connect to Ollama. Make sure Ollama is running at {}:{}",
                    AppConfig.OLLAMA_HOST, AppConfig.OLLAMA_PORT);
            logger.error("To start Ollama, run: ollama serve");
            System.exit(1);
        }
        logger.info("✓ Connected to Ollama successfully");

        // Initialize orchestrator
        AgentOrchestrator orchestrator = new AgentOrchestrator(ragService, config, ollamaClient);

        // Initialize MCP Server if enabled
        if (config.isMcpEnabled()) {
            logger.info("🔧 Initializing MCP Server...");
            initializeMCPServer(config, orchestrator);
        } else {
            logger.info("ℹ️  MCP Server is disabled (set MCP_ENABLED=true to enable)");
        }

        // Command line interface
        if (args.length > 0) {
            // CLI mode: execute with provided arguments
            executeWithArgs(orchestrator, args);
        } else {
            // Interactive mode
            interactiveMode(orchestrator);
        }
    }

    /**
     * Executes with command line arguments
     * Usage: java -jar CodeReviewAgent.jar "review" "/path/to/project"
     */
    private static void executeWithArgs(AgentOrchestrator orchestrator, String[] args) {
        String command = args[0];
        String projectPath = args.length > 1 ? args[1] : System.getProperty("user.dir");

        try {
            logger.info("Executing command: {} on project: {}", command, projectPath);
            orchestrator.executeTask(command, projectPath);
            logger.info("✓ Task completed successfully");
        } catch (Exception e) {
            logger.error("✗ Error executing task: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Interactive CLI mode
     */
    private static void interactiveMode(AgentOrchestrator orchestrator) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   CodeReviewAgent - Interactive Mode   ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        System.out.println("Commands:");
        System.out.println("  review <path>    - Review code in a directory");
        System.out.println("  review           - Review code in current directory");
        System.out.println("  mcp start        - Start MCP server");
        System.out.println("  mcp stop         - Stop MCP server");
        System.out.println("  mcp status       - Check MCP server status");
        System.out.println("  help             - Show this help message");
        System.out.println("  exit             - Exit the application\n");

        boolean running = true;
        while (running) {
            System.out.print("CodeReviewAgent> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "review" -> {
                        String projectPath = parts.length > 1 ? parts[1] : System.getProperty("user.dir");
                        logger.info("Starting code review for: {}", projectPath);
                        orchestrator.executeTask("review code", projectPath);
                        logger.info("✓ Code review completed");
                    }
                    case "mcp" -> handleMCPCommand(parts.length > 1 ? parts[1] : "help");
                    case "help" -> printHelp();
                    case "exit", "quit" -> {
                        System.out.println("Goodbye!");
                        running = false;
                    }
                    default ->
                            System.out.println("Unknown command: " + command + ". Type 'help' for available commands.");
                }
            } catch (Exception e) {
                logger.error("Error executing command: {}", e.getMessage());
                System.out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    /**
     * Prints help information
     */
    private static void printHelp() {
        System.out.println("""
                ╔════════════════════════════════════════╗
                │        CodeReviewAgent Help            │
                ╚════════════════════════════════════════╝
                
                COMMANDS:
                ─────────
                review [path]      Review code in specified directory (default: current)
                mcp start          Start MCP server for external tool access
                mcp stop           Stop running MCP server
                mcp status         Show MCP server status
                help               Show this help message
                exit               Exit the application
                
                EXAMPLES:
                ─────────
                > review /home/user/my-project
                > review
                > mcp start
                > mcp status
                > exit
                
                CONFIGURATION:
                ───────────────
                Set environment variables to configure:
                  EMAIL_ENABLED=true
                  EMAIL_TO=your@email.com
                  SMTP_HOST=smtp.gmail.com
                  SMTP_PORT=587
                  SMTP_USERNAME=your@gmail.com
                  SMTP_PASSWORD=your-app-password
                  MCP_ENABLED=true
                  MCP_PORT=9876
                  MCP_REQUEST_TIMEOUT=30
                  MAX_RETRIES=3
                  THREAD_POOL_SIZE=4
                
                Or create a codereview.properties file with these settings.
                """);
    }

    /**
     * Initialize MCP Server with orchestrator's agents and tools
     */
    private static void initializeMCPServer(AppConfig config, AgentOrchestrator orchestrator) {
        try {
            // Get agents and tools from orchestrator
            ReviewAgent reviewAgent = new ReviewAgent(
                    ragService,
                    new OllamaClient(
                            AppConfig.OLLAMA_HOST,
                            AppConfig.OLLAMA_PORT,
                            AppConfig.OLLAMA_MODEL
                    ),
                    config.getMaxRetries()
            );
            
            FileScannerTool fileScannerTool = new FileScannerTool();
            FileReaderTool fileReaderTool = new FileReaderTool();
            
            // Create MCP tool registry
            MCPToolRegistry toolRegistry = new MCPToolRegistry(
                    reviewAgent,
                    ragService,
                    fileScannerTool,
                    fileReaderTool
            );
            
            // Create and start MCP server
            MCPServer mcpServer = new MCPServer(toolRegistry, config.getMcpPort());
            mcpServerManager = new MCPServerManager(mcpServer);
            mcpServerManager.registerShutdownHook();
            mcpServerManager.startServer();
            
            logger.info("✅ MCP Server initialized on port {}", config.getMcpPort());
            
        } catch (Exception e) {
            logger.error("❌ Failed to initialize MCP Server: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle MCP commands
     */
    private static void handleMCPCommand(String subcommand) {
        try {
            switch (subcommand.toLowerCase()) {
                case "start" -> {
                    if (mcpServerManager != null) {
                        if (mcpServerManager.isRunning()) {
                            System.out.println("✓ MCP Server is already running");
                        } else {
                            mcpServerManager.startServer();
                            System.out.println("✓ MCP Server started successfully");
                        }
                    } else {
                        System.out.println("❌ MCP Server not initialized. Enable MCP_ENABLED=true in config");
                    }
                }
                case "stop" -> {
                    if (mcpServerManager != null && mcpServerManager.isRunning()) {
                        mcpServerManager.stopServer();
                        System.out.println("✓ MCP Server stopped successfully");
                    } else {
                        System.out.println("⚠️  MCP Server is not running");
                    }
                }
                case "status" -> {
                    if (mcpServerManager != null) {
                        System.out.println(mcpServerManager.getStatus());
                    } else {
                        System.out.println("⚠️  MCP Server not initialized");
                    }
                }
                case "test" -> {
                    if (mcpServerManager != null && mcpServerManager.isRunning()) {
                        testMCPServer();
                    } else {
                        System.out.println("❌ MCP Server is not running. Start it first with 'mcp start'");
                    }
                }
                default -> System.out.println("Unknown MCP command. Available: start, stop, status, test");
            }
        } catch (Exception e) {
            logger.error("Error handling MCP command: {}", e.getMessage(), e);
        }
    }

    /**
     * Test MCP server connection
     */
    private static void testMCPServer() {
        try {
            MCPTestClient client = new MCPTestClient("localhost", 9876, 5000);
            System.out.println("🧪 Testing MCP Server connection...\n");
            
            client.connect();
            client.runFullTest();
            client.disconnect();
            
        } catch (Exception e) {
            logger.error("❌ MCP Server test failed: {}", e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }
}
