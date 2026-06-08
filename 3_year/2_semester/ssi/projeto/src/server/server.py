"""Run the asynchronous secure chat server.

Initialize storage and certificate services, accept TCP clients, and delegate
each authenticated connection to a dedicated handler.
"""
import asyncio
import os
from pathlib import Path

from src.server.connection_handler import ConnectionHandler
from src.server.ca.certificate_authority import CertificateAuthority
from src.server.runtime_dashboard import ServerRuntimeDashboard
from src.storage.secure_db import SecureDB
from src.rich_logging import get_console, log_info, log_network, log_success
import config


#: Shared Rich console used for server lifecycle logging.
console = get_console()


class ChatServer:
    """Coordinate server startup and client-connection handling.

Manage long-lived runtime services such as persistent storage, certificate
issuance, active-connection registry, and asyncio synchronization primitives.

    :ivar db: Encrypted persistence backend for users, bundles, and messages.
    :vartype db: src.storage.secure_db.SecureDB
    :ivar ca: Certificate authority used for user certificate issuance.
    :vartype ca: src.server.ca.certificate_authority.CertificateAuthority
    :ivar connected_clients: Map of usernames to active connection handlers.
    :vartype connected_clients: dict[str, set[src.server.connection_handler.ConnectionHandler]]
    :ivar lock: Async lock protecting `connected_clients` mutations.
    :vartype lock: asyncio.Lock
    :ivar _server: Underlying asyncio TCP server instance.
    :vartype _server: asyncio.base_events.Server | None
    """

    def __init__(self):
        """Initialize server dependencies and mutable runtime state.

        :returns: None.
        :rtype: None
        """
        self.db = SecureDB(config.DB_PATH_SERVER, db_key=self._resolve_db_key())
        self.ca = CertificateAuthority()
        self.connected_clients: dict[str, set[ConnectionHandler]] = {}
        self.lock = asyncio.Lock()
        self._server = None
        self.runtime_dashboard = ServerRuntimeDashboard(
            console=console,
            host=config.SERVER_HOST,
            port=config.SERVER_PORT,
            connected_clients_getter=lambda: len(self.connected_clients),
        )
        log_success("server.init", "Server initialized")

    def _resolve_db_key(self) -> bytes:
        """Resolve server database key from config or persisted key file."""
        db_key_hex = (config.DB_KEY_HEX or "").strip()
        if db_key_hex:
            key = bytes.fromhex(db_key_hex)
            if len(key) != 32:
                raise ValueError("DB_KEY_HEX must decode to exactly 32 bytes")
            return key

        key_file = Path(config.DB_KEY_FILE)
        if key_file.exists():
            key = key_file.read_bytes()
            if len(key) != 32:
                raise ValueError(f"DB key file '{key_file}' must contain exactly 32 bytes")
            return key

        key_file.parent.mkdir(parents=True, exist_ok=True)
        key = os.urandom(32)
        key_file.write_bytes(key)
        try:
            os.chmod(key_file, 0o600)
        except OSError:
            pass
        return key

    def _print_banner(self):
        """Print server startup banner."""
        console.print()
        console.print("╔═══════════════════════════════════════════════════════════╗", style="bold cyan")
        console.print("║                                                           ║", style="bold cyan")
        console.print("║   [bold green]███████╗███████╗██████╗ ██╗   ██╗███████╗██████╗[/]        ║", style="bold cyan")
        console.print("║   [bold green]██╔════╝██╔════╝██╔══██╗██║   ██║██╔════╝██╔══██╗[/]       ║", style="bold cyan")
        console.print("║   [bold green]███████╗█████╗  ██████╔╝██║   ██║█████╗  ██████╔╝[/]       ║", style="bold cyan")
        console.print("║   [bold green]╚════██║██╔══╝  ██╔══██╗╚██╗ ╚██╗██╔══╝  ██╔══██╗[/]       ║", style="bold cyan")
        console.print("║   [bold green]███████║███████╗██║  ██║ ╚████╔╝ ███████╗██║  ██║[/]       ║", style="bold cyan")
        console.print("║   [bold green]╚══════╝╚══════╝╚═╝  ╚═╝  ╚═══╝  ╚══════╝╚═╝  ╚═╝[/]       ║", style="bold cyan")
        console.print("║              [bold magenta]SECURE CHAT SERVER[/]                           ║", style="bold cyan")
        console.print("║                                                           ║", style="bold cyan")
        console.print("║          [white]End-to-End Encrypted Messaging[/]                   ║", style="bold cyan")
        console.print("║          [dim]Press Ctrl+C to shutdown gracefully[/]              ║", style="bold cyan")
        console.print("╚═══════════════════════════════════════════════════════════╝", style="bold cyan")
        console.print()

    async def start(self):
        """Start listening for client connections and serve indefinitely.

        Print the startup banner, bind the configured host and port, and enter
        the asyncio server loop until cancellation or process termination.

        :returns: None.
        :rtype: None
        """
        self._print_banner()
        
        self._server = await asyncio.start_server(
            self.handle_client, 
            config.SERVER_HOST, 
            config.SERVER_PORT
        )
        
        log_network(
            "server.listen",
            "Listening for incoming connections",
            address=f"{config.SERVER_HOST}:{config.SERVER_PORT}",
        )
        log_info("server.idle", "Waiting for connections")
        console.print()

        await self.runtime_dashboard.start()
        try:
            async with self._server:
                await self._server.serve_forever()
        finally:
            await self.runtime_dashboard.stop()

    def increment_runtime_counter(self, counter_name: str, value: int = 1) -> None:
        """Forward runtime counter increments to live dashboard state."""
        self.runtime_dashboard.increment(counter_name, value)

    async def register_connected_client(self, username: str, handler: ConnectionHandler) -> int:
        """Register one active handler for a username and return active session count."""
        async with self.lock:
            handlers = self.connected_clients.setdefault(username, set())
            handlers.add(handler)
            return len(handlers)

    async def unregister_connected_client(self, username: str, handler: ConnectionHandler) -> int:
        """Unregister one handler and return remaining active session count."""
        async with self.lock:
            handlers = self.connected_clients.get(username)
            if not handlers:
                return 0

            handlers.discard(handler)
            if handlers:
                return len(handlers)

            self.connected_clients.pop(username, None)
            return 0

    async def get_connected_client_handlers(self, username: str) -> tuple[ConnectionHandler, ...]:
        """Return a snapshot of active handlers for a username."""
        async with self.lock:
            handlers = self.connected_clients.get(username)
            if not handlers:
                return tuple()
            return tuple(handlers)
            
    async def handle_client(self, reader, writer):
        """Handle one accepted client socket.

        Create a per-connection handler and run its lifecycle coroutine.

        :param reader: Async stream reader for client inbound data.
        :type reader: asyncio.StreamReader
        :param writer: Async stream writer for client outbound data.
        :type writer: asyncio.StreamWriter
        :returns: None.
        :rtype: None
        """
        addr = writer.get_extra_info('peername')
        self.increment_runtime_counter("connections_accepted")
        log_network("connection.open", "New connection accepted", peer=addr)
        handler = ConnectionHandler(reader, writer, addr, self)
        await asyncio.create_task(handler.run())
