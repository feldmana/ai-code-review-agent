# Repository/Data Access Layer Rules

## Database and ORM Best Practices

### 1. Repository Responsibilities
- ONLY handle database operations
- NEVER contain business logic
- NEVER call external APIs
- NEVER call other services
- Pure data access concerns only

### 2. Entity Mapping
- Use `@Entity` with proper `@Table` mapping
- Define all columns with `@Column` annotations
- Use appropriate data types (not everything is String)
- Set nullable constraints: `@Column(nullable = false)`
- Define indexes for frequently queried columns

### 3. Relationships
- Use lazy loading by default for performance
- Only use eager loading when necessary
- Use proper cascade types (avoid CascadeType.ALL without thinking)
- Define both sides of bidirectional relationships

Example:
```java
@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
private Set<Order> orders;

// In Order entity:
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;
```

### 4. Query Methods
- Use Spring Data naming conventions for simple queries
- Create custom `@Query` for complex queries
- Use named parameters: `@Query("SELECT u FROM User u WHERE u.email = :email")`
- Avoid N+1 query problems with proper fetching

Example:
```java
// Simple query - no @Query needed
List<User> findByEmailAndStatus(String email, String status);

// Complex query - use @Query
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.id = :id")
Optional<User> findByIdWithOrders(@Param("id") Long id);
```

### 5. Pagination and Sorting
- Always support pagination for list queries
- Define reasonable default page sizes
- Return `Page<T>` not `List<T>` for paginated queries
- Support sorting by multiple columns

Example:
```java
Page<User> findAll(Pageable pageable);
Page<User> findByStatus(String status, Pageable pageable);
```

### 6. Custom Repository Implementation
- Create custom repository interface extending `Repository<T, ID>`
- Implement with suffix `Impl`
- Inject `EntityManager` for complex queries
- Use `@Repository` annotation

Example:
```java
public interface UserRepositoryCustom {
    List<User> findActiveUsersByRole(String role);
}

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {
    @PersistenceContext
    private EntityManager em;
    
    public List<User> findActiveUsersByRole(String role) {
        // Custom implementation
    }
}
```

### 7. Transaction Management
- Use `@Transactional` on service layer, not repository
- Repository methods inherit transaction from calling service
- Mark query-only methods as `readOnly = true` on service

### 8. Database Performance
- Use database indexes on frequently queried columns
- Use partial indexes for filtered queries
- Denormalize when read performance is critical
- Monitor slow query logs

### 9. Connection Management
- Use connection pooling (HikariCP by default in Spring)
- Configure pool size based on expected load
- Set appropriate connection timeout
- Close resources properly (Spring handles this automatically)

### 10. Batch Operations
- Use batch inserts/updates for bulk operations
- Set `hibernate.jdbc.batch_size` appropriately
- Flush after batch operations to free memory
- Test performance implications

### 11. Caching
- Use `@Cacheable` on repository methods with `readOnly=true`
- Define cache eviction policy: `@CacheEvict` on updates
- Monitor cache hit rates
- Be careful with cache invalidation

### 12. Database Migration
- Use Flyway or Liquibase for schema versioning
- Version all database changes
- Support rollback capability
- Document schema changes

### 13. Testing Repository Layer
- Use `@DataJpaTest` for testing only repository layer
- Use in-memory H2 database for tests
- Clean up data after each test (use `@Transactional` on test methods)
- Test both positive and negative scenarios

Example:
```java
@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    
    @Test
    @Transactional
    public void testFindByEmail() {
        // Test implementation
    }
}
```

### 14. Handling Null and Empty Results
- Return `Optional<T>` for single entity queries
- Return empty `List<T>` not null for collection queries
- Return empty `Page<T>` for paginated queries
- Check presence with `isPresent()`, not null checks

Example:
```java
// Good
Optional<User> user = userRepository.findById(id);
if (user.isPresent()) {
    // process
}

// Better
User user = userRepository.findById(id)
    .orElseThrow(() -> new UserNotFoundException(id));
```

### 15. DTO Projection
- Use projection for selecting specific columns
- Reduces data transfer from database
- Improves query performance
- Use `@Query` with constructor expressions or interfaces

Example:
```java
@Query("SELECT new com.example.UserDTO(u.id, u.name, u.email) FROM User u WHERE u.id = :id")
UserDTO findUserDTOById(@Param("id") Long id);

// Or use interface projection
@Query("SELECT u.id, u.name, u.email FROM User u WHERE u.id = :id")
UserProjection findUserProjectionById(@Param("id") Long id);

interface UserProjection {
    Long getId();
    String getName();
    String getEmail();
}
```

### 16. Query Result Streaming
- Use stream for large result sets
- Remember to close stream after use
- Combine with pagination for very large datasets
- Example:
```java
try (Stream<User> stream = userRepository.streamAll()) {
    stream.filter(...).forEach(...);
}
```

### 17. Error Handling
- Map database exceptions to meaningful application exceptions
- Log database errors with context
- Don't expose database schema to clients
- Handle constraint violations gracefully

### 18. Audit Trail
- Use `@CreatedDate` and `@LastModifiedDate` for temporal data
- Use `@CreatedBy` and `@LastModifiedBy` if tracking users
- Enable Envers for full audit trail
- Consider soft deletes instead of hard deletes

### 19. Concurrency
- Use `@Version` field for optimistic locking
- Handle `OptimisticLockingFailureException` appropriately
- Use pessimistic locking carefully (deadlock risk)
- Prefer optimistic locking for most cases

