# CodeReviewAgent - Quick Start Guide

## 🚀 Quick Setup (5 minutes)

### 1. Start Ollama
```bash
ollama serve
# Wait for: "Listening on..."
```

### 2. Ensure Model Downloaded
```bash
ollama pull llama3
# or: ollama pull mistral
```

### 3. Build Project
```bash
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent
mvn clean package -DskipTests -q
```

### 4. Create Config (Optional)
```bash
cat > codereview.properties << 'EOF'
EMAIL_ENABLED=true
EMAIL_TO=afeldman66@gmail.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
SMTP_TLS_ENABLED=true
MAX_RETRIES=3
THREAD_POOL_SIZE=4
EOF
```

---

## ⚡ Quick Commands

### Review Warmest Project (Service Code)
```bash
# Prepare code
mkdir -p /tmp/warmest-review
cp /path/to/warmest/src/main/java/com/example/service/*.java /tmp/warmest-review/

# Run review
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review"

# View results
cat reports/code_review_report_*.md
```

### Review and Send Email
```bash
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review and send email"
```

### Interactive Mode
```bash
java -jar target/CodeReviewAgent.jar

# Then type:
# > review /tmp/warmest-review
# > review /tmp/warmest-review and send email
# > help
# > exit
```

### Test with Sample
```bash
# Create sample
mkdir -p /tmp/test-service
cat > /tmp/test-service/UserService.java << 'EOF'
package com.example.service;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public void saveUser(Long id, String email) {
        if (email.length() > 0) {
            // save user
        }
    }
}
EOF

# Review
java -jar target/CodeReviewAgent.jar "review /tmp/test-service"
```

---

## 📊 What Gets Reviewed

### Automatic Analysis
- ✅ Runtime bugs (NPE, IndexOutOfBounds, etc.)
- ✅ Logic errors
- ✅ Null safety
- ✅ Exception handling
- ✅ Code clarity
- ✅ Design patterns
- ✅ Best practices

### Code Types Detected
- `@Service` → Reviews for microservice patterns
- `@Controller` / `@RestController` → Reviews for REST API patterns
- `@Repository` → Reviews for data access patterns
- `@Entity` → Reviews for domain model patterns
- `@Configuration` → Reviews for Spring config patterns

---

## 📁 Output Locations

```
CodeReviewAgent/
├── reports/                          # Review reports
│   └── code_review_report_*.md      # Latest report
├── logs/
│   └── codereview-agent.log         # Debug logs
└── rag-docs/
    └── rules/                        # Knowledge base
        ├── microservices-design.md
        ├── rest-api-design.md
        ├── repository-data-access.md
        ├── architecture.md
        └── naming.md
```

### View Latest Report
```bash
# macOS/Linux
open reports/code_review_report_*.md

# or
cat reports/code_review_report_*.md | less
```

---

## 🔧 Troubleshooting

### "Failed to connect to Ollama"
```bash
# Check if Ollama is running
curl http://127.0.0.1:11434/api/tags

# If it fails, start Ollama
ollama serve
```

### "No model found"
```bash
# List models
ollama list

# If empty, download one
ollama pull llama3
```

### "Email failed"
```bash
# Check config
cat codereview.properties

# Verify Gmail app password (not regular password)
# https://myaccount.google.com/apppasswords
```

### "No files found"
```bash
# Verify directory
ls -la /path/to/directory | grep .java

# Ensure path is correct
```

---

## 📈 Performance Expectations

| Task | Time | Notes |
|------|------|-------|
| Build JAR | 30-60s | `mvn clean package` |
| Initialize RAG | 1-2s | Load rules and index |
| Review 1 file | 5-30s | Depends on file size and model |
| Review 10 files | 50-300s | Parallel: 4 threads |
| Send email | <5s | Only if configured |

---

## 📚 Rule Categories

Rules are automatically retrieved based on code type:

| Code Type | Rules Retrieved | Examples |
|-----------|-----------------|----------|
| @Service | Microservice Design | SRP, DI, Null Safety, Transactions |
| @Controller | REST API Design | HTTP Methods, Validation, Error Handling |
| @Repository | Repository/Data | Entity Mapping, Queries, Pagination |
| @Entity | Entity Design | Annotations, Relationships, Serialization |
| @Configuration | Configuration | Bean Definitions, Property Injection |
| Test | Testing | Coverage, Mocks, Isolation |
| Interface | Interface Design | Contract definition, Naming |

---

## 🎯 Sample Review Workflow

### Input
```java
@Service
public class UserService {
    private UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userRepository.delete(user);
        }
    }
}
```

### Process
1. Code type detected: `SERVICE`
2. RAG retrieves: `microservices-design.md` (top score)
3. Context added: "Pay attention to business logic, null safety..."
4. Sent to Ollama with full context
5. LLM analyzes against retrieved best practices

### Output (Example)
```
Issues Found: 2
- HIGH: Null pointer risk in deleteUser (returning null)
  Suggestion: Throw UserNotFoundException instead
- MEDIUM: Missing input validation for userId
  Suggestion: Add "if (userId == null || userId <= 0) throw..."

Suggestions:
- Add logging for audit trail
- Consider using Optional pattern
- Add transaction management
```

---

## 💡 Tips & Tricks

### Get More Detailed Output
```bash
# Set logging level
export LOG_LEVEL=DEBUG

java -jar target/CodeReviewAgent.jar "review /path"
```

### Review Specific File Type
```bash
# Only service files
find /path -name "*Service.java" -type f | head -10 > /tmp/files.txt
xargs -I {} cp {} /tmp/services/

java -jar target/CodeReviewAgent.jar "review /tmp/services"
```

### Test RAG Retrieval
```bash
# Check what rules are loaded
grep "Documents loaded" logs/codereview-agent.log

# Check ranking scores
grep "RANK" logs/codereview-agent.log
```

### Adjust Parallel Processing
```bash
cat > codereview.properties << 'EOF'
THREAD_POOL_SIZE=8  # More threads = faster but more resource usage
MAX_RETRIES=5       # More retries for reliability
EOF
```

---

## 🎓 Understanding the Output

### Report Structure
```markdown
# Code Review Report
Generated: 2026-05-06 14:30:00

## Summary
- Total Files Reviewed: 5
- Files with Issues: 4
- Issues by Severity:
  - HIGH: 3
  - MEDIUM: 8
  - LOW: 4

## File Reviews

### UserService.java
**Type Detected**: SERVICE
**Status**: ⚠️ Issues Found

#### Issues (3 found)
1. **HIGH** - Null pointer risk
2. **MEDIUM** - Missing validation
3. **LOW** - Incomplete logging

#### Suggestions
- Add custom exception
- Improve error messages
```

---

## 🚀 Next Steps

1. **Prepare code**
   ```bash
   mkdir -p /tmp/review
   cp your-services/*.java /tmp/review/
   ```

2. **Run review**
   ```bash
   java -jar target/CodeReviewAgent.jar "review /tmp/review"
   ```

3. **Review results**
   ```bash
   cat reports/code_review_report_*.md
   ```

4. **Send email** (if configured)
   ```bash
   java -jar target/CodeReviewAgent.jar "review /tmp/review and send email"
   ```

---

## 📞 Need Help?

- **Check logs**: `tail -f logs/codereview-agent.log`
- **Read full guide**: `TESTING_GUIDE_ENHANCED.md`
- **Understand architecture**: `RAG_ARCHITECTURE_v2.md`
- **Review summary**: `CODE_REVIEW_SUMMARY.md`

---

## ✅ Ready?

```bash
# 1. Start Ollama
ollama serve

# 2. In another terminal
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review"

# 3. Check results
cat reports/code_review_report_*.md
```

**Happy Code Reviewing! 🎉**

