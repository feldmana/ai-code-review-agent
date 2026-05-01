package com.agentic.codereview;

import com.agentic.codereview.config.AppConfig;
import com.agentic.codereview.llm.OllamaClient;
import com.agentic.codereview.orchestrator.AgentOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Main entry point for the CodeReviewAgent CLI application
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("=================================");
        logger.info("  CodeReviewAgent v1.0");
        logger.info("  AI-Powered Code Review System");
        logger.info("=================================");

        // Load configuration
        AppConfig config = AppConfig.getInstance();
        logger.info("Configuration loaded: {}", config);

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
        AgentOrchestrator orchestrator = new AgentOrchestrator(config, ollamaClient);

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
                    case "help" -> printHelp();
                    case "exit", "quit" -> {
                        System.out.println("Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Unknown command: " + command + ". Type 'help' for available commands.");
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
                help               Show this help message
                exit               Exit the application
                
                EXAMPLES:
                ─────────
                > review /home/user/my-project
                > review
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
                  MAX_RETRIES=3
                  THREAD_POOL_SIZE=4
                
                Or create a codereview.properties file with these settings.
                """);
    }
}

