# Java Coding Guidelines

## Purpose

Provide general Java coding guidelines that the Code Review Agent can use when analyzing Java source code.

## 1. Null Safety

Avoid code that can cause `NullPointerException`.

### Avoid

```java
return user.getProfile().getName();
```

If `user` or `profile` can be null, this can cause an NPE.

### Prefer

```java
if (user == null || user.getProfile() == null) {
    return null;
}

return user.getProfile().getName();
```

Or use `Optional` where appropriate.

```java
return Optional.ofNullable(user)
        .map(User::getProfile)
        .map(Profile::getName)
        .orElse(null);
```

## 2. Collection Handling

Prefer interfaces when declaring collection variables.

### Prefer

```java
List<String> names = new ArrayList<>();
```

instead of:

```java
ArrayList<String> names = new ArrayList<>();
```

Use the collection type that matches the required operation.

## 3. Avoid Unnecessary Loops

Do not repeatedly iterate over a collection when one pass is sufficient.

### Avoid

```java
for (User user : users) {
    if (user.getName().equals(name)) {
        // ...
    }
}

for (User user : users) {
    if (user.isActive()) {
        // ...
    }
}
```

Consider combining operations when it improves readability and performance.

## 4. Avoid Nested Loops When Possible

Nested loops can result in O(n²) or worse complexity.

### Avoid

```java
for (User user : users) {
    for (Role role : roles) {
        if (user.getRoleId().equals(role.getId())) {
            // ...
        }
    }
}
```

Consider using a `Map` for lookup operations.

```java
Map<Long, Role> rolesById = roles.stream()
        .collect(Collectors.toMap(Role::getId, Function.identity()));

for (User user : users) {
    Role role = rolesById.get(user.getRoleId());
}
```

## 5. Avoid Expensive Operations Inside Loops

Avoid database calls, network calls, file operations, or expensive calculations inside loops unless required.

### Avoid

```java
for (User user : users) {
    userService.getUserDetails(user.getId());
}
```

This may result in many database or network calls.

## 6. String Handling

Use `StringBuilder` when repeatedly modifying strings in a loop.

### Avoid

```java
String result = "";

for (String value : values) {
    result += value;
}
```

### Prefer

```java
StringBuilder result = new StringBuilder();

for (String value : values) {
    result.append(value);
}
```

## 7. Exception Handling

Do not silently ignore exceptions.

### Avoid

```java
try {
    process();
} catch (Exception e) {
}
```

Exceptions should either be handled appropriately or propagated.

### Prefer

```java
try {
    process();
} catch (IOException e) {
    log.error("Failed to process file", e);
    throw new ProcessingException("Unable to process file", e);
}
```

## 8. Logging

Use structured logging instead of `System.out.println`.

### Avoid

```java
System.out.println("User: " + userId);
```

### Prefer

```java
log.info("Processing user: {}", userId);
```

Do not log passwords, access tokens, refresh tokens, or other sensitive information.

## 9. Method Size

Methods should have a clear responsibility.

If a method performs multiple unrelated operations, consider extracting helper methods.

## 10. Constants

Avoid hardcoded values when they represent configuration or reusable constants.

### Avoid

```java
if (status.equals("ACTIVE")) {
    // ...
}
```

When appropriate, use constants or enums.

```java
if (Status.ACTIVE.name().equals(status)) {
    // ...
}
```

## 11. Resource Management

Resources such as streams, files, and connections should be closed properly.

### Prefer

```java
try (InputStream inputStream = file.getInputStream()) {
    // process
}
```

## 12. Code Review Rules

When reviewing Java code:

1. Identify potential correctness issues.
2. Identify null-safety problems.
3. Identify unnecessary complexity.
4. Identify performance concerns.
5. Identify resource-management issues.
6. Check exception handling.
7. Check logging practices.
8. Suggest practical improvements.
9. Do not modify code automatically.
10. Wait for human approval before applying fixes.
