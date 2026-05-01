# CodeReviewAgent - Input Reference & Configuration Guide

## 📥 What Input Do You Need?

### Input Type 1: Command Line Arguments

```bash
java -jar target/CodeReviewAgent.jar review /path/to/warmest/src/main/java/com/company/service
                                    ^^^^^^  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                                   Command              Project Path
```

| Input | Required | Type | Example |
|-------|----------|------|---------|
| **Command** | YES | Text | `review` |
| **Project Path** | YES | File Path | `/Users/alex/warmest/src/main/java/com/service` |

---

### Input Type 2: Configuration File

Create `codereview.properties` in project root:

```properties
# Email Settings (for sending reports)
EMAIL_ENABLED=true
EMAIL_TO=afeldman66@gmail.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=afeldman66@gmail.com
SMTP_PASSWORD=xxxx xxxx xxxx xxxx    # Google app password (16 chars)
SMTP_TLS_ENABLED=true

# Performance Settings
MAX_RETRIES=3                        # Retry failed reviews
THREAD_POOL_SIZE=4                   # Parallel threads
```

**Location**: `/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/codereview.properties`

---

### Input Type 3: Environment Variables

Alternative to properties file:

```bash
export EMAIL_ENABLED=true
export EMAIL_TO=afeldman66@gmail.com
export SMTP_USERNAME=afeldman66@gmail.com
export SMTP_PASSWORD="your-16-char-app-password"
export MAX_RETRIES=3
export THREAD_POOL_SIZE=4
```

---

## 🔑 Step 1: Get Gmail App Password

Since you want to send to **afeldman66@gmail.com**:

### Requirement:
- Gmail account with 2-Factor Authentication enabled

### Steps:

#### 1A. Enable 2FA (if not already done)
```
https://myaccount.google.com/security
→ 2-Step Verification
→ Enable
```

#### 1B. Generate App Password
```
https://myaccount.google.com/apppasswords
→ Select app: "Other (custom name)"
→ Enter: "CodeReviewAgent"
→ Select device: "Mac"
→ Click Generate
→ Copy 16-character password (format: xxxx xxxx xxxx xxxx)
```

#### 1C. Update Config
```properties
SMTP_PASSWORD=xxxx xxxx xxxx xxxx
```

**⚠️ Important**: This is NOT your Google password, it's the 16-char app password!

---

## 📋 Complete Testing Checklist

### Before You Start:
- [ ] Java 21+ installed
- [ ] Maven 3.8+ installed
- [ ] Ollama installed
- [ ] 2FA enabled on Gmail
- [ ] Google app password generated
- [ ] Know your project path

### Configuration Setup:
```bash
# 1. Navigate to project
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent

# 2. Update codereview.properties with:
#    - EMAIL_ENABLED=true
#    - EMAIL_TO=afeldman66@gmail.com
#    - SMTP_PASSWORD=your-16-char-app-password

# 3. Verify configuration
cat codereview.properties
```

### Build:
```bash
mvn clean package -DskipTests
# Creates: target/CodeReviewAgent.jar
```

### Run Tests:

**Test 1: Without Email (Recommended First)**
```bash
export EMAIL_ENABLED=false
java -jar target/CodeReviewAgent.jar review /path/to/warmest/src/main/java/com/company/service
```

**Test 2: With Email to Gmail**
```bash
java -jar target/CodeReviewAgent.jar review /path/to/warmest/src/main/java/com/company/service
```

---

## 🎯 Your Specific Test Case

### Review: Warmest Project Service Layer
### Send to: afeldman66@gmail.com

### Setup Steps:

#### Step 1: Find Warmest Project
```bash
# Find where your warmest project is located
# Example possibilities:
find ~ -type d -name "warmest" 2>/dev/null | head -5

# Result might be:
# /Users/alex/projects/warmest
# /Users/alex/warmest
# ~/Documents/warmest
```

#### Step 2: Identify Service Code
```bash
# Look for service layer
ls -la /path/to/warmest/src/main/java/com/*/service/

# You should see files like:
# UserService.java
# OrderService.java
# PaymentService.java
# etc.
```

#### Step 3: Update Configuration
```bash
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent

# Edit codereview.properties
# Change:
# EMAIL_ENABLED=true
# EMAIL_TO=afeldman66@gmail.com
# SMTP_PASSWORD=xxxx xxxx xxxx xxxx
```

#### Step 4: Build
```bash
mvn clean package -DskipTests
```

#### Step 5: Test
```bash
# Test without email first
export EMAIL_ENABLED=false
java -jar target/CodeReviewAgent.jar review /path/to/warmest/src/main/java/com/company/service

# Then with email
java -jar target/CodeReviewAgent.jar review /path/to/warmest/src/main/java/com/company/service
```

#### Step 6: Check Results
```bash
# View generated report
cat reports/code_review_report_*.md

# Check email inbox
# afeldman66@gmail.com should receive the report
```

---

## 📝 Input Summary Table

### Required Inputs

| What | Where | Format | Example |
|-----|-------|--------|---------|
| **Command** | Command line | Text | `review` |
| **Project Path** | Command line | Absolute path | `/Users/alex/warmest/src/main/java/com/service` |
| **Email Address** | codereview.properties | Email | `afeldman66@gmail.com` |
| **Gmail App Password** | codereview.properties | 16 chars with spaces | `xxxx xxxx xxxx xxxx` |

### Optional Inputs

| What | Where | Default | Example |
|-----|-------|---------|---------|
| **Email Enabled** | codereview.properties | false | `true` |
| **Max Retries** | codereview.properties | 3 | `5` |
| **Thread Pool Size** | codereview.properties | 4 | `8` |
| **SMTP Port** | codereview.properties | 587 | `587` |
| **SMTP Host** | codereview.properties | smtp.gmail.com | `smtp.gmail.com` |

---

## 🚀 Quick Start Command

```bash
# All-in-one test
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent

# Build
mvn clean package -DskipTests

# Run (replace with your actual warmest path)
java -jar target/CodeReviewAgent.jar review /path/to/warmest/src/main/java/com/company/service
```

---

## ✅ What Happens When You Run It?

### Input Flow:
```
1. Read command line: "review"
2. Read project path: "/path/to/warmest/..."
3. Load config from: codereview.properties
   ├─ EMAIL_ENABLED=true
   ├─ EMAIL_TO=afeldman66@gmail.com
   ├─ SMTP_USERNAME=afeldman66@gmail.com
   └─ SMTP_PASSWORD=your-app-password
4. Connect to Ollama at: localhost:11434
```

### Processing:
```
1. RouterAgent: Classify as REVIEW_CODE
2. PlannerAgent: Create plan (scan → review → summarize → write → email)
3. Scan: Find all .java files in service directory
4. Review: Send each file to Ollama
5. Summarize: Aggregate all findings
6. Write: Generate markdown report
7. Email: Send to afeldman66@gmail.com
```

### Output:
```
✓ Console: Detailed progress
✓ File: reports/code_review_report_20260501_*.md
✓ Email: Sent to afeldman66@gmail.com
✓ Logs: logs/codereview-agent.log
```

---

## 🔍 How to Find Your Warmest Project

```bash
# Search for warmest project
find ~ -type d -name "warmest" 2>/dev/null

# Or if it's in a specific location
ls -la ~/Documents/
ls -la ~/projects/
ls -la ~/workspace/

# Once found, navigate to service code
cd /path/to/warmest
find . -type d -name "service"
ls src/main/java/com/*/service/
```

---

## 📧 Email Testing

### To Test Email Without Reviewing Code:
```bash
# Just update codereview.properties with email config
# The system will:
# 1. Generate a test report
# 2. Send it to afeldman66@gmail.com
```

### To Debug Email Issues:
```bash
# Check logs
tail -f logs/codereview-agent.log | grep -i email

# Common issues:
# - App password incorrect
# - 2FA not enabled
# - Email address wrong
# - Wrong SMTP settings
```

---

## 💡 Pro Tips

1. **Test without email first**: Set `EMAIL_ENABLED=false`
2. **Use service layer only**: Smaller set of files for testing
3. **Check logs**: `tail -f logs/codereview-agent.log`
4. **View report locally**: `cat reports/code_review_report_*.md`
5. **Parallel vs Sequential**: Increase `THREAD_POOL_SIZE` for speed

---

## ❓ Common Questions

### Q: Where do I put codereview.properties?
A: In the CodeReviewAgent project root:
```
/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/codereview.properties
```

### Q: What if I don't have warmest project?
A: Review any Java project:
```bash
java -jar target/CodeReviewAgent.jar review /path/to/any/java/project
```

### Q: Can I test without email?
A: Yes! Just set `EMAIL_ENABLED=false` or don't create codereview.properties

### Q: How long does review take?
A: Depends on:
- Number of files
- File sizes
- Model used (mistral is faster than llama2)
- CPU speed

---

**Now you have all the inputs you need! 🚀**

Start with Step 1 (Gmail app password) and follow the checklist.

