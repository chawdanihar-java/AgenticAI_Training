# Java Code Review Agent — Implementation Plan

## 1. Project Goal

Build a simple **Agentic AI-based Java Code Review Agent** that can:

1. Accept Java source code.
2. Retrieve relevant coding guidelines using RAG.
3. Analyze the code using specialized skills.
4. Generate findings and suggested fixes.
5. Ask a human to approve or reject the fix.
6. Apply only approved fixes.
7. Validate the modified code.
8. Run tests.
9. Send test failures back to the agent for further analysis.
10. Continue the workflow until the issue is resolved or the developer enters `bye`.

The project should first work completely in **offline/mock mode** and later support **real Claude API integration**.

---

# 2. Agentic AI Flow

```text
Developer
    |
    | Submit Java Code
    v
CodeReviewRunner
    |
    v
CodeReviewAgent
    |
    +----------------------+
    |                      |
    v                      v
Before Review Hook         RAG
                           |
                           v
                    Coding Guidelines
                           |
                           v
                    CodeReviewService
                           |
                           v
                       AI Service
                      /          \
                     /            \
              Mock Claude       Real Claude
               OFFLINE            ONLINE
                     \            /
                      \          /
                       v        v
                         Findings
                            |
                            v
                     Human Approval
                       /        \
                     NO          YES
                     |            |
                     v            v
                 REJECTED    Before Fix Hook
                                  |
                                  v
                              Apply Fix
                                  |
                                  v
                           After Fix Hook
                                  |
                              +---+---+
                              |       |
                            PASS     FAIL
                              |       |
                              v       v
                         COMPLETED   Agent analyzes
                                      failure
                                         |
                                         v
                                        LOOP
```

---

# 3. Technology Stack

* Java 17
* Spring Boot 3.5.6
* Maven
* Anthropic Java SDK 2.64.0
* Spring Boot Web
* Spring Boot Validation
* Spring Boot Test
* Markdown files for skills, hooks, and knowledge
* CommandLineRunner

The initial version does **not** require:

* REST controller
* Postman
* Database
* Vector database

---

# 4. Project Structure

```text
src/main/java/org/example/
│
├── CodeReviewApplication.java
│
└── prac3/
    │
    ├── agent/
    │   └── CodeReviewAgent.java
    │
    ├── ai/
    │   ├── AiMode.java
    │   ├── ClaudeService.java
    │   ├── MockClaudeService.java
    │   └── RealClaudeService.java
    │
    ├── approval/
    │   └── HumanApprovalService.java
    │
    ├── hooks/
    │   ├── BeforeReviewHook.java
    │   ├── BeforeFixHook.java
    │   └── AfterFixHook.java
    │
    ├── model/
    │   ├── CodeReviewResult.java
    │   ├── Finding.java
    │   └── ReviewStatus.java
    │
    ├── rag/
    │   └── RagService.java
    │
    ├── runner/
    │   └── CodeReviewRunner.java
    │
    ├── service/
    │   ├── CodeReviewService.java
    │   └── CodeFixService.java
    │
    └── skills/
        ├── Skill.java
        ├── NpeSkill.java
        ├── SqlSkill.java
        └── PerformanceSkill.java
```

Resources:

```text
src/main/resources/
│
├── application.properties
│
├── skills/
│   ├── npe/
│   │   └── skill.md
│   ├── sql/
│   │   └── skill.md
│   └── performance/
│       └── skill.md
│
├── hooks/
│   ├── before-review.md
│   ├── before-fix.md
│   └── after-fix.md
│
└── knowledge/
    ├── java-guidelines.md
    ├── spring-guidelines.md
    └── sql-guidelines.md
```

---

# 5. Phase 1 — Spring Boot Setup

Create the Spring Boot project with:

* Java 17
* Spring Boot 3.5.6
* Maven
* Anthropic Java SDK 2.64.0

Required dependencies:

```text
spring-boot-starter-web
spring-boot-starter-validation
anthropic-java
spring-boot-starter-test
```

---

# 6. Phase 2 — Create Models

Create the `ReviewStatus` enum:

```java
public enum ReviewStatus {

    ANALYZING,

    WAITING_FOR_APPROVAL,

    APPROVED,

    REJECTED,

    FIX_APPLIED,

    TESTING,

    COMPLETED,

    FAILED,

    STOPPED
}
```

Create `Finding`:

```java
public record Finding(
        String type,
        String severity,
        String location,
        String description,
        String reason,
        String suggestedFix,
        String suggestedCode) {
}
```

Create `CodeReviewResult` with:

```text
status
message
code
findings
```

---

# 7. Phase 3 — Implement CodeReviewAgent

`CodeReviewAgent` is the main orchestrator.

It should coordinate the complete workflow:

```text
Before Review Hook
        |
        v
RAG
        |
        v
Code Review Service
        |
        v
Findings
        |
        v
Human Approval
        |
        v
Before Fix Hook
        |
        v
Apply Fix
        |
        v
After Fix Hook
        |
        v
Final Result
```

The agent should coordinate the workflow instead of containing all analysis logic.

---

# 8. Phase 4 — Implement RAG

Create:

```text
RagService
```

The initial RAG implementation should load Markdown knowledge files from:

```text
src/main/resources/knowledge/
```

Files:

```text
java-guidelines.md
spring-guidelines.md
sql-guidelines.md
```

The initial implementation can use `ClassPathResource`.

### Initial RAG

The first version does not require:

* Embeddings
* Vector database
* Semantic search

It simply retrieves the available knowledge documents.

### Future RAG

```text
Knowledge Documents
        |
        v
Chunking
        |
        v
Embeddings
        |
        v
Vector Database
        |
        v
Similarity Search
        |
        v
Relevant Knowledge
        |
        v
Claude
```

---

# 9. Phase 5 — Implement Skills

Create:

```java
public interface Skill {

    String getName();

    List<Finding> analyze(String code);
}
```

Implement the following skills.

---

## 9.1 NPE Skill

The NPE skill should detect:

* Direct nullable object access
* Nested method calls
* Method chaining
* Nullable parameters
* Nullable return values

Finding type:

```text
NPE_RISK
```

Example:

```java
user.getProfile().getName();
```

Potential issues:

```text
user == null
user.getProfile() == null
```

---

## 9.2 SQL Skill

The SQL skill should detect:

* SQL injection
* `SELECT *`
* Database calls inside loops
* N+1 queries
* Large result sets
* Database performance problems
* Poor SQL practices

Finding types:

```text
SQL_INJECTION
N_PLUS_ONE
DATABASE_PERFORMANCE
SQL_QUALITY
```

---

## 9.3 Performance Skill

The performance skill should detect:

* Nested loops
* Database calls inside loops
* File I/O inside loops
* Network calls inside loops
* Repeated calculations
* Unnecessary object creation
* Inefficient collections

Finding type:

```text
PERFORMANCE
```

---

# 10. Phase 6 — Skill Markdown Files

Each skill should have a Markdown definition.

Example:

```text
src/main/resources/skills/npe/skill.md
```

The skill file should contain:

* Skill name
* Purpose
* When to use
* What to check
* Severity rules
* Output format
* Suggested fix rules
* Human approval requirement

The skill must not automatically modify source code.

---

# 11. Phase 7 — Implement Hooks

Implement three hooks.

---

## Before Review Hook

Responsibilities:

* Validate code exists.
* Validate code is not empty.
* Check that input appears to be Java.
* Block invalid review requests.

Possible results:

```text
ALLOW_REVIEW
BLOCK_REVIEW
```

---

## Before Fix Hook

Responsibilities:

* Verify finding exists.
* Verify suggested fix exists.
* Verify human approval.
* Verify the fix matches the approved fix.

Possible results:

```text
ALLOW_FIX
BLOCK_FIX
```

The AI must not bypass this hook.

---

## After Fix Hook

Responsibilities:

* Validate modified code.
* Compile when possible.
* Run tests.
* Collect test results.
* Report failures.

Possible results:

```text
FIX_SUCCESSFUL
FIX_FAILED
```

---

# 12. Phase 8 — Human Approval

Create:

```text
HumanApprovalService
```

For the first version, use console input.

Example:

```text
===== HUMAN REVIEW =====

Type:
NPE_RISK

Severity:
HIGH

Location:
UserService.getUserName()

Description:
Potential NullPointerException detected.

Reason:
Nested method calls may execute on a null object.

Suggested Fix:
Validate user and profile before accessing name.

Suggested Code:
----------------------------------------

public String getUserName(User user) {
    if (user == null || user.getProfile() == null) {
        return null;
    }

    return user.getProfile().getName();
}

----------------------------------------

Approve fix? (yes/no):
```

The developer must explicitly approve the change.

---

# 13. Phase 9 — AI Abstraction

Create:

```java
public interface ClaudeService {

    String analyze(String prompt);
}
```

Implement:

```text
MockClaudeService
RealClaudeService
```

Architecture:

```text
ClaudeService
      |
      +---- MockClaudeService
      |
      +---- RealClaudeService
```

This makes it possible to switch between offline and online AI without changing the agent.

---

# 14. Phase 10 — Mock Claude

Use:

```properties
ai.mode=MOCK
```

`MockClaudeService` should return a predefined finding.

Example:

```text
Type:
NPE_RISK

Severity:
HIGH

Description:
Potential NullPointerException detected.

Suggested Fix:
Validate the object before accessing the profile.
```

Mock mode is only for testing the Agentic AI workflow.

It does not provide real AI reasoning.

---

# 15. Phase 11 — Connect AI to CodeReviewService

The existing `CodeReviewAgent` should continue to call:

```java
CodeReviewResult reviewResult =
        reviewService.review(code);
```

The AI call should be inside:

```text
CodeReviewService
```

The service should:

1. Receive Java code.
2. Retrieve relevant RAG context.
3. Load relevant skill instructions.
4. Build the AI prompt.
5. Call `ClaudeService`.
6. Parse the AI response.
7. Convert the response into findings.
8. Return `CodeReviewResult`.

Do not create a second review workflow inside `CodeReviewAgent`.

---

# 16. Phase 12 — Implement CodeFixService

Create:

```text
CodeFixService
```

Responsibilities:

* Receive original code.
* Receive approved finding.
* Apply the approved fix.
* Return the modified code.

The fix service should only be called after:

```text
Human Approval
        +
Before Fix Hook
```

---

# 17. Phase 13 — Create CommandLineRunner

Create:

```text
CodeReviewRunner
```

Implement:

```java
@Component
public class CodeReviewRunner implements CommandLineRunner {
```

Use sample code:

```java
String code = """
        public String getUserName(User user) {
            return user.getProfile().getName();
        }
        """;
```

Call:

```java
CodeReviewResult result = agent.execute(code);
```

Print the result.

No REST controller is required.

No Postman is required.

---

# 18. Phase 14 — Test Rejection

Run the application.

When prompted:

```text
Approve fix? (yes/no):
```

Enter:

```text
no
```

Expected:

```text
REJECTED
```

The original code should remain unchanged.

Flow:

```text
Finding
   |
   v
Human says NO
   |
   v
REJECTED
```

---

# 19. Phase 15 — Test Approval

Run again and enter:

```text
yes
```

Expected:

```text
Human Approval
      |
      v
Before Fix Hook
      |
      v
Apply Fix
      |
      v
After Fix Hook
      |
      v
COMPLETED
```

The application should print:

```text
Original Code
```

and:

```text
Fixed Code
```

so the developer can see the change.

---

# 20. Phase 16 — Add Test Execution

After the approved fix is applied:

```text
Apply Fix
    |
    v
Compile
    |
    v
Run Tests
    |
 +--+---+
 |      |
PASS   FAIL
 |      |
 v      v
DONE   Analyze Failure
```

The test result should be captured and passed to the agent when the test fails.

---

# 21. Phase 17 — Implement Feedback Loop

When tests fail:

```text
Test Failure
     |
     v
Agent receives failure
     |
     v
Analyze failure
     |
     v
Generate finding
     |
     v
Generate suggested fix
     |
     v
Human Approval
     |
     v
Apply Fix
     |
     v
Run Tests Again
```

Important:

A new fix must require human approval again.

The agent must not automatically apply another fix after a failure.

---

# 22. Phase 18 — Developer Session

Add an interactive developer session.

Example:

```text
Developer:
Review this code

Agent:
NPE issue found.

Approve fix? yes/no:

Developer:
yes

Agent:
Fix applied.
Tests passed.

Developer:
Review another method

Agent:
...

Developer:
bye

Agent:
Session stopped.
```

Stop condition:

```java
if ("bye".equalsIgnoreCase(userInput.trim())) {
    return ReviewStatus.STOPPED;
}
```

---

# 23. Phase 19 — Online Claude Mode

After the offline workflow works correctly, implement:

```text
RealClaudeService
```

Change:

```properties
ai.mode=MOCK
```

to:

```properties
ai.mode=ONLINE
```

Configure:

```text
ANTHROPIC_API_KEY
```

The API key must not be hardcoded.

---

# 24. Phase 20 — Online Architecture

```text
Developer Code
      |
      v
CodeReviewAgent
      |
      +---- RAG
      |
      +---- Skills
      |
      v
CodeReviewService
      |
      v
RealClaudeService
      |
      v
Claude API
      |
      v
Findings
      |
      v
Human Approval
      |
      v
Apply Approved Fix
      |
      v
Validation
      |
      v
Tests
```

---

# 25. Testing Plan

## Agent Tests

Test:

* Valid Java code.
* Empty code.
* Invalid code.
* Code with no findings.
* Code with findings.
* Human approval.
* Human rejection.
* Before-review hook failure.
* Before-fix hook failure.
* After-fix failure.

## NPE Skill Tests

Test:

* Null object.
* Nested method calls.
* Method chaining.
* Nullable method parameters.
* Nullable return values.

## SQL Skill Tests

Test:

* SQL injection.
* `SELECT *`.
* Database call inside loop.
* N+1 query.
* Large result set.

## Performance Skill Tests

Test:

* Nested loops.
* Database calls inside loops.
* File operations inside loops.
* Network operations inside loops.
* Repeated calculations.

## RAG Tests

Test:

* Knowledge files load successfully.
* Missing knowledge file.
* Empty knowledge file.

## Hook Tests

Test:

* Valid review.
* Invalid review.
* Valid approval.
* Rejected approval.
* Invalid fix.
* Failed validation.

---

# 26. Development Order

Implement the project in this order:

```text
1. Spring Boot Project
        |
2. Models
        |
3. CodeReviewAgent
        |
4. RAG
        |
5. Skills
        |
6. Hooks
        |
7. Human Approval
        |
8. Mock Claude
        |
9. CodeReviewService
        |
10. CodeFixService
        |
11. CommandLineRunner
        |
12. Test YES / NO
        |
13. Test Validation
        |
14. Test Feedback Loop
        |
15. Developer Session
        |
16. Real Claude
        |
17. Advanced RAG
```

---

# 27. Definition of Done

The initial project is complete when:

* [ ] Spring Boot application starts.
* [ ] `CodeReviewRunner` executes automatically.
* [ ] Java code is submitted to the agent.
* [ ] Before Review Hook runs.
* [ ] RAG retrieves knowledge.
* [ ] NPE skill works.
* [ ] SQL skill works.
* [ ] Performance skill works.
* [ ] `ClaudeService` abstraction exists.
* [ ] Mock Claude works.
* [ ] Findings are generated.
* [ ] Human approval is required.
* [ ] Rejection leaves the original code unchanged.
* [ ] Approval allows the fix.
* [ ] Before Fix Hook runs.
* [ ] Fixed code is displayed.
* [ ] After Fix Hook runs.
* [ ] Tests can be executed.
* [ ] Test failures are returned to the agent.
* [ ] New fixes require human approval.
* [ ] `bye` stops the session.
* [ ] Online Claude mode can be enabled.

---

# 28. Important Design Rules

## Rule 1 — Human Approval Is Mandatory

The AI must not silently modify developer code.

```text
AI Suggests
     |
     v
Human Approves
     |
     v
Fix Applied
```

---

## Rule 2 — Separate Responsibilities

```text
CodeReviewAgent
    -> Orchestration

RagService
    -> Knowledge retrieval

Skill
    -> Specialized analysis

ClaudeService
    -> AI communication

Hook
    -> Workflow control

HumanApprovalService
    -> Human decision

CodeFixService
    -> Apply approved fix

CodeReviewRunner
    -> Start application
```

---

## Rule 3 — Mock First

Start with:

```properties
ai.mode=MOCK
```

Complete and test the entire workflow.

Then switch to:

```properties
ai.mode=ONLINE
```

for real Claude integration.

---

## Rule 4 — Keep AI Replaceable

Application code should depend on:

```java
ClaudeService
```

instead of directly depending on a particular AI implementation.

This allows:

```text
Mock Claude
     |
     v
Real Claude
```

without changing the agent architecture.

---

## Rule 5 — Validate Before and After Changes

Every modification should follow:

```text
Before Fix
    |
    v
Apply Fix
    |
    v
After Fix
```

---

## Rule 6 — Failed Fixes Need New Approval

```text
Fix Failed
    |
    v
Agent Analysis
    |
    v
New Suggested Fix
    |
    v
Human Approval
```

The agent must not automatically apply the new fix.

---

# 29. Future Enhancements

After the basic implementation works, add:

### Code Analysis

* Java AST analysis.
* More NPE patterns.
* More SQL rules.
* More performance rules.
* Security analysis.
* Exception-handling analysis.
* Spring-specific analysis.

### RAG

* Document chunking.
* Embeddings.
* Vector database.
* Semantic search.
* Metadata filtering.
* Better context selection.

### Agent

* Agent memory.
* Multiple review iterations.
* Tool calling.
* Structured outputs.
* Multi-agent review.
* Planning and execution.

### Development Integration

* Git integration.
* Pull request review.
* Web UI.
* REST API.
* IDE integration.
* Review history.

### Validation

* Automated compilation.
* Unit test execution.
* Integration test execution.
* Test failure analysis.
* Automated diff generation.

---

# 30. Final Target Architecture

```text
                       Developer
                           |
                           v
                  CodeReviewRunner
                           |
                           v
                    CodeReviewAgent
                           |
            +--------------+--------------+
            |              |              |
            v              v              v
          Hooks            RAG           Skills
            |              |              |
            +--------------+--------------+
                           |
                           v
                  CodeReviewService
                           |
                           v
                     ClaudeService
                      /          \
                     /            \
                  MOCK           ONLINE
                   |                |
                   v                v
            MockClaudeService  RealClaudeService
                   \                /
                    \              /
                     +------v------+
                            |
                         Findings
                            |
                            v
                     Human Approval
                       /        \
                     NO          YES
                     |            |
                     v            v
                  Reject     Before Fix Hook
                                  |
                                  v
                              CodeFixService
                                  |
                                  v
                            After Fix Hook
                                  |
                             +----+----+
                             |         |
                           PASS       FAIL
                             |         |
                             v         v
                         COMPLETED   Agent Analysis
                                       |
                                       v
                                  New Finding
                                       |
                                       v
                                  Human Approval
                                       |
                                       v
                                      LOOP
```

---

# 31. Project Objective

The main objective is to understand that an Agentic AI application is more than simply calling an LLM.

The project should demonstrate:

```text
Observe
   |
   v
Retrieve Knowledge
   |
   v
Analyze
   |
   v
Reason
   |
   v
Suggest
   |
   v
Human Approval
   |
   v
Act
   |
   v
Validate
   |
   v
Analyze Failure
   |
   v
Repeat
```

The final system should provide a controlled workflow where AI assists the developer while the developer remains responsible for approving code changes.
