package com.agentic.codereview.mcp;

import java.util.Scanner;

public class ReasoningMCPRunner {

    private final MCPClient client;
    private final SimpleOllamaRouter router;

    public ReasoningMCPRunner(MCPClient client) {
        this.client = client;
        this.router = new SimpleOllamaRouter();
    }

    // ----------------------------
    // EXECUTE TOOL
    // ----------------------------
    private String execute1(String tool, String code) throws Exception {

        return switch (tool) {
            case "review_code" -> client.reviewCode("UserService.java", code);
            case "analyze_code_type" -> client.analyzeCodeType(code);
            case "get_rules" -> client.getRules(code);
            case "both" -> {
                String r1 = client.reviewCode("UserService.java", code);
                String r2 = client.analyzeCodeType(code);
                yield r1 + "\n\n---\n\n" + r2;
            }
            default -> throw new IllegalStateException("Unknown tool: " + tool);
        };
    }


    private String execute(String tool, String code) throws Exception {

        return switch (tool) {

            case "review_code" ->
                    client.reviewCode("UserService.java", code);

            case "analyze_code_type" ->
                    client.analyzeCodeType(code);

            case "get_rules" ->
                    client.getRules(code);

            case "both" -> {
                String r1 = client.reviewCode("UserService.java", code);
                String r2 = client.analyzeCodeType(code);
                yield r1 + "\n\n---\n\n" + r2;
            }

            default -> throw new IllegalStateException("Unknown tool: " + tool);
        };
    }
    // ----------------------------
    // MAIN LOOP (NO MENU)
    // ----------------------------
    public void start() throws Exception {

        Scanner scanner = new Scanner(System.in);

        System.out.println("🧠 MCP Reasoning Agent");
        System.out.println("Type your request (or 'exit')\n");

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

            System.out.print("\nYou: ");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("exit")) break;

            // 🧠 THIS IS THE REASONING STEP
            String tool = router.chooseTool(userInput);

            System.out.println("🤖 Selected tool: " + tool);

            String result = execute(tool, code);

            System.out.println("\n📦 RESULT:\n" + result);
        }

        scanner.close();
        client.disconnect();
    }

    // ----------------------------
    // MAIN
    // ----------------------------
    public static void main(String[] args) throws Exception {

        MCPClient client = new MCPClient("localhost", 9876);
        client.connect();

        new ReasoningMCPRunner(client).start();
    }
}