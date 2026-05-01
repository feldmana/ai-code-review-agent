#!/bin/bash

# CodeReviewAgent - Quick Test Script
# Usage: bash test-review.sh /path/to/warmest/service

set -e

PROJECT_PATH="${1:-.}"
AGENT_DIR="/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent"
JAR_FILE="$AGENT_DIR/target/CodeReviewAgent.jar"

echo "╔══════════════════════════════════════════════════════════╗"
echo "║        CodeReviewAgent - Quick Test                      ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# Check 1: JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    echo "   Run: cd $AGENT_DIR && mvn clean package"
    exit 1
fi
echo "✓ JAR file found: $(du -h $JAR_FILE | cut -f1)"

# Check 2: Ollama running
echo "✓ Checking Ollama connection..."
if ! curl -s http://localhost:11434/api/tags > /dev/null 2>&1; then
    echo "❌ Ollama not running!"
    echo "   Run: ollama serve"
    exit 1
fi
echo "✓ Ollama is running"

# Check 3: Project path
if [ ! -d "$PROJECT_PATH" ]; then
    echo "❌ Project path not found: $PROJECT_PATH"
    echo "   Usage: bash test-review.sh /path/to/warmest/service"
    exit 1
fi
echo "✓ Project path exists: $PROJECT_PATH"

# Count files
FILE_COUNT=$(find "$PROJECT_PATH" -name "*.java" 2>/dev/null | wc -l)
if [ "$FILE_COUNT" -eq 0 ]; then
    echo "❌ No Java files found in: $PROJECT_PATH"
    exit 1
fi
echo "✓ Found $FILE_COUNT Java files to review"

# Check 4: Configuration
if [ ! -f "$AGENT_DIR/codereview.properties" ]; then
    echo "⚠️  No codereview.properties file (email disabled)"
else
    echo "✓ Configuration file found"
fi

echo ""
echo "════════════════════════════════════════════════════════════"
echo "Starting Code Review..."
echo "════════════════════════════════════════════════════════════"
echo ""

# Run the review
cd "$AGENT_DIR"
java -jar "$JAR_FILE" review "$PROJECT_PATH"

# Check results
echo ""
echo "════════════════════════════════════════════════════════════"
echo "✓ Review Complete!"
echo "════════════════════════════════════════════════════════════"
echo ""

# Show report location
LATEST_REPORT=$(ls -t reports/code_review_report_*.md 2>/dev/null | head -1)
if [ -n "$LATEST_REPORT" ]; then
    echo "📄 Report generated: $LATEST_REPORT"
    echo ""
    echo "Report size: $(du -h $LATEST_REPORT | cut -f1)"
    echo "View report: cat $LATEST_REPORT"
else
    echo "⚠️  No report found"
fi

echo ""
echo "📧 Logs: logs/codereview-agent.log"
echo ""

