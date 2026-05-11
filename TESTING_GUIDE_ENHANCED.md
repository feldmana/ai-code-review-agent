# CodeReviewAgent - Testing & Input Guide

## 📋 Prerequisites

Before running the CodeReviewAgent, ensure you have:

1. **Ollama Running Locally**
   ```bash
   ollama serve
   ```
   Default: `http://127.0.0.1:11434`

2. **Ollama Model**
   ```bash
   ollama pull llama3
   # or: ollama pull mistral
   ```

3. **Java 21+**
   ```bash
   java -version
   ```

4. **Built JAR File**
   ```bash
   mvn clean package -DskipTests
   ```

---

## 🎯 Example 1: Review Warmest Project (Service Code Only)

### Configuration Setup

Create/Update `codereview.properties` in project root:

```properties
# Ollama Configuration
# OLLAMA_HOST=http://localhost
# OLLAMA_PORT=11434
# OLLAMA_MODEL=llama3

# Email Configuration
EMAIL_ENABLED=true
EMAIL_TO=afeldman66@gmail.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
SMTP_TLS_ENABLED=true

# Review Configuration
MAX_RETRIES=3
THREAD_POOL_SIZE=4
```

### Step 1: Prepare Input

Locate your Warmest project service code:
```bash
WARMEST_PROJECT=/path/to/warmest-project
SERVICE_CODE_PATH=$WARMEST_PROJECT/src/main/java/com/example/service
```

For testing, you can copy service files to a test directory:
```bash
mkdir -p /tmp/warmest-review
cp $SERVICE_CODE_PATH/*.java /tmp/warmest-review/
```

### Step 2: Run Code Review

```bash
# CLI Mode - Direct
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review"

# Or Interactive Mode
java -jar target/CodeReviewAgent.jar

# Then type:
# > review /tmp/warmest-review
```

### Step 3: View Results

Reports are saved to: `reports/code_review_report_TIMESTAMP.md`

```bash
# View the report
cat reports/code_review_report_*.md | head -100

# Or open in editor
open reports/code_review_report_*.md
```

### Step 4: Email Report (if configured)

If `EMAIL_ENABLED=true`, the report will be sent to your configured email automatically.

---

## 🎯 Example 2: Review and Send Email

### Full Workflow

```bash
# With email in one command
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review and send email"
```

### What Happens

1. ✅ Scans directory for `.java` files
2. ✅ Detects file type (Service, Controller, Repository, etc.)
3. ✅ Retrieves relevant RAG rules using BM25 ranking
4. ✅ Sends each file to Ollama for review
5. ✅ Aggregates all reviews
6. ✅ Writes markdown report
7. ✅ Sends via email (if enabled and configured)

---

## 🎯 Example 3: Testing with Sample Service

### Create Test Service

Create `/tmp/test-service/UserService.java`:

```java
package com.example.service;

import com.example.repository.UserRepository;
import com.example.model.User;
import com.example.dto.UserDTO;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }
        return mapToDTO(user);
    }
    
    public UserDTO createUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        userRepository.save(user);
        return mapToDTO(user);
    }
    
    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
```

### Run Review

```bash
java -jar target/CodeReviewAgent.jar "review /tmp/test-service"
```

### Expected Output

The reviewer should detect issues like:
- ❌ Null pointer risk in `getUserById` (returns null without exception)
- ❌ No input validation in `createUser`
- ✅ Proper dependency injection
- ✅ Single responsibility

---

## 📊 RAG System - What Gets Reviewed

The enhanced RAG system retrieves relevant rules based on code similarity:

### Rules by Category

1. **MICROSERVICES_DESIGN** (`microservices-design.md`)
   - Single Responsibility Principle
   - Dependency Injection
   - Null Safety
   - Logging
   - Transaction Management

2. **REST_API** (`rest-api-design.md`)
   - HTTP Method Usage
   - Error Handling
   - Validation
   - Authorization
   - Documentation

3. **REPOSITORY_DATA_ACCESS** (`repository-data-access.md`)
   - Entity Mapping
   - Relationships
   - Query Methods
   - Pagination
   - Performance Optimization

4. **ARCHITECTURE** (`architecture.md`)
   - Layer Separation
   - Controller → Service → Repository

5. **NAMING** (`naming.md`)
   - Naming Conventions
   - Method Naming

### How RAG Works

1. Code snippet is analyzed
2. BM25 algorithm ranks all rule documents
3. Top 5 most relevant rules are selected
4. Rules are prepended to LLM prompt with ranking info
5. LLM reviews code in context of retrieved rules

---

## 🔧 Configuration Details

### Email Setup (Gmail Example)

1. Enable 2-Factor Authentication on Gmail
2. Generate App Password: https://myaccount.google.com/apppasswords
3. Use App Password in config:
   ```properties
   SMTP_USERNAME=your-email@gmail.com
   SMTP_PASSWORD=your-16-char-app-password
   ```

### Ollama Configuration

Change model in properties file:
```properties
# Options: llama3, mistral, llama2, neural-chat, etc.
OLLAMA_MODEL=llama3
```

### Performance Tuning

```properties
# Thread pool for parallel processing (default: 4)
THREAD_POOL_SIZE=8

# LLM retry attempts (default: 3)
MAX_RETRIES=3
```

---

## 📈 Output Format

### Report Structure

```markdown
# Code Review Report
Generated: 2026-05-06 14:30:00

## Summary
- Total Files Reviewed: 3
- Issues Found: 15
  - HIGH: 3
  - MEDIUM: 8
  - LOW: 4

## File Reviews

### UserService.java
**Status:** ⚠️ Issues Found

#### Issues
1. **HIGH** - Null Pointer Risk
   - Line: 25
   - Message: Potential NPE when user not found
   - Suggestion: Throw UserNotFoundException instead

2. **MEDIUM** - Missing Validation
   - Parameter: email
   - Suggestion: Add @NotNull annotation

#### Suggestions
- Add comprehensive logging
- Use custom exceptions
```

---

## 🐛 Troubleshooting

### Error: "Failed to connect to Ollama"
- Ensure Ollama is running: `ollama serve`
- Check URL: `http://127.0.0.1:11434`
- Verify model is downloaded: `ollama list`

### Error: "Email not configured"
- Check `codereview.properties` exists
- Verify `EMAIL_ENABLED=true`
- Verify all SMTP settings

### LLM Response Issues
- Model may be slow first time
- Try smaller model: `ollama pull mistral`
- Increase timeout in code if needed

### No Files Found
- Verify path is correct
- Ensure `.java` files exist in directory
- Check file permissions

---

## 📝 Input Examples

### Interactive Mode Commands

```bash
# Review single directory
review /path/to/project

# Review current directory
review

# Review with email
review /path/to/project and send email

# Help
help

# Exit
exit
```

### CLI Mode Commands

```bash
# Review project
java -jar CodeReviewAgent.jar "review /path/to/project"

# Review with email
java -jar CodeReviewAgent.jar "review /path/to/project and email"
```

---

## ✅ Testing Checklist

- [ ] Ollama is running
- [ ] Model is downloaded
- [ ] Properties file is configured
- [ ] Test directory has Java files
- [ ] JAR is built successfully
- [ ] Review command executes
- [ ] Report is generated
- [ ] Email is sent (if enabled)

---

## 📞 Support

For issues:
1. Check logs: `logs/codereview-agent.log`
2. Enable debug output in code
3. Verify Ollama is responding: `curl http://127.0.0.1:11434/api/tags`

