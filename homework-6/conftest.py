import sys
from pathlib import Path

# Ensure agents/ and integrator.py are importable regardless of working directory
sys.path.insert(0, str(Path(__file__).parent))
