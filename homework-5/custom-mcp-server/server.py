"""Custom MCP server using FastMCP.

Exposes a resource URI and a `read` tool that return a configurable
number of words from lorem-ipsum.md.
"""

import re
from pathlib import Path

from fastmcp import FastMCP

mcp = FastMCP("Lorem Ipsum Server")

LOREM_IPSUM_PATH = Path(__file__).parent / "lorem-ipsum.md"


def _read_words(word_count: int = 30) -> str:
    """Return exactly *word_count* words from lorem-ipsum.md."""
    if word_count <= 0:
        return ""
    text = LOREM_IPSUM_PATH.read_text(encoding="utf-8")
    # Filter out markdown tokens (e.g. '#' headings) — keep only words starting with a letter
    words = [w for w in text.split() if re.match(r"^[a-zA-Z]", w)]
    return " ".join(words[:word_count])


# ---------------------------------------------------------------------------
# Resource – a URI that Claude can read from
# ---------------------------------------------------------------------------
@mcp.resource("lorem://content")
def lorem_resource() -> str:
    """Read the first 30 words from lorem-ipsum.md (default)."""
    return _read_words(30)


# ---------------------------------------------------------------------------
# Tool – an action Claude can call with an optional word_count parameter
# ---------------------------------------------------------------------------
@mcp.tool()
def read(word_count: int = 30) -> str:
    """Read words from lorem-ipsum.md.

    Args:
        word_count: Number of words to return (default 30).
    """
    return _read_words(word_count)
