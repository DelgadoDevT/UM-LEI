"""
Main entry point for the Secure Chat Server.
"""
import asyncio
import sys
from rich.console import Console

from src.server.server import ChatServer

console = Console()


async def main():
    """Initializes and starts the chat server."""
    chat_server = ChatServer()
    await chat_server.start()


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        pass
    except SystemExit:
        pass
    finally:
        console.print()
        console.print("[bold green]✓[/bold green] Server stopped. Goodbye! 👋")
