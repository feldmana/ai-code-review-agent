package com.agentic.codereview.mcp;

import java.util.Scanner;

public class InteractiveMCPRunner {

    private final MCPClient client;

    public InteractiveMCPRunner(MCPClient client) {
        this.client = client;
    }

    // ---------------------------
    // TOOL EXECUTION
    // ---------------------------
    private String runTool(String tool, String code) throws Exception {

        return switch (tool) {
            case "review" -> client.reviewCode("UserService.java", code);
            case "analyze" -> client.analyzeCodeType(code);
            case "rules" -> client.getRules(code);
            default -> throw new IllegalArgumentException("Unknown tool: " + tool);
        };
    }

    // ---------------------------
    // MAIN LOOP
    // ---------------------------
    public void start() throws Exception {

        Scanner scanner = new Scanner(System.in);

        System.out.println("🚀 MCP Interactive Runner");
        System.out.println("Type code or 'exit' to quit\n");

        String code = """
                @Service
                public class UserService {

                    public void processUser(String name, String email) {
                        if (email == null) {
                            throw new RuntimeException("email missing");
                        }

                        User user = new User(name, email);
                        save(user);
                    }

                    private void save(User user) {}
                }
                """;

        while (true) {

            System.out.println("\nChoose mode:");
            System.out.println("1 - review");
            System.out.println("2 - analyze");
            System.out.println("3 - rules");
            System.out.println("4 - both (review + analyze)");
            System.out.print("Your choice: ");

            String choice = scanner.nextLine();

            if (choice.equals("exit")) break;

            switch (choice) {

                case "1" -> {
                    System.out.println("👉 Running REVIEW...");
                    System.out.println(runTool("review", code));
                }

                case "2" -> {
                    System.out.println("👉 Running ANALYZE...");
                    System.out.println(runTool("analyze", code));
                }

                case "3" -> {
                    System.out.println("👉 Running RULES...");
                    System.out.println(runTool("rules", code));
                }

                case "4" -> {
                    System.out.println("👉 Running BOTH...");

                    System.out.println("\n--- REVIEW ---");
                    System.out.println(runTool("review", code));

                    System.out.println("\n--- ANALYZE ---");
                    System.out.println(runTool("analyze", code));
                }

                default -> System.out.println("❌ Unknown option");
            }
        }

        scanner.close();
        client.disconnect();
    }

    // ---------------------------
    // MAIN
    // ---------------------------
    public static void main(String[] args) throws Exception {

        MCPClient client = new MCPClient("localhost", 9876);
        client.connect();

        new InteractiveMCPRunner(client).start();
    }
}