# ✅ TESTING CHECKLIST - CodeReviewAgent for Warmest Project

Print this and check off as you go!

---

## 📋 PRE-TEST CHECKLIST

### Prerequisites
- [ ] Java 21+ installed: `java -version`
- [ ] Maven installed: `mvn -version`
- [ ] Ollama installed
- [ ] Gmail account with 2FA enabled

### Project Files Ready
- [ ] CodeReviewAgent built: `target/CodeReviewAgent.jar` exists
- [ ] Configuration file exists: `codereview.properties`
- [ ] Warmest project path identified

---

## 🔑 STEP 1: GMAIL APP PASSWORD (5 min)

### Get Password
- [ ] Go to: https://myaccount.google.com/security
- [ ] Verify 2FA is enabled
- [ ] Go to: https://myaccount.google.com/apppasswords
- [ ] Device: Select "Mac"
- [ ] App: Select "Other (custom name)" → Type "CodeReviewAgent"
- [ ] Generate
- [ ] **Copy the 16-character password**: `_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _`

**Write password here (for reference):**
```
SMTP_PASSWORD = ___________________________
```

---

## ⚙️ STEP 2: UPDATE CONFIGURATION (1 min)

### Edit Config File
- [ ] Open: `/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/codereview.properties`

### Update These Fields
- [ ] `EMAIL_ENABLED=true`
- [ ] `EMAIL_TO=afeldman66@gmail.com`
- [ ] `SMTP_HOST=smtp.gmail.com`
- [ ] `SMTP_PORT=587`
- [ ] `SMTP_USERNAME=afeldman66@gmail.com`
- [ ] `SMTP_PASSWORD=` [PASTE YOUR 16-CHAR PASSWORD]
- [ ] `SMTP_TLS_ENABLED=true`
- [ ] `MAX_RETRIES=3`
- [ ] `THREAD_POOL_SIZE=4`

### Verify
- [ ] Save file
- [ ] File exists: `ls codereview.properties`
- [ ] Password is 16 characters: `grep SMTP_PASSWORD codereview.properties`

---

## 🔨 STEP 3: BUILD PROJECT (2 min)

### Navigate & Build
- [ ] `cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent`
- [ ] Run: `mvn clean package -DskipTests`
- [ ] Wait for: `[INFO] BUILD SUCCESS`

### Verify Artifact
- [ ] JAR file exists: `ls -lh target/CodeReviewAgent.jar`
- [ ] Size is ~4.7 MB

---

## 🚀 STEP 4: START OLLAMA (2 min)

### Terminal 1: Start Server
- [ ] Run: `ollama serve`
- [ ] Wait for: "Listening on..."

### Terminal 2: Pull Model
- [ ] Run: `ollama pull llama2`
- [ ] Wait for completion

### Terminal 3: Test Connection
- [ ] Run: `curl http://localhost:11434/api/tags`
- [ ] Should see models listed

---

## 📍 STEP 5: IDENTIFY PROJECT PATH (2 min)

### Find Warmest Project
- [ ] Run: `find ~ -type d -name "warmest" 2>/dev/null`
- [ ] Found at: `________________________________`

### Find Service Code
- [ ] Navigate: `cd /path/to/warmest`
- [ ] Run: `find . -type d -name "service"`
- [ ] Found at: `________________________________`

### Get Full Path
- [ ] Run: `ls /path/to/warmest/src/main/java/com/*/service/*.java`
- [ ] See Java files listed

### Full Command Path
```
java -jar target/CodeReviewAgent.jar review ________________________________
```

---

## 🧪 STEP 6: TEST WITHOUT EMAIL (5 min)

### Run Review (No Email First)
- [ ] Run: `export EMAIL_ENABLED=false`
- [ ] Run: `java -jar target/CodeReviewAgent.jar review /YOUR/PATH/service`
- [ ] Wait for: "✓ Task execution completed"

### Verify Output
- [ ] Check logs: `tail -f logs/codereview-agent.log`
- [ ] See "Found X code files"
- [ ] See "Reviewing X files"
- [ ] See "Report written to: reports/code_review_report_*.md"

### Check Report
- [ ] Report exists: `ls reports/code_review_report_*.md`
- [ ] Has content: `wc -l reports/code_review_report_*.md` (50+ lines)
- [ ] View report: `cat reports/code_review_report_*.md` | head -30

---

## 📧 STEP 7: TEST WITH EMAIL (5 min)

### Run Review (With Email)
- [ ] Run: `java -jar target/CodeReviewAgent.jar review /YOUR/PATH/service`
- [ ] Wait for: "Email sent successfully to: afeldman66@gmail.com"
- [ ] See: "✓ Task execution completed"

### Check Email
- [ ] Open Gmail: https://mail.google.com
- [ ] Go to: afeldman66@gmail.com inbox
- [ ] Wait 1-2 minutes for email
- [ ] Check for: "Code Review Report - 2026-05-01"
- [ ] Open email and verify content

### Verify Email Content
- [ ] Subject contains date
- [ ] Body has markdown report
- [ ] Shows file count
- [ ] Shows issue count
- [ ] Shows severity levels

---

## ✅ FINAL VERIFICATION

### All Components Working
- [ ] Ollama responding: `curl http://localhost:11434/api/tags`
- [ ] JAR file built: `ls target/CodeReviewAgent.jar`
- [ ] Config loaded: `grep EMAIL_ENABLED codereview.properties`
- [ ] Report generated: `ls reports/code_review_report_*.md`
- [ ] Email received: Check afeldman66@gmail.com

### Performance
- [ ] Review time: _______ minutes
- [ ] File count: _______ files
- [ ] Issue count: _______ issues

### Output Files
- [ ] Report file: `reports/code_review_report_20260501_*.md`
- [ ] Log file: `logs/codereview-agent.log`
- [ ] Email inbox: afeldman66@gmail.com

---

## 🎯 SUCCESS INDICATORS

All should be checked:
- [ ] Console shows "✓ Task execution completed"
- [ ] Report file created in reports/
- [ ] Email received at afeldman66@gmail.com
- [ ] Report contains issue summary
- [ ] Report lists reviewed files
- [ ] No errors in logs

---

## 🆘 TROUBLESHOOTING

### Email Not Sending?
- [ ] Check password has 16 chars: `grep SMTP_PASSWORD codereview.properties`
- [ ] Check EMAIL_ENABLED=true: `grep EMAIL_ENABLED codereview.properties`
- [ ] Check Gmail 2FA enabled: https://myaccount.google.com/security
- [ ] View error: `tail -20 logs/codereview-agent.log | grep -i email`

### No Files Found?
- [ ] Verify path exists: `ls /your/path`
- [ ] Check for Java files: `find /your/path -name "*.java"`
- [ ] Use absolute path (not relative)

### Ollama Connection Failed?
- [ ] Terminal 1: `ollama serve` running?
- [ ] Terminal 2: `ollama pull llama2` complete?
- [ ] Test: `curl http://localhost:11434/api/tags`

### Build Failed?
- [ ] Check Java version: `java -version` (must be 21+)
- [ ] Check Maven: `mvn -version` (must be 3.8+)
- [ ] Clean rebuild: `mvn clean package -DskipTests`

---

## 📝 NOTES

```
Test Date: _________________
Warmest Project Path: _______________________________
Warmest Service Path: _______________________________
Number of Files Reviewed: ___________________________
Number of Issues Found: ______________________________
Highest Severity: __________________________________
Email Sent To: afeldman66@gmail.com
Email Received Time: _________________________________
Test Status: [ ] PASSED  [ ] FAILED
Issues Encountered: ________________________________
```

---

## 🎉 COMPLETION STATUS

- [ ] All steps completed
- [ ] Email received successfully
- [ ] Report contains valuable insights
- [ ] Ready for production use

**Date Completed:** _______________  
**Tester Name:** _______________  
**Status:** ☐ READY / ☐ NEEDS FIXES

---

## 📞 QUICK REFERENCE

| Step | Command | Time |
|------|---------|------|
| 1 | Get app password | 5 min |
| 2 | Update config | 1 min |
| 3 | Build: `mvn clean package` | 2 min |
| 4 | Start Ollama + model | 2 min |
| 5 | Find project path | 2 min |
| 6 | Test without email | 5 min |
| 7 | Test with email | 5 min |
| **TOTAL** | | **22 min** |

Plus: 5-30 minutes for actual code review

---

**Good luck! You've got this! 🚀**

