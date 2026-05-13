package com.agentic.codereview.mcp;

import com.agentic.codereview.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MCPServerManager
 * 
 * Manages the lifecycle of the MCP Server including:
 * - Server startup and shutdown
 * - Graceful termination
 * - Health checks
 * - Connection pooling
 */
public class MCPServerManager {
    private static final Logger logger = LoggerFactory.getLogger(MCPServerManager.class);
    
    private final MCPServer mcpServer;
    private final ExecutorService serverExecutor;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final int gracefulShutdownTimeoutSeconds = 30;
    
    public MCPServerManager(MCPServer mcpServer) {
        this.mcpServer = mcpServer;
        this.serverExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "MCP-Server-Thread");
            t.setDaemon(false);
            return t;
        });
    }
    
    /**
     * Start MCP server in background thread
     */
    public synchronized void startServer() {
        if (isRunning.get()) {
            logger.warn("⚠️ MCP Server is already running");
            return;
        }
        
        try {
            logger.info("🚀 Starting MCP Server on port {}...", mcpServer.getPort());
            
            serverExecutor.submit(() -> {
                try {
                    mcpServer.start();
                    isRunning.set(true);
                    logger.info("✅ MCP Server started successfully");
                    
                    // Keep the server running
                    Thread.currentThread().join();
                } catch (InterruptedException e) {
                    logger.debug("MCP Server interrupted");
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error("❌ MCP Server error: {}", e.getMessage(), e);
                    isRunning.set(false);
                }
            });
            
            // Wait for server to be ready
            waitForServerReady();
            
        } catch (Exception e) {
            logger.error("❌ Failed to start MCP Server: {}", e.getMessage(), e);
            isRunning.set(false);
        }
    }
    
    /**
     * Stop MCP server gracefully
     */
    public synchronized void stopServer() {
        if (!isRunning.get()) {
            logger.warn("⚠️ MCP Server is not running");
            return;
        }
        
        try {
            logger.info("🛑 Stopping MCP Server...");
            mcpServer.stop();
            isRunning.set(false);
            
            // Shutdown executor with graceful timeout
            serverExecutor.shutdown();
            if (!serverExecutor.awaitTermination(gracefulShutdownTimeoutSeconds, TimeUnit.SECONDS)) {
                logger.warn("⚠️ Executor did not terminate gracefully, forcing shutdown");
                serverExecutor.shutdownNow();
            }
            
            logger.info("✅ MCP Server stopped successfully");
        } catch (Exception e) {
            logger.error("❌ Error stopping MCP Server: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return isRunning.get() && mcpServer.isRunning();
    }
    
    /**
     * Wait for server to be ready
     */
    private void waitForServerReady() {
        int maxAttempts = 10;
        int attempts = 0;
        
        while (attempts < maxAttempts && !isRunning.get()) {
            try {
                Thread.sleep(500);
                attempts++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        if (!isRunning.get()) {
            logger.warn("⚠️ MCP Server may not have started within timeout");
        }
    }
    
    /**
     * Register shutdown hook for graceful termination
     */
    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("🔴 Shutdown signal received");
            stopServer();
        }, "MCP-Shutdown-Hook"));
        logger.debug("✓ MCP Server shutdown hook registered");
    }
    
    /**
     * Get server status
     */
    public String getStatus() {
        return String.format(
            "MCP Server Status:\n" +
            "  Running: %s\n" +
            "  Port: %d\n" +
            "  Executor: %s",
            isRunning() ? "✓ Yes" : "✗ No",
            mcpServer.getPort(),
            serverExecutor.isShutdown() ? "Shutdown" : "Active"
        );
    }
}

