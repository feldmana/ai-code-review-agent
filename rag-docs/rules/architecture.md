# Architecture Rules

## Controllers
- Must NOT contain business logic
- Must delegate to services only

## Services
- Must contain business logic
- Must be stateless

## Repositories
- Must only handle database access
- Must NOT call external APIs

## Layer rule
Controller → Service → Repository (strict)