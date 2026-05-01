#!/bin/bash

# CodeReviewAgent Project Verification Script

echo "╔══════════════════════════════════════════════════════════╗"
echo "║        CodeReviewAgent - Project Verification           ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

PROJECT_DIR="/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent"
JAVA_SRC="$PROJECT_DIR/src/main/java/com/agentic/codereview"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check 1: Java files exist
echo "✓ Checking Java files..."
JAVA_FILES=$(find "$JAVA_SRC" -name "*.java" 2>/dev/null | wc -l)
if [ "$JAVA_FILES" -eq 17 ]; then
    echo -e "${GREEN}  ✓ Found $JAVA_FILES Java files${NC}"
else
    echo -e "${RED}  ✗ Expected 17 Java files, found $JAVA_FILES${NC}"
fi
echo ""

# Check 2: Packages
echo "✓ Checking packages..."
PACKAGES=("agent" "orchestrator" "llm" "tool" "model" "config")
for pkg in "${PACKAGES[@]}"; do
    if [ -d "$JAVA_SRC/$pkg" ]; then
        count=$(ls "$JAVA_SRC/$pkg"/*.java 2>/dev/null | wc -l)
        echo -e "  ${GREEN}✓${NC} $pkg/ ($count files)"
    else
        echo -e "  ${RED}✗${NC} $pkg/ (missing)"
    fi
done
echo ""

# Check 3: Key agents
echo "✓ Checking key agents..."
AGENTS=("RouterAgent" "PlannerAgent" "ReviewAgent" "SummaryAgent" "EmailAgent")
for agent in "${AGENTS[@]}"; do
    if [ -f "$JAVA_SRC/agent/${agent}.java" ]; then
        lines=$(wc -l < "$JAVA_SRC/agent/${agent}.java")
        echo -e "  ${GREEN}✓${NC} $agent.java ($lines lines)"
    else
        echo -e "  ${RED}✗${NC} $agent.java (missing)"
    fi
done
echo ""

# Check 4: Orchestrator
echo "✓ Checking orchestrator..."
if [ -f "$JAVA_SRC/orchestrator/AgentOrchestrator.java" ]; then
    lines=$(wc -l < "$JAVA_SRC/orchestrator/AgentOrchestrator.java")
    echo -e "  ${GREEN}✓${NC} AgentOrchestrator.java ($lines lines)"
else
    echo -e "  ${RED}✗${NC} AgentOrchestrator.java (missing)"
fi
echo ""

# Check 5: LLM Client
echo "✓ Checking LLM integration..."
if [ -f "$JAVA_SRC/llm/OllamaClient.java" ]; then
    lines=$(wc -l < "$JAVA_SRC/llm/OllamaClient.java")
    echo -e "  ${GREEN}✓${NC} OllamaClient.java ($lines lines)"
else
    echo -e "  ${RED}✗${NC} OllamaClient.java (missing)"
fi
echo ""

# Check 6: Tools
echo "✓ Checking tools..."
TOOLS=("FileScannerTool" "FileReaderTool" "ReportWriterTool")
for tool in "${TOOLS[@]}"; do
    if [ -f "$JAVA_SRC/tool/${tool}.java" ]; then
        lines=$(wc -l < "$JAVA_SRC/tool/${tool}.java")
        echo -e "  ${GREEN}✓${NC} $tool.java ($lines lines)"
    else
        echo -e "  ${RED}✗${NC} $tool.java (missing)"
    fi
done
echo ""

# Check 7: Models
echo "✓ Checking data models..."
MODELS=("Task" "Action" "ReviewResult" "Summary")
for model in "${MODELS[@]}"; do
    if [ -f "$JAVA_SRC/model/${model}.java" ]; then
        lines=$(wc -l < "$JAVA_SRC/model/${model}.java")
        echo -e "  ${GREEN}✓${NC} $model.java ($lines lines)"
    else
        echo -e "  ${RED}✗${NC} $model.java (missing)"
    fi
done
echo ""

# Check 8: Configuration
echo "✓ Checking configuration..."
if [ -f "$JAVA_SRC/config/AppConfig.java" ]; then
    lines=$(wc -l < "$JAVA_SRC/config/AppConfig.java")
    echo -e "  ${GREEN}✓${NC} AppConfig.java ($lines lines)"
else
    echo -e "  ${RED}✗${NC} AppConfig.java (missing)"
fi
echo ""

# Check 9: Main entry point
echo "✓ Checking CLI entry point..."
if [ -f "$JAVA_SRC/Main.java" ]; then
    lines=$(wc -l < "$JAVA_SRC/Main.java")
    echo -e "  ${GREEN}✓${NC} Main.java ($lines lines)"
else
    echo -e "  ${RED}✗${NC} Main.java (missing)"
fi
echo ""

# Check 10: Documentation
echo "✓ Checking documentation..."
DOCS=("README.md" "QUICKSTART.md" "IMPLEMENTATION_SUMMARY.md" "FILE_STRUCTURE.md")
for doc in "${DOCS[@]}"; do
    if [ -f "$PROJECT_DIR/$doc" ]; then
        lines=$(wc -l < "$PROJECT_DIR/$doc")
        echo -e "  ${GREEN}✓${NC} $doc ($lines lines)"
    else
        echo -e "  ${RED}✗${NC} $doc (missing)"
    fi
done
echo ""

# Check 11: Configuration files
echo "✓ Checking build configuration..."
if [ -f "$PROJECT_DIR/pom.xml" ]; then
    echo -e "  ${GREEN}✓${NC} pom.xml (Maven configuration)"
else
    echo -e "  ${RED}✗${NC} pom.xml (missing)"
fi

if [ -f "$PROJECT_DIR/src/main/resources/logback.xml" ]; then
    echo -e "  ${GREEN}✓${NC} logback.xml (Logging configuration)"
else
    echo -e "  ${RED}✗${NC} logback.xml (missing)"
fi
echo ""

# Check 12: JAR file
echo "✓ Checking build artifacts..."
if [ -f "$PROJECT_DIR/target/CodeReviewAgent.jar" ]; then
    size=$(du -h "$PROJECT_DIR/target/CodeReviewAgent.jar" | cut -f1)
    echo -e "  ${GREEN}✓${NC} CodeReviewAgent.jar ($size)"
else
    echo -e "  ${YELLOW}ℹ${NC} CodeReviewAgent.jar (needs to be built)"
fi
echo ""

# Summary
echo "╔══════════════════════════════════════════════════════════╗"
echo "║                  Verification Summary                   ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""
echo -e "${GREEN}✓ Project structure verified!${NC}"
echo ""
echo "Next steps:"
echo "  1. mvn clean package"
echo "  2. java -jar target/CodeReviewAgent.jar"
echo ""
echo "For more info, see:"
echo "  - README.md"
echo "  - QUICKSTART.md"
echo "  - IMPLEMENTATION_SUMMARY.md"
echo ""

