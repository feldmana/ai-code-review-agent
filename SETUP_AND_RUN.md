# 🚀 CodeReviewAgent v2.0 - Complete Setup & Run Instructions

**Status**: Production Ready | Last Updated: May 6, 2026

---

## 📋 Prerequisites Checklist

Before running CodeReviewAgent, ensure you have:

- [ ] **Java 21+** installed
- [ ] **Maven 3.8+** installed
- [ ] **Ollama** installed and running
- [ ] **Ollama model** downloaded (llama3 or mistral)

---

## 🔧 STEP 1: Install Prerequisites

### Java 21+
```bash
# Check if Java is installed
java -version

# If not installed, install from:
# - Mac: brew install openjdk@21
# - Linux: apt-get install openjdk-21-jdk
# - Windows: Download from https://www.oracle.com/java/technologies/downloads/
```

### Maven
```bash
# Check if Maven is installed
mvn -version

# If not installed:
# - Mac: brew install maven
# - Linux: apt-get install maven
# - Windows: Download from https://maven.apache.org/download.cgi
```

### Ollama (Local LLM)
```bash
# Install from: https://ollama.ai
# 
# After installation, start Ollama:
ollama serve

# Wait for: "Listening on 127.0.0.1:11434"
```

### Ollama Model
```bash
# In a NEW TERMINAL (while ollama serve is running):
ollama pull llama3

# Alternatives (faster):
ollama pull mistral
ollama pull neural-chat

# To see available models:
ollama list
```

---

## 🏗️ STEP 2: Build CodeReviewAgent

Navigate to the project directory:
```bash
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent
```

Build the project:
```bash
# Clean build (recommended first time)
mvn clean package -DskipTests -q

# Or just compile (faster if already built)
mvn compile -q
```

**Expected Output:**
```
BUILD SUCCESS
[INFO] Building jar: target/CodeReviewAgent.jar
```

Verify JAR was created:
```bash
ls -lh target/CodeReviewAgent.jar
# Should show: CodeReviewAgent.jar (45MB)
```

---

## 🎯 STEP 3: Prepare Test Code

Create a test directory with sample code:
```bash
mkdir -p /tmp/warmest-review
```

Copy your Warmest project service code:
```bash
# Replace with your actual path
cp /path/to/warmest/src/main/java/com/example/service/*.java /tmp/warmest-review/
```

Or use the example service provided in the setup script:
```bash
cat > /tmp/warmest-review/UserService.java << 'EOF'
package com.example.service;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userRepository.delete(user);
        }
    }
}
EOF
```

---

## ▶️ STEP 4: Run CodeReviewAgent

**Make sure Ollama is running** (from Step 1):
```bash
ollama serve
```

In another terminal, run the review:
```bash
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review"
```

**Expected Output:**
```
=================================
  CodeReviewAgent v1.0
  AI-Powered Code Review System
=================================

Configuration loaded: ...
✓ Connected to Ollama successfully
=== Orchestrator: Starting task execution ===
User input: review /tmp/warmest-review
Project path: /tmp/warmest-review
Task routed to: REVIEW_CODE
Execution plan created with 4 actions
Action 1: SCAN_FILES
...
Report written to: reports/code_review_report_20260506_143000.md
✓ Task completed successfully
```

---

## 📊 STEP 5: View Results

Check the generated report:
```bash
# View the latest report
cat reports/code_review_report_*.md | less

# Or open in editor
open reports/code_review_report_*.md
```

**Expected Report Format:**
```markdown
# Code Review Report
Generated: 2026-05-06 14:30:00

## Summary
- Total Files Reviewed: 1
- Files with Issues: 1
- Issues by Severity:
  - HIGH: 1
  - MEDIUM: 0
  - LOW: 0

## File Reviews

### UserService.java
**Type Detected**: SERVICE
**Status**: ⚠️ Issues Found

#### Issues
1. **HIGH** - Null pointer risk in deleteUser()
   Message: User.orElse(null) can cause NPE
   Suggestion: Throw UserNotFoundException instead
```

---

## 📧 STEP 6: (Optional) Configure Email

To send reports via email, create a configuration file:

```bash
cat > /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/codereview.properties << 'EOF'
# Email Configuration
EMAIL_ENABLED=true
EMAIL_TO=afeldman66@gmail.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-specific-password
SMTP_TLS_ENABLED=true

# Review Configuration
MAX_RETRIES=3
THREAD_POOL_SIZE=4
EOF
```

**Gmail Setup (Required):**
1. Enable 2-Factor Authentication: https://myaccount.google.com/security
2. Generate App Password: https://myaccount.google.com/apppasswords
3. Select "Mail" and "Other (custom name)"
4. Copy the 16-character password to `SMTP_PASSWORD` above

Then run with email:
```bash
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review and send email"
```

---

## 🎓 Common Commands

### Interactive Mode
```bash
java -jar target/CodeReviewAgent.jar

# Commands:
# > review /path/to/code
# > review /path/to/code and send email
# > help
# > exit
```

### CLI Mode
```bash
# Review code
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review"

# Review and send email
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review and send email"
```

### Check Logs
```bash
# View logs in real-time
tail -f logs/codereview-agent.log

# View last 50 lines
tail -50 logs/codereview-agent.log

# Search for errors
grep "ERROR" logs/codereview-agent.log
```

### Verify Ollama
```bash
# Check if Ollama is running
curl http://127.0.0.1:11434/api/tags

# List models
ollama list
```

---

## 🐛 Troubleshooting

### "Failed to connect to Ollama"
**Problem**: Ollama is not running
```bash
# Solution: Start Ollama in a new terminal
ollama serve

# Verify it's running
curl http://127.0.0.1:11434/api/tags
```

### "No model found"
**Problem**: No Ollama model is downloaded
```bash
# Solution: Download a model
ollama pull llama3

# Or faster model:
ollama pull mistral

# Verify it's installed
ollama list
```

### "No files found"
**Problem**: Directory path is incorrect or has no .java files
```bash
# Solution: Verify the directory
ls -la /tmp/warmest-review/*.java

# Verify path syntax
echo "Your path: /tmp/warmest-review"
```

### "Build failed"
**Problem**: Maven build error
```bash
# Solution: Clean rebuild
mvn clean package -DskipTests -q

# Or with verbose output
mvn clean package -DskipTests
```

### "Email send failed"
**Problem**: Email configuration is incorrect
```bash
# Solution: Verify config
cat codereview.properties | grep SMTP

# Gmail: Make sure you're using app-specific password, not regular password
# Gmail: Make sure 2-Factor Authentication is enabled
```

---

## 📈 Performance Guide

### Expected Times
| Task | Duration |
|------|----------|
| Build JAR | 30-60 seconds |
| Initialize RAG | 1-2 seconds |
| Review 1 file | 5-30 seconds |
| Review 10 files | 1-5 minutes |
| Review 50 files | 10-15 minutes |

### Optimization Tips
```bash
# 1. Use faster model
ollama pull mistral  # Faster than llama3

# 2. Increase parallel threads
echo "THREAD_POOL_SIZE=8" >> codereview.properties

# 3. Review service code only
cp src/main/java/*/service/*.java /tmp/review/
java -jar target/CodeReviewAgent.jar "review /tmp/review"

# 4. Review smaller batches
# Instead of 100 files, review 10 at a time
```

---

## ✅ Complete Workflow Example

**Full end-to-end example:**

```bash
# 1. Ensure Ollama is running
ollama serve &

# 2. Download model
ollama pull llama3

# 3. Navigate to project
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent

# 4. Build
mvn clean package -DskipTests -q

# 5. Prepare code
mkdir -p /tmp/warmest-review
cp /path/to/warmest/src/main/java/com/example/service/*.java /tmp/warmest-review/

# 6. Configure email (optional)
cat > codereview.properties << 'EOF'
EMAIL_ENABLED=true
EMAIL_TO=afeldman66@gmail.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
SMTP_TLS_ENABLED=true
EOF

# 7. Run review with email
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review and send email"

# 8. View results
cat reports/code_review_report_*.md

# 9. Check email
# Report should arrive at afeldman66@gmail.com
```

---

## 📚 Documentation Files

### Getting Started
- **README_v2.md** - Main documentation (start here)
- **QUICK_START_v2.md** - Quick reference guide

### Setup & Configuration
- **TESTING_GUIDE_ENHANCED.md** - Detailed setup guide
- **codereview.properties.example.detailed** - Configuration options

### Technical Details
- **RAG_ARCHITECTURE_v2.md** - How BM25 ranking works
- **AGENT_IMPROVEMENTS.md** - Architecture improvements
- **CODE_REVIEW_SUMMARY.md** - What was changed

### Reference
- **RUN.sh** - Automated setup script

---

## 🎯 Next Steps

1. ✅ Install prerequisites (Java, Maven, Ollama)
2. ✅ Build CodeReviewAgent
3. ✅ Prepare test code
4. ✅ Run first review
5. ✅ View results
6. ✅ (Optional) Configure email
7. ✅ Review your actual Warmest project code

---

## 💡 Tips & Tricks

### Test with Small File First
```bash
# Create a simple test file
mkdir -p /tmp/test
echo '@Service public class Test { }' > /tmp/test/Test.java

# Review to verify setup works
java -jar target/CodeReviewAgent.jar "review /tmp/test"
```

### View RAG Retrieval Details
```bash
# Check which rules were retrieved
grep "RANK" logs/codereview-agent.log

# Should show:
# RANK 1 - SERVICE_DESIGN - Score: 2.45
# RANK 2 - ARCHITECTURE - Score: 0.78
```

### Review Just Service Layer
```bash
# Copy only service files
mkdir -p /tmp/services
find /path/to/warmest -name "*Service.java" -exec cp {} /tmp/services/ \;

# Review services
java -jar target/CodeReviewAgent.jar "review /tmp/services"
```

---

## 📞 Need Help?

1. **Check Logs**: `tail -f logs/codereview-agent.log`
2. **Test Ollama**: `curl http://127.0.0.1:11434/api/tags`
3. **Read Documentation**: See files listed above
4. **Check Troubleshooting**: Section in this file

---

## ✨ You're Ready!

Everything is set up. Follow the steps above to start reviewing code with AI!

**Quick Start:**
```bash
# Terminal 1: Start Ollama
ollama serve

# Terminal 2: Run review
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review"

# View results
cat reports/code_review_report_*.md
```

Happy Code Reviewing! 🚀

