# Controller Layer Design Rules

## REST API Best Practices

### 1. HTTP Method Usage
- GET: Retrieve data (should be idempotent, no side effects)
- POST: Create new resource
- PUT: Update entire resource
- PATCH: Partial resource update
- DELETE: Remove resource
- HEAD: Like GET but without body

### 2. Mapping and Routing
- Use specific `@GetMapping`, `@PostMapping` etc. instead of `@RequestMapping`
- Include version in URL path if versioning: `/api/v1/users`
- Use singular nouns: `/api/users/{id}` not `/api/users/list`
- Resource IDs in path: `/api/users/{userId}`, not query params

### 3. Request/Response Handling
- Always specify `produces` and `consumes` when needed
- Use DTOs (Data Transfer Objects) for all input/output
- Never expose internal entities directly
- Map entities to DTOs before returning

Example:
```java
@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest request) {
    UserDTO user = userService.create(request);
    return ResponseEntity.created(URI.create("/users/" + user.getId())).body(user);
}
```

### 4. Error Handling and HTTP Status Codes
- 200 OK: Successful GET/PUT/PATCH
- 201 Created: Successful POST
- 204 No Content: Successful DELETE or empty response
- 400 Bad Request: Invalid input data
- 401 Unauthorized: Authentication failed
- 403 Forbidden: Authenticated but not authorized
- 404 Not Found: Resource doesn't exist
- 409 Conflict: Business logic violation (duplicate)
- 422 Unprocessable Entity: Validation failed
- 500 Internal Server Error: Unexpected error

### 5. Validation
- Use `@Valid` with `@RequestBody` for automatic validation
- Define validation rules in DTOs using `@NotNull`, `@Size`, etc.
- Return 400 Bad Request for validation failures
- Include detailed error messages

Example:
```java
@PostMapping
public ResponseEntity<UserDTO> create(@Valid @RequestBody CreateUserRequest request) {
    // request is guaranteed to be valid here
    return ResponseEntity.ok(userService.create(request));
}
```

### 6. Authorization and Security
- Use `@PreAuthorize` for method-level security
- Check permissions in controller or security layer, not business logic
- Log all authentication failures
- Use HTTPS only in production

Example:
```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
}
```

### 7. Exception Handling
- Create global `@ControllerAdvice` for exception handling
- Map custom exceptions to appropriate HTTP status codes
- Return structured error responses
- NEVER return stack traces to clients

Example:
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException e) {
        return ResponseEntity.status(404).body(new ErrorResponse("User not found", e.getMessage()));
    }
}
```

### 8. Logging in Controllers
- Log requests at DEBUG level (include method, path, params)
- Log responses at DEBUG level (status, execution time)
- Log errors at ERROR level with full context
- Use correlation IDs for tracing

### 9. Pagination and Filtering
- Always support pagination for list endpoints
- Use query parameters: `?page=0&size=20&sort=name,asc`
- Return pagination metadata in response
- Set reasonable defaults and limits

Example:
```java
@GetMapping
public Page<UserDTO> listUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size) {
    return userService.listUsers(PageRequest.of(page, size));
}
```

### 10. URI Versioning Strategy
- Prefer header versioning: `Accept: application/vnd.api.v1+json`
- Or URL versioning: `/api/v1/users`, `/api/v2/users`
- Support multiple versions during transition period
- Document version deprecation clearly

### 11. Content Negotiation
- Support both JSON and XML if needed
- Use `Accept` header for client preference
- Fall back to JSON as default
- Specify charset: `application/json;charset=UTF-8`

### 12. Resource Linking (HATEOAS)
- Include links to related resources
- Include link to self in response
- Use `Location` header for created resources
- Example: `{ "id": 1, "name": "John", "_links": { "self": "/api/users/1" } }`

### 13. Documentation
- Use Swagger/OpenAPI annotations
- Document all endpoints with `@Operation`
- Include request/response examples
- Document error codes and scenarios

Example:
```java
@Operation(summary = "Get user by ID")
@GetMapping("/{id}")
public UserDTO getUser(@PathVariable Long id) {
    return userService.getUser(id);
}
```

### 14. Performance Optimization
- Use `@GetMapping` with `If-Modified-Since` header support for caching
- Return only required fields in response
- Use pagination to limit response size
- Cache responses when appropriate (specify cache headers)

### 15. Security Headers
- Set `X-Content-Type-Options: nosniff`
- Set `X-Frame-Options: DENY` for non-iframe endpoints
- Set `Content-Security-Policy` as needed
- Set `Strict-Transport-Security` for HTTPS

