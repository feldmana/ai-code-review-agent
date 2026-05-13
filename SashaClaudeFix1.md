# SashaClaudeFix1 — Code Review & MCP Chat Client

## Session Date
2026-05-13

---

## Bugs Fixed

### 1. `OllamaClient.java` — debug println removed
**File:** `src/main/java/com/agentic/codereview/llm/OllamaClient.java`  
**Problem:** Two `System.out.println` statements were left in `generateResponse()` — they printed every URL and full request body to stdout in production.  
**Fix:** Replaced with `logger.debug(...)`.

---

### 2. `MCPClient.listTools()` — was silently broken
**File:** `src/main/java/com/agentic/codereview/mcp/MCPClient.java`  
**Problem:** Method sent the `list_tools` request but then returned `List.of()` instead of parsing the JSON response. Any caller got an empty list every time.  
**Fix:** Now parses the `tools` array from the server response and returns the real tool names.

---

### 3. `AgentOrchestrator.java` — dead code removed
**File:** `src/main/java/com/agentic/codereview/orchestrator/AgentOrchestrator.java`  
**Problem:** `writeReport1()` was a duplicate of `writeReport()`, with the original body commented out below it. Unused and confusing.  
**Fix:** Removed `writeReport1()` entirely.

---

## New Code Added

### 4. `AgentOrchestrator.java` — two new public methods
**File:** `src/main/java/com/agentic/codereview/orchestrator/AgentOrchestrator.java`  
**Why:** The MCP tools need to call the orchestrator pipeline and get results back.

| Method | What it does |
|---|---|
| `runReviewAndGetReport(projectPath)` | Runs scan → review → summarize → write report, returns the report String |
| `runReviewAndSendEmail(projectPath)` | Runs the full pipeline including email send |

---

### 5. `MCPToolRegistry.java` — two new MCP tools + `registerOrchestratorTools()`
**File:** `src/main/java/com/agentic/codereview/mcp/MCPToolRegistry.java`  
**Why:** The chat client needs orchestrator-level actions exposed as MCP tools (not just file-level review).

| Tool name | Input | What it does |
|---|---|---|
| `run_full_review` | `projectPath` | Scans all files, reviews them with AI, returns full report |
| `send_email_report` | `projectPath` | Runs full review + sends report via email |

Call `registry.registerOrchestratorTools(orchestrator)` after constructing the registry to add these two tools.

---

### 6. `ChatMCPClient.java` — NEW FILE (main goal)
**File:** `src/main/java/com/agentic/codereview/mcp/ChatMCPClient.java`  
**Purpose:** Interactive chat that demonstrates MCP end-to-end.

#### How to run
```
mvn compile exec:java -Dexec.mainClass="com.agentic.codereview.mcp.ChatMCPClient"
```
Or run `ChatMCPClient.main()` from your IDE.  
Ollama must be running: `ollama serve`

#### What it does step by step

```
1. Starts embedded MCP server (port 9877)
2. MCPClient connects to it
3. Prints available tools (calls list_tools via MCP)
4. Chat loop:
     You type: "I want to review my code"
     [AI]  → Ollama classifies: REVIEW
     [Chat] → asks for project path
     [MCP] ──► scan_files          (prints JSON request + response)
     [MCP] ──► review_code (x3)    (prints JSON request + response per file)

     You type: "send me an email with the report"
     [AI]  → Ollama classifies: EMAIL
     [Chat] → asks for project path
     [MCP] ──► send_email_report   (prints JSON request + response)
```

#### MCP flow visible in console
Every tool call prints:
```
[MCP] ──► Calling tool: scan_files
      Request  : {"method":"invoke_tool","toolName":"scan_files","input":{...}}
      Response : {"result":{"filesFound":5,"files":[...]}}
```
This shows the full MCP protocol: client → server → tool handler → response.

---

## Architecture Observation (not changed)
`RouterAgent.routeTask()` uses keyword matching while `ChatMCPClient` uses Ollama for routing. Both work, but the intent of the project seems to be LLM-based routing. Worth aligning in a future session.

---

## Status

| Item | Status |
|---|---|
| Fix OllamaClient println | DONE |
| Fix MCPClient.listTools() | DONE |
| Remove AgentOrchestrator dead code | DONE |
| Add runReviewAndGetReport / runReviewAndSendEmail | DONE |
| Add run_full_review MCP tool | DONE |
| Add send_email_report MCP tool | DONE |
| ChatMCPClient.java | DONE |