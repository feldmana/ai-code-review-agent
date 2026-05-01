# CodeReviewAgent - AI-Powered Code Review System

A production-grade Java application that performs autonomous, AI-powered code review locally using Ollama. This system uses multi-agent architecture to orchestrate code analysis, aggregation, and reporting.

## 🎯 Features

- **Local LLM Integration**: Uses Ollama for local, private code analysis (no external APIs)
- **Multi-Agent Architecture**: Modular design with specialized agents for routing, planning, reviewing, summarizing, and emailing
- **Parallel Processing**: Reviews multiple files concurrently for efficiency
- **Structured Output**: Generates markdown reports with categorized issues and suggestions
- **Email Integration**: Optionally sends reports via SMTP (configured via environment variables)
- **Retry Logic**: Automatic retry mechanism with exponential backoff
- **CLI Interface**: Interactive and command-line modes for flexibility

## 🏗️ Architecture

### Agents

1. **RouterAgent** - Classifies user requests and determines task type
2. **PlannerAgent** - Breaks tasks into actionable steps
3. **ReviewAgent** - Sends code to Ollama for analysis and parses results
4. **SummaryAgent** - Aggregates reviews into a structured summary
5. **EmailAgent** - Sends reports via email (if configured)

### Tools

- **FileScannerTool** - Scans directories for source code files
- **FileReaderTool** - Reads file content with size limits
- **ReportWriterTool** - Generates markdown reports

### Orchestrator

**AgentOrchestrator** - Manages agent execution, workflow state, and parallel processing

## 📋 Package Structure

```
com.agentic.codereview
├── agent/              # All agent implementations
├── orchestrator/       # AgentOrchestrator
├── llm/               # Ollama client
├── tool/              # Reusable tools
├── model/             # Data models (Task, Action, ReviewResult, Summary)
├── config/            # Configuration management
└── Main.java          # CLI entry point
```

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Ollama installed and running

### Installation

1. Clone the repository:
```bash
cd /path/to/CodeReviewAgent
```

2. Build the project:
```bash
mvn clean package
```

3. Run Ollama (in a separate terminal):
```bash
ollama serve
ollama pull llama2  # or your preferred model
```

### Usage

#### Interactive Mode
```bash
java -jar target/CodeReviewAgent.jar
```

Then use commands:
```
CodeReviewAgent> review /path/to/project
CodeReviewAgent> help
CodeReviewAgent> exit
```

#### Command-Line Mode
```bash
java -jar target/CodeReviewAgent.jar review /path/to/project
```

### Configuration

Create a `codereview.properties` file in the project root or set environment variables:

```properties
# Email Configuration (optional)
EMAIL_ENABLED=false
EMAIL_TO=your-email@gmail.com
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
SMTP_TLS_ENABLED=true

# Review Configuration
MAX_RETRIES=3
THREAD_POOL_SIZE=4
```

Or set environment variables:
```bash
export EMAIL_ENABLED=true
export EMAIL_TO=your@email.com
export SMTP_USERNAME=your@gmail.com
export SMTP_PASSWORD=your-app-password
```

## 📊 Workflow

1. **Routing** - RouterAgent identifies task type (REVIEW_CODE, SUMMARIZE, SEND_EMAIL)
2. **Planning** - PlannerAgent creates execution steps
3. **File Scanning** - FileScannerTool finds all code files
4. **Code Review** - ReviewAgent sends each file to Ollama for analysis
5. **Summarization** - SummaryAgent aggregates all reviews
6. **Report Generation** - ReportWriterTool creates markdown report
7. **Email (Optional)** - EmailAgent sends report if configured

## 📝 Output

Generated reports are saved to `reports/` directory with timestamp:
```
reports/code_review_report_20260501_143022.md
```

Report includes:
- Summary statistics (total issues, severity breakdown)
- File-by-file review with issues and suggestions
- Severity levels (LOW, MEDIUM, HIGH)

## 🔧 Code Structure

### Models

- **Task**: Represents user request with ID, type, and project path
- **Action**: Represents execution steps in a plan
- **ReviewResult**: Contains file review findings
- **Summary**: Aggregates multiple reviews

### Example: Creating a Custom Agent

```java
public class MyCustomAgent {
    private static final Logger logger = LoggerFactory.getLogger(MyCustomAgent.class);
    
    public void performAction(String input) {
        logger.info("My agent is working on: {}", input);
        // Implementation here
    }
}
```

## 🧪 Testing

Run the tests:
```bash
mvn test
```

## 📦 Dependencies

- **com.google.code.gson** - JSON processing
- **com.squareup.okhttp3** - HTTP client
- **jakarta.mail** - Email support
- **org.slf4j / ch.qos.logback** - Logging
- **junit-jupiter** - Testing

## 🚨 Error Handling

- **Connection Errors**: Verifies Ollama connectivity before starting
- **Retry Logic**: Implements exponential backoff for LLM calls
- **Large Files**: Automatically truncates files larger than 100KB
- **JSON Parsing**: Gracefully handles malformed responses

## 📧 Email Setup (Gmail)

1. Enable 2-Factor Authentication on your Google Account
2. Generate an [App-Specific Password](https://support.google.com/accounts/answer/185833)
3. Set `SMTP_PASSWORD` to this app password
4. Set `EMAIL_ENABLED=true`

## 🔐 Security Notes

- Credentials are NOT stored in the application
- Use environment variables or `.properties` files for sensitive data
- `.properties` file should be added to `.gitignore`
- Ollama runs locally (no external API exposure)

## 🎓 Extending the System

### Adding a New Agent

1. Create class extending agent pattern
2. Implement action methods
3. Register in AgentOrchestrator
4. Add to execution plan logic

### Adding a New Tool

1. Create tool class with specific functionality
2. Implement error handling
3. Add to AgentOrchestrator
4. Use in agents

### Example: Adding a Database Tool

```java
public class DatabaseTool {
    public void saveReviewResult(ReviewResult result) {
        // Save to database
    }
}
```

## 📈 Parallel Processing

The system supports parallel file review:

```properties
THREAD_POOL_SIZE=4  # Number of concurrent threads
```

Increase for faster processing on multi-core systems.

## 🐛 Debugging

Enable debug logging by setting environment variable:
```bash
export DEBUG=true
```

View logs:
```bash
tail -f logs/codereview-agent.log
```

## 📄 License

This project is provided as-is for educational and commercial use.

## 👨‍💻 Support

For issues or questions:
1. Check the logs in `logs/` directory
2. Verify Ollama is running and accessible
3. Ensure Java 21+ is installed
4. Check project path exists and contains source files

## 🌟 Future Enhancements

- [ ] EvaluatorAgent for review quality checking
- [ ] Support for multiple LLM providers
- [ ] Real-time review dashboard
- [ ] Integration with Git webhooks
- [ ] Custom review templates
- [ ] Performance metrics and analytics

---

**Built with ☕ Java and 🤖 AI**

