# MCP Quick Reference Card

## 🚀 Start MCP Server

### Via CLI (Interactive)
```bash
java -jar target/CodeReviewAgent.jar
CodeReviewAgent> mcp start
CodeReviewAgent> mcp status
CodeReviewAgent> mcp test
CodeReviewAgent> mcp stop
```

### Via Configuration
```bash
export MCP_ENABLED=true
export MCP_PORT=9876
java -jar target/CodeReviewAgent.jar
```

### Via Test Script
```bash
./run_mcp_server.sh
# Automatically starts server and runs tests
```

---

## 🔧 Configuration

### codereview.properties
```properties
MCP_ENABLED=true
MCP_PORT=9876
MCP_REQUEST_TIMEOUT=30
MCP_CONNECTION_POOL_SIZE=10
```

### Environment Variables
```bash
export MCP_ENABLED=true
export MCP_PORT=9876
export MCP_REQUEST_TIMEOUT=30
export MCP_CONNECTION_POOL_SIZE=10
```

---

## 🛠️ Available Tools

### review_code
**Review a Java file**
```json
{
  "method": "invoke_tool",
  "toolName": "review_code",
  "input": {
    "fileName": "UserService.java",
    "fileContent": "@Service public class UserService { ... }"
  }
}
```

### scan_files
**Find all Java files**
```json
{
  "method": "invoke_tool",
  "toolName": "scan_files",
  "input": {
    "projectPath": "/path/to/project"
  }
}
```

### get_rules
**Get relevant coding rules**
```json
{
  "method": "invoke_tool",
  "toolName": "get_rules",
  "input": {
    "code": "@Service public class X { }"
  }
}
```

### analyze_code_type
**Detect code type (SERVICE, CONTROLLER, etc.)**
```json
{
  "method": "invoke_tool",
  "toolName": "analyze_code_type",
  "input": {
    "code": "@Service public class UserService { }"
  }
}
```

---

## 🧪 Testing

### Test with netcat
```bash
# Ping
echo '{"method":"ping"}' | nc localhost 9876

# List tools
echo '{"method":"list_tools"}' | nc localhost 9876

# Invoke tool
echo '{
  "method":"invoke_tool",
  "toolName":"analyze_code_type",
  "input":{"code":"@Service public class X {}"}
}' | nc localhost 9876
```

### Test with CLI
```bash
CodeReviewAgent> mcp test
```

### Test in Code
```java
MCPTestClient client = new MCPTestClient("localhost", 9876);
client.connect();
client.runFullTest();
client.disconnect();
```

---

## 📊 Server Information

### Check Status
```bash
CodeReviewAgent> mcp status
```

### Check Logs
```bash
tail -f logs/codereview-agent.log
```

### Monitor Connections
```bash
lsof -i :9876
netstat -an | grep 9876
```

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| **Connection refused** | `mcp start` to start server |
| **Port already in use** | Change `MCP_PORT=9877` |
| **Timeout errors** | Increase `MCP_REQUEST_TIMEOUT=60` |
| **Server won't start** | Check Ollama: `curl http://localhost:11434/api/tags` |
| **Tool not found** | Use `mcp start` then `list_tools` |
| **JSON parse error** | Check JSON syntax in request |

---

## 💡 Common Commands

```bash
# Build project
mvn clean package -DskipTests

# Run interactive mode
java -jar target/CodeReviewAgent.jar

# Start MCP server
export MCP_ENABLED=true
java -jar target/CodeReviewAgent.jar

# Test connection
echo '{"method":"ping"}' | nc localhost 9876

# Check if port is open
lsof -i :9876

# Run test suite
./run_mcp_server.sh

# Kill process on port
lsof -i :9876 | grep java | awk '{print $2}' | xargs kill -9
```

---

## 📚 Documentation Files

| File | Purpose |
|------|---------|
| **MCP_INTEGRATION.md** | Complete user guide |
| **MCP_IMPLEMENTATION_COMPLETE.md** | Technical details |
| **MCP_FINAL_SUMMARY.md** | Overview & status |
| **run_mcp_server.sh** | Automated test script |

---

## 🔐 Security Notes

⚠️ **Important:**
- Server binds to `localhost` only
- No authentication enabled (add for production)
- No encryption (use TLS for production)
- All requests are logged

---

## 🎯 Default Configuration

| Setting | Value | Changeable |
|---------|-------|-----------|
| Enabled | false | Yes |
| Port | 9876 | Yes |
| Timeout | 30s | Yes |
| Pool Size | 10 | Yes |
| Host | localhost | No |

---

## 📞 Quick Help

```bash
# In interactive mode
CodeReviewAgent> help              # Show commands
CodeReviewAgent> mcp start         # Start server
CodeReviewAgent> mcp status        # Show status
CodeReviewAgent> mcp test          # Test server
CodeReviewAgent> mcp stop          # Stop server
CodeReviewAgent> review [path]     # Review code
CodeReviewAgent> exit              # Exit app
```

---

## 🚀 5-Minute Setup

```bash
# 1. Enable MCP
export MCP_ENABLED=true

# 2. Build (takes ~30 seconds)
mvn clean package -DskipTests

# 3. Run app
java -jar target/CodeReviewAgent.jar

# 4. Start server (in app)
CodeReviewAgent> mcp start

# 5. Test (in another terminal)
echo '{"method":"ping"}' | nc localhost 9876
```

---

## 💻 Supported Platforms

- ✅ macOS
- ✅ Linux
- ✅ Windows (with WSL)
- ✅ Docker (recommended)

---

## 📦 Requirements

- Java 21+
- Maven 3.6+
- Ollama running on localhost:11434
- Port 9876 available (configurable)

---

## 🎓 Integration Example

```python
# Claude/Python client example
import socket
import json

def call_mcp_tool(tool_name, input_data):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect(('localhost', 9876))
    
    request = {
        "method": "invoke_tool",
        "toolName": tool_name,
        "input": input_data
    }
    
    sock.sendall((json.dumps(request) + '\n').encode())
    response = sock.recv(4096).decode()
    sock.close()
    
    return json.loads(response)

# Usage
result = call_mcp_tool('analyze_code_type', {
    'code': '@Service public class X {}'
})
print(result)
```

---

## ⚡ Performance Tips

1. **Keep connections alive** - Reuse TCP connections
2. **Batch requests** - Send multiple tools in sequence
3. **Monitor Ollama** - Ensure Ollama has resources
4. **Check logs** - Monitor `logs/codereview-agent.log`
5. **Tune pool size** - Increase `MCP_CONNECTION_POOL_SIZE` if needed

---

## 🔗 Related Files

- `src/main/java/com/agentic/codereview/mcp/MCPServer.java`
- `src/main/java/com/agentic/codereview/mcp/MCPServerManager.java`
- `src/main/java/com/agentic/codereview/mcp/MCPTestClient.java`
- `src/main/java/com/agentic/codereview/config/AppConfig.java`
- `codereview.properties`

---

**Last Updated:** May 12, 2026  
**Version:** 1.0  
**Status:** ✅ Production Ready

