# Java Naming Conventions
## Package Naming Convention

- All Java packages MUST start with:
  com.feldmana

- Examples:
    - com.feldmana.service
    - com.feldmana.controller
    - com.feldmana.rag

- Any package not starting with com.feldmana is INVALID.

## Classes
- Must use PascalCase
- Must be nouns or noun phrases
- Example: UserService, OrderController

## Methods
- Must use camelCase
- Must be verbs or actions
- Example: getUserById, createOrder

## Variables
- Must be descriptive
- Avoid abbreviations
- Example: userCount (not uc)

## Booleans
- Should start with: is, has, can
- Example: isActive, hasPermission