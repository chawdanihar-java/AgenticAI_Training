# SQL Guidelines

## Purpose

Provide SQL and database guidelines that the Code Review Agent can use when reviewing Java database code, SQL queries, JPA queries, native SQL, and JDBC operations.

## 1. SQL Injection

Never build SQL queries by directly concatenating user input.

### Avoid

```java
String sql = "SELECT * FROM users WHERE username = '" + username + "'";
```

User-controlled input can alter the SQL query.

### Prefer

Use parameterized queries.

```java
String sql = "SELECT * FROM users WHERE username = ?";
```

## 2. Avoid SELECT *

### Avoid

```sql
SELECT *
FROM users;
```

Retrieve only the columns required by the application.

### Prefer

```sql
SELECT id, username, email
FROM users;
```

This can reduce unnecessary data transfer and processing.

## 3. Database Calls Inside Loops

Avoid executing a database query for every item in a loop.

### Avoid

```java
for (Long userId : userIds) {
    userRepository.findById(userId);
}
```

This can create many database queries.

### Prefer

Use a batch query when appropriate.

```java
userRepository.findAllById(userIds);
```

## 4. N+1 Query Problem

Be alert for code that loads a collection and then performs another query for each item.

### Example

```java
List<User> users = userRepository.findAll();

for (User user : users) {
    userRepository.findRolesByUserId(user.getId());
}
```

This can result in:

```text
1 query for users
+
N queries for roles
```

Consider joins, fetch strategies, projections, or batch queries where appropriate.

## 5. Indexing

Columns frequently used in:

```sql
WHERE
JOIN
ORDER BY
GROUP BY
```

may benefit from indexes.

Example:

```sql
SELECT *
FROM users
WHERE username = ?;
```

If `username` is frequently searched, an appropriate index may improve lookup performance.

Indexes should be chosen based on actual query patterns and database workload.

## 6. Large Result Sets

Avoid retrieving large amounts of data when only a small subset is required.

### Avoid

```sql
SELECT id, name
FROM users;
```

when the application only needs one user.

### Prefer

```sql
SELECT id, name
FROM users
WHERE id = ?;
```

Use pagination for large datasets where appropriate.

## 7. Filtering

Prefer filtering data in the database rather than retrieving everything and filtering in Java.

### Avoid

```java
List<User> users = userRepository.findAll();

List<User> activeUsers = users.stream()
        .filter(User::isActive)
        .toList();
```

### Prefer

Let the database perform the filtering when appropriate.

```java
List<User> activeUsers = userRepository.findByActiveTrue();
```

## 8. Sorting

Prefer database-side sorting when the result is coming directly from a database query.

### Avoid

```java
List<User> users = userRepository.findAll();

users.sort(Comparator.comparing(User::getName));
```

when database-side sorting is appropriate.

### Prefer

```sql
SELECT id, name
FROM users
ORDER BY name;
```

## 9. Transactions

Use transactions for operations that must succeed or fail as a unit.

Example:

```java
@Transactional
public void transferData() {
    // operation 1
    // operation 2
}
```

Keep transaction boundaries appropriate to the operation.

## 10. Native SQL

Native SQL should be used when it provides a clear benefit over JPQL or repository methods.

When using native SQL:

1. Use parameters.
2. Avoid unnecessary columns.
3. Review query performance.
4. Check indexes.
5. Consider database-specific behavior.

## 11. Query Complexity

Review complex queries for:

* unnecessary joins
* repeated subqueries
* unnecessary sorting
* unnecessary grouping
* missing filters
* excessive data retrieval

Do not optimize a query solely based on appearance. Consider execution plans and actual workload when available.

## 12. Database Calls in Reactive Applications

If using Spring WebFlux with a blocking database driver, avoid executing blocking database operations on the reactive event-loop thread.

Use a reactive database driver such as R2DBC when the application requires fully reactive database access.

## 13. SQL Review Output

For SQL findings, use one of:

```text
SQL_INJECTION
N_PLUS_ONE
DATABASE_PERFORMANCE
SQL_QUALITY
```

Severity:

```text
HIGH
MEDIUM
LOW
```

Each finding should include:

```text
Type:
Severity:
Location:
Description:
Reason:
Suggested Fix:
Suggested Code:
```

## 14. Code Review Rules

When reviewing SQL/database code:

1. Check for SQL injection.
2. Check for queries inside loops.
3. Check for N+1 queries.
4. Check for unnecessary `SELECT *`.
5. Check for excessive result sets.
6. Check filtering and sorting.
7. Check transaction boundaries.
8. Check potential indexing issues.
9. Check native SQL parameterization.
10. Suggest improvements without automatically modifying the code.
