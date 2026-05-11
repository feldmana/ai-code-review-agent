# 🎓 Vector RAG Service - Complete Guide

## What is Vector RAG?

**RAG = Retrieval-Augmented Generation**

It enhances AI-powered code reviews by:
1. **Retrieving** relevant coding standards/best practices from a knowledge base
2. **Augmenting** the LLM prompt with this context
3. **Generating** more contextual, accurate reviews

---

## 🚀 Quick Start

### Step 1: Create RAG Documents Directory

```bash
mkdir -p rag-docs/rules
mkdir -p rag-docs/standards
mkdir -p rag-docs/guides
```

### Step 2: Add Your Knowledge Base

**File: `rag-docs/rules/java-best-practices.md`**
```markdown
# Java Best Practices for Code Review

## Naming Conventions
- Use camelCase for variables and methods
- Use PascalCase for class names
- Use UPPER_SNAKE_CASE for constants
- Avoid single letter variable names (except loop counters)

## Code Organization
- Maximum 20 lines per method
- One responsibility per class (SRP)
- Keep files under 500 lines
- Organize imports alphabetically

## Error Handling
- Use try-with-resources for file operations
- Never swallow exceptions silently
- Log exceptions with full context
- Use checked exceptions for recoverable errors
```

**File: `rag-docs/rules/security-guidelines.md`**
```markdown
# Security Guidelines

## Password & Secrets
- Never hardcode credentials
- Use environment variables or secure vaults
- Hash passwords with bcrypt/scrypt
- Implement rate limiting on auth endpoints

## Input Validation
- Validate all user input
- Use parameterized queries (prevent SQL injection)
- Sanitize output to prevent XSS
- Limit file upload sizes
```

### Step 3: Initialize RAG in Your Code

```java
// In ReviewAgent or Main
VectorRagService rag = new VectorRagService("rag-docs/rules", 5);
rag.initialize();

// Now use in reviews
List<String> relevant = rag.getRelevantRules(codeSnippet);
```

### Step 4: Use Context in LLM Prompts

```java
String buildReviewPromptWithContext(String fileName, String code) {
    // Get relevant guidelines
    List<String> guidelines = rag.getRelevantRules(code);
    
    return """
        You are a Java code reviewer. Consider these guidelines:
        
        """ + String.join("\n\n", guidelines) + """
        
        Now review this file: """ + fileName + """
        
        Code:
        ```
        """ + code + """
        ```
        
        Provide issues and suggestions in JSON format.
        """;
}
```

---

## 📚 Document Format

### Markdown Files (.md)
```markdown
# Title

## Section
Content here.

### Subsection
More content.

- Bullet point 1
- Bullet point 2
```

### Text Files (.txt)
Plain text files are also supported.

### Rules for Best Results
1. **Clear sections** - Use headers for organization
2. **Specific examples** - Include code samples
3. **Actionable content** - Be prescriptive, not descriptive
4. **Reasonable length** - 200-500 words per section

---

## 💡 Example Knowledge Bases

### Language-Specific Rules
```
rag-docs/java/       → Java conventions
rag-docs/python/     → Python PEP 8 guidelines
rag-docs/typescript/ → TypeScript best practices
```

### By Topic
```
rag-docs/performance/    → Performance optimization
rag-docs/security/       → Security considerations
rag-docs/testing/        → Testing best practices
rag-docs/architecture/   → Architectural patterns
```

### By Team
```
rag-docs/company/   → Company standards
rag-docs/project/   → Project-specific rules
rag-docs/team-a/    → Team A conventions
rag-docs/team-b/    → Team B conventions
```

---

## 🔍 How Similarity Search Works

### Example Query
```
Code snippet with a database connection...
```

### Matching Documents (Top 5)
1. ✅ "Database connection best practices" (98% match)
2. ✅ "Error handling in database operations" (94% match)
3. ✅ "Connection pooling guidelines" (92% match)
4. ✅ "Resource cleanup patterns" (88% match)
5. ✅ "Transaction management" (85% match)

### Why These Match?
- **Keywords**: database, connection, error, handling, etc.
- **Context**: Operations similar to query intent
- **Relevance**: Most important documents ranked first

---

## 📊 Workflow Integration

### Current Flow (Without RAG)
```
Code → LLM Review → Issues/Suggestions
```

### Enhanced Flow (With RAG)
```
Code → RAG Search → Standards/Guidelines
  ↓                        ↓
  └────────────────────────┘
           ↓
    Combined Context
           ↓
        LLM Review
           ↓
   Better Issues/Suggestions
```

---

## 🎯 Use Cases

### 1. Company Standard Enforcement
```
rag-docs/company-standards/
├── naming-conventions.md
├── error-handling.md
├── logging-standards.md
└── security-requirements.md
```

### 2. Language-Specific Best Practices
```
rag-docs/java/
├── spring-boot-patterns.md
├── database-access.md
├── testing-patterns.md
└── concurrent-programming.md
```

### 3. Architecture Patterns
```
rag-docs/architecture/
├── microservices.md
├── event-driven.md
├── api-design.md
└── caching-strategies.md
```

### 4. Security & Compliance
```
rag-docs/security/
├── owasp-top-10.md
├── authentication.md
├── data-protection.md
└── audit-logging.md
```

---

## 🔧 Configuration

### Basic Setup
```java
// Default: top 5 results
VectorRagService rag = new VectorRagService("rag-docs");
```

### Custom Top-K
```java
// Get top 10 results instead
VectorRagService rag = new VectorRagService("rag-docs", 10);
```

### Get Statistics
```java
Map<String, Object> stats = rag.getStats();
System.out.println(stats);
// {initialized=true, documentCount=8, totalChunks=42, topK=5}
```

### List All Documents
```java
List<Map<String, Object>> docs = rag.listDocuments();
docs.forEach(doc -> {
    System.out.println(doc.get("title") + ": " + 
                      doc.get("chunks") + " chunks");
});
```

---

## 🚀 Advanced Usage

### Filter Results
```java
List<String> relevant = rag.getRelevantRules(code);
// Filter to only security-related rules
List<String> securityRules = relevant.stream()
    .filter(r -> r.toLowerCase().contains("security"))
    .collect(Collectors.toList());
```

### Custom Similarity Threshold
```java
// Only include high-confidence matches
List<String> results = rag.getRelevantRules(code)
    .stream()
    .limit(3)  // Only top 3
    .collect(Collectors.toList());
```

### Combine Multiple RAG Services
```java
VectorRagService companyRag = new VectorRagService("rag-docs/company", 5);
VectorRagService projectRag = new VectorRagService("rag-docs/project", 5);

companyRag.initialize();
projectRag.initialize();

List<String> allRules = new ArrayList<>();
allRules.addAll(companyRag.getRelevantRules(code));
allRules.addAll(projectRag.getRelevantRules(code));
```

---

## 📈 Performance

### Typical Performance
- **Initialize** (100 docs): ~100ms
- **Search** (single query): ~10ms
- **Memory** (1000 chunks): ~50MB

### Optimization Tips
1. **Organize documents** - Use directory structure
2. **Chunk size** - Aim for 200-500 chars per chunk
3. **Top-K** - Use 5-10 for good balance
4. **Caching** - Cache RAG initialization

---

## ✅ Best Practices

### DO ✅
- Use clear, specific guidelines
- Organize by topic/category
- Include code examples
- Keep documents focused
- Update regularly

### DON'T ❌
- Don't include entire books
- Don't mix multiple languages in one document
- Don't use vague descriptions
- Don't include outdated information
- Don't store secrets or credentials

---

## 🧪 Testing

### Verify Initialization
```java
VectorRagService rag = new VectorRagService("rag-docs");
rag.initialize();

assertTrue(rag.isInitialized());
assertTrue(rag.getStats().get("documentCount") > 0);
assertTrue(rag.getStats().get("totalChunks") > 0);
```

### Test Search Quality
```java
List<String> results = rag.getRelevantRules("database connection");
assertFalse(results.isEmpty());
assertTrue(results.get(0).toLowerCase().contains("database"));
```

### Benchmark
```java
long start = System.currentTimeMillis();
rag.getRelevantRules(code);
long elapsed = System.currentTimeMillis() - start;
assertTrue(elapsed < 100);  // Should complete in < 100ms
```

---

## 🔄 Maintenance

### Update Guidelines
1. Edit markdown files in `rag-docs/`
2. Restart RAG service (re-initialize)
3. Changes take effect immediately

### Monitor Quality
1. Track review accuracy
2. Collect feedback from team
3. Update guidelines based on feedback
4. Measure impact

---

## 🎯 Next Steps

1. **Create knowledge base** - Add your standards
2. **Initialize RAG service** - In ReviewAgent
3. **Enhance prompts** - Include RAG context
4. **Test** - Review sample code
5. **Measure** - Compare quality before/after
6. **Iterate** - Update guidelines based on results

---

**Your Vector RAG service is ready to use! 🚀**

Start by creating your `rag-docs/` directory and adding your first guidelines.

