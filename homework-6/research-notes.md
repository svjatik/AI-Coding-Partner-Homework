# Research Notes — Context7 Queries

## Query 1: FastMCP tool and resource decorators
- Search: "fastmcp tools resources decorators"
- context7 library ID: /jlowin/fastmcp
- Applied: Used `@mcp.tool()` decorator to expose `get_transaction_status` and `list_pipeline_results` as callable tools. Used `@mcp.resource("pipeline://summary")` to expose the pipeline summary as a readable resource. FastMCP handles the MCP protocol automatically when `mcp.run()` is called.

## Query 2: Python decimal module for financial arithmetic
- Search: "Python decimal module ROUND_HALF_UP financial"
- context7 library ID: /python/cpython
- Applied: Used `decimal.Decimal(str(amount))` to parse string amounts from JSON envelopes without floating-point precision loss. Applied `ROUND_HALF_UP` rounding mode awareness — all comparisons use Decimal arithmetic to avoid issues like `float("10000.01") > 10000` being imprecise.
