# CodeReviewAgent - Quick Start Guide

## Installation & Setup

### 1. Prerequisites
- Java 21+
- Maven 3.8+
- Ollama (for local LLM)

### 2. Install Ollama
Download and install from https://ollama.ai

### 3. Start Ollama
```bash
ollama serve
```

In another terminal, pull a model:
```bash
ollama pull llama2  # or mistral, neural-chat, etc.
```

### 4. Build CodeReviewAgent
```bash
cd /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent
mvn clean package
```

## Running the Application

### Interactive Mode
```bash
java -jar target/CodeReviewAgent.jar
```

Then at the prompt:
```
CodeReviewAgent> review /path/to/your/project
CodeReviewAgent> exit
```

### CLI Mode
```bash
java -jar target/CodeReviewAgent.jar review /path/to/your/project
```

## Configuration (Optional)

### Email Setup

Create `codereview.properties`:
```properties
EMAIL_ENABLED=true
EMAIL_TO=your-email@gmail.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-specific-password
SMTP_TLS_ENABLED=true
```

Or use environment variables:
```bash
export EMAIL_ENABLED=true
export EMAIL_TO=your@gmail.com
export SMTP_USERNAME=your@gmail.com
export SMTP_PASSWORD=your-app-password
java -jar target/CodeReviewAgent.jar review /path/to/project
```

### For Gmail
1. Enable 2-Factor Authentication
2. Generate App-Specific Password: https://myaccount.google.com/apppasswords
3. Use that password in config

## Output

Reports are generated in `reports/` directory:
```
reports/code_review_report_20260501_143022.md
```

## Troubleshooting

### Connection Error
```
❌ Failed to connect to Ollama at http://localhost:11434
```
**Solution:** Make sure Ollama is running with `ollama serve`

### Files Not Found
**Solution:** Use absolute path and ensure it contains source files

### JSON Parse Errors
**Solution:** Model might be returning non-JSON. Try: `ollama pull llama2`

## Project Structure
```
target/
├── CodeReviewAgent.jar          ← Executable JAR
├── CodeReviewAgent-1.0-SNAPSHOT.jar
└── classes/
    └── com/agentic/codereview/  ← Compiled classes

src/main/java/com/agentic/codereview/
├── agent/
│   ├── RouterAgent.java
│   ├── PlannerAgent.java
│   ├── ReviewAgent.java
│   ├── SummaryAgent.java
│   └── EmailAgent.java
├── orchestrator/
│   └── AgentOrchestrator.java
├── llm/
│   └── OllamaClient.java
├── tool/
│   ├── FileScannerTool.java
│   ├── FileReaderTool.java
│   └── ReportWriterTool.java
├── model/
│   ├── Task.java
│   ├── Action.java
│   ├── ReviewResult.java
│   └── Summary.java
├── config/
│   └── AppConfig.java
└── Main.java
```

## Example: Review a Java Project

```bash
# Option 1: Interactive
java -jar target/CodeReviewAgent.jar
CodeReviewAgent> review ~/Documents/MyJavaProject

# Option 2: Direct
java -jar target/CodeReviewAgent.jar review ~/Documents/MyJavaProject
```

The agent will:
1. ✓ Scan for Java files
2. ✓ Send each file to Ollama for review
3. ✓ Aggregate findings
4. ✓ Generate markdown report
5. ✓ Optionally send email

## Architecture Overview

```
User Input
    ↓
RouterAgent (classify task)
    ↓
PlannerAgent (create execution plan)
    ↓
AgentOrchestrator (execute plan)
    ├→ FileScannerTool (find files)
    ├→ ReviewAgent → OllamaClient (review each file)
    ├→ SummaryAgent (aggregate results)
    ├→ ReportWriterTool (generate report)
    └→ EmailAgent (optionally send email)
    ↓
reports/code_review_report_*.md
```

## Performance Tips

- **Parallel Processing:** Set `THREAD_POOL_SIZE=8` for faster reviews
- **Model Selection:** `mistral` is faster than `llama2`
- **File Limits:** Large files (>100KB) are automatically truncated
- **Retries:** `MAX_RETRIES=3` for resilience

## Next Steps

1. ✅ Build and test with a small project
2. ✅ Configure email if needed
3. ✅ Integrate into CI/CD pipeline
4. ✅ Customize agents for your workflow

---

**Happy Code Reviewing! 🤖**

