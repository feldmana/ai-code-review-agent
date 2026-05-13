package com.agentic.codereview.mcp;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

/**
 * MCP Tool Definition
 * Represents a tool that Claude/other models can invoke
 */
public class MCPTool {
    private String name;
    private String description;
    private Map<String, Object> inputSchema;
    private MCPToolHandler handler;

    public MCPTool(String name, String description, Map<String, Object> inputSchema, MCPToolHandler handler) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
        this.handler = handler;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }

    public MCPToolHandler getHandler() {
        return handler;
    }

    /**
     * Invoke the tool with given input
     */
    public String invoke(JsonObject input) throws Exception {
        return handler.execute(input);
    }

    /**
     * Functional interface for tool handlers
     */
    @FunctionalInterface
    public interface MCPToolHandler {
        String execute(JsonObject input) throws Exception;
    }

    /**
     * Builder for MCPTool
     */
    public static class Builder {
        private String name;
        private String description;
        private Map<String, Object> inputSchema = new HashMap<>();
        private MCPToolHandler handler;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder inputSchema(Map<String, Object> schema) {
            this.inputSchema = schema;
            return this;
        }

        public Builder handler(MCPToolHandler handler) {
            this.handler = handler;
            return this;
        }

        public MCPTool build() {
            if (name == null || description == null || handler == null) {
                throw new IllegalArgumentException("name, description, and handler are required");
            }
            return new MCPTool(name, description, inputSchema, handler);
        }
    }
}

