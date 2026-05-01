# CodeReviewAgent - Testing Guide

## Setup for Testing

### 1. Gmail App Password Setup (REQUIRED for Email)

Since you want to send reports to **afeldman66@gmail.com**, follow these steps:

#### Step 1: Enable 2-Factor Authentication
1. Go to: https://myaccount.google.com/security
2. Scroll to "2-Step Verification"
3. Enable it

#### Step 2: Generate App Password
1. Go to: https://myaccount.google.com/apppasswords
2. Select:
   - Device type: **Other (custom name)**
   - Enter: "CodeReviewAgent"
3. Click **Generate**
4. Copy the 16-character password

#### Step 3: Update Configuration
Edit `codereview.properties`:
```properties
EMAIL_ENABLED=true
EMAIL_TO=afeldman66@gmail.com
SMTP_USERNAME=afeldman66@gmail.com
SMTP_PASSWORD=xxxx xxxx xxxx xxxx    ← Paste your 16-char password here
```

---

## Input Parameters Explained

### Command Format
```bash
java -jar target/CodeReviewAgent.jar review /path/to/warmest
```

### Required Inputs:
1. **Command**: `review` (or just run interactively)
2. **Project Path**: Path to your "warmest" project

### Environment Variables (Alternative):
```bash
export EMAIL_ENABLED=true
export EMAIL_TO=afeldman66@gmail.com
export SMTP_USERNAME=afeldman66@gmail.com
export SMTP_PASSWORD="your-16-char-app-password"
```

---

## Testing Steps

### 1. Verify Ollama is Running
```bash
# In Terminal 1
ollama serve

# In Terminal 2 - test connection
curl http://localhost:11434/api/tags
```

If it works, you'll see available models.

### 2. Pull a Model (if needed)
```bash
ollama pull llama2
# or faster model:
ollama pull mistral
```

### 3. Build the Project
```bash
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent
mvn clean package
```

### 4. Test 1: Review without Email (Recommended First)
```bash
# Set EMAIL to false for first test
export EMAIL_ENABLED=false
java -jar target/CodeReviewAgent.jar review /path/to/warmest/src/main/java/com/yourcompany/service
```

**What to expect:**
- Scans for Java files
- Sends each to Ollama for review
- Generates: `reports/code_review_report_*.md`

### 5. Test 2: Review with Email Enabled
```bash
# Make sure codereview.properties has:
# - EMAIL_ENABLED=true
# - Correct password

java -jar target/CodeReviewAgent.jar review /path/to/warmest/src/main/java/com/yourcompany/service
```

**What to expect:**
- Reviews files
- Generates report
- **Sends email to afeldman66@gmail.com** with the report

---

## Input Configuration Checklist

### ✅ Before Running:

- [ ] Java 21+ installed: `java -version`
- [ ] Maven installed: `mvn -version`
- [ ] Ollama running: `ollama serve`
- [ ] Model available: `ollama pull llama2`
- [ ] Gmail app password generated
- [ ] `codereview.properties` updated with:
  - [ ] EMAIL_ENABLED=true
  - [ ] EMAIL_TO=afeldman66@gmail.com
  - [ ] SMTP_USERNAME=afeldman66@gmail.com
  - [ ] SMTP_PASSWORD=your-16-char-password
- [ ] Know your warmest project path

---

## Example Test Run

### Test Scenario: Review Only Service Layer

Find your service code:
```bash
# Example structure
/path/to/warmest/
└── src/main/java/
    └── com/yourcompany/
        └── service/
            ├── UserService.java
            ├── OrderService.java
            └── PaymentService.java
```

### Run Command:
```bash
java -jar target/CodeReviewAgent.jar review /path/to/warmest/src/main/java/com/yourcompany/service
```

### Expected Output:

#### In Console:
```
=================================
  CodeReviewAgent v1.0
  AI-Powered Code Review System
=================================

✓ Connected to Ollama successfully

Action: Scanning files
Found 3 code files

Action: Reviewing files
Reviewing 3 files

Action: Summarizing reviews
Summary: 3 files, X total issues, Y high severity, Z medium, etc.

Action: Writing report
Report written to: reports/code_review_report_20260501_143022.md

Action: Sending email report
Email sent successfully to: afeldman66@gmail.com
```

#### Generated Report (`reports/code_review_report_*.md`):
```markdown
# Code Review Report

**Generated:** 2026-05-01 14:30:22
**Project Path:** /path/to/warmest/src/main/java/com/yourcompany/service

## Summary Statistics

- **Files Reviewed:** 3
- **Total Issues:** 12
- **High Severity:** 1
- **Medium Severity:** 3
- **Low Severity:** 8

## Detailed Reviews

### File: `UserService.java`
**Severity:** MEDIUM

#### Issues
- Null pointer risk on line 45
- Missing validation on input parameter
- ...

#### Suggestions
- Add null checks before dereferencing
- Validate user input
- ...
```

#### Email:
- Subject: "Code Review Report - 2026-05-01"
- To: afeldman66@gmail.com
- Body: Full markdown report

---

## Troubleshooting

### Email Not Sending
```
❌ Failed to send email
```

**Check:**
1. Is EMAIL_ENABLED=true in codereview.properties?
2. Is the app password correct? (16 characters with spaces)
3. Did you enable 2FA on Gmail?
4. Check logs: `tail -f logs/codereview-agent.log`

### Connection to Ollama Failed
```
❌ Failed to connect to Ollama at http://localhost:11434
```

**Fix:**
```bash
# Terminal 1
ollama serve

# Terminal 2
ollama pull llama2
```

### Files Not Found
```
No code files found
```

**Check:**
- Is the path correct?
- Does it contain Java files?
- Use absolute path: `/Users/.../warmest/src/...`

---

## Input Format Summary

| Component | Input | Example |
|-----------|-------|---------|
| Command | `review` | `java -jar CodeReviewAgent.jar review ...` |
| Project Path | Absolute path | `/path/to/warmest/src/main/java/com/service` |
| Email Config | Properties file | `codereview.properties` |
| LLM | Ollama local | Running at localhost:11434 |
| Output | Markdown | `reports/code_review_report_*.md` |

---

## Configuration File Location

Place `codereview.properties` in:
```
/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/codereview.properties
```

The app automatically loads it on startup.

---

## Next Steps to Test

1. **Setup Gmail app password** ← START HERE
2. Update `codereview.properties`
3. Run test without email first
4. Check `reports/` directory
5. Run with email enabled
6. Check your Gmail inbox

---

## Questions?

If something isn't working:
1. Check: `logs/codereview-agent.log`
2. Verify Ollama: `ollama serve` running?
3. Check path exists: `ls -la /path/to/warmest`
4. Try without email first: `EMAIL_ENABLED=false`

---

**Ready to test! 🚀**

