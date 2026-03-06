# How to Run — Homework 5: MCP Servers

## Prerequisites

- **Node.js** 18+ and **npm** (for GitHub, Filesystem, and Jira MCP servers)
- **Python** 3.10+ and **pip** (for the custom FastMCP server)
- **VS Code** with GitHub Copilot extension (or Claude Code)

---

## Task 1: GitHub MCP

### Setup

1. Generate a GitHub Personal Access Token at https://github.com/settings/tokens with `repo` scope.
2. Open `.mcp.json` and replace `<YOUR_GITHUB_TOKEN>` with your actual token.

### Verify

In Copilot Chat (Agent mode), the GitHub MCP should auto-connect. Ask:
```
List recent pull requests in this repository
```

---

## Task 2: Filesystem MCP

### Setup

No additional credentials needed. The path in `.mcp.json` points to the project workspace.

To change the directory, edit the last argument in the `filesystem` server args in `.mcp.json`.

### Verify

In Copilot Chat (Agent mode), ask:
```
List all files in the homework-5 directory
```

---

## Task 3: Jira MCP

### Setup

1. The Jira MCP uses Atlassian's hosted MCP endpoint via `mcp-remote`.
2. On first connection, it will open a browser for Atlassian OAuth authentication.
3. Authorize access to your Jira instance.

### Verify

In Copilot Chat (Agent mode), ask:
```
Give me the Jira tickets of the last 5 bugs on a project
```

---

## Task 4: Custom MCP Server (Lorem Ipsum)

### Install Dependencies

```bash
cd homework-5/custom-mcp-server
pip install -r requirements.txt
```

### Run the Server (standalone test)

```bash
cd homework-5/custom-mcp-server
fastmcp run server.py
```

The server starts and listens for MCP protocol connections via stdio.

### Connect via MCP Configuration

The `.mcp.json` in `homework-5/` already includes the `custom-lorem` server entry. When VS Code loads the MCP config, it will start the server automatically.

### Use the `read` Tool

In Copilot Chat (Agent mode), ask:
```
Use the read tool to get 15 words from lorem-ipsum
```

Or for the default (30 words):
```
Use the read tool from the custom lorem server
```

### Test Manually with FastMCP Dev Mode

```bash
cd homework-5/custom-mcp-server
fastmcp dev server.py
```

This opens a web inspector at `http://localhost:5173` where you can test the resource and tool interactively.

---

## MCP Configuration

All servers are configured in `homework-5/.mcp.json`. This file is auto-detected by VS Code / Copilot when the workspace is opened.

```json
{
  "mcpServers": {
    "github": { ... },
    "filesystem": { ... },
    "jira": { ... },
    "custom-lorem": { ... }
  }
}
```

---

## Screenshots

After verifying each server, capture screenshots and place them in:
```
homework-5/docs/screenshots/
```
