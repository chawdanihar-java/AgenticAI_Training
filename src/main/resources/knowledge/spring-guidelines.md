# Spring Boot Guidelines

## Purpose

Provide Spring Boot and Spring Framework guidelines that the Code Review Agent can use during code analysis.

## 1. Dependency Injection

Prefer constructor injection over field injection.

### Avoid

```java
@Autowired
private UserService userService;
```

### Prefer

```java
private final UserService userService;

public UserController(UserService userService) {
    this.userService = userService;
}
```

Constructor injection makes dependencies explicit and improves testability.

## 2. Controller Responsibility

Controllers should handle HTTP-related concerns.

Avoid putting complex business logic directly inside controllers.

### Avoid

```java
@PostMapping("/users")
public User createUser(@RequestBody UserRequest request) {
    // large amount of business logic
    // database operations
    // validation
    // calculations
}
```

### Prefer

```java
@PostMapping("/users")
public Mono<UserResponse> createUser(@RequestBody UserRequest request) {
    return userService.createUser(request);
}
```

Business logic should generally reside in the service layer.

## 3. Service Layer

Services should contain business logic and coordinate repositories and other services.

Avoid putting database-specific logic directly into controllers.

## 4. Repository Usage

Use Spring Data repositories where appropriate.

```java
public interface UserRepository extends JpaRepository<User, Long> {
}
```

Avoid unnecessary database calls.

### Avoid

```java
User user = repository.findById(id).orElse(null);

if (user != null) {
    repository.findRolesByUserId(user.getId());
}
```

When possible, design queries to retrieve the required data efficiently.

## 5. Avoid Database Calls Inside Loops

### Avoid

```java
for (Long userId : userIds) {
    userRepository.findById(userId);
}
```

This can generate many database queries.

Consider a batch query:

```java
userRepository.findAllById(userIds);
```

## 6. Reactive Programming

When using Spring WebFlux, avoid blocking operations inside reactive pipelines.

### Avoid

```java
public Mono<User> getUser(Long id) {
    return Mono.fromCallable(() -> blockingRepository.findById(id));
}
```

If the repository is blocking, it should be isolated appropriately rather than executed directly on the reactive event-loop thread.

## 7. Mono and Flux

Use:

* `Mono<T>` for zero or one result.
* `Flux<T>` for multiple results.

### Example

```java
public Mono<User> getUser(Long id) {
    return userRepository.findById(id);
}
```

```java
public Flux<User> getUsers() {
    return userRepository.findAll();
}
```

Avoid unnecessary conversions between `Mono` and `Flux`.

## 8. Reactive Pipeline

Avoid unnecessary `block()` calls.

### Avoid

```java
User user = userService.getUser(id).block();
```

Blocking defeats the purpose of reactive processing and can cause performance problems.

## 9. Exception Handling

Use appropriate Spring exception handling.

For REST APIs, consider centralized exception handling using:

```java
@RestControllerAdvice
```

Avoid repeating identical exception-handling logic across controllers.

## 10. Configuration

Avoid hardcoding environment-specific values.

### Avoid

```java
String url = "http://localhost:8080";
```

Prefer configuration:

```yaml
service:
  url: ${SERVICE_URL}
```

And inject it into the application.

## 11. Validation

Validate incoming request data.

```java
public Mono<ResponseEntity<?>> createUser(
        @Valid @RequestBody UserRequest request) {
    // ...
}
```

Use Bean Validation annotations where appropriate.

```java
@NotBlank
private String username;
```

## 12. Security

Do not expose sensitive information in API responses or logs.

Sensitive information includes:

* Passwords
* Access tokens
* Refresh tokens
* Client secrets
* API keys

Use Spring Security mechanisms for authentication and authorization.

## 13. API Design

Use meaningful HTTP methods.

* `GET` → retrieve data
* `POST` → create/process
* `PUT` → replace/update
* `PATCH` → partial update
* `DELETE` → delete

Use appropriate HTTP status codes.

## 14. Code Review Rules

When reviewing Spring Boot code:

1. Check dependency injection.
2. Check controller/service separation.
3. Check repository usage.
4. Check database calls.
5. Check reactive code for blocking operations.
6. Check exception handling.
7. Check validation.
8. Check configuration management.
9. Check security-sensitive data handling.
10. Suggest fixes without automatically applying them.
