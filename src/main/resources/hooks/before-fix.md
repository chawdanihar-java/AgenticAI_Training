# Before Fix Hook

## Name

Before Fix

## Purpose

Prevent the AI agent from modifying code without human approval.

## Trigger

Execute this hook immediately before applying a suggested code fix.

## Required Checks

1. A valid code review must exist.
2. A finding must exist.
3. A suggested fix must exist.
4. Human approval must be received.
5. The fix being applied must match the approved fix.

## Rules

### No Human Approval

If human approval has not been received:

```text
BLOCK_FIX
Reason: Human approval is required before modifying code.
```

### Human Rejected

If the human rejected the suggested fix:

```text
BLOCK_FIX
Reason: Human rejected the suggested fix.
```

### Human Approved

If the human approved the suggested fix:

```text
ALLOW_FIX
```

## Output

Return one of:

```text
ALLOW_FIX
BLOCK_FIX
```

## Important

The AI agent must never bypass this hook.

No code modification is allowed when the result is:

```text
BLOCK_FIX
```

Human approval is mandatory before applying any fix.
