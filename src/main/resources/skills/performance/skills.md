# Performance Analysis Skill

## Name

Performance Analysis

## Purpose

Identify potential performance problems in Java applications and suggest practical optimizations.

## When to Use

Use this skill when the code contains:

* Loops
* Nested loops
* Database operations
* File operations
* Network calls
* Large collections
* Repeated calculations
* Expensive operations

## Instructions

1. Read and understand the Java code.
2. Identify potentially expensive operations.
3. Check loops and nested loops.
4. Check database calls inside loops.
5. Check file or network I/O inside loops.
6. Check repeated calculations.
7. Check unnecessary object creation.
8. Check inefficient collection usage.
9. Identify the most significant performance concerns.
10. Explain the reason for each concern.
11. Suggest an optimization.
12. Do not modify the original code automatically.
13. Wait for human approval before applying any fix.

## Check For

### Nested Loops

Example:

```java
for (User user : users) {
    for (Order order : orders) {
        // ...
    }
}
```

Check whether the nested iteration can be optimized.

### Database Calls Inside Loops

Example:

```java
for (Long id : ids) {
    repository.findById(id);
}
```

Check whether a batch query could reduce database calls.

### File I/O Inside Loops

Example:

```java
for (File file : files) {
    Files.readAllBytes(file.toPath());
}
```

Check whether repeated file operations could be reduced.

### Repeated Calculations

Identify expensive operations that are unnecessarily executed multiple times.

### Unnecessary Object Creation

Identify objects that are repeatedly created when reuse may be possible.

## Severity

* `HIGH` - Potentially significant performance impact.
* `MEDIUM` - Noticeable performance concern under certain workloads.
* `LOW` - Minor optimization opportunity.

## Output Format

Return:

```text
Type:
PERFORMANCE

Severity:
HIGH | MEDIUM | LOW

Location:
Class/method/line if available

Description:
Explain the performance concern.

Reason:
Explain why it may affect performance.

Suggested Fix:
Explain the optimization.

Suggested Code:
Provide corrected code when possible.
```

## Example

Input:

```java
for (User user : users) {
    repository.findById(user.getId());
}
```

Output:

```text
Type:
PERFORMANCE

Severity:
MEDIUM

Description:
A database query is executed for every user.

Reason:
For N users, this can result in N database calls.

Suggested Fix:
Consider retrieving the required users using a single batch query.
```
