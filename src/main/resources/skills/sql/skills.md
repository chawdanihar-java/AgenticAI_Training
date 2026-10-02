# SQL Analysis Skill

## Name

SQL Analysis

## Purpose

Identify common SQL-related problems in Java applications and suggest improvements.

## When to Use

Use this skill when the code contains:

* SQL queries
* Native SQL
* JDBC queries
* JPA queries
* Repository queries
* Database calls
* Multiple database calls

## Instructions

1. Identify database-related code.
2. Find SQL queries or database operations.
3. Check how the query is executed.
4. Look for potential SQL injection risks.
5. Check for unnecessary database calls.
6. Check for database calls inside loops.
7. Check for `SELECT *`.
8. Check for potentially large result sets.
9. Check for possible N+1 query problems.
10. Explain each identified issue.
11. Suggest an improvement.
12. Do not modify the original code automatically.
13. Wait for human approval before applying any fix.

## Check For

### SQL Injection

Identify dynamically constructed SQL such as:

```java
String sql = "SELECT * FROM users WHERE name = '" + name + "'";
```

Recommend parameterized queries.

### Database Calls Inside Loops

Identify patterns such as:

```java
for (User user : users) {
    repository.findById(user.getId());
}
```

Explain the potential performance problem and suggest batch retrieval when appropriate.

### SELECT *

Identify:

```sql
SELECT * FROM users
```

Suggest selecting only the required columns when appropriate.

### N+1 Queries

Identify situations where one query retrieves a list and additional queries are executed for each item.

## Severity

* `HIGH` - Security issue or potentially serious database problem.
* `MEDIUM` - Potential performance or scalability issue.
* `LOW` - Code-quality or optimization opportunity.

## Output Format

Return:

```text
Type:
SQL_INJECTION | N_PLUS_ONE | DATABASE_PERFORMANCE | SQL_QUALITY

Severity:
HIGH | MEDIUM | LOW

Location:
Class/method/line if available

Description:
Explain the problem.

Suggested Fix:
Explain the recommended improvement.

Suggested Code:
Provide corrected code when possible.
```

## Example

Input:

```java
String query = "SELECT * FROM users WHERE name = '" + name + "'";
```

Output:

```text
Type:
SQL_INJECTION

Severity:
HIGH

Description:
User input is directly concatenated into the SQL query.

Suggested Fix:
Use a parameterized query.
```
