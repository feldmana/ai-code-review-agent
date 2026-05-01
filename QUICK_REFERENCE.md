# 🎯 QUICK REFERENCE CARD - CodeReviewAgent Testing

**Print this and keep it handy!**

---

## THE 3 INPUTS (All You Need)

```
┌─────────────────────────────────────────┐
│ INPUT #1: GOOGLE APP PASSWORD           │
├─────────────────────────────────────────┤
│ Where: https://myaccount.google.com/    │
│        apppasswords                     │
│ What: Generate for "CodeReviewAgent"    │
│ Get: 16-character password              │
│ Time: 5 minutes                         │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ INPUT #2: UPDATE CONFIG FILE            │
├─────────────────────────────────────────┤
│ File: codereview.properties              │
│ Add: SMTP_PASSWORD=xxxx xxxx xxxx xxxx  │
│ Time: 1 minute                          │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ INPUT #3: RUN COMMAND                   │
├─────────────────────────────────────────┤
│ Command: java -jar target/CodeReviewAgent│
│          .jar review /path/to/warmest   │
│ Time: 5-30 minutes                      │
└─────────────────────────────────────────┘
```

---

## QUICK SETUP (6 Commands)

```bash
# 1. Get app password (manual - go to Google)
https://myaccount.google.com/apppasswords

# 2. Update config (nano editor)
nano /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/codereview.properties

# 3. Build (Maven)
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent
mvn clean package -DskipTests

# 4. Start Ollama (Terminal 1)
ollama serve

# 5. Pull model (Terminal 2)
ollama pull llama2

# 6. Run review (Terminal 3)
java -jar target/CodeReviewAgent.jar review /path/to/warmest/src/main/java/com/company/service
```

---

## CONFIGURATION SETTINGS

```properties
# Email
EMAIL_ENABLED=true
EMAIL_TO=afeldman66@gmail.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=afeldman66@gmail.com
SMTP_PASSWORD=xxxx xxxx xxxx xxxx    ← YOUR PASSWORD HERE
SMTP_TLS_ENABLED=true

# Performance
MAX_RETRIES=3
THREAD_POOL_SIZE=4
```

---

## PROJECT PATHS

```
📁 Project Root:
/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent

📁 Config File:
codereview.properties

📁 Executable:
target/CodeReviewAgent.jar

📁 Reports:
reports/code_review_report_*.md

📁 Logs:
logs/codereview-agent.log
```

---

## EXPECTED WORKFLOW

```
1. ✓ Command executed
   └─> Console shows progress

2. ✓ Files scanned
   └─> "Found X code files"

3. ✓ Reviews processed
   └─> "Reviewing X files"

4. ✓ Report generated
   └─> "Report written to: reports/..."

5. ✓ Email sent
   └─> "Email sent successfully"

6. ✓ Task complete
   └─> "✓ Task execution completed"
```

---

## SUCCESS CHECKLIST

- [ ] Console shows: "✓ Task execution completed"
- [ ] Report exists: `reports/code_review_report_*.md`
- [ ] Report has content: `wc -l reports/code_review_report_*.md` (50+ lines)
- [ ] Email in inbox: Check afeldman66@gmail.com (wait 1-2 min)
- [ ] Email has report: Subject contains date, body shows issues

---

## TROUBLESHOOTING (3 Common Issues)

### ❌ Email Not Sending
```
FIX: Check password is 16 characters with spaces
grep SMTP_PASSWORD codereview.properties
Should show: SMTP_PASSWORD=xxxx xxxx xxxx xxxx
```

### ❌ No Files Found
```
FIX: Use absolute path (not relative)
ls /Users/alex/projects/warmest/src/main/java/com/*/service/
Should list .java files
```

### ❌ Ollama Connection Failed
```
FIX: Start Ollama in separate terminal
Terminal 1: ollama serve
Terminal 2: ollama pull llama2
Terminal 3: Run review command
```

---

## TIME ESTIMATE

```
Get Password ............ 5 min
Update Config ........... 1 min
Build Project ........... 2 min
Start Ollama ............ 2 min
Find Project Path ....... 2 min
Run Review .............. 5-30 min
Check Results ........... 1 min
────────────────────────────
TOTAL ................... 18-43 min
```

---

## KEY FILES TO KNOW

| File | Purpose |
|------|---------|
| codereview.properties | Configuration with email settings |
| target/CodeReviewAgent.jar | Executable application |
| reports/code_review_report_*.md | Generated markdown report |
| logs/codereview-agent.log | Application logs & details |

---

## EMAIL DETAILS

```
TO: afeldman66@gmail.com
FROM: afeldman66@gmail.com (via CodeReviewAgent)
SUBJECT: Code Review Report - YYYY-MM-DD
BODY: Full markdown report with:
  - File summary
  - Issues per file
  - Suggestions
  - Severity levels (HIGH/MEDIUM/LOW)
```

---

## IMPORTANT NOTES

⚠️ **Gmail Password:**
- NOT your regular Gmail password
- Must be 16-character APP PASSWORD
- Get from: https://myaccount.google.com/apppasswords
- Requires 2FA enabled

⚠️ **Project Path:**
- Must be ABSOLUTE path (not relative)
- Must end with service directory (not root)
- Should contain .java files
- Example: `/Users/alex/warmest/src/main/java/com/service`

⚠️ **Ollama:**
- Must be running in separate terminal
- Use: `ollama serve`
- Pull model: `ollama pull llama2`

---

## USEFUL COMMANDS

```bash
# Check Java version (must be 21+)
java -version

# Check Maven
mvn -version

# Test Ollama connection
curl http://localhost:11434/api/tags

# View report
cat reports/code_review_report_*.md

# View logs
tail -f logs/codereview-agent.log

# List generated reports
ls -lh reports/

# Count files in project
find /path/to/warmest -name "*.java" | wc -l
```

---

## NEXT STEP

**Read:** YOUR_TEST_SETUP.md

**Then:** Follow 5 simple steps

**Result:** Report in email ✅

---

**Good luck! You've got this! 🚀**

*Keep this card handy while testing*

