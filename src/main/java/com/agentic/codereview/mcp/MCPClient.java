package com.agentic.codereview.mcp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * MCP Client for testing and interacting with CodeReviewAgent MCP Server
 */
public class MCPClient {
    private static final Logger logger = LoggerFactory.getLogger(MCPClient.class);
    
    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private final Gson gson = new Gson();

    public MCPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Connect to MCP server
     */
    public void connect() throws Exception {
        socket = new Socket(host, port);
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        logger.info("✓ Connected to MCP Server at {}:{}", host, port);
    }

    /**
     * Close connection
     */
    public void disconnect() throws Exception {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        logger.info("✓ Disconnected from MCP Server");
    }

    /**
     * List available tools
     */
    public List<String> listTools() throws Exception {
        JsonObject request = new JsonObject();
        request.addProperty("method", "list_tools");

        JsonObject response = sendRequest(request);
        logger.info("📋 Available tools: {}", response);

        List<String> names = new java.util.ArrayList<>();
        if (response.has("tools")) {
            for (var element : response.getAsJsonArray("tools")) {
                names.add(element.getAsJsonObject().get("name").getAsString());
            }
        }
        return names;
    }

    /**
     * Review code using MCP tool
     */
    public String reviewCode(String fileName, String fileContent) throws Exception {
        JsonObject input = new JsonObject();
        input.addProperty("fileName", fileName);
        input.addProperty("fileContent", fileContent);
        
        return invokeTool("review_code", input);
    }

    /**
     * Scan project using MCP tool
     */
    public String scanFiles(String projectPath) throws Exception {
        JsonObject input = new JsonObject();
        input.addProperty("projectPath", projectPath);
        
        return invokeTool("scan_files", input);
    }

    /**
     * Get relevant rules using MCP tool
     */
    public String getRules(String code) throws Exception {
        JsonObject input = new JsonObject();
        input.addProperty("code", code);
        
        return invokeTool("get_rules", input);
    }

    /**
     * Analyze code type using MCP tool
     */
    public String analyzeCodeType(String code) throws Exception {
        JsonObject input = new JsonObject();
        input.addProperty("code", code);
        
        return invokeTool("analyze_code_type", input);
    }

    /**
     * Invoke a tool
     */
    public String invokeTool(String toolName, JsonObject input) throws Exception {
        JsonObject request = new JsonObject();
        request.addProperty("method", "invoke_tool");
        request.addProperty("toolName", toolName);
        request.add("input", input);
        
        JsonObject response = sendRequest(request);
        logger.info("✓ Tool '{}' invoked successfully", toolName);
        
        return response.toString();
    }

    /**
     * Send request to server
     */
    private JsonObject sendRequest(JsonObject request) throws Exception {
        writer.println(request);
        String response = reader.readLine();
        
        if (response == null) {
            throw new Exception("No response from server");
        }
        
        return JsonParser.parseString(response).getAsJsonObject();
    }

    /**
     * Ping server
     */
    public boolean ping() throws Exception {
        JsonObject request = new JsonObject();
        request.addProperty("method", "ping");
        
        JsonObject response = sendRequest(request);
        return response.has("status");
    }

    public static void main(String[] args) throws Exception {
        MCPClient client = new MCPClient("localhost", 9876);
        
        try {
            client.connect();
            
            // Test ping
            logger.info("🔔 Testing ping...");
            if (client.ping()) {
                logger.info("✓ Server is alive");
            }
            
            // Test list tools
            logger.info("📋 Listing tools...");
            client.listTools();
            
            // Test analyze code type
            logger.info("🔎 Analyzing code type...");
            String result = client.analyzeCodeType("@Service public class UserService { }");
            logger.info("Result: {}", result);
            
        } finally {
            client.disconnect();
        }
    }
}

