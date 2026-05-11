# forSashaExplanations - Learning Path & Practical Exercises

## 📚 Structured Learning Path

### **Phase 1: Foundation (Days 1-2)**

#### What You'll Learn
- System overview and architecture
- Multi-agent concept
- Basic RAG principles
- Why this design matters

#### Reading Materials
1. **Executive Summary** from forSashaExplanations.md
2. **Architecture Overview** diagrams
3. [Multi-Agent Systems](https://en.wikipedia.org/wiki/Multi-agent_system)
4. [RAG Introduction](https://python.langchain.com/docs/modules/data_connection/)

#### Practical Exercise 1: Draw the Architecture

**Task**: Redraw the CodeReviewAgent architecture from memory

```
Try to recreate this without looking:
INPUT → [Router] → [Planner] → [Orchestrator] → ...

Time: 20 minutes
Success: Can label all main components
```

---

### **Phase 2: Design Patterns (Days 3-4)**

#### What You'll Learn
- 6 key design patterns used
- Why each pattern was chosen
- How to apply them to your own code

#### Reading Materials
1. **Design Patterns Used** section from forSashaExplanations.md
2. [Refactoring Guru Design Patterns](https://refactoring.guru/design-patterns)
3. [GoF Design Patterns](https://en.wikipedia.org/wiki/Design_Patterns)

#### Pattern Study Guide

**Agent Pattern** (Day 3 Morning)
```
Read:
1. Code example in forSashaExplanations.md
2. Wikipedia article on software agents
3. Look at RouterAgent.java in codebase

Code along:
1. Create a simple DummyAgent class
2. Implement review() method
3. Make it independent
4. Test it in isolation

Questions:
- Why should agents be independent?
- How would you test a single agent?
- Can agents communicate?
```

**Strategy Pattern** (Day 3 Afternoon)
```
Read:
1. Strategy Pattern in forSashaExplanations.md
2. Compare KeyRagService vs VectorRagService
3. Understand RagService interface

Code along:
1. Create a SimpleStrategy class
2. Create an AdvancedStrategy class
3. Implement same interface
4. Switch between them at runtime

Questions:
- How is this different from inheritance?
- When would you use this pattern?
- Can you add a ThirdStrategy?
```

**Builder Pattern** (Day 4 Morning)
```
Read:
1. Builder Pattern in forSashaExplanations.md
2. Study RagContextBuilder.java
3. See StringBuilder usage in Java

Code along:
1. Create a ConfigBuilder
2. Build config step by step
3. Compare with constructor approach
4. Notice cleanliness difference

Questions:
- What problems does Builder solve?
- When is it better than constructors?
- How complex should builders be?
```

**Orchestrator & Others** (Day 4 Afternoon)
```
Read:
1. Orchestrator pattern
2. Repository pattern
3. Factory pattern

Review in code:
1. AgentOrchestrator.java
2. FileScannerTool, FileReaderTool
3. How dependencies are created
```

#### Practical Exercise 2: Design Pattern Matching

**Task**: Match patterns to their uses

```
Match each pattern to its use case:

Patterns:          Use Cases:
A) Agent          1) Switching between algorithms
B) Strategy       2) Building complex objects
C) Builder        3) Independent task handlers
D) Orchestrator   4) Coordinating workflow
E) Repository     5) Data access abstraction
F) Factory        6) Creating objects

Answers: A→3, B→1, C→2, D→4, E→5, F→6
```

**Time**: 10 minutes to match, 30 minutes to verify in code

---

### **Phase 3: RAG System Deep Dive (Days 5-7)**

#### What You'll Learn
- BM25 ranking algorithm
- Vector database concepts
- How retrieval improves LLM accuracy
- Code type detection

#### Reading Materials
1. **System Design Solutions** from forSashaExplanations.md
2. [BM25 Wikipedia](https://en.wikipedia.org/wiki/Okapi_BM25)
3. [Vector Databases Explained](https://www.pinecone.io/learn/vector-database/)
4. [Elasticsearch BM25 Blog](https://www.elastic.co/blog/found-elasticsearch-from-the-bottom-up)

#### BM25 Algorithm Learning (Day 5)

**Understanding the Math**
```
BM25(d, q) = Σ(i=1 to n) IDF(qi) * (f(qi, d) * (k1 + 1)) / 
             (f(qi, d) + k1 * (1 - b + b * |d| / avgdl))

Breakdown:
1. IDF(qi) = log((N - n(qi) + 0.5) / (n(qi) + 0.5) + 1)
   - How rare is this term?
   - Rare terms get high weight
   
2. f(qi, d) = how often term appears in document
   - High frequency helps, but not linear
   
3. (k1 + 1) * f / (f + k1 * ...) = saturation curve
   - Very frequent terms don't help more
   
4. Length normalization (b)
   - Longer documents shouldn't be favored
```

**Practical Example**
```
Dataset:
- Document A: "service dependency injection database"
- Document B: "service rest api controller"
- Document C: "database connection pooling"
- Document D: "architecture clean code"

Query: "service dependency"

Step 1: Calculate IDF
- service: log(4/2) = 0.69
- dependency: log(4/1) = 1.39

Step 2: Calculate TF for each doc
- Doc A: service=1, dependency=1
- Doc B: service=1, dependency=0
- Doc C: service=0, dependency=0
- Doc D: service=0, dependency=0

Step 3: Apply BM25 formula
- Doc A: HIGH (both terms, relevant)
- Doc B: MEDIUM (service only)
- Doc C: LOW (no match)
- Doc D: LOW (no match)

Result: A > B > C ≈ D
```

**Code Implementation Analysis**
```
Read EnhancedVectorStore.java:
1. How does it store vectors?
2. How does it compute IDF?
3. How does computeBM25() work?
4. Try computing by hand for one query
```

#### Vector Database Learning (Day 6)

**Core Concepts**
```
Vector Store = Database of Vectors + Metadata

Each entry contains:
- Vector ID: "rule_001"
- Vector content: "# Null Safety: Check before using"
- Metadata:
  - source: "microservices-design.md"
  - category: "SERVICE_DESIGN"
  - length: 250 words
  - terms: {null: 0.8, check: 0.6, ...}

Operations:
1. ADD: Store new vector
2. SEARCH: Find similar vectors (BM25)
3. UPDATE: Modify existing
4. DELETE: Remove vector
```

**Practical Simulation**
```
Simulate a vector store:

Step 1: Add documents
├─ Doc1: "service design patterns"
├─ Doc2: "rest api best practices"
└─ Doc3: "database optimization tips"

Step 2: Query with "service api"
├─ Score Doc1: 2.1 (has service, api not present)
├─ Score Doc2: 1.8 (api present, service not present)
└─ Score Doc3: 0.3 (neither term)

Step 3: Return ranked results
[Doc1: 2.1, Doc2: 1.8, Doc3: 0.3]

Step 4: Use top-2 for LLM context
```

#### Code Type Detection (Day 7)

**Detection Logic**
```
Read RagContextBuilder.java:

detectCodeType() function:
1. Scans for @annotations
2. Maps to types
3. Returns category

Example:
@Service
public class UserService { }
↓
Detected: "SERVICE"
↓
Load context: "Service-specific rules..."
```

**Practical Exercise: Add New Type**

```
Task: Add detection for @Configuration

In RagContextBuilder.java:

public static String detectCodeType(String code) {
    // ... existing code ...
    
    // ADD THIS:
    if (code.contains("@Configuration")) {
        return "CONFIGURATION";
    }
    
    return "GENERAL";
}

Then in buildRecommendationContext():
if ("CONFIGURATION".equals(codeType)) {
    return "This is a Configuration class. Pay attention to:\n" +
           "- Bean definitions\n" +
           "- Property injection\n" +
           "- Component scanning\n";
}
```

#### Practical Exercise 3: Manual BM25 Calculation

**Given**:
```
Documents:
- A: "java spring boot microservices"
- B: "java spring mvc web"
- C: "python django web"

Query: "java spring microservices"
```

**Calculate**:
1. IDF for each term
2. TF for each document
3. BM25 scores
4. Rank results

**Answer** (Check against your calculation):
```
IDF:
- java: log(3/3 + 0.5 / 1 + 0.5) = 0.0
- spring: log(3/2 + 0.5 / 1 + 0.5) = 0.46
- microservices: log(3/1 + 0.5 / 1 + 0.5) = 1.16

TF + BM25:
- A: HIGH (has all, microservices rare)
- B: MEDIUM (has java & spring)
- C: LOW (missing key terms)

Final: A > B > C
```

---

### **Phase 4: Codebase Exploration (Days 8-10)**

#### What You'll Learn
- How to navigate the actual codebase
- Read and understand real code
- Trace execution flow
- Understand dependencies

#### Day 8: Main.java & Orchestrator

**Read**:
1. src/main/java/com/agentic/codereview/Main.java
2. src/main/java/com/agentic/codereview/orchestrator/AgentOrchestrator.java

**Questions to Answer**:
```
1. What happens first in main()?
2. How is configuration loaded?
3. How does orchestrator get created?
4. What's the execution sequence?
5. How are errors handled?
```

**Exercise: Trace One Request**

```
Write out the complete flow:

User Input: "review /path/to/code"
   ↓
main() calls _____
   ↓
_____ calls _____
   ↓
[Continue until output]
```

#### Day 9: Review Agent & RAG Integration

**Read**:
1. src/main/java/com/agentic/codereview/agent/ReviewAgent.java
2. src/main/java/com/agentic/codereview/rag/RagContextBuilder.java
3. src/main/java/com/agentic/codereview/rag/EnhancedVectorRagService.java

**Questions**:
```
1. How does ReviewAgent call RAG?
2. What does RagContextBuilder.buildReviewContext() return?
3. How is code type detected?
4. What's the final prompt structure?
5. How are retries handled?
```

**Exercise: Print the Actual Prompt**

```
Add logging to ReviewAgent:

logger.info("=== FINAL PROMPT ===");
logger.info(prompt);
logger.info("=== END PROMPT ===");

Run the system and see actual prompt sent to LLM
```

#### Day 10: Tool Layer & Utilities

**Read**:
1. src/main/java/com/agentic/codereview/tool/FileScannerTool.java
2. src/main/java/com/agentic/codereview/tool/FileReaderTool.java
3. src/main/java/com/agentic/codereview/util/JsonExtractor.java

**Questions**:
```
1. How does FileScannerTool work?
2. How does FileReaderTool read files?
3. How does JsonExtractor parse responses?
4. What error handling is in place?
5. How testable is each component?
```

---

### **Phase 5: Improvements & Extensions (Days 11-15)**

#### What You'll Learn
- Identify improvement opportunities
- Design new features
- Implement enhancements
- Test modifications

#### Day 11: Code Review Checklist

**Review** the codebase for:
```
☐ Code is clean and readable
☐ Error handling is complete
☐ Logging is appropriate
☐ Tests are present
☐ Documentation is clear
☐ Performance is acceptable
☐ Security is considered
```

**Document findings** in a report

#### Day 12-13: Implement One Improvement

**Choose from**:
1. Add caching layer
2. Add database persistence
3. Add metrics/monitoring
4. Add new code type detection
5. Add REST API endpoint

**Steps**:
```
1. Design the change
2. Write tests first (TDD)
3. Implement feature
4. Verify tests pass
5. Write documentation
6. Review with team
```

#### Day 14-15: Create New Agent or Tool

**Choose from**:
1. SecurityAnalysisAgent
2. PerformanceOptimizationAgent
3. DocumentationQualityAgent
4. CoverageAnalysisAgent

**Steps**:
```
1. Define agent responsibility
2. Create interface/class
3. Implement core logic
4. Integrate with orchestrator
5. Write tests
6. Document
```

---

## 📋 Practical Exercises with Solutions

### Exercise 1: Modify BM25 Parameters

**Problem**: Current BM25 uses k1=1.5, b=0.75. What if we change them?

**Task**:
```java
// In EnhancedVectorStore.java
private float computeBM25(EmbeddingVector query, EmbeddingVector doc) {
    final float k1 = 2.0f;    // Try 2.0 instead of 1.5
    final float b = 0.5f;     // Try 0.5 instead of 0.75
    
    // ... rest of code ...
}

Test with different values and measure:
- Accuracy: Does it find relevant rules?
- Speed: Is retrieval faster?
- Results: Are rankings better?
```

**Solution Guide**:
- Higher k1 → More weight to term frequency
- Higher b → More penalty for long documents
- Experiment and measure results

---

### Exercise 2: Add New Rule File

**Problem**: Add a "Security Best Practices" rule file

**Task**:
```
1. Create rag-docs/rules/security-best-practices.md
2. Write 10-15 security rules
3. Rules should be auto-detected (filename hint)
4. Test that rules get loaded
5. Verify they appear in reviews
```

**Solution**:
```markdown
# Security Best Practices

## 1. Input Validation
- Always validate input
- Use whitelists not blacklists
- Example: Check email format

## 2. SQL Injection Prevention
- Use prepared statements
- Never concatenate SQL
- Use ORM when possible

... (8-13 more rules)
```

---

### Exercise 3: Add Logging Levels

**Problem**: Some logs are INFO, some are DEBUG. Standardize.

**Task**:
```java
// Review log levels:
logger.debug() - Development details
logger.info()  - Important events
logger.warn()  - Warnings
logger.error() - Errors

// Go through codebase and verify:
- Are debug logs really for debugging?
- Are info logs for important info?
- Are errors logged as errors?
```

---

### Exercise 4: Write Unit Tests

**Problem**: ReviewAgent has no unit tests

**Task**:
```java
@Test
public void testReviewFileWithValidInput() {
    String fileName = "UserService.java";
    String content = "@Service\npublic class UserService { }";
    
    ReviewResult result = reviewAgent.reviewFileWithRetry(fileName, content);
    
    assertNotNull(result);
    assertEquals(fileName, result.getFileName());
}

@Test
public void testReviewFileWithNullInput() {
    assertThrows(IllegalArgumentException.class, () -> {
        reviewAgent.reviewFileWithRetry(null, null);
    });
}

// Add 5-10 more tests
```

---

### Exercise 5: Add Configuration Option

**Problem**: Users want to choose LLM model

**Task**:
```java
// In AppConfig.java
private String ollama_model = "llama3";

// Add setter/getter
public String getOllamaModel() {
    return System.getenv("OLLAMA_MODEL") 
        != null ? System.getenv("OLLAMA_MODEL") 
        : "llama3";
}

// In codereview.properties
# OLLAMA_MODEL=mistral
# or
# OLLAMA_MODEL=neural-chat

// Test it works
```

---

## 🎓 Knowledge Verification Quiz

### Section 1: Fundamentals
```
1. What is the purpose of RouterAgent?
   A) Review code
   B) Classify task
   C) Generate report
   
Answer: B

2. What does RAG stand for?
   A) Random Access Gateway
   B) Retrieval-Augmented Generation
   C) Rules API Gateway
   
Answer: B

3. What algorithm does EnhancedVectorRagService use?
   A) Jaccard similarity
   B) Cosine similarity
   C) BM25
   
Answer: C
```

### Section 2: Design Patterns
```
4. Which pattern is used for RagService?
   A) Factory
   B) Strategy
   C) Builder
   
Answer: B

5. What does AgentOrchestrator do?
   A) Execute agents in sequence
   B) Create agents
   C) Test agents
   
Answer: A

6. What problem does Builder pattern solve?
   A) Creating simple objects
   B) Building complex objects step-by-step
   C) Switching algorithms
   
Answer: B
```

### Section 3: RAG System
```
7. What does IDF measure in BM25?
   A) How often term appears
   B) How rare a term is
   C) Document length
   
Answer: B

8. What metadata is stored per rule?
   A) Just the content
   B) Content, source, category, terms
   C) Only source
   
Answer: B

9. How is code type detected?
   A) File extension
   B) Looking for annotations
   C) Regex pattern matching
   
Answer: B
```

---

## 🏆 Mastery Checklist

By the end of Phase 5, you should be able to:

```
UNDERSTANDING:
☐ Explain multi-agent architecture
☐ Describe BM25 algorithm
☐ Explain RAG concept
☐ Identify design patterns in code
☐ Trace request through system

CODING:
☐ Add new agent
☐ Add new rule file
☐ Modify configuration
☐ Write unit tests
☐ Fix bugs

DESIGN:
☐ Propose architecture change
☐ Design new feature
☐ Choose appropriate pattern
☐ Estimate complexity
☐ Review other's code

DEPLOYMENT:
☐ Build and run system
☐ Configure settings
☐ Monitor execution
☐ Handle errors
☐ Send reports via email
```

---

## 📞 Learning Support

**When You're Stuck**:
1. Reread the relevant section of forSashaExplanations.md
2. Check code comments
3. Look at similar implementations
4. Test with simple examples
5. Ask team/AI for help

**Resources**:
- All links provided in forSashaExplanations.md
- Code comments throughout project
- Video tutorials for each concept
- Online documentation

---

**Keep Learning! 🚀**

This is a 15-day intensive learning program to master CodeReviewAgent v2.0 and learn valuable software engineering concepts along the way.


