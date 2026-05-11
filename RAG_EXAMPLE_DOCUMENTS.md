# 📚 Example RAG Documents - Ready to Use

Create these files in your `rag-docs/` directory to get started immediately.

---

## 1. Java Best Practices

**File:** `rag-docs/rules/java-best-practices.md`

```markdown
# Java Best Practices

## Naming Conventions

### Variables and Methods
- Use camelCase for variable names: `firstName`, `getUserId()`
- Use nouns for variables: `user`, `account`, not `getValue`
- Use verbs for methods: `getName()`, `calculateTotal()`
- Avoid single letter variables except loop counters (i, j, k)
- Use descriptive names: `numberOfRetries` not `n`

### Classes and Interfaces
- Use PascalCase: `UserService`, `PaymentProcessor`
- Use nouns for classes: `User`, `Account`
- Use adjectives for interfaces: `Readable`, `Serializable`
- Use Impl suffix sparingly: `UserServiceImpl`

### Constants
- Use UPPER_SNAKE_CASE: `MAX_CONNECTIONS`, `DEFAULT_TIMEOUT`
- Should be static final
- Group related constants in a class or interface

## Method Design

### Method Size
- Keep methods under 20 lines
- One responsibility per method (Single Responsibility Principle)
- If you can't name it in one sentence, it's too complex

### Parameters
- Maximum 3 parameters (use objects for more)
- Avoid boolean parameters (use builder pattern instead)
- Use immutable parameters when possible

## Error Handling

### Exception Handling
- Use checked exceptions for recoverable errors
- Use runtime exceptions for programming errors
- Always log exceptions with full context
- Never ignore exceptions (don't leave empty catch blocks)

### Try-with-resources
Always use try-with-resources for closeable resources:
```java
try (FileInputStream fis = new FileInputStream(file)) {
    // Use file stream
} catch (IOException e) {
    logger.error("Failed to read file", e);
}
```

## Logging

### Log Levels
- **ERROR**: System errors that require attention
- **WARN**: Potentially harmful situations
- **INFO**: High-level progress messages
- **DEBUG**: Detailed diagnostic information

### Log Messages
- Include context: what operation, what resource
- Use structured logging for data
- Don't log sensitive data (passwords, tokens)
```java
logger.info("User login successful: userId={}", userId);
logger.error("Database connection failed: host={}, port={}", host, port);
```

## Collections

### Using Collections
- Use List for ordered collections: `List<User> users`
- Use Set for unique elements: `Set<Long> userId`
- Use Map for key-value pairs: `Map<String, User>`

### Avoid Raw Types
- ✅ Good: `List<String> names`
- ❌ Bad: `List names`

### Stream API
- Use streams for transformations
- Prefer method references over lambdas
- Keep stream operations under 3 chained calls
```java
list.stream()
    .filter(u -> u.isActive())
    .map(User::getName)
    .toList();
```

## Null Handling

### Prevent NullPointerException
- Validate parameters early (fail fast)
- Use Optional for nullable values
- Never return null from collections (return empty instead)
```java
public List<User> getUsers() {
    return users != null ? users : Collections.emptyList();
}
```

## Testing

### Test Coverage
- Aim for 80%+ code coverage
- Test happy path and error cases
- Use descriptive test names

### Test Structure
```java
@Test
void shouldReturnUserWhenIdExists() {
    // Given
    Long userId = 1L;
    
    // When
    User user = repository.findById(userId);
    
    // Then
    assertNotNull(user);
    assertEquals("John", user.getName());
}
```
```

---

## 2. Security Guidelines

**File:** `rag-docs/security/security-guidelines.md`

```markdown
# Security Guidelines

## Authentication & Authorization

### Password Management
- Never hardcode passwords or secrets
- Store hashed passwords using bcrypt or scrypt (never MD5 or SHA1)
- Implement rate limiting on login attempts
- Enforce strong password policies
- Never log passwords or sensitive tokens

### API Keys & Tokens
- Rotate API keys periodically
- Store in environment variables or secret vaults
- Use JWT with short expiration times
- Implement token refresh mechanisms

## Input Validation

### Always Validate User Input
- Check type, length, and format
- Use whitelists instead of blacklists
- Reject invalid input early
```java
if (email == null || !email.matches(EMAIL_REGEX)) {
    throw new InvalidInputException("Invalid email");
}
```

### Prevent SQL Injection
- Use parameterized queries:
```java
String sql = "SELECT * FROM users WHERE email = ?";
preparedStatement.setString(1, email);
```

### Prevent XSS (Cross-Site Scripting)
- Escape output to HTML
- Use templating engines that escape by default
- Never eval() user input

## Data Protection

### Sensitive Data
- Encrypt personally identifiable information (PII)
- Use TLS/SSL for all network communication
- Never store plaintext passwords
- Implement proper access controls

### Data Minimization
- Collect only necessary data
- Delete data after retention period
- Anonymize data when possible

## Error Messages

### Don't Leak Information
- ❌ Bad: "User not found in database"
- ✅ Good: "Invalid credentials"
- ❌ Bad: "SQL error: table 'users' not found"
- ✅ Good: "System error occurred"

## Dependencies

### Keep Dependencies Updated
- Regularly update libraries
- Monitor for security vulnerabilities
- Use tools like OWASP Dependency-Check

### Review Dependencies
- Use trusted sources
- Check dependency licenses
- Minimize number of dependencies
```

---

## 3. Code Organization

**File:** `rag-docs/rules/code-organization.md`

```markdown
# Code Organization

## Package Structure

### Recommended Structure
```
com.example.app
├── controller        (REST endpoints)
├── service          (Business logic)
├── repository       (Data access)
├── model            (Domain objects)
├── exception        (Custom exceptions)
├── util             (Utility classes)
└── config           (Configuration)
```

### Package Naming
- Use reverse domain: `com.company.project`
- Use lowercase: `com.example.service`
- One concept per package
- Keep related classes together

## File Organization

### File Size
- Keep files under 500 lines
- Maximum 5-10 public methods per class
- Split large files into smaller classes

### Import Organization
```java
import java.util.*;  // Standard library first
import javax.*;      // Then javax

import org.springframework.*;  // Then third-party
import com.company.*;  // Then company
```

## Class Design

### Single Responsibility
Each class should have one reason to change:
- ✅ `UserService` - User business logic
- ❌ `UserManager` - Too vague

### Composition over Inheritance
- Prefer composition
- Limit inheritance depth (max 2-3 levels)
- Use interfaces for contracts

## Constants vs Magic Numbers

### Use Constants
```java
public static final int MAX_RETRY_ATTEMPTS = 3;
public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
```

### Never Use Magic Numbers
- ❌ Bad: `if (count > 5) {...}`
- ✅ Good: `if (count > MAX_ITEMS_PER_PAGE) {...}`

## Immutability

### Favor Immutable Objects
- Make fields private and final
- Use records for data holders
- Return defensive copies when needed
```java
public record User(String name, String email) {}
```

## Configuration

### Externalize Configuration
- Use properties files
- Use environment variables
- Use configuration servers
- Never hardcode configuration values

### Application Properties
```properties
# ✅ Good practices
app.name=MyApp
app.database.url=${DB_URL}
app.database.port=${DB_PORT:5432}  # Default value
```
```

---

## 4. Testing Standards

**File:** `rag-docs/testing/testing-standards.md`

```markdown
# Testing Standards

## Test Coverage

### Coverage Goals
- Aim for 80%+ overall coverage
- 100% for critical paths (authentication, payments)
- Test all public methods
- Test edge cases and error conditions

### What Not to Test
- Don't test getters/setters
- Don't test library code
- Don't test trivial code

## Test Naming

### Clear Test Names
- Use descriptive names that explain the test
- Format: `should[ExpectedBehavior]When[Condition]()`

```java
void shouldReturnTrueWhenEmailIsValid() { }
void shouldThrowExceptionWhenPasswordIsNull() { }
void shouldIncrementCounterWhenItemIsAdded() { }
```

## Test Structure (AAA Pattern)

### Arrange, Act, Assert
```java
@Test
void shouldCalculateDiscountCorrectly() {
    // Arrange - Set up test data
    Order order = new Order();
    order.setSubtotal(100.0);
    
    // Act - Execute the function
    double discount = order.applyDiscount(0.1);
    
    // Assert - Verify results
    assertEquals(10.0, discount);
}
```

## Unit vs Integration Tests

### Unit Tests
- Test single class in isolation
- Use mocks for dependencies
- Fast execution (milliseconds)
- Run on every build

### Integration Tests
- Test multiple components together
- Use test containers or test databases
- Slower execution (seconds)
- Run periodically

## Mocking

### When to Mock
- External services (APIs, databases)
- Expensive operations
- Non-deterministic behavior

### Using Mocks
```java
@Test
void shouldFetchUserFromService() {
    // Mock the repository
    UserRepository mockRepo = mock(UserRepository.class);
    when(mockRepo.findById(1L)).thenReturn(new User("John"));
    
    UserService service = new UserService(mockRepo);
    User user = service.getUser(1L);
    
    assertEquals("John", user.getName());
}
```

## Test Data

### Use Test Fixtures
- Create reusable test data builders
- Avoid hard-coded test data
- Use factory methods

## Continuous Integration

### Test Automation
- All tests must pass before merge
- Run tests on every commit
- Measure and report coverage
- Fail build on coverage drop
```

---

## 5. Performance Guidelines

**File:** `rag-docs/performance/performance-guidelines.md`

```markdown
# Performance Guidelines

## Database Queries

### Query Optimization
- Use indexes on frequently queried columns
- Avoid N+1 queries with eager loading
- Use pagination for large result sets
- Batch operations when possible

```java
// ❌ Bad: N+1 queries
for (User user : users) {
    List<Orders> orders = repo.findOrdersByUserId(user.getId());
}

// ✅ Good: Single query with JOIN
List<UserWithOrders> results = repo.findUsersWithOrders();
```

### Connection Pooling
- Use connection pooling (HikariCP)
- Set appropriate pool size
- Monitor connection usage

## Caching

### Cache Strategically
- Cache expensive computations
- Cache frequently accessed data
- Implement cache invalidation strategy
- Use cache warming for critical data

```java
@Cacheable("users")
public User getUser(Long id) {
    return repository.findById(id);
}
```

## Memory Management

### Avoid Memory Leaks
- Close resources properly (try-with-resources)
- Remove listeners when done
- Avoid large static collections
- Use weak references when appropriate

### Collection Sizing
```java
// Bad: Creates 10 million empty elements
List<String> list = new ArrayList<>(10_000_000);

// Good: Start small, let it grow
List<String> list = new ArrayList<>();
```

## Concurrency

### Thread Safety
- Use synchronized only when necessary
- Prefer concurrent collections: `ConcurrentHashMap`
- Use immutable objects
- Document thread-safety assumptions

### Lock Contention
- Keep synchronized blocks small
- Use read-write locks for high read scenarios
- Avoid nested locks
```

---

## Quick Setup

To use these examples:

1. Create directory structure:
```bash
mkdir -p rag-docs/rules
mkdir -p rag-docs/security
mkdir -p rag-docs/testing
mkdir -p rag-docs/performance
```

2. Copy the content above into respective files

3. Initialize RAG service:
```java
VectorRagService rag = new VectorRagService("rag-docs");
rag.initialize();
```

4. Use in code reviews:
```java
List<String> guidelines = rag.getRelevantRules(code);
```

---

**Your knowledge base is ready! Add more documents as needed. 📚**

