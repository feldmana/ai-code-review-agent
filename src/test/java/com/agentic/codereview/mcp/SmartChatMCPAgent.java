package com.agentic.codereview.mcp;

import com.google.gson.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class SmartChatMCPAgent {

    private final MCPClient client = new MCPClient("localhost", 9876);
    private final Gson gson = new Gson();

    public void start() throws Exception {
        client.connect();

        Scanner scanner = new Scanner(System.in);

        System.out.println("🧠 Smart MCP Chat Ready");

        while (true) {
            System.out.print("\nYou: ");
            String userInput = scanner.nextLine();

            if ("exit".equalsIgnoreCase(userInput)) break;

            String tool = chooseToolWithLLM(userInput);
            System.out.println("🧭 Chosen tool: " + tool);

            String result = execute(tool, userInput);

            System.out.println("\n🤖 Result:\n" + result);
        }

        client.disconnect();
    }

    /**
     * 🔥 THIS is the REAL MCP idea:
     * LLM decides routing
     */
    private String chooseToolWithLLM(String userInput) throws Exception {

        String prompt =
                "You are a router.\n" +
                        "Choose ONE tool only:\n" +
                        "- review_code\n" +
                        "- analyze_code_type\n" +
                        "- get_rules\n" +
                        "- both\n\n" +
                        "Return ONLY JSON like:\n" +
                        "{\"tool\":\"review_code\"}\n\n" +
                        "User request:\n" + userInput;

        String jsonResponse = callOllama(prompt);

        JsonObject obj = JsonParser.parseString(jsonResponse).getAsJsonObject();

        return obj.get("tool").getAsString();
    }

    private String callOllama(String userMessage) throws Exception {

        URL url = new URL("http://localhost:11434/api/chat");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JsonObject body = new JsonObject();
        body.addProperty("model", "llama3");
        body.addProperty("stream", false);

        JsonArray messages = new JsonArray();

        JsonObject msg = new JsonObject();
        msg.addProperty("role", "user");
        msg.addProperty("content", userMessage);

        messages.add(msg);
        body.add("messages", messages);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes());
        }

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new RuntimeException("Ollama error code: " + code);
        }

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        JsonObject response = JsonParser.parseString(sb.toString()).getAsJsonObject();

        // IMPORTANT: correct field for /api/chat
        return response
                .getAsJsonObject("message")
                .get("content")
                .getAsString();
    }
    /**
     * 🔥 MCP execution layer
     */
    private String execute(String tool, String input) throws Exception {

        return switch (tool) {
            case "review_code" ->
                    client.reviewCode("UserService.java", input);

            case "analyze_code_type" ->
                    client.analyzeCodeType(input);

            case "get_rules" ->
                    client.getRules(input);

            case "both" -> {
                String r1 = client.reviewCode("UserService.java", input);
                String r2 = client.analyzeCodeType(input);
                yield r1 + "\n\n---\n\n" + r2;
            }

            default -> "Unknown tool: " + tool;
        };
    }

    public static void main(String[] args) throws Exception {
        new SmartChatMCPAgent().start();
    }

    public String chooseToolWithContext(String userInput, String projectPath) throws Exception {

        String prompt =
                "You are a coding assistant router.\n" +
                        "Decide what action to take.\n\n" +
                        "Available actions:\n" +
                        "- review\n" +
                        "- analyze\n" +
                        "- rules\n" +
                        "- scan\n" +
                        "- full\n\n" +
                        "Project path:\n" + projectPath + "\n\n" +
                        "User request:\n" + userInput + "\n\n" +
                        "Return ONLY one word.";

        String raw = callOllama(prompt);

        return raw.trim().toLowerCase();
    }
}