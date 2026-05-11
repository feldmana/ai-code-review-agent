#!/bin/bash

# CodeReviewAgent v2.0 - Setup & Run Script
# This script will set everything up and run the code review

set -e

PROJECT_DIR="/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent"
TEST_DIR="/tmp/warmest-review"

echo "==============================================="
echo "  CodeReviewAgent v2.0 - Setup & Run"
echo "==============================================="
echo ""

# Step 1: Check Prerequisites
echo "✓ Step 1: Checking prerequisites..."
echo ""

echo "  Checking Java..."
if ! command -v java &> /dev/null; then
    echo "  ❌ Java not found. Please install Java 21+"
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | grep -i version | head -1)
echo "  ✅ Java found: $JAVA_VERSION"
echo ""

echo "  Checking Maven..."
if ! command -v mvn &> /dev/null; then
    echo "  ❌ Maven not found. Please install Maven"
    exit 1
fi
echo "  ✅ Maven found"
echo ""

echo "  Checking Ollama..."
if ! command -v ollama &> /dev/null; then
    echo "  ⚠️  Ollama not found."
    echo "  📥 To install Ollama:"
    echo "     1. Visit: https://ollama.ai"
    echo "     2. Download and install Ollama for your OS"
    echo "     3. Run: ollama serve"
    echo "     4. In another terminal: ollama pull llama3"
    echo ""
    echo "  For now, I'll continue with the build."
    echo ""
else
    echo "  ✅ Ollama found"
    echo ""

    echo "  Checking if Ollama is running..."
    if curl -s http://127.0.0.1:11434/api/tags > /dev/null 2>&1; then
        echo "  ✅ Ollama is running"

        echo "  Checking available models..."
        MODELS=$(curl -s http://127.0.0.1:11434/api/tags | grep -o '"name":"[^"]*"' | head -3)
        if [ -z "$MODELS" ]; then
            echo "  ⚠️  No models found. Please run: ollama pull llama3"
        else
            echo "  ✅ Models available"
        fi
    else
        echo "  ⚠️  Ollama is not running"
        echo "     Start it with: ollama serve"
    fi
    echo ""
fi

# Step 2: Build Project
echo "✓ Step 2: Building project..."
cd "$PROJECT_DIR"

if [ -f "target/CodeReviewAgent.jar" ]; then
    echo "  ℹ️  JAR already built. Skipping Maven build..."
    echo "  📦 JAR: target/CodeReviewAgent.jar"
else
    echo "  🔨 Building with Maven..."
    mvn clean package -DskipTests -q
    echo "  ✅ Build complete"
fi
echo ""

# Step 3: Prepare Test Code
echo "✓ Step 3: Preparing test code..."
mkdir -p "$TEST_DIR"

cat > "$TEST_DIR/UserService.java" << 'JAVAEOF'
package com.example.service;

import org.springframework.stereotype.Service;

/**
 * Example Service for testing CodeReviewAgent
 */
@Service
public class UserService {

    public void deleteUser(Long userId) {
        // Problem: Returns null from Optional
        // This can cause NullPointerException
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    public String getUserEmail(Long userId) {
        // Problem: No null check, no exception handling
        User user = userRepository.findById(userId).get();
        return user.getEmail();
    }

    public void saveUser(String email, String password) {
        // Problem: No input validation
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        userRepository.save(user);
    }
}

class User {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
JAVAEOF

echo "  ✅ Test service created: $TEST_DIR/UserService.java"
echo ""

# Step 4: Display Next Steps
echo "✓ Step 4: Ready to run!"
echo ""
echo "==============================================="
echo "  NEXT STEPS"
echo "==============================================="
echo ""
echo "1️⃣  START OLLAMA (in a new terminal):"
echo "   $ ollama serve"
echo ""
echo "2️⃣  ENSURE MODEL IS DOWNLOADED:"
echo "   $ ollama pull llama3"
echo ""
echo "3️⃣  RUN CODE REVIEW (in another terminal):"
echo "   $ cd $PROJECT_DIR"
echo "   $ java -jar target/CodeReviewAgent.jar \"review $TEST_DIR\""
echo ""
echo "4️⃣  VIEW RESULTS:"
echo "   $ cat reports/code_review_report_*.md"
echo ""
echo "5️⃣  (OPTIONAL) SEND EMAIL:"
echo "   First, create codereview.properties:"
echo "   $ cat > codereview.properties << 'EOF'"
echo "   EMAIL_ENABLED=true"
echo "   EMAIL_TO=afeldman66@gmail.com"
echo "   SMTP_HOST=smtp.gmail.com"
echo "   SMTP_PORT=587"
echo "   SMTP_USERNAME=your-email@gmail.com"
echo "   SMTP_PASSWORD=your-app-password"
echo "   SMTP_TLS_ENABLED=true"
echo "   EOF"
echo ""
echo "   Then run with email:"
echo "   $ java -jar target/CodeReviewAgent.jar \"review $TEST_DIR and send email\""
echo ""
echo "==============================================="
echo ""
echo "📚 Documentation:"
echo "   - README_v2.md - Main documentation"
echo "   - QUICK_START_v2.md - Quick start guide"
echo "   - TESTING_GUIDE_ENHANCED.md - Complete setup guide"
echo ""
echo "✅ Setup complete! Follow the steps above to run."
echo ""

