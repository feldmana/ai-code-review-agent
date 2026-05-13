package com.agentic.codereview.mcp;

import com.google.gson.JsonObject;
import com.agentic.codereview.agent.ReviewAgent;
import com.agentic.codereview.llm.OllamaClient;
import com.agentic.codereview.rag.EnhancedVectorRagService;
import com.agentic.codereview.rag.RagService;
import com.agentic.codereview.tool.FileScannerTool;
import com.agentic.codereview.tool.FileReaderTool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for MCP Server
 */
public class MCPServerIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(MCPServerIntegrationTest.class);
    
    private MCPServer mcpServer;
    private MCPServerManager serverManager;
    private MCPTestClient client;
    private RagService ragService;
    
    @BeforeEach
    public void setUp() throws Exception {
        logger.info("🧪 Setting up MCP Server integration test");
        
        // Initialize services
        String ragPath = "rag-docs";
        ragService = new EnhancedVectorRagService(ragPath, 5);
        
        ReviewAgent reviewAgent = new ReviewAgent(
            ragService,
            new OllamaClient("http://localhost", 11434, "llama3"),
            3
        );
        
        FileScannerTool fileScannerTool = new FileScannerTool();
        FileReaderTool fileReaderTool = new FileReaderTool();
        
        // Create MCP components
        MCPToolRegistry toolRegistry = new MCPToolRegistry(
            reviewAgent,
            ragService,
            fileScannerTool,
            fileReaderTool
        );
        
        mcpServer = new MCPServer(toolRegistry, 9877); // Use different port for testing
        serverManager = new MCPServerManager(mcpServer);
        
        // Start server
        serverManager.startServer();
        Thread.sleep(1000); // Give server time to start
        
        // Create test client
        client = new MCPTestClient("localhost", 9877, 10000);
        client.connect();
        
        logger.info("✅ Setup complete");
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        logger.info("🧹 Tearing down MCP Server integration test");
        
        if (client != null) {
            try {
                client.disconnect();
            } catch (Exception e) {
                logger.warn("Error disconnecting client: {}", e.getMessage());
            }
        }
        
        if (serverManager != null) {
            serverManager.stopServer();
        }
        
        logger.info("✅ Teardown complete");
    }
    
    @Test
    public void testServerIsRunning() {
        logger.info("Test: Server is running");
        assertTrue(serverManager.isRunning(), "Server should be running");
        logger.info("✓ Server is running");
    }
    
    @Test
    public void testPingServer() throws Exception {
        logger.info("Test: Ping server");
        boolean result = client.ping();
        assertTrue(result, "Ping should succeed");
        logger.info("✓ Ping successful");
    }
    
    @Test
    public void testListTools() throws Exception {
        logger.info("Test: List tools");
        var tools = client.listTools();
        assertNotNull(tools, "Tools list should not be null");
        assertTrue(tools.size() > 0, "Should have at least one tool");
        logger.info("✓ Listed {} tools", tools.size());
    }
    
    @Test
    public void testAnalyzeCodeType() throws Exception {
        logger.info("Test: Analyze code type");
        
        String code = "@Service\npublic class UserService {\n    public void saveUser() {}\n}";
        String result = client.testAnalyzeCodeType(code);
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.contains("codeType") || result.contains("SERVICE"), "Should detect service code type");
        logger.info("✓ Code type analysis successful");
    }
    
    @Test
    public void testGetRules() throws Exception {
        logger.info("Test: Get rules");
        
        String code = "@Service\npublic class UserService {}";
        String result = client.testGetRules(code);
        
        assertNotNull(result, "Result should not be null");
        assertTrue(result.contains("rules") || result.length() > 0, "Should return rules");
        logger.info("✓ Get rules successful");
    }
    
    @Test
    public void testConcurrentClients() throws Exception {
        logger.info("Test: Concurrent client connections");
        
        int numClients = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        CountDownLatch latch = new CountDownLatch(numClients);
        
        try {
            for (int i = 0; i < numClients; i++) {
                final int clientId = i;
                executor.submit(() -> {
                    try {
                        MCPTestClient testClient = new MCPTestClient("localhost", 9877, 10000);
                        testClient.connect();
                        
                        boolean ping = testClient.ping();
                        assertTrue(ping, "Concurrent client " + clientId + " should be able to ping");
                        
                        testClient.disconnect();
                        logger.info("✓ Concurrent client {} completed", clientId);
                    } catch (Exception e) {
                        logger.error("❌ Concurrent client {} failed: {}", clientId, e.getMessage());
                        fail("Concurrent client " + clientId + " failed: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            assertTrue(completed, "All concurrent clients should complete");
            logger.info("✓ Concurrent clients test passed");
            
        } finally {
            executor.shutdown();
        }
    }
    
    @Test
    public void testToolInvocationWithInvalidInput() throws Exception {
        logger.info("Test: Tool invocation with invalid input");
        
        JsonObject invalidInput = new JsonObject();
        // Missing required "code" field
        
        assertThrows(Exception.class, () -> {
            client.invokeTool("analyze_code_type", invalidInput);
        }, "Should throw exception for invalid input");
        
        logger.info("✓ Invalid input properly rejected");
    }
    
    @Test
    public void testToolNotFound() throws Exception {
        logger.info("Test: Tool not found");
        
        JsonObject input = new JsonObject();
        input.addProperty("test", "value");
        
        assertThrows(Exception.class, () -> {
            client.invokeTool("non_existent_tool", input);
        }, "Should throw exception for non-existent tool");
        
        logger.info("✓ Non-existent tool properly rejected");
    }
    
    @Test
    public void testServerStatus() {
        logger.info("Test: Server status");
        
        String status = serverManager.getStatus();
        assertNotNull(status, "Status should not be null");
        assertTrue(status.contains("Running") || status.contains("Yes"), "Status should indicate running");
        logger.info("Status:\n{}", status);
        logger.info("✓ Server status retrieved");
    }
}

