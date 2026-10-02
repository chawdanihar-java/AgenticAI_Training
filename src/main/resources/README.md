# Java Code Review Agent

## Overview

**Java Code Review Agent** is a simple Agentic AI learning project built with **Java, Spring Boot, Maven, RAG, Skills, Hooks, Human-in-the-Loop, and Claude**.

The project demonstrates how an AI agent can:

1. Receive Java code.
2. Retrieve coding guidelines using RAG.
3. Analyze the code using specialized skills.
4. Identify potential problems.
5. Generate suggested fixes.
6. Ask a human to approve or reject the fix.
7. Apply the fix only after approval.
8. Validate the modified code.
9. Re-analyze the code when validation fails.

The project supports two AI modes:

* **OFFLINE / MOCK** — no API key and no internet connection required.
* **ONLINE / CLAUDE** — uses the Anthropic Claude API.

The current project runs from Java using `CommandLineRunner`.

**Postman and a REST controller are not required.**

---

# Features

The project currently demonstrates:

* Java code review
* NullPointerException detection
* SQL analysis
* Performance analysis
* RAG-based coding guidelines
* Specialized AI skills
* Before-review hooks
* Before-fix hooks
* After-fix hooks
* Human approval
* AI-generated suggested fixes
* Applying approved fixes
* Original vs fixed code comparison
* Mock AI for offline development
* Claude integration for online mode
* Agent feedback loop architecture

---

# Agentic AI Workflow

```text
Developer / Sample Code
          |
          v
   CodeReviewRunner
          |
          v
    CodeReviewAgent
          |
          +----------------------+
          |                      |
          v                      v
   Before Review Hook            RAG
          |                      |
          +----------+-----------+
                     |
                     v
             Code Review Service
                     |
                     v
                AI Service
                /         \
               /           \
      Mock Claude        Real Claude
       OFFLINE             ONLINE
           \                /
            \              /
             v            v
                Findings
                    |
                    v
             Human Approval
              /          \
            YES           NO
             |             |
             v             v
       Before Fix       REJECTED
          Hook
             |
             v
         Apply Fix
             |
             v
       After Fix Hook
             |
        +----+----+
        |         |
       PASS      FAIL
        |         |
        v         v
   COMPLETED   Agent analyzes
               failure
                  |
                  v
                 LOOP
```

---

# Project Structure

```text
AgenticAI_Training/
│
├── pom.xml
├── README.md
│
└── src/
    └── main/
        ├── java/
        │   └── org/example/
        │       ├── CodeReviewApplication.java
        │       │
        │       └── prac3/
        │           ├── agent/
        │           │   └── CodeReviewAgent.java
        │           │
        │           ├── ai/
        │           │   ├── AiMode.java
        │           │   ├── ClaudeService.java
        │           │   ├── MockClaudeService.java
        │           │   └── RealClaudeService.java
        │           │
        │           ├── approval/
        │           │   └── HumanApprovalService.java
        │           │
        │           ├── hooks/
        │           │   ├── BeforeReviewHook.java
        │           │   ├── BeforeFixHook.java
        │           │   └── AfterFixHook.java
        │           │
        │           ├── model/
        │           │   ├── CodeReviewResult.java
        │           │   ├── Finding.java
        │           │   └── ReviewStatus.java
        │           │
        │           ├── rag/
        │           │   └── RagService.java
        │           │
        │           ├── runner/
        │           │   └── CodeReviewRunner.java
        │           │
        │           ├── service/
        │           │   ├── CodeReviewService.java
        │           │   └── CodeFixService.java
        │           │
        │           └── skills/
        │               ├── Skill.java
        │               ├── NpeSkill.java
        │               ├── SqlSkill.java
        │               └── PerformanceSkill.java
        │
        └── resources/
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

# Technologies

| Technology                | Purpose                         |
| ------------------------- | ------------------------------- |
| Java 17                   | Programming language            |
| Spring Boot 3.5.6         | Application framework           |
| Maven                     | Build and dependency management |
| Anthropic Java SDK 2.64.0 | Claude API integration          |
| Spring Web                | Spring web support              |
| Spring Validation         | Validation                      |
| Spring Boot Test          | Testing                         |

---

# Prerequisites

## Offline Mode

You need:

* Java 17 or later
* Maven 3.6.3 or later
* IntelliJ IDEA or another Java IDE

Check Java:

```bash
java -version
```

Check Maven:

```bash
mvn -version
```

## Online Mode

In addition to the above:

* Internet connection
* Anthropic API key
* `ANTHROPIC_API_KEY` environment variable

---

# Configuration

The configuration is located at:

```text
src/main/resources/application.properties
```

---

# Offline Mode

Offline mode uses `MockClaudeService`.

It does not call Claude.

It is useful for:

* Learning the Agentic AI flow.
* Testing skills.
* Testing RAG.
* Testing hooks.
* Testing human approval.
* Testing code-fix logic.
* Developing without an API key.
* Developing without internet access.

## Configure Offline Mode

Set:

```properties
ai.mode=MOCK
```

The mock implementation should contain:

```java
@ConditionalOnProperty(
        name = "ai.mode",
        havingValue = "MOCK",
        matchIfMissing = true
)
```

The real Claude implementation should contain:

```java
@ConditionalOnProperty(
        name = "ai.mode",
        havingValue = "ONLINE"
)
```

This allows Spring to activate the appropriate AI implementation.

---

# Run Offline

## Option 1: IntelliJ IDEA

1. Open the project in IntelliJ IDEA.
2. Wait for Maven dependencies to download.
3. Open:

```text
org.example.CodeReviewApplication
```

4. Click **Run**.

The application will start.

`CodeReviewRunner` implements:

```java
CommandLineRunner
```

Therefore, its `run()` method executes when the Spring Boot application starts.

---

# Run With Maven

From the project root:

```bash
mvn clean install
```

Then:

```bash
mvn spring-boot:run
```

---

# Run the JAR

Build the application:

```bash
mvn clean package
```

Then:

```bash
java -jar target/AgenticAI_Training-1.0-SNAPSHOT.jar
```

The exact JAR name can vary depending on the Maven configuration.

---

# No Postman Required

The current project does not require:

* Postman
* REST API
* `@RestController`
* `@RequestBody`

The application directly executes:

```java
CodeReviewResult result = agent.execute(code);
```

through:

```java
@Component
public class CodeReviewRunner implements CommandLineRunner
```

---

# Example Input

The runner can submit:

```java
String code = """
        public String getUserName(User user) {
            return user.getProfile().getName();
        }
        """;
```

The code may cause a `NullPointerException` if:

```text
user == null
```

or:

```text
user.getProfile() == null
```

---

# Example Human Review

The agent displays the finding:

```text
===== HUMAN REVIEW =====

Type:
NPE_RISK

Severity:
HIGH

Description:
Potential NullPointerException detected.

Reason:
Nested method calls may execute on a null object.

Suggested Fix:
Validate the object and profile before accessing the name.

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

---

# Human Approval

The developer must explicitly approve the suggested fix.

## YES

Enter:

```text
yes
```

The agent continues:

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

## NO

Enter:

```text
no
```

The agent returns:

```text
REJECTED
```

The original code remains unchanged.

---

# Code Change Output

When the fix is approved, the application displays the original and fixed code.

Example:

```text
===== CODE CHANGE =====

Original Code:
----------------------------------------
public String getUserName(User user) {
    return user.getProfile().getName();
}
----------------------------------------

Fixed Code:
----------------------------------------
public String getUserName(User user) {
    if (user == null || user.getProfile() == null) {
        return null;
    }

    return user.getProfile().getName();
}
----------------------------------------
```

This allows the developer to clearly see what changed.

---

# RAG

RAG stands for **Retrieval-Augmented Generation**.

The project stores coding knowledge in:

```text
src/main/resources/knowledge/
```

Files:

```text
java-guidelines.md
spring-guidelines.md
sql-guidelines.md
```

The RAG service retrieves these guidelines and makes them available to the review process.

## Current RAG

The current implementation is intentionally simple.

It loads Markdown files from application resources.

It does not yet use:

* Embeddings
* Vector databases
* Semantic search
* Vector similarity

A future version can implement a production-style RAG pipeline.

---

# Skills

Skills provide specialized analysis capabilities.

## NPE Skill

The NPE skill looks for:

* Potential `NullPointerException`.
* Unsafe method chaining.
* Nullable parameters.
* Nested object access.
* Nullable return values.

Finding type:

```text
NPE_RISK
```

---

## SQL Skill

The SQL skill checks for:

* SQL injection.
* `SELECT *`.
* Database calls inside loops.
* N+1 queries.
* Large result sets.
* Database performance issues.
* SQL quality problems.

Finding types:

```text
SQL_INJECTION
N_PLUS_ONE
DATABASE_PERFORMANCE
SQL_QUALITY
```

---

## Performance Skill

The performance skill checks for:

* Nested loops.
* Database calls inside loops.
* File I/O inside loops.
* Network I/O inside loops.
* Repeated calculations.
* Unnecessary object creation.
* Inefficient collections.

Finding type:

```text
PERFORMANCE
```

---

# Hooks

Hooks are control points in the Agentic AI workflow.

## Before Review Hook

Runs before the code is analyzed.

It checks:

* Code exists.
* Code is not empty.
* Input appears to be Java.
* Review request is valid.

Invalid requests can be blocked.

---

## Before Fix Hook

Runs before applying a fix.

It checks:

* Finding exists.
* Suggested fix exists.
* Human approval was received.
* The fix being applied matches the approved fix.

The AI should not bypass this hook.

---

## After Fix Hook

Runs after the fix.

It can:

* Validate modified code.
* Check syntax.
* Compile code when possible.
* Run tests.
* Collect test results.

If validation fails, the failure can be returned to the agent.

---

# Mock Claude

The project uses an abstraction:

```java
public interface ClaudeService {
    String analyze(String prompt);
}
```

There are two implementations:

```text
ClaudeService
     |
     +---- MockClaudeService
     |
     +---- RealClaudeService
```

The current offline implementation is:

```text
MockClaudeService
```

The mock service returns a predefined response.

Therefore, Mock mode is **not real AI reasoning**. It is used to test the Agentic AI architecture without an external API.

---

# Online Mode

Online mode connects the project to Claude through the Anthropic Java SDK.

Change:

```properties
ai.mode=MOCK
```

to:

```properties
ai.mode=ONLINE
```

---

# Anthropic API Key

Do not hardcode the API key.

Do not commit it to Git.

Use:

```text
ANTHROPIC_API_KEY
```

as an environment variable.

---

# Windows PowerShell

Set the API key for the current PowerShell session:

```powershell
$env:ANTHROPIC_API_KEY="your-api-key"
```

Check:

```powershell
echo $env:ANTHROPIC_API_KEY
```

Then run:

```powershell
mvn spring-boot:run
```

---

# Windows Command Prompt

```cmd
set ANTHROPIC_API_KEY=your-api-key
```

Then:

```cmd
mvn spring-boot:run
```

---

# Linux / macOS

```bash
export ANTHROPIC_API_KEY="your-api-key"
```

Then:

```bash
mvn spring-boot:run
```

---

# Real Claude Service

The online implementation can initialize the Anthropic client from the environment:

```java
AnthropicClient client = AnthropicOkHttpClient.fromEnv();
```

A simplified implementation looks like:

```java
@Service
@ConditionalOnProperty(
        name = "ai.mode",
        havingValue = "ONLINE"
)
public class RealClaudeService implements ClaudeService {

    private final AnthropicClient client;

    public RealClaudeService() {
        this.client = AnthropicOkHttpClient.fromEnv();
    }

    @Override
    public String analyze(String prompt) {
        // Create Claude request.
        // Send prompt.
        // Return Claude response.
        return "...";
    }
}
```

Use the current Anthropic Java SDK documentation for the exact request and response APIs.

---

# Online Flow

```text
CodeReviewRunner
       |
       v
CodeReviewAgent
       |
       v
RAG
       |
       v
Skills
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
AI Finding
       |
       v
Human Approval
       |
       v
Apply Approved Fix
       |
       v
After Fix Validation
```

---

# Offline vs Online

| Feature        | Offline / MOCK  | Online / Claude          |
| -------------- | --------------- | ------------------------ |
| Internet       | Not required    | Required                 |
| API key        | Not required    | Required                 |
| Claude API     | No              | Yes                      |
| AI response    | Mock/predefined | Generated by Claude      |
| API cost       | No              | May incur API usage cost |
| RAG            | Yes             | Yes                      |
| Skills         | Yes             | Yes                      |
| Hooks          | Yes             | Yes                      |
| Human approval | Yes             | Yes                      |
| Code fixing    | Yes             | Yes                      |
| Agent workflow | Yes             | Yes                      |

---

# Review Result

The main result contains:

```text
status
message
code
findings
```

Example statuses:

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

# Finding

A finding contains:

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

Example:

```text
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
Validate the object and profile before accessing the name.

Suggested Code:
...
```

---

# Agent Responsibilities

`CodeReviewAgent` is the orchestrator.

It coordinates:

```text
Before Review
      ↓
RAG
      ↓
Skills / Review Service
      ↓
Human Approval
      ↓
Before Fix
      ↓
Apply Fix
      ↓
After Fix
```

The agent should not contain all analysis logic itself.

---

# Human-in-the-Loop

Human intervention is intentionally required before code modification.

The AI can:

```text
Analyze
Explain
Suggest
```

But the human decides whether the suggested fix is applied.

```text
AI Finding
    |
    v
Suggested Fix
    |
    v
Human Decision
    |
 +--+--+
 |     |
YES    NO
 |     |
 v     v
Fix   Reject
```

---

# Test Feedback Loop

The intended future workflow includes test execution.

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
Done   Analyze Failure
          |
          v
      New Suggested Fix
          |
          v
      Human Approval
          |
          v
      Apply Fix
          |
          v
       Test Again
```

A new fix should require human approval again.

---

# Future Enhancements

## 1. Better Java Analysis

Add:

* Java AST parsing.
* More NPE patterns.
* More SQL rules.
* More performance rules.
* Security analysis.
* Exception-handling analysis.

## 2. Real RAG

Future architecture:

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

## 3. Real Claude Integration

Replace:

```text
MockClaudeService
```

with:

```text
RealClaudeService
```

without changing the rest of the agent architecture.

## 4. Test Execution

Add:

* Maven test execution.
* Compilation checks.
* Test result parsing.
* Failure analysis.
* Retry loop.

## 5. Interactive Developer Input

The current runner uses sample code.

A future version can allow the developer to enter code interactively.

The agent can also support:

```text
bye
```

to stop the current agent session.

Example:

```java
if ("bye".equalsIgnoreCase(userInput.trim())) {
    return ReviewStatus.STOPPED;
}
```

---

# Current Limitations

This is a learning/demo project.

It currently does not provide:

* Production-grade Java AST analysis.
* Production-grade vector RAG.
* Persistent review history.
* Web UI.
* REST API requirement.
* Secure sandbox execution for arbitrary code.
* Automatic approval.
* Full automated test-repair loop.
* Real AI reasoning in MOCK mode.

The Mock AI response is predefined.

---

# Security Considerations

Never:

* Hardcode an API key.
* Commit API keys to Git.
* Put secrets inside `application.properties`.
* Print API keys in logs.
* Automatically execute untrusted generated code without sandboxing.
* Send confidential source code to an external AI service without authorization.

When using online mode, source code sent to Claude is processed through the external API.

---

# Build

Run:

```bash
mvn clean package
```

---

# Test

Run:

```bash
mvn test
```

---

# Troubleshooting

## Mock service is not running

Check:

```properties
ai.mode=MOCK
```

and verify that `MockClaudeService` uses:

```java
@ConditionalOnProperty(
        name = "ai.mode",
        havingValue = "MOCK",
        matchIfMissing = true
)
```

---

## Real Claude service is not running

Check:

```properties
ai.mode=ONLINE
```

and verify:

```java
@ConditionalOnProperty(
        name = "ai.mode",
        havingValue = "ONLINE"
)
```

---

## API key error

PowerShell:

```powershell
echo $env:ANTHROPIC_API_KEY
```

If empty:

```powershell
$env:ANTHROPIC_API_KEY="your-api-key"
```

---

## Maven build error

Run:

```bash
mvn clean
mvn install
```

Then reload Maven in IntelliJ.

---

## Runner does not execute

Verify that `CodeReviewRunner` contains:

```java
@Component
public class CodeReviewRunner implements CommandLineRunner
```

and:

```java
@Override
public void run(String... args) {
    // code review
}
```

---

# Recommended Learning Sequence

Follow these steps:

### Step 1 — Run Offline

Set:

```properties
ai.mode=MOCK
```

Run the application.

### Step 2 — Test NPE Detection

Use:

```java
public String getUserName(User user) {
    return user.getProfile().getName();
}
```

### Step 3 — Test Human Rejection

Enter:

```text
no
```

Expected:

```text
REJECTED
```

The original code should remain unchanged.

### Step 4 — Test Human Approval

Enter:

```text
yes
```

Expected:

```text
COMPLETED
```

The fixed code should be displayed.

### Step 5 — Test RAG

Modify the knowledge files and observe how the review context changes.

### Step 6 — Test Other Skills

Test:

* SQL
* Performance

### Step 7 — Add Test Validation

Compile and test the generated code.

### Step 8 — Implement Failure Feedback

If tests fail:

```text
Failure
   ↓
Agent Analysis
   ↓
Suggested Fix
   ↓
Human Approval
   ↓
Apply Fix
   ↓
Test Again
```

### Step 9 — Switch to Online

Change:

```properties
ai.mode=ONLINE
```

Set:

```text
ANTHROPIC_API_KEY
```

Then run the application.

---

# Design Principle

The project follows a simple principle:

> AI should suggest changes, not silently modify code.

The intended workflow is:

```text
Analyze
   ↓
Explain
   ↓
Suggest
   ↓
Human Approval
   ↓
Apply
   ↓
Validate
   ↓
Feedback
   ↓
Repeat
```

This demonstrates the difference between a simple LLM call and an Agentic AI workflow.

---

# Summary

The Java Code Review Agent is a learning project for understanding Agentic AI concepts using Java.

It combines:

* Agent orchestration
* RAG
* Skills
* Hooks
* Human-in-the-loop
* AI-generated fixes
* Validation
* Feedback loops
* Mock AI
* Real Claude integration

Start with offline mode:

```properties
ai.mode=MOCK
```

Once the complete workflow works, switch to:

```properties
ai.mode=ONLINE
```

and configure:

```text
ANTHROPIC_API_KEY
```

The same agent architecture can then use the real Claude service instead of the mock implementation.
