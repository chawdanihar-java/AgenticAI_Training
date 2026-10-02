# Java Code Review Agent — Development Guide

## 1. Purpose

This document explains how to develop the **Java Code Review Agent** step by step.

The project is designed as an Agentic AI learning project using:

* Java
* Spring Boot
* RAG
* Skills
* Hooks
* Human-in-the-Loop
* Mock AI
* Claude API
* Test feedback loops

The development should start with a completely offline implementation and gradually introduce real AI capabilities.

---

# 2. Development Principles

Follow these principles during development:

### 2.1 Mock First

Do not start with the real Claude API.

Start with:

```properties
ai.mode=MOCK
```

This allows the complete agent workflow to be tested without:

* API key
* Internet
* API cost

---

### 2.2 Keep Components Independent

Each component should have one main responsibility.

```text
Agent
  -> Orchestrates workflow

RAG
  -> Retrieves knowledge

Skills
  -> Analyze specific problem types

ClaudeService
  -> Communicates with AI

Hooks
  -> Control workflow

Approval Service
  -> Gets human decision

Fix Service
  -> Applies approved fixes

Runner
  -> Starts the application
```

---

### 2.3 Human Approval Is Mandatory

Never allow the AI to automatically modify code without approval.

The required flow is:

```text
Analyze
   |
   v
Suggest Fix
   |
   v
Human Approval
   |
   +---- NO ----> Reject
   |
  YES
   |
   v
Apply Fix
```

---

### 2.4 Validate Every Change

Every approved fix should go through:

```text
Before Fix Hook
       |
       v
Apply Fix
       |
       v
After Fix Hook
       |
       v
Compile / Test
```

---

# 3. Development Environment

## Requirements

Install:

* JDK 17
* Maven
* IntelliJ IDEA or another Java IDE

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

The project targets Java 17.

---

# 4. Project Configuration

Use:

```properties
ai.mode=MOCK
```

in:

```text
src/main/resources/application.properties
```

This is the default development configuration.

---

# 5. Development Sequence

Develop the project in the following order:

```text
1. Models
2. RAG
3. Skills
4. Hooks
5. Human Approval
6. AI abstraction
7. Mock AI
8. Code Review Service
9. Code Fix Service
10. Agent
11. Runner
12. Testing
13. Feedback Loop
14. Developer Session
15. Real Claude
```

Do not implement everything at once.

---

# 6. Step 1 — Create the Models

Start with the data models.

## ReviewStatus

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

---

## Finding

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

---

## CodeReviewResult

The result should contain:

```text
status
message
code
findings
```

Example:

```text
CodeReviewResult[
    status=COMPLETED,
    message=Fix applied successfully,
    code=...,
    findings=[...]
]
```

---

# 7. Step 2 — Implement RAG

Create:

```text
org.example.prac3.rag.RagService
```

The initial RAG implementation should load Markdown files from:

```text
src/main/resources/knowledge/
```

Files:

```text
java-guidelines.md
spring-guidelines.md
sql-guidelines.md
```

Use:

```java
ClassPathResource
```

to read the resources.

---

## RAG Development Goal

The first version only needs to prove:

```text
Java Code
   |
   v
RAG Service
   |
   v
Knowledge Documents
   |
   v
Review Context
```

Do not introduce a vector database at this stage.

---

# 8. Step 3 — Implement Skills

Create:

```java
public interface Skill {

    String getName();

    List<Finding> analyze(String code);
}
```

Then create:

```text
NpeSkill
SqlSkill
PerformanceSkill
```

---

# 9. NPE Skill Development

Start with a simple example:

```java
public String getUserName(User user) {
    return user.getProfile().getName();
}
```

The skill should identify:

```text
user may be null
```

and:

```text
user.getProfile() may be null
```

Return:

```text
Type:
NPE_RISK

Severity:
HIGH
```

The skill should generate a suggested fix.

It should not directly modify the original source code.

---

# 10. SQL Skill Development

Test examples such as:

```java
String query =
        "SELECT * FROM users WHERE name = '" + name + "'";
```

The skill should identify possible SQL injection.

Also test:

```java
for (User user : users) {
    repository.findById(user.getId());
}
```

This can represent a possible N+1/database-call problem.

The skill should return a `Finding`.

---

# 11. Performance Skill Development

Test:

```java
for (User user : users) {

    for (Order order : orders) {

        // processing
    }
}
```

The skill should identify the nested loop.

Also test:

```java
for (File file : files) {
    Files.readAllBytes(file.toPath());
}
```

The skill can identify file I/O inside a loop as a possible performance concern.

---

# 12. Step 4 — Implement Hooks

Create:

```text
BeforeReviewHook
BeforeFixHook
AfterFixHook
```

---

## BeforeReviewHook

The hook should reject invalid input.

Example:

```java
if (code == null || code.isBlank()) {
    return false;
}
```

The hook should not modify code.

---

## BeforeFixHook

The hook should verify:

```text
Finding exists
       +
Suggested fix exists
       +
Human approved
       =
Allow fix
```

Example:

```java
public boolean execute(
        CodeReviewResult result,
        boolean humanApproved) {

    if (result == null) {
        return false;
    }

    if (!humanApproved) {
        return false;
    }

    return result.status() == ReviewStatus.APPROVED;
}
```

---

## AfterFixHook

The hook should validate the modified code.

Initially it can perform simple validation.

Later it should:

```text
Compile
   |
   v
Run Tests
   |
   v
Collect Result
```

---

# 13. Step 5 — Implement Human Approval

Create:

```text
HumanApprovalService
```

Use `Scanner` for the first version.

Example:

```java
@Service
public class HumanApprovalService {

    private final Scanner scanner = new Scanner(System.in);

    public boolean requestApproval(String message) {

        System.out.println("===== HUMAN REVIEW =====");
        System.out.println(message);

        System.out.print("Approve fix? (yes/no): ");

        String input = scanner.nextLine();

        return "yes".equalsIgnoreCase(input.trim());
    }
}
```

---

# 14. Step 6 — Implement ClaudeService

Create:

```java
public interface ClaudeService {

    String analyze(String prompt);
}
```

This interface is important because the application should not directly depend on the Claude implementation.

---

# 15. Step 7 — Implement MockClaudeService

Create:

```text
MockClaudeService
```

Use:

```java
@Service
@ConditionalOnProperty(
        name = "ai.mode",
        havingValue = "MOCK",
        matchIfMissing = true
)
```

The mock service should return a fixed finding.

Example:

```json
{
  "findings": [
    {
      "type": "NPE_RISK",
      "severity": "HIGH",
      "location": "UserService.getUserName()",
      "description": "Potential NullPointerException detected.",
      "reason": "user.getProfile() may return null.",
      "suggestedFix": "Validate user and profile before accessing name."
    }
  ]
}
```

The mock response is only for development.

---

# 16. Step 8 — Implement CodeReviewService

This is where the AI service should actually be called.

The flow should be:

```text
Code
 |
 v
Retrieve RAG
 |
 v
Load Skill Instructions
 |
 v
Build Prompt
 |
 v
ClaudeService.analyze()
 |
 v
Parse Response
 |
 v
Create Findings
 |
 v
CodeReviewResult
```

Example:

```java
String response = claudeService.analyze(prompt);
```

This code belongs in `CodeReviewService`.

Do not move this responsibility into `CodeReviewAgent`.

---

# 17. Building the AI Prompt

The prompt should contain:

```text
1. Role
2. Java source code
3. RAG knowledge
4. Skill instructions
5. Review requirements
6. Expected output format
```

Example:

```text
You are a Java code review assistant.

Review the following Java code.

Code:
<source code>

Relevant Java guidelines:
<retrieved knowledge>

NPE skill instructions:
<skill instructions>

Identify problems and provide:

Type
Severity
Location
Description
Reason
Suggested Fix
Suggested Code
```

---

# 18. Step 9 — Implement CodeFixService

The fix service receives:

```text
Original Code
+
Approved Finding
```

and produces:

```text
Fixed Code
```

Example:

Original:

```java
public String getUserName(User user) {
    return user.getProfile().getName();
}
```

Fixed:

```java
public String getUserName(User user) {

    if (user == null || user.getProfile() == null) {
        return null;
    }

    return user.getProfile().getName();
}
```

---

# 19. Step 10 — Connect Everything in CodeReviewAgent

The agent should orchestrate:

```text
Before Review
       |
       v
RAG
       |
       v
Review Service
       |
       v
Finding
       |
       v
Human Approval
       |
       v
Before Fix
       |
       v
Code Fix
       |
       v
After Fix
       |
       v
Result
```

The existing `CodeReviewAgent` should remain the main orchestrator.

---

# 20. Step 11 — Create CodeReviewRunner

Create:

```text
org.example.prac3.runner.CodeReviewRunner
```

Example:

```java
@Component
public class CodeReviewRunner implements CommandLineRunner {

    private final CodeReviewAgent agent;

    public CodeReviewRunner(CodeReviewAgent agent) {
        this.agent = agent;
    }

    @Override
    public void run(String... args) {

        String code = """
                public String getUserName(User user) {
                    return user.getProfile().getName();
                }
                """;

        CodeReviewResult result = agent.execute(code);

        System.out.println("===== CODE REVIEW RESULT =====");
        System.out.println(result);
    }
}
```

Because Spring automatically performs constructor injection for a single constructor, `@Autowired` is not required.

---

# 21. Step 12 — Test the Complete Offline Flow

Start with:

```properties
ai.mode=MOCK
```

Run the application.

Expected:

```text
===== HUMAN REVIEW =====

Potential NullPointerException detected.

Suggested Fix:
Validate the object and profile before accessing the name.

Approve fix? (yes/no):
```

---

# 22. Test Rejection

Enter:

```text
no
```

Expected:

```text
REJECTED
```

The original code must remain unchanged.

Example:

```text
status=REJECTED
```

---

# 23. Test Approval

Run again and enter:

```text
yes
```

Expected:

```text
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
===== CODE CHANGE =====

Original Code:
...

Fixed Code:
...
```

---

# 24. Step 13 — Add Code Validation

After applying a fix:

```text
Fixed Code
    |
    v
Validation
    |
    +---- Invalid ---> FAILED
    |
    v
Compile
```

The validation process should prevent invalid code from being treated as a successful fix.

---

# 25. Step 14 — Add Test Execution

After compilation:

```text
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
DONE   Agent
       Analysis
```

Test output should be captured.

---

# 26. Step 15 — Implement Failure Feedback

When a test fails, pass the failure information back to the agent.

Example:

```text
Test Failure:
NullPointerException in UserServiceTest

Agent:
Analyze failure

Agent:
New issue detected.

Suggested Fix:
...

Human:
yes

Agent:
Apply approved fix.

Agent:
Run tests again.
```

---

# 27. Approval Must Happen Again

A new fix should always require a new approval.

Never implement:

```text
Test Failed
   |
   v
AI Automatically Changes Code
```

Instead:

```text
Test Failed
   |
   v
AI Analyzes Failure
   |
   v
AI Suggests Fix
   |
   v
Human Approval
   |
   v
Apply Fix
```

---

# 28. Step 16 — Add Developer Session

The future runner should support multiple interactions.

Example:

```text
Developer:
Review this code

Agent:
NPE found.

Approve fix? yes/no:

Developer:
yes

Agent:
Fix applied.
Tests passed.

Developer:
Review another code block

Agent:
...

Developer:
bye
```

Stop condition:

```java
if ("bye".equalsIgnoreCase(userInput.trim())) {
    return ReviewStatus.STOPPED;
}
```

---

# 29. Step 17 — Implement Real Claude

Only after Mock mode works correctly.

Create:

```text
RealClaudeService
```

Use:

```java
@Service
@ConditionalOnProperty(
        name = "ai.mode",
        havingValue = "ONLINE"
)
```

Configuration:

```properties
ai.mode=ONLINE
```

Set:

```text
ANTHROPIC_API_KEY
```

Never hardcode the API key.

---

# 30. Switching Between Mock and Online

## Offline

```properties
ai.mode=MOCK
```

Run:

```bash
mvn spring-boot:run
```

## Online

```properties
ai.mode=ONLINE
```

Set API key.

PowerShell:

```powershell
$env:ANTHROPIC_API_KEY="your-api-key"
```

Then:

```powershell
mvn spring-boot:run
```

The rest of the agent workflow should remain unchanged.

---

# 31. Development Testing Matrix

| Component          | Test                |
| ------------------ | ------------------- |
| Agent              | Complete workflow   |
| RAG                | Knowledge retrieval |
| NPE Skill          | Null access         |
| SQL Skill          | SQL injection       |
| SQL Skill          | N+1                 |
| Performance Skill  | Nested loops        |
| Before Review Hook | Invalid code        |
| Before Fix Hook    | Rejected fix        |
| After Fix Hook     | Invalid fixed code  |
| Approval Service   | YES                 |
| Approval Service   | NO                  |
| Mock Claude        | Fixed response      |
| Code Fix Service   | Approved fix        |
| Test Runner        | PASS                |
| Test Runner        | FAIL                |
| Feedback Loop      | Re-analysis         |

---

# 32. Debugging Strategy

When something fails, debug the components in this order:

```text
Runner
   |
   v
Agent
   |
   v
Before Review Hook
   |
   v
RAG
   |
   v
Review Service
   |
   v
Claude Service
   |
   v
Finding
   |
   v
Approval
   |
   v
Before Fix Hook
   |
   v
Fix Service
   |
   v
After Fix Hook
```

Add logging around each major stage.

Example:

```java
System.out.println("Starting code review...");
System.out.println("RAG completed...");
System.out.println("AI analysis completed...");
System.out.println("Human approval received...");
System.out.println("Fix applied...");
```

---

# 33. Common Development Problems

## Problem: Mock service is not selected

Check:

```properties
ai.mode=MOCK
```

and:

```java
@ConditionalOnProperty(
        name = "ai.mode",
        havingValue = "MOCK",
        matchIfMissing = true
)
```

---

## Problem: Online service is selected unexpectedly

Check that:

```properties
ai.mode=MOCK
```

and the real service has:

```java
@ConditionalOnProperty(
        name = "ai.mode",
        havingValue = "ONLINE"
)
```

---

## Problem: API key is missing

PowerShell:

```powershell
echo $env:ANTHROPIC_API_KEY
```

Linux/macOS:

```bash
echo $ANTHROPIC_API_KEY
```

---

## Problem: Runner does not execute

Verify:

```java
@Component
public class CodeReviewRunner implements CommandLineRunner
```

Also verify that the class is inside a package scanned by Spring Boot.

---

## Problem: Human approval does not wait

Verify that:

```java
scanner.nextLine();
```

is being executed and that the application is running with an interactive console.

---

# 34. Unit Testing Strategy

Use JUnit and Spring Boot Test.

Example:

```java
@Test
void shouldRejectWhenHumanDoesNotApprove() {

    boolean approved = false;

    assertFalse(approved);
}
```

Agent tests should verify state transitions.

Example:

```text
ANALYZING
    ↓
WAITING_FOR_APPROVAL
    ↓
APPROVED
    ↓
FIX_APPLIED
    ↓
COMPLETED
```

Rejection:

```text
ANALYZING
    ↓
WAITING_FOR_APPROVAL
    ↓
REJECTED
```

Failure:

```text
ANALYZING
    ↓
APPROVED
    ↓
FIX_APPLIED
    ↓
TESTING
    ↓
FAILED
```

---

# 35. Git Development Strategy

Use small commits.

Example:

```text
feat: add review result models

feat: add RAG service

feat: add NPE skill

feat: add SQL skill

feat: add performance skill

feat: add review hooks

feat: add human approval

feat: add mock Claude service

feat: add code review service

feat: add code fix service

feat: add review runner

feat: add test validation

feat: add failure feedback loop

feat: add real Claude integration
```

This makes it easier to identify which change introduced a problem.

---

# 36. Development Milestones

## Milestone 1

```text
Application starts
        +
Runner works
        +
Mock Claude works
```

---

## Milestone 2

```text
RAG
+
Skills
+
Hooks
```

---

## Milestone 3

```text
Human Approval
+
Code Fix
+
Code Change Output
```

---

## Milestone 4

```text
Compile
+
Tests
+
Failure Detection
```

---

## Milestone 5

```text
Failure Feedback
+
Agent Loop
+
Developer Session
```

---

## Milestone 6

```text
Real Claude API
```

---

## Milestone 7

```text
Advanced RAG
+
More Skills
+
More Tools
```

---

# 37. Final Development Workflow

The final development workflow should look like:

```text
                Developer
                    |
                    v
               Submit Code
                    |
                    v
              CodeReviewAgent
                    |
                    v
             Before Review Hook
                    |
                    v
                   RAG
                    |
                    v
                 Skills
                    |
                    v
              Claude Service
                    |
                    v
                Findings
                    |
                    v
             Human Approval
              /          \
            NO            YES
            |              |
            v              v
         Reject       Before Fix Hook
                           |
                           v
                       Apply Fix
                           |
                           v
                    After Fix Hook
                           |
                           v
                      Compile/Test
                       /       \
                    PASS       FAIL
                     |           |
                     v           v
                 Complete    Analyze Failure
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

# 38. Final Goal

The finished project should demonstrate the following Agentic AI concepts:

```text
                    Agent
                      |
       +--------------+--------------+
       |              |              |
       v              v              v
      RAG           Skills          Hooks
       |              |              |
       +--------------+--------------+
                      |
                      v
                    AI
                      |
                      v
                 Reasoning
                      |
                      v
                 Suggest Fix
                      |
                      v
              Human Intervention
                      |
                      v
                  Take Action
                      |
                      v
                  Validate
                      |
                      v
               Test Feedback
                      |
                      v
                  Re-analyze
                      |
                      v
                    LOOP
```

The purpose of this project is to learn how to build a **controlled Agentic AI workflow**, rather than simply sending Java code to an LLM and displaying its response.
