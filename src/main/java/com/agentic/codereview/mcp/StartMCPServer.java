package com.agentic.codereview.mcp;

import com.agentic.codereview.agent.ReviewAgent;
import com.agentic.codereview.config.AppConfig;
import com.agentic.codereview.llm.OllamaClient;
import com.agentic.codereview.rag.EnhancedVectorRagService;
import com.agentic.codereview.rag.RagService;
import com.agentic.codereview.tool.FileReaderTool;
import com.agentic.codereview.tool.FileScannerTool;

public class StartMCPServer {

public static void main(String[] args) throws Exception {
    // Initialize services
    String ragPath = "rag-docs";
    RagService ragService = new EnhancedVectorRagService(ragPath, 5);
    // Initialize Ollama client
    OllamaClient ollamaClient = new OllamaClient(
            AppConfig.OLLAMA_HOST,
            AppConfig.OLLAMA_PORT,
            AppConfig.OLLAMA_MODEL
    );

    ReviewAgent reviewAgent = new ReviewAgent(ragService, ollamaClient, 3);

    MCPToolRegistry registry = new MCPToolRegistry(
            reviewAgent,
            ragService,
            new FileScannerTool(),
            new FileReaderTool()
    );

    MCPServer server = new MCPServer(registry, 9876);
    MCPServerManager manager = new MCPServerManager(server);

    manager.startServer();
}
}
