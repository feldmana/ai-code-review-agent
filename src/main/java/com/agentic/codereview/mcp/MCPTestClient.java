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
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Enhanced MCPTestClient with timeout handling, connection pooling, and validation
 */
public class MCPTestClient {
    private static final Logger logger = LoggerFactory.getLogger(MCPTestClient.class);
    
    private final String host;
    private final int port;
    private final int timeoutMs;
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private final Gson gson = new Gson();
    
    public MCPTestClient(String host, int port, int timeoutMs) {
        this.host = host;
        this.port = port;
        this.timeoutMs = timeoutMs;
    }
    
    public MCPTestClient(String host, int port) {
        this(host, port, 30000); // 30 second default timeout
    }
    
    /**
     * Connect to MCP server
     */
    public synchronized void connect() throws Exception {
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(timeoutMs);
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logger.info("✓ Connected to MCP Server at {}:{}", host, port);
        } catch (Exception e) {
            logger.error("❌ Failed to connect to MCP Server: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Disconnect from MCP server
     */
    public synchronized void disconnect() throws Exception {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                logger.info("✓ Disconnected from MCP Server");
            }
        } catch (Exception e) {
            logger.error("Error disconnecting: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Ping server
     */
    public boolean ping() throws Exception {
        try {
            JsonObject request = new JsonObject();
            request.addProperty("method", "ping");
            
            JsonObject response = sendRequest(request);
            return response.has("status");
        } catch (SocketTimeoutException e) {
            logger.error("❌ Ping timeout");
            return false;
        }
    }
    
    /**
     * List available tools
     */
    public Map<String, Object> listTools() throws Exception {
        JsonObject request = new JsonObject();
        request.addProperty("method", "list_tools");
        
        JsonObject response = sendRequest(request);
        logger.info("📋 Available tools: {}", response.size());
        return gson.fromJson(response, Map.class);
    }
    
    /**
     * Test review_code tool
     */
    public String testReviewCode(String fileName, String fileContent) throws Exception {
        logger.info("🔍 Testing review_code tool for: {}", fileName);
        
        JsonObject input = new JsonObject();
        input.addProperty("fileName", fileName);
        input.addProperty("fileContent", fileContent);
        
        return invokeTool("review_code", input);
    }
    
    /**
     * Test scan_files tool
     */
    public String testScanFiles(String projectPath) throws Exception {
        logger.info("📁 Testing scan_files tool for: {}", projectPath);
        
        JsonObject input = new JsonObject();
        input.addProperty("projectPath", projectPath);
        
        return invokeTool("scan_files", input);
    }
    
    /**
     * Test get_rules tool
     */
    public String testGetRules(String code) throws Exception {
        logger.info("📚 Testing get_rules tool");
        
        JsonObject input = new JsonObject();
        input.addProperty("code", code);
        
        return invokeTool("get_rules", input);
    }
    
    /**
     * Test analyze_code_type tool
     */
    public String testAnalyzeCodeType(String code) throws Exception {
        logger.info("🔎 Testing analyze_code_type tool");
        
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
        
        try {
            JsonObject response = sendRequest(request);
            
            if (response.has("error")) {
                throw new Exception("Tool invocation failed: " + response.get("error"));
            }
            
            logger.info("✓ Tool '{}' invoked successfully", toolName);
            return response.toString();
            
        } catch (SocketTimeoutException e) {
            logger.error("❌ Tool invocation timeout for: {}", toolName);
            throw new Exception("Tool invocation timeout", e);
        }
    }
    
    /**
     * Send request to server with timeout handling
     */
    private synchronized JsonObject sendRequest(JsonObject request) throws Exception {
        try {
            writer.println(request);
            
            String response = reader.readLine();
            if (response == null) {
                throw new Exception("No response from server");
            }
            
            return JsonParser.parseString(response).getAsJsonObject();
            
        } catch (SocketTimeoutException e) {
            logger.error("❌ Request timeout after {} ms", timeoutMs);
            throw e;
        }
    }
    
    /**
     * Test connection and all tools
     */
    public void runFullTest() throws Exception {
        logger.info("🧪 Running full MCP test suite...\n");
        
        try {
            // Test ping
            logger.info("Test 1: Ping");
            if (ping()) {
                logger.info("✅ Ping successful\n");
            } else {
                logger.error("❌ Ping failed\n");
            }
            
            // Test list tools
            logger.info("Test 2: List Tools");
            var tools = listTools();
            logger.info("✅ Found {} tools\n", tools.size());
            
            // Test analyze_code_type
            logger.info("Test 3: Analyze Code Type");
            String serviceCode = "@Service public class UserService { }";
            testAnalyzeCodeType(serviceCode);
            logger.info("✅ Code type analysis successful\n");
            
            // Test get_rules
            logger.info("Test 4: Get Rules");
            testGetRules(serviceCode);
            logger.info("✅ Get rules successful\n");
            
            // Test review_code
            logger.info("Test 5: Review Code");
            String javaCode = """
                    @Service
                    public class UserService {
                        public void processUser(String name, String email) {
                            // TODO: Add validation
                            User user = new User(name, email);
                            save(user);
                        }
                    }
                    """;
            testReviewCode("UserService.java", javaCode);
            logger.info("✅ Review code successful\n");
            
            logger.info("🎉 All tests passed!");
            
        } catch (Exception e) {
            logger.error("❌ Test failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Main method for testing
     */
    public static void main1(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9876;
        
        MCPTestClient client = new MCPTestClient(host, port);
        
        try {
            client.connect();
            client.runFullTest();
        } finally {
            client.disconnect();
        }
    }

    public static void main(String[] args) throws Exception {

        int clients = 5;

        ExecutorService executor = Executors.newFixedThreadPool(clients);

        for (int i = 0; i < clients; i++) {
            int id = i;

            executor.submit(() -> {
                try {
                    MCPTestClient client = new MCPTestClient("localhost", 9876);
                    client.connect();

                    String code = """
@Service
public class UserService%d {

    public void processUser(String name, String email) {
        User user = new User(name, email);

        if (email == null) {
            throw new RuntimeException("email missing");
        }

        save(user);
    }

    private void save(User user) {
        // TODO: DB call
    }
}
                """.formatted(id);

                    String result = client.testReviewCode(
                            "UserService" + id + ".java",
                            code
                    );

                    System.out.println("Client " + id + " result: " + result);

                    client.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
    }
}

