# Homework 5: Configure MCP Servers

**Author**: Sviatoslav Glushchenko

## Overview

This homework configures **three external MCP servers** (GitHub, Filesystem, Jira) and implements **one custom MCP server** with FastMCP. Each server is integrated with the development environment (VS Code / Copilot / Claude) via the MCP protocol.

## Configured Servers

### 1. GitHub MCP ⭐
Connects to GitHub via the official `@modelcontextprotocol/server-github` server. Enables AI to list PRs, summarize commits, create issues, and interact with repositories.

### 2. Filesystem MCP ⭐
Connects to the local project directory via `@modelcontextprotocol/server-filesystem`. Enables AI to list files, read content, and explore directory structures.

### 3. Jira MCP ⭐⭐
Connects to Jira via `mcp-remote` using Atlassian's MCP endpoint. Enables AI to query projects, list tickets, and retrieve bug information.

### 4. Custom MCP Server (Lorem Ipsum) ⭐⭐⭐
A custom FastMCP server that exposes:
- **Resource** (`lorem://content`) — reads the first 30 words from `lorem-ipsum.md`
- **Tool** (`read`) — accepts an optional `word_count` parameter and returns that many words from the file

#### Resources vs Tools
- **Resources** are URIs that Claude can read from (e.g., files, APIs). They provide data passively when accessed.
- **Tools** are actions Claude can call to perform operations (e.g., reading a file with parameters, running a command). They accept input and produce output.

## Technology Stack

| Component | Technology |
|-----------|------------|
| Custom MCP Server | Python + FastMCP |
| GitHub MCP | `@modelcontextprotocol/server-github` (Node.js) |
| Filesystem MCP | `@modelcontextprotocol/server-filesystem` (Node.js) |
| Jira MCP | `mcp-remote` → Atlassian MCP SSE endpoint |
| Client Config | `.mcp.json` |

## AI Tools Used

- GitHub Copilot (Claude) for code generation and MCP configuration

## Project Structure

```
homework-5/
├── README.md
├── HOWTORUN.md
├── TASKS.md
├── .mcp.json                    # MCP server configuration
├── custom-mcp-server/
│   ├── server.py                # Custom FastMCP server
│   ├── lorem-ipsum.md           # Source text for resource/tool
│   └── requirements.txt         # Python dependencies (fastmcp)
└── docs/
    └── screenshots/
        ├── github-mcp-result.png
        ├── filesystem-mcp-result.png
        ├── jira-mcp-result.png
        └── custom-mcp-read-tool-result.png
```
