#!/bin/bash

# Real Code Review Test Script
# This will run CodeReviewAgent on real code

PROJECT_DIR="/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent"
TEST_PROJECT="/Users/alexandrafeldman/Documents/Learning/OpenAI/testProject"
JAR_FILE="$PROJECT_DIR/target/CodeReviewAgent.jar"

echo "╔═══════════════════════════════════════════════════════════╗"
echo "║  CodeReviewAgent v2.0 - Real Code Review Test"
echo "║  Date: $(date)"
echo "╚═══════════════════════════════════════════════════════════╝"
echo ""

# Step 1: Check if JAR exists
echo "📦 Step 1: Checking JAR file..."
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR not found at: $JAR_FILE"
    echo "📝 Building JAR..."
    cd "$PROJECT_DIR"
    mvn clean package -DskipTests -q 2>&1 | tail -5
    if [ ! -f "$JAR_FILE" ]; then
        echo "❌ Failed to build JAR"
        exit 1
    fi
fi
echo "✅ JAR exists: $JAR_FILE"
echo "   Size: $(ls -lh $JAR_FILE | awk '{print $5}')"
echo ""

# Step 2: Verify test code exists
echo "📄 Step 2: Checking test code..."
TEST_FILES=$(find "$TEST_PROJECT/src" -name "*.java" -type f 2>/dev/null | wc -l)
if [ "$TEST_FILES" -eq 0 ]; then
    echo "❌ No test files found"
    exit 1
fi
echo "✅ Found $TEST_FILES Java files"
find "$TEST_PROJECT/src" -name "*.java" -type f | while read f; do
    echo "   • $(basename $f)"
done
echo ""

# Step 3: Check for Ollama
echo "🧠 Step 3: Checking Ollama..."
if ! command -v ollama &> /dev/null; then
    echo "⚠️  Ollama not installed (required for LLM review)"
    echo "   Install from: https://ollama.ai"
    echo ""
    echo "📝 Proceeding without LLM (will show RAG and structure only)"
    echo ""
else
    echo "✅ Ollama found: $(which ollama)"

    if curl -s http://127.0.0.1:11434/api/tags > /dev/null 2>&1; then
        echo "✅ Ollama is running"
        OLLAMA_RUNNING=true
    else
        echo "⚠️  Ollama is installed but not running"
        echo "   Start with: ollama serve"
        echo ""
        echo "📝 Proceeding without LLM review"
        echo ""
        OLLAMA_RUNNING=false
    fi
fi
echo ""

# Step 4: Show test code
echo "═══════════════════════════════════════════════════════════"
echo "📋 TEST CODE SAMPLES"
echo "═══════════════════════════════════════════════════════════"
echo ""

echo "1️⃣  UserService.java (Service Layer)"
echo "─────────────────────────────────────"
head -40 "$TEST_PROJECT/src/UserService.java" 2>/dev/null | tail -30
echo "   ..."
echo ""

echo "2️⃣  UserController.java (Controller Layer)"
echo "──────────────────────────────────────────"
head -40 "$TEST_PROJECT/src/UserController.java" 2>/dev/null | tail -30
echo "   ..."
echo ""

# Step 5: Run code review
echo "═══════════════════════════════════════════════════════════"
echo "🔍 RUNNING CODE REVIEW"
echo "═══════════════════════════════════════════════════════════"
echo ""

if [ "$OLLAMA_RUNNING" = true ]; then
    echo "▶️  Executing: java -jar $JAR_FILE 'review $TEST_PROJECT/src'"
    echo ""
    cd "$PROJECT_DIR"
    timeout 120 java -jar "$JAR_FILE" "review $TEST_PROJECT/src" 2>&1 || {
        echo "⚠️  Review timed out or failed"
    }
else
    echo "⚠️  Skipping live LLM review (Ollama not running)"
    echo ""
    echo "To enable live review:"
    echo "1. Terminal 1: ollama serve"
    echo "2. Terminal 2: ollama pull llama3"
    echo "3. Terminal 3: java -jar target/CodeReviewAgent.jar 'review /Users/alexandrafeldman/Documents/Learning/OpenAI/testProject/src'"
fi

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "📊 CHECKING FOR REPORTS"
echo "═══════════════════════════════════════════════════════════"
echo ""

REPORTS=$(find "$PROJECT_DIR/reports" -name "code_review_report_*.md" -type f 2>/dev/null | sort -r | head -1)
if [ -n "$REPORTS" ]; then
    echo "✅ Latest report found:"
    echo "   $REPORTS"
    echo ""
    echo "📖 Report Preview:"
    echo "─────────────────────────────────────"
    head -100 "$REPORTS"
    echo ""
    echo "   ... (full report saved)"
else
    echo "⚠️  No reports found yet"
    echo "   Reports will be saved to: $PROJECT_DIR/reports/"
fi

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "✅ Test Complete"
echo "═══════════════════════════════════════════════════════════"

