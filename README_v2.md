# CodeReviewAgent v2.0 - AI-Powered Code Review System

**Status**: ✅ Production-Ready | **Version**: 2.0.0 | **Date**: May 2026

---

## 🎯 Overview

CodeReviewAgent is an autonomous multi-agent system that performs AI-powered code reviews locally using **Ollama** and an enhanced **vector-based RAG system** with **BM25 ranking**.

### Key Features

✨ **Multi-Agent Architecture**
- RouterAgent: Task classification
- PlannerAgent: Action planning
- ReviewAgent: Code analysis with context awareness
- SummaryAgent: Results aggregation
- EmailAgent: Report delivery

🧠 **Enhanced RAG System**
- BM25 ranking (industry-standard relevance scoring)
- Vector-based knowledge retrieval
- 49 comprehensive best practices across 5 rule files
- Automatic category detection and routing
- Rich metadata tracking

🎓 **Intelligent Code Analysis**
- 7 code type detection (@Service, @Controller, @Repository, etc.)
- Category-specific review hints
- Structured, ranked RAG context in prompts
- Null safety and runtime bug detection
- Best practice validation

📧 **Report Generation**
- Markdown reports with detailed findings
- Severity-based classification (LOW/MEDIUM/HIGH)
- Email delivery support (Gmail, Outlook, SendGrid)
- Structured JSON review results

---

## 🚀 Quick Start (5 Minutes)

### Prerequisites
```bash
# 1. Ollama running
ollama serve

# 2. Model downloaded
ollama pull llama3  # or mistral

# 3. Java 21+
java -version

# 4. Maven installed
mvn -version
```

### Build & Run
```bash
# Build
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent
mvn clean package -DskipTests -q

# Review code
java -jar target/CodeReviewAgent.jar "review /path/to/code"

# View report
cat reports/code_review_report_*.md

# Review with email
java -jar target/CodeReviewAgent.jar "review /path/to/code and send email"
```

### Example: Warmest Project
```bash
# Prepare
mkdir -p /tmp/warmest-review
cp /path/to/warmest/src/main/java/com/example/service/*.java /tmp/warmest-review/

# Review
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review"

# Email
# Create codereview.properties with email config
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review and send email"
```

---

## 📚 Documentation

### Getting Started
- **QUICK_START_v2.md** - 5-minute setup guide with examples
- **TESTING_GUIDE_ENHANCED.md** - Complete setup, configuration, and examples

### Architecture & Design
- **RAG_ARCHITECTURE_v2.md** - Technical deep dive with diagrams and algorithms
- **AGENT_IMPROVEMENTS.md** - Detailed agent review and enhancements
- **CODE_REVIEW_SUMMARY.md** - What was changed and why

### Complete Status
- **IMPLEMENTATION_COMPLETE.md** - Full project summary and status

### Configuration
- **codereview.properties.example.detailed** - All options with examples

---

## 🏗️ System Architecture

### Component Overview
```
User Input (CLI)
    ↓
RouterAgent (Task Classification)
    ↓
PlannerAgent (Action Planning)
    ↓
AgentOrchestrator (Execution Engine)
    ├─ FileScannerTool
    ├─ FileReaderTool
    ├─ ReviewAgent
    │   ├─ RagContextBuilder (Context Structuring)
    │   ├─ EnhancedVectorRagService (BM25 Retrieval)
    │   ├─ EnhancedVectorStore (Vector DB)
    │   └─ OllamaClient (LLM Interface)
    ├─ SummaryAgent
    ├─ ReportWriterTool
    └─ EmailAgent (Optional)
```

### Data Flow
```
Code File
    ↓
Code Type Detection (@Service, @Controller, etc.)
    ↓
BM25 Vector Similarity Ranking
    ↓
Top-5 Relevant Rules Retrieved (with scores)
    ↓
Structured Prompt Built
    ├─ RAG Rules (ranked by relevance)
    ├─ Category-Specific Context
    ├─ Code to Review
    └─ Review Instructions
    ↓
Ollama LLM Analysis
    ↓
JSON Review Result
    ↓
Aggregated Report & Optional Email
```

---

## 🧠 RAG System (Enhanced)

### What Makes It Special

**BM25 Ranking Algorithm**
- Industry-standard relevance scoring
- Better than simple keyword matching
- Factors in term frequency and document frequency
- Handles term saturation and length normalization

**Metadata Tracking**
- Source file tracking
- Category classification (auto-detected)
- Relevance scoring per query
- Rich debugging information

**Intelligent Chunking**
- Splits by logical sections (Markdown headers)
- Preserves context within chunks
- Intelligent sizing for large documents
- Avoids context loss

### Knowledge Base

| File | Rules | Focus |
|------|-------|-------|
| microservices-design.md | 15 | Service layer best practices |
| rest-api-design.md | 15 | Controller & REST API patterns |
| repository-data-access.md | 19 | Data access layer patterns |
| architecture.md | 4 | Layer separation and architecture |
| naming.md | 3 | Naming conventions |
| **Total** | **49** | **Comprehensive** |

---

## 🎯 Code Type Detection

Automatically detects and routes to specialized rules:

| Annotation | Detected As | Rules Retrieved |
|-----------|------------|-----------------|
| @Service | SERVICE | Microservices design (15 rules) |
| @Controller | CONTROLLER | REST API design (15 rules) |
| @RestController | CONTROLLER | REST API design (15 rules) |
| @Repository | REPOSITORY | Data access (19 rules) |
| @Entity | ENTITY | Domain model patterns |
| @Configuration | CONFIGURATION | Spring configuration patterns |
| (Test class) | TEST | Testing patterns |
| (Interface) | INTERFACE | Interface design |

---

## 📊 What Gets Reviewed

### Runtime Safety
- ✅ Null pointer exception risks
- ✅ Array index out of bounds
- ✅ Empty collection usage
- ✅ Divide by zero risks
- ✅ Type cast errors

### Design Quality
- ✅ Single responsibility principle
- ✅ Dependency injection patterns
- ✅ Layer separation violations
- ✅ Naming conventions
- ✅ Code clarity

### Best Practices
- ✅ Exception handling strategy
- ✅ Logging patterns
- ✅ Transaction management
- ✅ HTTP method correctness
- ✅ Entity mapping
- ✅ Query optimization

### NOT Reviewed (Intentional)
- ❌ Code style/formatting (use Checkstyle/Spotless)
- ❌ Static analysis (use SpotBugs)
- ❌ Performance optimization (use JMH)

---

## 📧 Email Configuration

### Gmail Setup (Recommended)
```properties
EMAIL_ENABLED=true
EMAIL_TO=your-email@gmail.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-16-char-app-password
SMTP_TLS_ENABLED=true
```

Steps:
1. Enable 2-Factor Authentication: https://myaccount.google.com/security
2. Create App Password: https://myaccount.google.com/apppasswords
3. Select "Mail" and "Other (custom name)"
4. Copy 16-character password to config

### Alternative Providers
```properties
# Outlook/Office 365
SMTP_HOST=smtp.office365.com
SMTP_PORT=587

# SendGrid
SMTP_HOST=smtp.sendgrid.net
SMTP_PORT=587
SMTP_USERNAME=apikey
SMTP_PASSWORD=your-sendgrid-api-key
```

---

## ⚙️ Configuration

### File Location
```bash
# Project root
/Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/codereview.properties
```

### Options
```properties
# Email
EMAIL_ENABLED=true
EMAIL_TO=recipient@example.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=sender@gmail.com
SMTP_PASSWORD=app-specific-password
SMTP_TLS_ENABLED=true

# Review
MAX_RETRIES=3           # LLM retry attempts
THREAD_POOL_SIZE=4      # Parallel file processing
```

### Environment Variable Override
```bash
export EMAIL_ENABLED=true
export EMAIL_TO=afeldman66@gmail.com
export SMTP_HOST=smtp.gmail.com
export SMTP_PORT=587
export SMTP_USERNAME=your-email@gmail.com
export SMTP_PASSWORD=your-app-password
export SMTP_TLS_ENABLED=true

java -jar CodeReviewAgent.jar "review /path"
```

### Priority
1. Environment variables (highest)
2. codereview.properties file
3. Defaults in code (lowest)

---

## 🎓 Usage Examples

### Interactive Mode
```bash
java -jar target/CodeReviewAgent.jar

# Commands:
# > review /path/to/project
# > review /path/to/project and send email
# > help
# > exit
```

### CLI Mode
```bash
# Review
java -jar target/CodeReviewAgent.jar "review /path"

# Review with email
java -jar target/CodeReviewAgent.jar "review /path and send email"
```

### Review Warmest Project
```bash
# Prepare service code
mkdir -p /tmp/warmest-review
cp /path/to/warmest/src/main/java/com/example/service/*.java /tmp/warmest-review/

# Review
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review"

# View results
cat reports/code_review_report_*.md | less

# Send via email (if configured)
java -jar target/CodeReviewAgent.jar "review /tmp/warmest-review and send email"
```

---

## 📊 Output Format

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

#### Issues
1. **HIGH** - Null pointer risk in deleteUser()
   Message: User.orElse(null) can cause NPE
   Suggestion: Throw UserNotFoundException instead

2. **MEDIUM** - Missing validation
   Parameter: userId
   Suggestion: Add "if (userId <= 0) throw IllegalArgumentException"

#### Suggestions
- Add logging for audit trail
- Use Optional pattern consistently
```

### Files Generated
```
reports/code_review_report_20260506_143000.md
logs/codereview-agent.log
```

---

## 🔧 Troubleshooting

### "Failed to connect to Ollama"
```bash
# Check if Ollama is running
curl http://127.0.0.1:11434/api/tags

# Start Ollama
ollama serve
```

### "No model found"
```bash
# List models
ollama list

# Download model
ollama pull llama3
```

### "Email not configured"
```bash
# Create config
cat > codereview.properties << 'EOF'
EMAIL_ENABLED=true
EMAIL_TO=your-email@gmail.com
# ... other settings
EOF
```

### "No files found"
```bash
# Verify directory has Java files
ls -la /path/to/directory | grep .java

# Verify path syntax
echo $PATH_VAR  # For environment variables
```

### "LLM responses inconsistent"
```bash
# Increase retries
MAX_RETRIES=5

# Use more deterministic model
ollama pull mistral
```

---

## 📈 Performance

### Expectations
| Task | Time |
|------|------|
| Build JAR | 30-60s |
| Initialize RAG | 1-2s |
| Review 1 file | 5-30s |
| Review 10 files (parallel) | 50-300s |
| Send email | <5s |

### For Warmest Project
- Typical service file: ~1000 lines
- Per file: ~10-15 seconds
- 10 services: ~2-3 minutes
- 50 services: ~10-15 minutes

### Optimization Tips
- Set `THREAD_POOL_SIZE=8` for parallel processing
- Use `mistral` model (faster than llama3)
- Review service code only (skip controllers/repositories for speed)

---

## 🧪 Testing

### Test with Sample Code
```bash
mkdir -p /tmp/test-service

cat > /tmp/test-service/UserService.java << 'EOF'
package com.example.service;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public void saveUser(Long id, String email) {
        if (email != null && email.length() > 0) {
            // database save
        }
    }
}
EOF

java -jar target/CodeReviewAgent.jar "review /tmp/test-service"
```

### Verify RAG Retrieval
```bash
# Check logs for ranking
tail -f logs/codereview-agent.log | grep "RANK"

# Should show:
# RANK 1 - SERVICE_DESIGN - Score: 2.45
# RANK 2 - ARCHITECTURE - Score: 0.78
```

---

## 📁 Project Structure

```
CodeReviewAgent/
├── src/main/java/com/agentic/codereview/
│   ├── agent/                        # Multi-agent system
│   │   ├── RouterAgent.java
│   │   ├── PlannerAgent.java
│   │   ├── ReviewAgent.java         # ✨ Enhanced
│   │   ├── SummaryAgent.java
│   │   └── EmailAgent.java
│   │
│   ├── rag/                         # ✨ Enhanced Vector RAG
│   │   ├── EnhancedVectorStore.java
│   │   ├── EnhancedVectorRagService.java
│   │   ├── EmbeddingVector.java
│   │   ├── RagContextBuilder.java   # ✨ New
│   │   └── RagService.java          # Interface
│   │
│   ├── orchestrator/
│   │   └── AgentOrchestrator.java   # Main execution engine
│   │
│   ├── tool/
│   │   ├── FileScannerTool.java
│   │   ├── FileReaderTool.java
│   │   └── ReportWriterTool.java
│   │
│   ├── llm/
│   │   └── OllamaClient.java        # LLM HTTP client
│   │
│   ├── model/                       # Data models
│   ├── config/                      # Configuration
│   ├── prompt/                      # Prompt templates
│   └── Main.java                    # ✨ Updated entry point
│
├── rag-docs/rules/                  # Knowledge base
│   ├── microservices-design.md      # ✨ New (15 rules)
│   ├── rest-api-design.md           # ✨ New (15 rules)
│   ├── repository-data-access.md    # ✨ New (19 rules)
│   ├── architecture.md
│   └── naming.md
│
├── reports/                         # Generated reports
├── logs/                            # Application logs
│
├── pom.xml                          # Maven configuration
├── codereview.properties            # Configuration (optional)
│
└── docs/                            # ✨ Enhanced documentation
    ├── QUICK_START_v2.md
    ├── TESTING_GUIDE_ENHANCED.md
    ├── RAG_ARCHITECTURE_v2.md
    ├── AGENT_IMPROVEMENTS.md
    ├── CODE_REVIEW_SUMMARY.md
    └── IMPLEMENTATION_COMPLETE.md
```

---

## ✅ Features Checklist

- [x] Multi-agent architecture
- [x] Router agent for task classification
- [x] Planner agent for action sequencing
- [x] Review agent with context awareness
- [x] Summary agent for aggregation
- [x] Email agent with SMTP support
- [x] File scanning and reading
- [x] Report generation (Markdown)
- [x] Enhanced RAG with BM25 ranking
- [x] Vector database (in-memory)
- [x] Code type detection (7 types)
- [x] Category-specific rules (49 total)
- [x] Metadata tracking and logging
- [x] Configuration management
- [x] Error handling and retry logic
- [x] Parallel processing
- [x] Comprehensive documentation

---

## 🚀 Version History

### v2.0 (Current) - May 2026
✨ **Major Enhancements**:
- Enhanced RAG with BM25 ranking
- Vector database implementation
- Context-aware code analysis
- 34 new best practice rules
- Code type detection
- Structured prompt building
- Rich metadata tracking

### v1.0 - Initial Release
- Basic multi-agent architecture
- Keyword-based RAG
- Email integration
- Report generation

---

## 📞 Support

### Documentation
1. **QUICK_START_v2.md** - Get started
2. **TESTING_GUIDE_ENHANCED.md** - Setup & configuration
3. **RAG_ARCHITECTURE_v2.md** - Technical details
4. **AGENT_IMPROVEMENTS.md** - Code changes
5. **CODE_REVIEW_SUMMARY.md** - What changed

### Logs
```bash
# View logs
tail -f logs/codereview-agent.log

# Enable debug
export LOG_LEVEL=DEBUG
```

### Debugging
```bash
# Test Ollama
curl http://127.0.0.1:11434/api/tags

# Test email config
grep "^SMTP" codereview.properties

# Check RAG stats
grep "STATS:" logs/codereview-agent.log
```

---

## 📄 License

This project is part of the Learning/OpenAI educational initiative.

---

## 🎉 Ready to Review Code?

```bash
# 1. Start Ollama
ollama serve &

# 2. Build
mvn clean package -DskipTests

# 3. Review
java -jar target/CodeReviewAgent.jar "review /path/to/warmest/services"

# 4. Check Results
cat reports/code_review_report_*.md

# Happy Coding! 🚀
```

---

**Last Updated**: May 6, 2026
**Status**: ✅ Production-Ready
**Version**: 2.0.0

