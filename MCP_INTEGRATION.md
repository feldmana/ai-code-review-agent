# MCP Integration Guide

## Overview

The CodeReviewAgent now integrates with the **Model Context Protocol (MCP)**, enabling Claude and other AI models to invoke code review tools as a remote server.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Claude / AI Model                        │
│                   (via MCP Client)                          │
└────────────────────────┬────────────────────────────────────┘
                         │ MCP Protocol
                         │ (JSON over TCP)
┌────────────────────────▼────────────────────────────────────┐
│              CodeReviewAgent MCP Server                      │
│              (TCP Server on port 9876)                      │
├─────────────────────────────────────────────────────────────┤
│  MCPToolRegistry                                             │
│  ├── review_code         → ReviewAgent                      │
│  ├── scan_files          → FileScannerTool                  │
│  ├── get_rules           → RagService                       │
│  └── analyze_code_type   → Code Type Analyzer               │
├─────────────────────────────────────────────────────────────┤
│              CodeReviewAgent Backend                         │
│              (Ollama + RAG + Services)                      │
└─────────────────────────────────────────────────────────────┘
```

## Configuration

### Enable MCP in codereview.properties

```properties
# MCP Configuration
MCP_ENABLED=true
MCP_PORT=9876
MCP_REQUEST_TIMEOUT=30
MCP_CONNECTION_POOL_SIZE=10
```

Or use environment variables:
```bash
export MCP_ENABLED=true
export MCP_PORT=9876
export MCP_REQUEST_TIMEOUT=30
export MCP_CONNECTION_POOL_SIZE=10
```

## Starting the MCP Server

### Option 1: Via CLI

```bash
# Start the application in interactive mode
java -jar CodeReviewAgent.jar

# Then use commands
CodeReviewAgent> mcp start
CodeReviewAgent> mcp status
CodeReviewAgent> mcp test
CodeReviewAgent> mcp stop
```

### Option 2: Via CLI Arguments

```bash
# Review code first
java -jar CodeReviewAgent.jar review /path/to/project

# Then start MCP server in background (if configured)
```

### Option 3: Programmatically

```java
AppConfig config = AppConfig.getInstance();
if (config.isMcpEnabled()) {
    // MCP server will start automatically
}
```

## Available Tools

### 1. review_code

Reviews a single Java file using AI-powered analysis and RAG.

**Input Schema:**
```json
{
  "fileName": "string (required)",
  "fileContent": "string (required)",
  "projectPath": "string (optional)"
}
```

**Response:**
```json
{
  "fileName": "UserService.java",
  "issuesCount": 3,
  "severity": "MEDIUM",
  "issues": [...],
  "suggestions": [...]
}
```

**Example Request:**
```json
{
  "method": "invoke_tool",
  "toolName": "review_code",
  "input": {
    "fileName": "UserService.java",
    "fileContent": "@Service\npublic class UserService { ... }"
  }
}
```

### 2. scan_files

Scans a project directory and finds all Java source files.

**Input Schema:**
```json
{
  "projectPath": "string (required)"
}
```

**Response:**
```json
{
  "projectPath": "/home/user/project",
  "filesFound": 25,
  "files": ["UserService.java", "UserRepository.java", ...]
}
```

**Example Request:**
```json
{
  "method": "invoke_tool",
  "toolName": "scan_files",
  "input": {
    "projectPath": "/home/user/project"
  }
}
```

### 3. get_rules

Retrieves relevant coding rules for a code snippet using the RAG system.

**Input Schema:**
```json
{
  "code": "string (required)"
}
```

**Response:**
```json
{
  "rulesCount": 5,
  "rules": [
    "Use dependency injection for services",
    "Handle null values explicitly",
    ...
  ]
}
```

### 4. analyze_code_type

Detects the type of Java code (Service, Controller, Repository, Entity, etc.).

**Input Schema:**
```json
{
  "code": "string (required)"
}
```

**Response:**
```json
{
  "codeType": "SERVICE",
  "applicableRules": [
    "Microservices Design",
    "Single Responsibility Principle",
    ...
  ]
}
```

## Testing the MCP Server

### 1. Using CLI

```bash
CodeReviewAgent> mcp start
CodeReviewAgent> mcp test
CodeReviewAgent> mcp status
```

### 2. Using MCPTestClient

```java
MCPTestClient client = new MCPTestClient("localhost", 9876);
client.connect();
client.runFullTest();
client.disconnect();
```

### 3. Using curl (Raw TCP)

```bash
# Ping the server
echo '{"method":"ping"}' | nc localhost 9876

# List available tools
echo '{"method":"list_tools"}' | nc localhost 9876

# Invoke a tool
echo '{
  "method":"invoke_tool",
  "toolName":"analyze_code_type",
  "input":{"code":"@Service public class UserService {}"}
}' | nc localhost 9876
```

## Claude Integration Example

### Using Claude with MCP

```python
# Example Claude prompt to use the MCP tools
prompt = """
Use the MCP tools available to you to review the Java code in the project at /home/user/project.

1. First, scan the project files
2. For each service file, review the code
3. Identify patterns and suggest improvements
4. Generate a summary report

Use the following tools:
- scan_files: Find all Java files
- analyze_code_type: Determine code type
- get_rules: Get relevant coding rules
- review_code: Perform detailed code review
"""
```

## API Protocol

### MCP Message Format

All messages use JSON with the following structure:

**Request:**
```json
{
  "method": "invoke_tool|list_tools|get_tool|ping",
  "toolName": "tool_name (for invoke_tool)",
  "input": { /* tool-specific input */ }
}
```

**Response:**
```json
{
  "result": { /* tool result or data */ }
}
```

**Error Response:**
```json
{
  "error": "Error message describing what went wrong"
}
```

## Performance Considerations

### Timeout Handling
- Default request timeout: 30 seconds (configurable)
- Socket timeout: 30 seconds
- If a request exceeds timeout, the client receives an error

### Connection Pooling
- Maximum concurrent connections: 10 (configurable)
- Connection reuse for subsequent requests
- Automatic cleanup of idle connections

### Concurrency
- Thread pool for handling multiple clients
- Each client connection handled in a separate thread
- Stateless tool handlers support concurrent invocations

## Error Handling

### Common Errors

1. **Connection Refused**
   - MCP server is not running
   - Solution: `mcp start`

2. **Request Timeout**
   - Tool execution took too long
   - Solution: Increase `MCP_REQUEST_TIMEOUT` in config

3. **Tool Not Found**
   - Tool name is incorrect
   - Solution: Check `mcp status` or use `list_tools`

4. **Invalid Input**
   - Input doesn't match tool schema
   - Solution: Review tool documentation above

## Troubleshooting

### Server Won't Start
```bash
# Check if port 9876 is in use
lsof -i :9876

# Check logs
tail -f logs/codereview-agent.log

# Try a different port
export MCP_PORT=9877
```

### Timeout Issues
```bash
# Increase timeout
export MCP_REQUEST_TIMEOUT=60

# Check system resources
top
```

### Connection Pool Issues
```bash
# Increase pool size
export MCP_CONNECTION_POOL_SIZE=20

# Monitor connections
netstat -an | grep 9876
```

## Security Considerations

⚠️ **Important**: The MCP server binds to localhost by default for security.

### Best Practices

1. **Network Isolation**: Only expose MCP server on trusted networks
2. **Authentication**: Consider adding token-based auth for production
3. **Input Validation**: Tools validate all inputs before processing
4. **Rate Limiting**: Consider adding rate limiting for production
5. **Logging**: All tool invocations are logged for audit trail

### For Production

1. Add authentication middleware
2. Implement rate limiting
3. Use TLS for encrypted communication
4. Add request signing
5. Implement audit logging

## Advanced Configuration

### Custom Tool Handlers

Add custom tools to MCPToolRegistry:

```java
MCPTool customTool = new MCPTool.Builder()
    .name("my_custom_tool")
    .description("Description of my tool")
    .inputSchema(Map.of(...))
    .handler(input -> {
        // Custom logic here
        return gson.toJson(result);
    })
    .build();

toolRegistry.registerTool("my_custom_tool", customTool);
```

### Resource Limits

```properties
# Max concurrent connections
MCP_CONNECTION_POOL_SIZE=20

# Request timeout (seconds)
MCP_REQUEST_TIMEOUT=60

# Max request size (bytes) - to be implemented
MCP_MAX_REQUEST_SIZE=1048576
```

## Examples

### Example 1: Review a Single File

```bash
cat > request.json << 'EOF'
{
  "method": "invoke_tool",
  "toolName": "review_code",
  "input": {
    "fileName": "UserService.java",
    "fileContent": "@Service\npublic class UserService { public void saveUser() { } }"
  }
}
EOF

echo $(cat request.json) | nc localhost 9876
```

### Example 2: Scan and Review Project

```bash
cat > batch.json << 'EOF'
[
  {"method": "invoke_tool", "toolName": "scan_files", "input": {"projectPath": "/project"}},
  {"method": "invoke_tool", "toolName": "analyze_code_type", "input": {"code": "@Service public class X{}"}}
]
EOF

while IFS= read -r line; do
  echo "$line" | nc localhost 9876
done < batch.json
```

## Related Documentation

- [Main README](README.md)
- [RAG Integration Guide](RAG_INTEGRATION.md)
- [Architecture Guide](ARCHITECTURE.md)
- [API Reference](API_REFERENCE.md)

## Support

For issues or questions:
1. Check logs: `logs/codereview-agent.log`
2. Run diagnostics: `mcp test`
3. Review configuration: `codereview.properties`

