# NPE Detection Skill

## Name

NPE Detection

## Purpose

Identify potential `NullPointerException` risks in Java code and suggest safe fixes.

## When to Use

Use this skill when the Java code contains:

* Object method calls without null checks
* Chained method calls
* Nullable method parameters
* Nested object access
* Methods that may return `null`
* Collection or object access that may be null

## Instructions

1. Read and understand the provided Java code.
2. Identify variables and objects that could potentially be `null`.
3. Check chained method calls.
4. Check method parameters that are used without validation.
5. Check return values that may be `null`.
6. Identify the exact code that could cause a `NullPointerException`.
7. Explain why the NPE could occur.
8. Suggest a safe fix.
9. Do not modify the original code automatically.
10. Wait for human approval before applying any fix.

## Severity

Use the following severity levels:

* `HIGH` - NPE is highly likely or can occur in a commonly executed path.
* `MEDIUM` - NPE is possible but depends on specific conditions.
* `LOW` - Potential risk with limited impact or uncertainty.

## Output Format

Return:

```text
Type:
NPE_RISK

Severity:
HIGH | MEDIUM | LOW

Location:
Class/method/line if available

Description:
Explain the potential NPE.

Suggested Fix:
Explain how the problem can be fixed.

Suggested Code:
Provide the corrected code when possible.
```

## Example

Input:

```java
public String getUserName(User user) {
    return user.getProfile().getName();
}
```

Output:

```text
Type:
NPE_RISK

Severity:
HIGH

Description:
user or user.getProfile() may be null before getName() is called.

Suggested Fix:
Validate user and profile before accessing name.
```
