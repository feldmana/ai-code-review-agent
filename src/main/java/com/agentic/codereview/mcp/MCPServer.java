package com.agentic.codereview.mcp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 * MCP Server for CodeReviewAgent
 * Allows Claude and other AI models to invoke code review tools via MCP protocol
 */
public class MCPServer {
    private static final Logger logger = LoggerFactory.getLogger(MCPServer.class);
    
    private final MCPToolRegistry toolRegistry;
    private final int port;
    private ServerSocket serverSocket;
    private boolean running = false;

    public MCPServer(MCPToolRegistry toolRegistry, int port) {
        this.toolRegistry = toolRegistry;
        this.port = port;
    }

    /**
     * Start the MCP server
     */
    public void start() throws Exception {
        serverSocket = new ServerSocket(port);
        running = true;
        
        logger.info("🚀 MCP Server started on port {}", port);
        logger.info("📋 Available tools: {}", toolRegistry.getToolNames());
        
        // Accept connections in a new thread
        new Thread(() -> {
            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    new Thread(() -> handleClient(client)).start();
                } catch (Exception e) {
                    if (running) {
                        logger.error("❌ Server error", e);
                    }
                }
            }
        }).start();
    }

    /**
     * Stop the MCP server
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            logger.error("Error stopping server", e);
        }
        logger.info("✓ MCP Server stopped");
    }

    /**
     * Handle client connection.
     * No socket timeout — LLM calls (Ollama) can take well over 30s per file.
     */
    private void handleClient(Socket client) {
        try {
            try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true)
            ) {
                logger.info("📡 New MCP client connected from: {}", client.getInetAddress().getHostAddress());

                String line;
                while ((line = reader.readLine()) != null && running) {
                    try {
                        if (line.trim().isEmpty()) {
                            continue;
                        }

                        JsonObject request = JsonParser.parseString(line).getAsJsonObject();
                        JsonObject response = handleRequest(request);
                        writer.println(response.toString());

                    } catch (com.google.gson.JsonSyntaxException e) {
                        logger.warn("❌ Invalid JSON received: {}", e.getMessage());
                        JsonObject error = createError("Invalid JSON: " + e.getMessage());
                        writer.println(error.toString());
                    } catch (Exception e) {
                        logger.error("❌ Error processing request: {}", e.getMessage(), e);
                        JsonObject error = createError("Request processing error: " + e.getMessage());
                        writer.println(error.toString());
                    }
                }

                logger.info("📡 Client disconnected: {}", client.getInetAddress().getHostAddress());
            }
        } catch (Exception e) {
            logger.error("❌ Client connection error: {}", e.getMessage(), e);
        } finally {
            try {
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (Exception e) {
                logger.debug("Error closing client socket", e);
            }
        }
    }

    /**
     * Handle MCP request
     */
    private JsonObject handleRequest(JsonObject request) {
        String method = request.has("method") ? request.get("method").getAsString() : null;
        
        return switch(method) {
            case "list_tools" -> handleListTools();
            case "get_tool" -> handleGetTool(request);
            case "invoke_tool" -> handleInvokeTool(request);
            case "ping" -> handlePing();
            default -> createError("Unknown method: " + method);
        };
    }

    /**
     * Handle list_tools request
     */
    private JsonObject handleListTools() {
        JsonObject response = new JsonObject();
        
        var toolsJson = new com.google.gson.JsonArray();
        for (var tool : toolRegistry.getAllTools().values()) {
            var toolJson = new JsonObject();
            toolJson.addProperty("name", tool.getName());
            toolJson.addProperty("description", tool.getDescription());
            
            var schemaJson = com.google.gson.JsonParser.parseString(
                new com.google.gson.Gson().toJson(tool.getInputSchema())
            );
            toolJson.add("schema", schemaJson);
            
            toolsJson.add(toolJson);
        }
        
        response.add("tools", toolsJson);
        logger.debug("✓ Listed {} tools", toolsJson.size());
        return response;
    }

    /**
     * Handle get_tool request
     */
    private JsonObject handleGetTool(JsonObject request) {
        String toolName = request.get("toolName").getAsString();
        MCPTool tool = toolRegistry.getTool(toolName);
        
        if (tool == null) {
            return createError("Tool not found: " + toolName);
        }
        
        JsonObject response = new JsonObject();
        response.addProperty("name", tool.getName());
        response.addProperty("description", tool.getDescription());
        
        var schemaJson = com.google.gson.JsonParser.parseString(
            new com.google.gson.Gson().toJson(tool.getInputSchema())
        );
        response.add("schema", schemaJson);
        
        logger.debug("✓ Retrieved tool: {}", toolName);
        return response;
    }

    /**
     * Handle invoke_tool request
     */
    private JsonObject handleInvokeTool(JsonObject request) {
        String toolName = request.get("toolName").getAsString();
        JsonObject input = request.getAsJsonObject("input");
        
        try {
            String result = toolRegistry.invokeTool(toolName, input);
            JsonObject response = new JsonObject();
            
            // Parse result if it's JSON
            try {
                response.add("result", com.google.gson.JsonParser.parseString(result));
            } catch (Exception e) {
                response.addProperty("result", result);
            }
            
            logger.info("✓ Tool invoked successfully: {}", toolName);
            return response;
        } catch (Exception e) {
            logger.error("❌ Tool invocation failed: {}", toolName, e);
            return createError("Tool invocation failed: " + e.getMessage());
        }
    }

    /**
     * Handle ping request
     */
    private JsonObject handlePing() {
        JsonObject response = new JsonObject();
        response.addProperty("status", "pong");
        response.addProperty("timestamp", System.currentTimeMillis());
        logger.debug("✓ Ping received");
        return response;
    }

    /**
     * Create error response
     */
    private JsonObject createError(String message) {
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        logger.warn("⚠️ Error: {}", message);
        return error;
    }

    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Get server port
     */
    public int getPort() {
        return port;
    }


}

