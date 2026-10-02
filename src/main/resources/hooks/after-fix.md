# After Fix Hook

## Name

After Fix

## Purpose

Validate the code after an approved fix has been applied.

## Trigger

Execute this hook immediately after applying a code fix.

## Actions

1. Check that the modified code is available.
2. Check that the code is syntactically valid.
3. Compile the project if compilation is available.
4. Run available tests.
5. Collect the test results.

## Decision

### Tests Pass

If compilation and tests succeed:

```text
FIX_SUCCESSFUL
```

The review can be completed.

### Tests Fail

If compilation or tests fail:

```text
FIX_FAILED
```

The failure should be sent back to the AI agent for analysis.

## Loop

When the fix fails:

```text
Apply Fix
    ↓
After Fix Hook
    ↓
Tests Failed
    ↓
AI analyzes failure
    ↓
Generate new fix
    ↓
Human Approval
    ↓
Apply Fix
```

## Important

Do not automatically apply another fix after a test failure.

The new suggested fix must go through the human approval process again.
