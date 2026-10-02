# Before Review Hook

## Name

Before Review

## Purpose

Validate the code review request before the AI agent starts analyzing the code.

## Trigger

Execute this hook before starting code analysis.

## Checks

1. Check that code has been provided.
2. Check that the code is not empty.
3. Check that the input appears to contain Java code.
4. Check that the review request is valid.

## Rules

If the code is empty:

```text
BLOCK_REVIEW
Reason: Java code was not provided.
```

If the input is not valid Java code:

```text
BLOCK_REVIEW
Reason: Input does not appear to be Java code.
```

If validation succeeds:

```text
ALLOW_REVIEW
```

## Output

Return one of:

```text
ALLOW_REVIEW
BLOCK_REVIEW
```

## Important

This hook only validates the request.

It must not modify the developer's code.
