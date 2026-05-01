# 📥 INPUT & CONFIG VISUAL GUIDE

## 🎯 Your Test Case: Review Warmest Project → Send to afeldman66@gmail.com

---

## 📍 INPUT #1: Configuration File

### Location:
```
/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/codereview.properties
```

### Content to Add:

```bash
# ============================================
# EMAIL CONFIGURATION FOR YOUR TEST
# ============================================

# ✅ Enable email sending
EMAIL_ENABLED=true

# ✅ Send report to your Gmail
EMAIL_TO=afeldman66@gmail.com

# ✅ Gmail SMTP settings (DONT CHANGE)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587

# ✅ Your Gmail address
SMTP_USERNAME=afeldman66@gmail.com

# ❗ REPLACE THIS with your 16-char Google App Password
# Get it from: https://myaccount.google.com/apppasswords
# Format: xxxx xxxx xxxx xxxx (with spaces)
SMTP_PASSWORD=xxxx xxxx xxxx xxxx

# ✅ Enable TLS (DONT CHANGE)
SMTP_TLS_ENABLED=true

# ============================================
# PERFORMANCE SETTINGS (OPTIONAL)
# ============================================

# How many times to retry if review fails
MAX_RETRIES=3

# How many files to review in parallel
THREAD_POOL_SIZE=4
```

### Where to Get Your Google App Password:

```
1. Go to: https://myaccount.google.com/apppasswords

2. Select:
   - Device: "Mac"
   - App: "Other (Custom name)"
   - Type: "CodeReviewAgent"

3. Click: Generate

4. Copy: 16-character password (format: xxxx xxxx xxxx xxxx)

5. Paste into: SMTP_PASSWORD=xxxx xxxx xxxx xxxx
```

---

## 🖥️ INPUT #2: Command Line

### Option A: CLI Mode (One Command)

```bash
# Navigate to project
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent

# Run review command
java -jar target/CodeReviewAgent.jar review /path/to/warmest/src/main/java/com/company/service
```

**Replace** `/path/to/warmest/src/main/java/com/company/service` with your actual path

**Example:**
```bash
java -jar target/CodeReviewAgent.jar review /Users/alex/projects/warmest/src/main/java/com/mycompany/service
```

---

### Option B: Interactive Mode (Multiple Commands)

```bash
# Start application
java -jar target/CodeReviewAgent.jar

# At prompt, type:
CodeReviewAgent> review /path/to/warmest/src/main/java/com/company/service

# For help:
CodeReviewAgent> help

# To exit:
CodeReviewAgent> exit
```

---

## 🔍 How to Find Your Project Path

### Step 1: Find Warmest Directory

```bash
# Search for warmest project
find ~ -type d -name "warmest" 2>/dev/null

# Example output:
/Users/alex/projects/warmest
/Users/alex/warmest
/home/alex/workspace/warmest
```

### Step 2: Find Service Layer

```bash
# Go to warmest directory
cd /Users/alex/projects/warmest

# Look for service code
find . -type d -name "service"

# Or specifically:
ls src/main/java/com/*/service/

# Should show files like:
# UserService.java
# OrderService.java
# PaymentService.java
```

### Step 3: Get Full Path

```bash
# Get absolute path of service directory
pwd
# /Users/alex/projects/warmest

# Full path for command:
# /Users/alex/projects/warmest/src/main/java/com/company/service
```

---

## 🏗️ INPUT FLOW DIAGRAM

```
┌─────────────────────────────────────────────────────────┐
│                   YOU RUN COMMAND                       │
│  java -jar CodeReviewAgent.jar review /path/to/warmest  │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
        ┌────────────────────────────┐
        │ CodeReviewAgent Reads:      │
        │                            │
        │ 1. Command: "review"       │
        │ 2. Project Path: /path     │
        └────────────────┬───────────┘
                         │
                         ↓
        ┌────────────────────────────────┐
        │ Loads codereview.properties:   │
        │                                │
        │ EMAIL_ENABLED=true             │
        │ EMAIL_TO=afeldman66@gmail.com  │
        │ SMTP_PASSWORD=xxxx xxxx...    │
        └────────────────┬───────────────┘
                         │
                         ↓
        ┌────────────────────────────────┐
        │ Connects to Ollama:            │
        │ localhost:11434                │
        └────────────────┬───────────────┘
                         │
                         ↓
        ┌────────────────────────────────┐
        │ Scans for Java files:          │
        │ /path/to/warmest/service/*.java│
        └────────────────┬───────────────┘
                         │
                         ↓
        ┌────────────────────────────────┐
        │ Reviews each file with Ollama  │
        └────────────────┬───────────────┘
                         │
                         ↓
        ┌────────────────────────────────┐
        │ Generates markdown report      │
        └────────────────┬───────────────┘
                         │
                         ↓
        ┌────────────────────────────────┐
        │ Sends email via SMTP:          │
        │ To: afeldman66@gmail.com       │
        │ With: Report attached          │
        └────────────────┬───────────────┘
                         │
                         ↓
        ┌────────────────────────────────┐
        │ Output Generated:              │
        │ ✓ reports/code_review_*.md    │
        │ ✓ Email sent                   │
        │ ✓ logs/codereview-agent.log   │
        └────────────────────────────────┘
```

---

## ✅ Configuration Checklist

### Step 1: Gmail Setup (5 min)
- [ ] Go to: https://myaccount.google.com/security
- [ ] Enable 2-Step Verification (if not done)
- [ ] Go to: https://myaccount.google.com/apppasswords
- [ ] Generate app password for "CodeReviewAgent"
- [ ] Copy 16-character password

### Step 2: Update Configuration (1 min)
- [ ] Open: `codereview.properties`
- [ ] Update: `SMTP_PASSWORD=xxxx xxxx xxxx xxxx`
- [ ] Save file

### Step 3: Project Setup (2 min)
- [ ] Find warmest project: `find ~ -type d -name "warmest"`
- [ ] Find service code: `ls /path/to/warmest/src/main/java/com/*/service/`
- [ ] Get full path ready

### Step 4: Build (2 min)
- [ ] Run: `mvn clean package -DskipTests`
- [ ] Verify: `ls target/CodeReviewAgent.jar`

### Step 5: Ollama (2 min)
- [ ] Start Ollama: `ollama serve`
- [ ] Pull model: `ollama pull llama2`
- [ ] Test: `curl http://localhost:11434/api/tags`

### Step 6: Run Test (5-30 min)
- [ ] Run: `java -jar target/CodeReviewAgent.jar review /path/...`
- [ ] Wait for: "Email sent successfully"
- [ ] Check: `cat reports/code_review_report_*.md`
- [ ] Check Gmail: afeldman66@gmail.com

---

## 🎯 Quick Copy-Paste Setup

### 1. Edit codereview.properties

```bash
nano /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/codereview.properties
```

### 2. Replace with This Content:

```properties
EMAIL_ENABLED=true
EMAIL_TO=afeldman66@gmail.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=afeldman66@gmail.com
SMTP_PASSWORD=YOUR_16_CHAR_PASSWORD_HERE
SMTP_TLS_ENABLED=true
MAX_RETRIES=3
THREAD_POOL_SIZE=4
```

**Replace:** `YOUR_16_CHAR_PASSWORD_HERE` with your actual Google app password

### 3. Save & Exit
- Press: `Ctrl + O`
- Press: `Enter`
- Press: `Ctrl + X`

### 4. Run Command

```bash
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent
java -jar target/CodeReviewAgent.jar review /YOUR/ACTUAL/PATH/to/warmest/src/main/java/com/service
```

---

## 📊 Input Summary

| Component | Input Value | Type | Example |
|-----------|------------|------|---------|
| **Config File** | codereview.properties | File | `EMAIL_ENABLED=true` |
| **Email Address** | afeldman66@gmail.com | Email | `EMAIL_TO=afeldman66@gmail.com` |
| **Gmail Password** | 16-char app password | Text | `SMTP_PASSWORD=xxxx xxxx xxxx xxxx` |
| **Command** | review | Text | `java -jar ... review` |
| **Project Path** | /path/to/warmest/service | Path | `/Users/alex/.../service` |
| **Ollama** | localhost:11434 | URL | Running at port 11434 |

---

## 🔧 Environment Variables (Alternative)

If you don't want to use properties file:

```bash
export EMAIL_ENABLED=true
export EMAIL_TO=afeldman66@gmail.com
export SMTP_USERNAME=afeldman66@gmail.com
export SMTP_PASSWORD="xxxx xxxx xxxx xxxx"
export MAX_RETRIES=3
export THREAD_POOL_SIZE=4

java -jar target/CodeReviewAgent.jar review /path/to/warmest/service
```

---

## ✨ What Gets Sent to Gmail

### Email Details:

| Field | Value |
|-------|-------|
| **To** | afeldman66@gmail.com |
| **Subject** | Code Review Report - 2026-05-01 |
| **From** | afeldman66@gmail.com (sent via CodeReviewAgent) |
| **Body Format** | Markdown (also converted to HTML) |
| **Attachment** | None (body only) |

### Email Body Example:

```markdown
# Code Review Report

**Generated:** 2026-05-01 14:30:22
**Project Path:** /path/to/warmest/src/main/java/com/service

## Summary Statistics

- **Files Reviewed:** 8
- **Total Issues:** 42
- **High Severity:** 2
- **Medium Severity:** 5
- **Low Severity:** 35

## Detailed Reviews

### File: `UserService.java`
**Severity:** HIGH

#### Issues
- Null pointer risk on line 45
- Missing input validation
...
```

---

## 🚀 You're Ready!

### Your Action Plan:

1. **Get Google App Password** (5 min)
   - Visit: https://myaccount.google.com/apppasswords
   - Generate for "CodeReviewAgent"
   - Copy 16-char password

2. **Update codereview.properties** (1 min)
   - Edit: `/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/codereview.properties`
   - Update: `SMTP_PASSWORD=xxxx xxxx xxxx xxxx`

3. **Build Project** (2 min)
   - Run: `mvn clean package -DskipTests`

4. **Start Ollama** (2 min)
   - Run: `ollama serve`
   - Pull: `ollama pull llama2`

5. **Run Review** (5-30 min)
   - Run: `java -jar target/CodeReviewAgent.jar review /path/to/warmest/service`

6. **Check Results**
   - Report: `reports/code_review_report_*.md`
   - Email: Check afeldman66@gmail.com

**All inputs needed are above! Start now! 🎉**

