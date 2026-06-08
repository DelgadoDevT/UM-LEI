"""
Entry point for the E2EE chat client.
"""
import asyncio
import signal
import sys
import os

from src.client.cli import ChatCLI


def main() -> None:
    """Start the chat client CLI."""
    cli = ChatCLI()
    
    try:
        asyncio.run(cli.run())
    except KeyboardInterrupt:
        pass
    except SystemExit:
        pass
    finally:
        # Force exit to avoid threading cleanup issues
        os._exit(0)


if __name__ == "__main__":
    main()