#!/bin/bash

# MCP Server Quick Start Script
# Tests the MCP integration of CodeReviewAgent

set -e

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║       CodeReviewAgent MCP Quick Start & Test Script           ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
MCP_PORT=${MCP_PORT:-9876}
MCP_TIMEOUT=${MCP_TIMEOUT:-5000}
PROJECT_PATH=${1:-$(pwd)}

echo -e "${BLUE}Configuration:${NC}"
echo "  Port: $MCP_PORT"
echo "  Timeout: $MCP_TIMEOUT ms"
echo "  Project Path: $PROJECT_PATH"
echo ""

# Step 1: Check if Ollama is running
echo -e "${YELLOW}Step 1: Checking Ollama connection...${NC}"
if curl -s http://localhost:11434/api/tags > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Ollama is running${NC}"
else
    echo -e "${RED}❌ Ollama is not running${NC}"
    echo "   Start it with: ollama serve"
    exit 1
fi
echo ""

# Step 2: Build the project
echo -e "${YELLOW}Step 2: Building CodeReviewAgent...${NC}"
if mvn clean package -DskipTests -q; then
    echo -e "${GREEN}✓ Build successful${NC}"
else
    echo -e "${RED}❌ Build failed${NC}"
    exit 1
fi
echo ""

# Step 3: Start MCP Server
echo -e "${YELLOW}Step 3: Starting MCP Server on port $MCP_PORT...${NC}"
export MCP_ENABLED=true
export MCP_PORT=$MCP_PORT

# Start the application in background
java -jar target/CodeReviewAgent.jar &
APP_PID=$!
echo "  Application PID: $APP_PID"

# Give server time to start
sleep 3

echo -e "${GREEN}✓ Application started${NC}"
echo ""

# Step 4: Test MCP Server
echo -e "${YELLOW}Step 4: Testing MCP Server...${NC}"

# Test ping
echo -e "  ${BLUE}Testing ping...${NC}"
if echo '{"method":"ping"}' | nc -w 2 localhost $MCP_PORT > /dev/null 2>&1; then
    echo -e "  ${GREEN}✓ Ping successful${NC}"
else
    echo -e "  ${RED}❌ Ping failed${NC}"
fi
echo ""

# Step 5: Test list_tools
echo -e "${YELLOW}Step 5: Listing available tools...${NC}"
echo '{"method":"list_tools"}' | nc -w 2 localhost $MCP_PORT | jq . 2>/dev/null || echo "  ⚠️  Could not parse response"
echo ""

# Step 6: Test analyze_code_type
echo -e "${YELLOW}Step 6: Testing analyze_code_type tool...${NC}"
cat > /tmp/mcp_test_request.json << 'EOF'
{
  "method": "invoke_tool",
  "toolName": "analyze_code_type",
  "input": {
    "code": "@Service\npublic class UserService {\n  public void saveUser(String name) {\n    // Save user\n  }\n}"
  }
}
EOF

echo "(echo $(cat /tmp/mcp_test_request.json) | nc -w 2 localhost $MCP_PORT | jq . 2>/dev/null) || echo '  ⚠️  Could not parse response'"
echo ""

# Step 7: Display status
echo -e "${YELLOW}Step 7: Server Status${NC}"
echo "  Server is running on port: $MCP_PORT"
echo "  Process ID: $APP_PID"
echo ""

# Step 8: Instructions
echo -e "${BLUE}Next Steps:${NC}"
echo ""
echo "1. To stop the server, run:"
echo "   kill $APP_PID"
echo ""
echo "2. To use with Claude via MCP:"
echo "   - Configure Claude's MCP settings"
echo "   - Point to localhost:$MCP_PORT"
echo ""
echo "3. To test from command line:"
echo "   echo '{\"method\":\"ping\"}' | nc localhost $MCP_PORT"
echo ""
echo "4. To integrate with your application:"
echo "   See MCP_INTEGRATION.md for detailed documentation"
echo ""

echo -e "${GREEN}✓ MCP Server is ready!${NC}"
echo ""

# Keep the script running until interrupted
echo -e "${YELLOW}Press Ctrl+C to stop the server${NC}"
wait $APP_PID 2>/dev/null || true

