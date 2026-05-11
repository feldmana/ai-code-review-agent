# Microservices Design Rules

## Service Layer Best Practices

### 1. Single Responsibility Principle (SRP)
- Each service should handle ONE business capability
- If service name contains "And", it violates SRP
- Example BAD: `UserAndOrderService`
- Example GOOD: `UserService`, `OrderService`

### 2. Dependency Injection
- All dependencies must be injected (constructor or field injection)
- NEVER use `new` to instantiate service dependencies
- NEVER use static factories for services
- Use `@Autowired` or constructor injection

### 3. Exception Handling
- Services must throw custom exceptions, not RuntimeException
- Always provide meaningful error context
- Do NOT suppress exceptions silently
- Example: `throw new UserNotFoundException("User ID: " + userId);`

### 4. Null Safety
- Check null references before using
- Throw `IllegalArgumentException` for null parameters
- Example:
```java
public UserDTO getUserById(Long id) {
    if (id == null || id <= 0) {
        throw new IllegalArgumentException("User ID must be positive");
    }
    // ... implementation
}
```

### 5. Statelessness
- Services must be stateless (no instance fields with state)
- Thread-safe by default
- All mutable state should be in repositories/database
- Static state MUST be either immutable or synchronized

### 6. Logging
- Log at method entry for important operations
- Log at method exit (success or failure)
- Use appropriate log levels:
  - INFO: Normal business operations
  - WARN: Potentially problematic situations
  - ERROR: Error conditions
  - DEBUG: Detailed diagnostic information

### 7. Transaction Management
- Use `@Transactional` for methods that modify data
- Set `readOnly=true` for query-only methods
- NEVER nest transactions without careful consideration
- Use appropriate isolation levels

### 8. Method Signatures
- Methods should be simple (max 3-4 parameters)
- Use DTOs for complex parameter passing
- Always define return types (never Object)
- Methods should be public, package-private or private (no protected without reason)

### 9. Code Clarity
- Method names must clearly express intent
- Use present tense: `getUserById()` not `fetchUserData()`
- Getter methods: `getUser()` not `user()`
- Boolean methods: `isActive()`, `hasPermission()`

### 10. Error Handling Strategy
```java
// DO: Specific exception handling
try {
    return userRepository.findById(id);
} catch (DataAccessException e) {
    throw new UserAccessException("Failed to retrieve user", e);
}

// DON'T: Generic catch-all
try {
    return userRepository.findById(id);
} catch (Exception e) {
    return null;
}
```

### 11. Collections and Streams
- Initialize collections with capacity when size is known
- Use immutable collections for return values when possible
- Be careful with null in stream operations
- Filter before map for performance

### 12. Performance Considerations
- Lazy load relationships to avoid N+1 queries
- Use pagination for large result sets
- Cache frequently accessed data
- Monitor query performance

### 13. API Contract
- Document all public methods with JavaDoc
- Include parameter descriptions and return value
- Include thrown exceptions in JavaDoc
- Example:
```java
/**
 * Creates a new user account
 * @param email user email address (must not be null)
 * @param password user password (must be min 8 chars)
 * @return created user DTO
 * @throws UserAlreadyExistsException if email already registered
 * @throws InvalidPasswordException if password too weak
 */
public UserDTO createUser(String email, String password) {
    // ...
}
```

### 14. Service Naming Conventions
- Suffix with "Service": `UserService`, `OrderService`
- Interface names don't need "I" prefix
- Implementation class: `UserServiceImpl` only if interface exists
- Repository names suffix with "Repository"

### 15. Testing Coverage
- Unit tests must cover both success and failure paths
- Mock all external dependencies
- Test edge cases (null, empty, max values)
- Test error conditions with custom exceptions

