# Bad Examples

## Naming
Bad:
class userservice {}

Good:
class UserService {}

## Architecture
Bad:
controller directly calls repository

Good:
controller → service → repository