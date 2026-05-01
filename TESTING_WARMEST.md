# 🧪 CodeReviewAgent - Testing Your Warmest Project

## Your Test Goal
✅ Review **Warmest project** service code  
✅ Send report to **afeldman66@gmail.com**  
✅ Test email integration

---

## 🎯 What You Need (Input Checklist)

### Required Inputs:

```
1. Project Path
   └─ Where your warmest project service code is located
      Example: /Users/alex/projects/warmest/src/main/java/com/mycompany/service

2. Email Configuration
   ├─ EMAIL_ENABLED=true
   ├─ EMAIL_TO=afeldman66@gmail.com
   ├─ SMTP_USERNAME=afeldman66@gmail.com
   └─ SMTP_PASSWORD=your-16-char-google-app-password

3. Ollama Running
   └─ Running on localhost:11434 with a model (llama2 or mistral)
```

---

## ⚡ Quick Setup (5 Steps)

### Step 1: Get Gmail App Password (2 minutes)

**Why?** Gmail requires an app-specific password instead of your account password.

**How:**

a) Enable 2FA (if not done):
   - Go to: https://myaccount.google.com/security
   - Enable 2-Step Verification

b) Generate App Password:
   - Go to: https://myaccount.google.com/apppasswords
   - Device: Select "Mac"
   - App: Select "Other (Custom name)" → Type "CodeReviewAgent"
   - Click **Generate**
   - **Copy the 16-character password** (format: `xxxx xxxx xxxx xxxx`)

c) **DO NOT use your Gmail password** - use ONLY the 16-char app password!

---

### Step 2: Update Configuration (1 minute)

Edit this file with your app password:

**File:** `/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/codereview.properties`

```properties
# Email Configuration
EMAIL_ENABLED=true
EMAIL_TO=afeldman66@gmail.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=afeldman66@gmail.com
SMTP_PASSWORD=xxxx xxxx xxxx xxxx    ← PASTE YOUR 16-CHAR PASSWORD HERE
SMTP_TLS_ENABLED=true

# Performance
MAX_RETRIES=3
THREAD_POOL_SIZE=4
```

**Important:**
- Keep the 16-character password with spaces
- Save the file

---

### Step 3: Build the Project (2 minutes)

```bash
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent
mvn clean package -DskipTests
```

**Expected output:**
```
[INFO] Building CodeReviewAgent 1.0-SNAPSHOT
...
[INFO] BUILD SUCCESS
```

**Artifact created:** `target/CodeReviewAgent.jar` (~4.7 MB)

---

### Step 4: Start Ollama (2 minutes)

**Terminal 1:**
```bash
ollama serve
```

**Terminal 2:**
```bash
ollama pull llama2
# or faster:
ollama pull mistral
```

**Test connection:**
```bash
curl http://localhost:11434/api/tags
```

---

### Step 5: Run the Review (5-30 minutes)

**Find your warmest project service code:**
```bash
# Find warmest directory
find ~ -type d -name "warmest" 2>/dev/null

# Example: /Users/alex/projects/warmest
# Navigate to service code
ls /Users/alex/projects/warmest/src/main/java/com/mycompany/service/
```

**Run the review:**
```bash
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent

# First test WITHOUT email
export EMAIL_ENABLED=false
java -jar target/CodeReviewAgent.jar review /path/to/warmest/src/main/java/com/mycompany/service

# Then WITH email (if first test works)
java -jar target/CodeReviewAgent.jar review /path/to/warmest/src/main/java/com/mycompany/service
```

**Replace** `/path/to/warmest/...` with your actual path!

---

## 📊 Expected Results

### Console Output:
```
=================================
  CodeReviewAgent v1.0
  AI-Powered Code Review System
=================================

✓ Connected to Ollama successfully

Action: Scanning files
Found 8 code files

Action: Reviewing files
Reviewing 8 files (using 4 threads)

Action: Summarizing reviews
Summary: 8 files, 42 total issues, 2 high, 5 medium, 35 low

Action: Writing report
Report written to: reports/code_review_report_20260501_143022.md

Action: Sending email report
Email sent successfully to: afeldman66@gmail.com

✓ Task execution completed
```

### Generated Files:

**1. Markdown Report:**
```
reports/code_review_report_20260501_143022.md
```

Contains:
```markdown
# Code Review Report

**Generated:** 2026-05-01 14:30:22
**Project Path:** /path/to/warmest/...

## Summary Statistics
- Files Reviewed: 8
- Total Issues: 42
- High Severity: 2
- Medium Severity: 5
- Low Severity: 35

## Detailed Reviews

### File: `UserService.java`
**Severity:** HIGH

#### Issues
- Null pointer risk on line 45
- Missing input validation
...
```

**2. Email:**
- **To:** afeldman66@gmail.com
- **Subject:** Code Review Report - 2026-05-01
- **Body:** Full markdown report

**3. Logs:**
```
logs/codereview-agent.log
```

---

## ✅ Verification Checklist

### Before Running:
- [ ] Java 21+: `java -version`
- [ ] Maven: `mvn -version`
- [ ] Ollama running: Terminal shows "Listening on..."
- [ ] Model available: `ollama pull llama2`
- [ ] Gmail 2FA enabled
- [ ] App password generated (16 characters)
- [ ] `codereview.properties` updated with password
- [ ] Project path found: `ls /path/to/warmest/...`

### After Running:
- [ ] Console shows "✓ Task execution completed"
- [ ] Report file exists: `ls reports/code_review_report_*.md`
- [ ] Report has content: `wc -l reports/code_review_report_*.md` (should be 50+ lines)
- [ ] Email received in afeldman66@gmail.com (check 1-2 minutes)
- [ ] Email subject matches pattern: "Code Review Report - YYYY-MM-DD"

---

## 🆘 Troubleshooting

### Problem: "Failed to connect to Ollama"
```
❌ Failed to connect to Ollama at http://localhost:11434
```

**Solution:**
```bash
# Terminal 1: Start Ollama
ollama serve

# Terminal 2: Pull model
ollama pull llama2

# Terminal 3: Test connection
curl http://localhost:11434/api/tags
```

---

### Problem: "No code files found"
```
❌ No code files found
```

**Solution:**
- Use ABSOLUTE path (not relative)
- Make sure directory contains .java files
- Example: `/Users/alex/projects/warmest/src/main/java/com/service`

```bash
# Verify path
ls /path/to/warmest/src/main/java/com/service/*.java
# Should list Java files
```

---

### Problem: "Email not sending"
```
❌ Failed to send email
```

**Check:**
```bash
# 1. Is EMAIL_ENABLED=true?
grep EMAIL_ENABLED codereview.properties

# 2. Is app password correct? (16 chars, with spaces)
grep SMTP_PASSWORD codereview.properties

# 3. Is 2FA enabled on Gmail?
# Check: https://myaccount.google.com/security

# 4. Check logs
tail -f logs/codereview-agent.log | grep -i email
```

---

### Problem: "JSON parse error from Ollama"
```
❌ Failed to parse response from Ollama
```

**Solution:**
- Try different model: `ollama pull mistral`
- Make sure llama2 is fully downloaded: `ollama show llama2`

---

## 📝 Input Configuration Summary

### What Gets Loaded:

1. **From Command Line:**
   - Command: `review`
   - Project Path: `/path/to/warmest/src/main/java/com/service`

2. **From codereview.properties:**
   - `EMAIL_ENABLED` → Enable email sending
   - `EMAIL_TO` → Recipient (afeldman66@gmail.com)
   - `SMTP_USERNAME` → Gmail address
   - `SMTP_PASSWORD` → 16-char app password
   - `MAX_RETRIES` → Retry failed reviews
   - `THREAD_POOL_SIZE` → Parallel threads

3. **From Ollama:**
   - Model: llama2 (or mistral)
   - Connection: localhost:11434

---

## 🎬 Step-by-Step Test Run

```bash
# 1. Check prerequisites
java -version          # Must be 21+
mvn -version          # Must be 3.8+
ollama --version      # Make sure installed

# 2. Update configuration
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent
nano codereview.properties
# Update SMTP_PASSWORD with your 16-char app password
# Save: Ctrl+O, Enter, Ctrl+X

# 3. Build
mvn clean package -DskipTests

# 4. Start Ollama (in separate terminal)
ollama serve

# 5. Pull model (in another terminal)
ollama pull llama2

# 6. Test without email first
export EMAIL_ENABLED=false
java -jar target/CodeReviewAgent.jar review /Users/alex/projects/warmest/src/main/java/com/service
# Wait for: "✓ Task execution completed"
# Check: ls reports/code_review_report_*.md

# 7. Test with email
java -jar target/CodeReviewAgent.jar review /Users/alex/projects/warmest/src/main/java/com/service
# Wait for: "Email sent successfully"
# Check Gmail: afeldman66@gmail.com (inbox or spam)

# 8. View results
cat reports/code_review_report_*.md
tail -f logs/codereview-agent.log
```

---

## 🎯 Your Next Action

1. **Generate Google App Password** (5 min)
   - Go to: https://myaccount.google.com/apppasswords
   - Get your 16-character password

2. **Update codereview.properties** (1 min)
   - Edit: `/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/codereview.properties`
   - Paste your app password

3. **Build Project** (2 min)
   - Run: `mvn clean package -DskipTests`

4. **Run Review** (5-30 min)
   - Run: `java -jar target/CodeReviewAgent.jar review /path/to/warmest/service`

5. **Check Results** (1 min)
   - Report: `reports/code_review_report_*.md`
   - Email: Check afeldman66@gmail.com

---

**You're all set! Start with the Google App Password. 🚀**

