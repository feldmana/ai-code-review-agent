package com.agentic.codereview.mcp;

import java.util.Scanner;

public class SmartChatMain {

    private static final String PROJECT_PATH = "/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/src";

    public static void main(String[] args) throws Exception {

        MCPClient client = new MCPClient("localhost", 9876);
        SmartChatMCPAgent agent = new SmartChatMCPAgent();

        client.connect();

        Scanner scanner = new Scanner(System.in);

        System.out.println("🧠 MCP Chat Agent Ready");
        System.out.println("Say: review / analyze / rules / explain / full review");
        System.out.println("Type exit to quit\n");

        while (true) {

            System.out.print("\nYou: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("exit")) break;

            // 🔥 IMPORTANT: give LLM context
            String decision = agent.chooseToolWithContext(input, PROJECT_PATH);

            System.out.println("🧭 Decision: " + decision);

            String result = execute(client, decision, PROJECT_PATH, input);

            System.out.println("\n🤖 Result:\n" + result);
        }

        client.disconnect();
    }

    private static String execute(MCPClient client,
                                  String decision,
                                  String projectPath,
                                  String input) throws Exception {

        return switch (decision) {

            case "review" ->
                    client.reviewCode(projectPath, input);

            case "analyze" ->
                    client.analyzeCodeType(input);

            case "rules" ->
                    client.getRules(input);

            case "scan" ->
                    client.scanFiles(projectPath);

            case "full" -> {
                String scan = client.scanFiles(projectPath);
                String review = client.reviewCode(projectPath, scan);
                String rules = client.getRules(scan);

                yield review + "\n\n--- RULES ---\n" + rules;
            }

            default -> "Unknown decision: " + decision;
        };
    }
}