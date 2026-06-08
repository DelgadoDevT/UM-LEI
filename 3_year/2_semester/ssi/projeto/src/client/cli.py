"""
Command Line Interface for E2EE Chat Client.

Provides an interactive terminal UI using the Rich library.
"""
from __future__ import annotations

import asyncio
import base64
import json
import os
import readline
import sys
import termios
import tty
import uuid
import zlib
from datetime import datetime

from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from rich.console import Console
from rich.panel import Panel
from rich.rule import Rule
from rich.table import Table
from rich import box
from rich.prompt import Prompt

from src.client.client import ChatClient
from src.client.app_service import ClientAppService
from src.client.key_manager import KeyManager
from src.client.session_manager import SessionManager
from src.client.contact_manager import ContactManager
from src.client.group_manager import GroupManager
from src.storage.client_db import ClientDB
from src.protocol.messages import DeliverMessage, OfflineMessages, SendMessage, BaseMessage

import config


class ChatCLI:
    """Interactive CLI for the E2EE chat client.

    Commands::

        /help                       - Show help
        /connect                    - Connect to server
        /disconnect                 - Disconnect from server (keep direct mode)
        /register <user>            - Register new user
        /login <user>               - Login existing user
        /contacts                   - List contacts
        /add <user>                 - Add contact
        /msg <user> <text>          - Send message
        /history <user>             - Show message history
        /offline                    - Fetch offline messages
        /group create <name>        - Create a group
        /group list                 - List groups
        /group members <id>         - List group members
        /group invite <id> <user>   - Invite user to group
        /group remove <id> <user>   - Remove user and trigger group rekey
        /group msg <id> <text>      - Send group message
        /quit                       - Exit
    """

    _ROOT_COMMANDS = (
        "/help",
        "/connect",
        "/disconnect",
        "/register",
        "/login",
        "/contacts",
        "/add",
        "/msg",
        "/history",
        "/offline",
        "/group",
        "/quit",
        "/exit",
    )
    _GROUP_SUBCOMMANDS = (
        "create",
        "list",
        "invites",
        "accept",
        "reject",
        "members",
        "invite",
        "remove",
        "msg",
        "history",
    )
    
    def __init__(self, data_dir: str = "client_data"):
        """Initialize CLI.
        
        Args:
            data_dir: Directory for storing client data
        """
        self.console = Console()
        self.data_dir = data_dir
        
        # Components (initialized after login)
        self.client = ChatClient()
        self.key_manager: KeyManager | None = None
        self.session_manager: SessionManager | None = None
        self.contact_manager: ContactManager | None = None
        self.group_manager: GroupManager | None = None
        self.db: ClientDB | None = None
        
        # Pending group invites: {group_id: {inviter, group_name, sender_key_b64}}
        self._pending_invites: dict[str, dict] = {}
        self._sensitive_input_active = False
        self._readline_completion_enabled = False
        self._previous_completer = None
        self._previous_completer_delims: str | None = None
        
        self._running = False
    
    def _print_banner(self) -> None:
        """Print welcome banner."""
        self.console.print()
        self.console.print("╔═══════════════════════════════════════════════════════════╗", style="bold cyan")
        self.console.print("║                                                           ║", style="bold cyan")
        self.console.print("║   [bold green]███████╗███████╗ ██████╗██╗   ██╗██████╗ ███████╗[/]       ║", style="bold cyan")
        self.console.print("║   [bold green]██╔════╝██╔════╝██╔════╝██║   ██║██╔══██╗██╔════╝[/]       ║", style="bold cyan")
        self.console.print("║   [bold green]███████╗█████╗  ██║     ██║   ██║██████╔╝█████╗[/]         ║", style="bold cyan")
        self.console.print("║   [bold green]╚════██║██╔══╝  ██║     ██║   ██║██╔══██╗██╔══╝[/]         ║", style="bold cyan")
        self.console.print("║   [bold green]███████║███████╗╚██████╗╚██████╔╝██║  ██║███████╗[/]       ║", style="bold cyan")
        self.console.print("║   [bold green]╚══════╝╚══════╝ ╚═════╝ ╚═════╝ ╚═╝  ╚═╝╚══════╝[/]       ║", style="bold cyan")
        self.console.print("║                    [bold magenta]CHAT E2EE[/]                              ║", style="bold cyan")
        self.console.print("║                                                           ║", style="bold cyan")
        self.console.print("║          [white]End-to-End Encrypted Messaging[/]                   ║", style="bold cyan")
        self.console.print("║          [dim]Type /help for available commands[/]                ║", style="bold cyan")
        self.console.print("║          [dim]Press Ctrl+C to exit gracefully[/]                  ║", style="bold cyan")
        self.console.print("╚═══════════════════════════════════════════════════════════╝", style="bold cyan")
        self.console.print()
    
    def _print_help(self) -> None:
        """Print help message."""
        table = Table(
            title="[bold cyan]Command Reference[/bold cyan]",
            show_header=True,
            header_style="bold cyan",
            border_style="cyan",
            title_style="bold cyan",
            box=box.ROUNDED,
        )
        table.add_column("Category", style="bold magenta", no_wrap=True)
        table.add_column("Command", style="yellow", no_wrap=True)
        table.add_column("Description", style="white")
        table.add_row("Connection", "/connect", "Connect to the chat server")
        table.add_row("Connection", "/disconnect", "Disconnect from server (direct mode stays active)")
        table.add_row("Connection", "/register <username>", "Register a new account")
        table.add_row("Connection", "/login <username>", "Login to an existing account")
        table.add_row("Connection", "/quit or /exit", "Exit the application")
        table.add_row("Contacts", "/contacts", "List your contacts")
        table.add_row("Contacts", "/add <username>", "Add a new contact")
        table.add_row("Messaging", "/msg <user> <message>", "Send an encrypted message")
        table.add_row("Messaging", "/history <username>", "View message history")
        table.add_row("Messaging", "/offline", "Fetch pending offline messages")
        table.add_row("Groups", "/group create <name>", "Create a new group")
        table.add_row("Groups", "/group list", "List your groups")
        table.add_row("Groups", "/group invites", "List pending group invites")
        table.add_row("Groups", "/group accept <id>", "Accept a group invite")
        table.add_row("Groups", "/group reject <id>", "Reject a group invite")
        table.add_row("Groups", "/group members <id>", "List group members")
        table.add_row("Groups", "/group invite <id> <user>", "Invite a user to a group")
        table.add_row("Groups", "/group remove <id> <user>", "Remove a member and rotate keys")
        table.add_row("Groups", "/group msg <id> <message>", "Send a message to a group")
        table.add_row("Groups", "/group history <id>", "View group message history")

        self.console.print(
            Panel.fit(
                "[dim]Tip:[/dim] commands must start with [bold]/[/bold].",
                border_style="dim",
            )
        )
        self.console.print(table)
    
    def _print_status(self, level: str, message: str) -> None:
        """Print a formatted status line."""
        palette = {
            "success": ("green", "✓", "OK"),
            "error": ("red", "✗", "ERROR"),
            "info": ("blue", "ℹ", "INFO"),
            "warning": ("yellow", "⚠", "WARN"),
        }
        color, icon, label = palette[level]
        self.console.print(f"[bold {color}]{icon} {label}[/] {message}")

    def _print_state(self, context: str, message: str, level: str = "info") -> None:
        """Print contextual status feedback."""
        self._print_status(level, f"[bold]{context}[/bold] · {message}")

    def _print_success(self, message: str) -> None:
        """Print success message."""
        self._print_status("success", message)
    
    def _print_error(self, message: str) -> None:
        """Print error message."""
        self._print_status("error", message)
    
    def _print_info(self, message: str) -> None:
        """Print info message."""
        self._print_status("info", message)
    
    def _print_warning(self, message: str) -> None:
        """Print warning message."""
        self._print_status("warning", message)

    def _build_prompt(self) -> str:
        """Build shell prompt based on connection state."""
        if self.client.authenticated:
            return f"[{self.client.username}] > "
        if self.client.connected:
            return "[connected] > "
        return "> "

    def _preferred_p2p_port(self, username: str) -> int:
        """Choose a stable per-user P2P port when no fixed port is configured."""
        configured_port = int(config.CLIENT_P2P_PORT or 0)
        if configured_port > 0:
            return configured_port
        return 30000 + (zlib.crc32(username.encode("utf-8")) % 15000)

    def _app_service(self) -> ClientAppService:
        """Build a shared application service for orchestration flows."""
        return ClientAppService(
            client=self.client,
            session_manager=self.session_manager,
            contact_manager=self.contact_manager,
            preferred_p2p_port=self._preferred_p2p_port,
        )

    async def _ensure_user_direct_listener(self, username: str) -> None:
        """Ensure direct listener is up, preferring a stable per-user endpoint."""
        preferred_port = self._preferred_p2p_port(username)
        try:
            await self.client.ensure_direct_listener(
                host=config.CLIENT_P2P_HOST,
                port=preferred_port,
                force_rebind=True,
            )
        except OSError:
            # If the preferred port is unavailable, keep/restore any available listener.
            await self.client.ensure_direct_listener()

    def _is_interactive_terminal(self) -> bool:
        """Check if stdin/stdout are interactive terminals."""
        return sys.stdin.isatty() and sys.stdout.isatty()

    def _format_timestamp(self, timestamp: float | None = None) -> str:
        """Format timestamp with consistent readability."""
        try:
            dt = datetime.fromtimestamp(float(timestamp)) if timestamp is not None else datetime.now()
        except (TypeError, ValueError, OSError):
            dt = datetime.now()
        if dt.date() == datetime.now().date():
            return dt.strftime("%H:%M")
        return dt.strftime("%Y-%m-%d %H:%M")

    def _print_async_section(self, title: str, source: str = "live") -> None:
        """Render a visible section header for async output."""
        if source == "live":
            badge = "[bold bright_green]LIVE[/]"
        elif source == "offline":
            badge = "[bold bright_blue]OFFLINE[/]"
        else:
            badge = "[bold bright_cyan]EVENT[/]"
        self.console.print()
        self.console.print(Rule(f"{badge} {title}", style="bright_black"))

    def _command_output_title(self, line: str) -> str:
        """Build a short title for command output separators."""
        stripped = line.strip()
        if not stripped.startswith("/"):
            return "Output"

        parts = stripped.split()
        if not parts:
            return "Output"

        root = parts[0].lower()
        if root == "/group" and len(parts) > 1:
            return f"Output · {root} {parts[1].lower()}"
        return f"Output · {root}"

    def _print_command_section(self, line: str) -> None:
        """Render a lightweight divider before command output."""
        title = self._command_output_title(line)
        self.console.print()
        self.console.print(Rule(f"[dim]{title}[/dim]", style="bright_black"))

    def _build_completion_candidates(self, line_buffer: str, text: str) -> list[str]:
        """Build command completion candidates for readline."""
        stripped = line_buffer.lstrip()
        if not stripped.startswith("/"):
            return []

        words = stripped.split()
        starts_new_token = stripped.endswith(" ")
        prefix = text.lower()

        if words and words[0] == "/group":
            if len(words) == 1:
                if starts_new_token:
                    return [f"{sub} " for sub in self._GROUP_SUBCOMMANDS]
                return ["/group "] if "/group".startswith(prefix) else []
            if len(words) == 2 and not starts_new_token:
                return [f"{sub} " for sub in self._GROUP_SUBCOMMANDS if sub.startswith(prefix)]
            return []

        if len(words) <= 1 and not starts_new_token:
            return [f"{cmd} " for cmd in self._ROOT_COMMANDS if cmd.startswith(prefix)]
        return []

    def _command_completer(self, text: str, state: int) -> str | None:
        """Readline completer for slash commands."""
        try:
            line_buffer = readline.get_line_buffer()
        except Exception:
            line_buffer = text
        candidates = self._build_completion_candidates(line_buffer, text)
        return candidates[state] if state < len(candidates) else None

    def _configure_readline_completion(self) -> None:
        """Enable TAB completion for interactive terminals."""
        if not self._is_interactive_terminal():
            return
        try:
            self._previous_completer = readline.get_completer()
            self._previous_completer_delims = readline.get_completer_delims()
            readline.set_completer_delims(" \t\n")
            readline.parse_and_bind("tab: complete")
            readline.set_completer(self._command_completer)
            self._readline_completion_enabled = True
        except Exception:
            self._readline_completion_enabled = False

    def _restore_readline_completion(self) -> None:
        """Restore previous readline completion settings."""
        if not self._readline_completion_enabled:
            return
        try:
            readline.set_completer(self._previous_completer)
            if self._previous_completer_delims is not None:
                readline.set_completer_delims(self._previous_completer_delims)
        except Exception:
            pass
        finally:
            self._readline_completion_enabled = False
    
    def _reprint_prompt(self) -> None:
        """Reprint prompt after async message - helps user see they can type."""
        if not self._running:
            return
        if self._sensitive_input_active:
            return
        if not self._is_interactive_terminal():
            return

        prompt = self._build_prompt()
        try:
            line_buffer = readline.get_line_buffer()
        except Exception:
            line_buffer = ""

        # Clear current line and redraw prompt + current buffer.
        sys.stdout.write("\r\033[2K")
        sys.stdout.write(f"{prompt}{line_buffer}")
        sys.stdout.flush()
    
    def _print_message(
        self,
        sender: str,
        text: str,
        timestamp: float | None = None,
        direction: str = "received",
        source: str = "live",
        reprint_prompt: bool | None = None,
    ) -> None:
        """Print a chat message."""
        time_str = self._format_timestamp(timestamp)
        
        if direction == "sent":
            self.console.print(
                f"[dim]{time_str}[/dim] [bold cyan]You → {sender}[/bold cyan]: {text}"
            )
        else:
            source_prefix = ""
            if source == "live":
                source_prefix = "[bold bright_green]LIVE[/] "
            elif source == "offline":
                source_prefix = "[bold bright_blue]OFFLINE[/] "
            self.console.print(
                f"[dim]{time_str}[/dim] {source_prefix}[bold green]{sender}[/bold green]: {text}"
            )
            should_reprint = source in {"live", "offline"} if reprint_prompt is None else reprint_prompt
            if should_reprint:
                self._reprint_prompt()
    
    async def _cmd_connect(self) -> None:
        """Handle /connect command."""
        if self.client.connected:
            self._print_state("Connection", "Server connection is already active")
            return
        
        try:
            self._print_state(
                "Connection",
                f"Connecting to {config.SERVER_HOST}:{config.SERVER_PORT}",
            )
            await self.client.connect()
            await self.client.ensure_direct_listener()
            self._print_state("Connection", "Connected to server", level="success")
            p2p_host, p2p_port = self.client.get_advertised_endpoint()
            if p2p_host and p2p_port:
                self._print_state(
                    "Direct mode",
                    f"Listening at {p2p_host}:{p2p_port}",
                )
        except Exception as e:
            self._print_state("Connection", f"Failed to connect: {e}", level="error")

    async def _cmd_disconnect(self) -> None:
        """Handle /disconnect command."""
        if not self.client.connected:
            self._print_state("Connection", "Server is already disconnected")
            return

        try:
            await self.client.disconnect_server_only()
            self._print_state("Connection", "Disconnected from server", level="success")
            p2p_host, p2p_port = self.client.get_advertised_endpoint()
            if p2p_host and p2p_port:
                self._print_state(
                    "Direct fallback",
                    f"P2P listener remains active at {p2p_host}:{p2p_port}",
                )
            else:
                self._print_warning("Direct listener endpoint is currently unavailable")
            self._print_state(
                "Direct fallback",
                "Existing sessions may still deliver messages peer-to-peer",
            )
            self._print_state("Session", "If you reconnect, run /login again")
        except Exception as e:
            self._print_state("Connection", f"Failed to disconnect: {e}", level="error")
    
    def _read_password_sync(self, prompt: str) -> str:
        """Read password synchronously with echo disabled.
        
        Args:
            prompt: The prompt text to display
            
        Returns:
            The password entered by the user
        """
        self._sensitive_input_active = True
        self.console.print(f"[cyan]{prompt}[/cyan]: ", end="")

        try:
            # Save terminal settings and disable echo
            fd = sys.stdin.fileno()
            old_settings = termios.tcgetattr(fd)
            try:
                tty.setraw(fd, termios.TCSADRAIN)
                # Read password character by character
                password = []
                while True:
                    ch = sys.stdin.read(1)
                    if ch in ('\r', '\n'):  # Enter pressed
                        break
                    elif ch == '\x7f' or ch == '\x08':  # Backspace
                        if password:
                            password.pop()
                            # Move cursor back, overwrite with space, move back again
                            sys.stdout.write('\b \b')
                            sys.stdout.flush()
                    elif ch == '\x03':  # Ctrl+C
                        raise KeyboardInterrupt
                    elif ch >= ' ':  # Printable character
                        password.append(ch)
                        sys.stdout.write('*')
                        sys.stdout.flush()
            finally:
                # Restore terminal settings
                termios.tcsetattr(fd, termios.TCSADRAIN, old_settings)
        finally:
            self._sensitive_input_active = False

        self.console.print()  # New line after password
        return ''.join(password)
    
    async def _get_password(self, prompt: str = "Enter password") -> str:
        """Get password input asynchronously.
        
        Args:
            prompt: The prompt text to display
            
        Returns:
            The password entered by the user
        """
        loop = asyncio.get_event_loop()
        return await loop.run_in_executor(None, lambda: self._read_password_sync(prompt))

    def _db_salt_path(self, username: str) -> str:
        """Return the filesystem path for the user's DB salt file."""
        return os.path.join(self.data_dir, username, "db_salt.bin")

    def _load_or_create_db_salt(self, username: str) -> bytes:
        """Load an existing DB salt or create a new 16-byte salt."""
        salt_path = self._db_salt_path(username)
        if os.path.exists(salt_path):
            with open(salt_path, "rb") as salt_file:
                salt = salt_file.read()
            if len(salt) != 16:
                raise ValueError(f"Invalid DB salt at {salt_path}")
            return salt

        os.makedirs(os.path.dirname(salt_path), exist_ok=True)
        salt = os.urandom(16)
        with open(salt_path, "wb") as salt_file:
            salt_file.write(salt)
        return salt

    def _derive_client_db_key(self, username: str, password: str) -> bytes:
        """Derive a stable 32-byte local DB key from password and per-user salt."""
        salt = self._load_or_create_db_salt(username)
        kdf = PBKDF2HMAC(
            algorithm=hashes.SHA256(),
            length=32,
            salt=salt,
            iterations=400_000,
        )
        return kdf.derive(password.encode("utf-8"))
    
    async def _cmd_register(self, username: str) -> None:
        """Handle /register command."""
        if not self.client.connected:
            self._print_error("Not connected. Use /connect first")
            return
        
        if self.client.authenticated:
            self._print_error("Already logged in. Disconnect first")
            return
        
        # Get password (run in executor to not block event loop)
        password = await self._get_password("Enter password")
        password2 = await self._get_password("Confirm password")
        
        if password != password2:
            self._print_error("Passwords don't match")
            return
        
        if len(password) < 6:
            self._print_error("Password must be at least 6 characters")
            return
        
        # Initialize key manager
        self.key_manager = KeyManager(username, self.data_dir)
        
        try:
            # Register with server
            bundle = self.key_manager.get_registration_bundle()
            await self._ensure_user_direct_listener(username)
            p2p_host, p2p_port = self.client.get_advertised_endpoint()
            success = await self.client.register(
                username,
                password,
                bundle,
                p2p_host=p2p_host,
                p2p_port=p2p_port,
            )
            
            if success:
                await self._init_after_auth(username, password)
                self._print_state("Account", f"Registered as '{username}'", level="success")
            else:
                self._print_state(
                    "Account",
                    "Registration failed (username may already be taken)",
                    level="error",
                )
                if not self.client.connected:
                    self._print_info("Server closed auth session. Run /connect and retry /register.")
                self.key_manager = None
        except Exception as e:
            self._print_state("Account", f"Registration failed: {e}", level="error")
            if not self.client.connected:
                self._print_info("Run /connect before retrying /register.")
            self.key_manager = None
    
    async def _cmd_login(self, username: str) -> None:
        """Handle /login command."""
        if not self.client.connected:
            self._print_error("Not connected. Use /connect first")
            return
        
        if self.client.authenticated:
            self._print_error("Already logged in. Disconnect first")
            return
        
        # Get password (run in executor to not block event loop)
        password = await self._get_password("Enter password")
        
        # Check if we have keys for this user
        key_file = os.path.join(self.data_dir, username, "identity.json")
        if not os.path.exists(key_file):
            self._print_error(f"No local keys for {username}. Did you register on this device?")
            return
        
        # Initialize key manager
        self.key_manager = KeyManager(username, self.data_dir)
        
        try:
            await self._ensure_user_direct_listener(username)
            p2p_host, p2p_port = self.client.get_advertised_endpoint()
            success = await self.client.login(
                username,
                password,
                p2p_host=p2p_host,
                p2p_port=p2p_port,
            )
            
            if success:
                await self._init_after_auth(username, password)
                self._print_state("Session", f"Logged in as '{username}'", level="success")
                
                # Server automatically sends offline messages after auth
                # They will arrive via _handle_incoming_message
                self._print_state(
                    "Offline inbox",
                    "Pending messages will be delivered automatically",
                )
            else:
                self._print_state(
                    "Session",
                    "Login failed (wrong password?)",
                    level="error",
                )
                if not self.client.connected:
                    self._print_info("Server closed auth session. Run /connect and retry /login.")
                self.key_manager = None
        except Exception as e:
            self._print_state("Session", f"Login failed: {e}", level="error")
            if not self.client.connected:
                self._print_info("Run /connect before retrying /login.")
            self.key_manager = None
    
    async def _init_after_auth(self, username: str, password: str) -> None:
        """Initialize components after successful authentication."""
        # Initialize database
        db_path = os.path.join(self.data_dir, username, "client.db")
        os.makedirs(os.path.dirname(db_path), exist_ok=True)
        db_key = self._derive_client_db_key(username, password)
        self.db = ClientDB(db_path, db_key=db_key)
        
        # Initialize managers
        self.session_manager = SessionManager(self.key_manager, self.db)
        self.contact_manager = ContactManager(self.db, self.client.ca_cert_pem)
        self.group_manager = GroupManager(self.db, username)
        
        # Set up message handler
        self.client.set_message_handler(self._handle_incoming_message)
        
        # Start receiving messages in background (managed by client)
        await self.client.start_receiving()
    
    async def _handle_incoming_message(self, msg: BaseMessage) -> bool | None:
        """Handle incoming messages from server."""
        if isinstance(msg, OfflineMessages):
            # Handle offline messages notification from server
            if msg.messages:
                self._print_state(
                    "Offline inbox",
                    f"Received {len(msg.messages)} message(s)",
                )
                for msg_json in msg.messages:
                    try:
                        deliver_msg = DeliverMessage(payload_json=msg_json)
                        await self._process_delivered_message(deliver_msg, source="offline")
                    except Exception as e:
                        self._print_error(f"Failed to process offline message: {e}")
            # Don't print "No offline messages" - causes timing issues with prompt
            return True
        elif isinstance(msg, DeliverMessage):
            return await self._process_delivered_message(msg, source="live")
        return None
    
    async def _process_delivered_message(self, msg: DeliverMessage, source: str = "live") -> bool:
        """Process a delivered message (from real-time or offline)."""
        try:
            # Parse the inner message
            inner = json.loads(msg.payload_json)
            if not isinstance(inner, dict):
                return False
            sender = inner.get("sender")
            recipient = inner.get("recipient")
            msg_id = inner.get("msg_id", "")
            
            if not sender or not recipient or recipient != self.client.username or not msg_id:
                return False

            if self.db.has_seen_message(msg_id):
                return True
            
            # Check if this is an initial message (has X3DH params)
            ephemeral_pub_b64 = inner.get("ephemeral_pub_b64", "")
            opk_id = inner.get("opk_id")

            contact = self.contact_manager.get_contact(sender)
            if not contact:
                bundle = await self.client.fetch_bundle(sender)
                if not bundle:
                    return False
                self.contact_manager.add_contact(sender, bundle, verify=True)
                contact = self.contact_manager.get_contact(sender)
                if not contact:
                    return False
            
            if ephemeral_pub_b64 and not self.session_manager.has_session(sender):
                # This is a first message - need to establish session
                self.session_manager.receive_initial_message(
                    peer=sender,
                    peer_ik_ed_pub=base64.b64decode(contact["ik_ed_pub"]),
                    peer_ik_dh_pub=base64.b64decode(contact["ik_dh_pub"]),
                    ephemeral_pub=base64.b64decode(ephemeral_pub_b64),
                    opk_id=opk_id,
                )
            
            # Decrypt the message
            ciphertext_b64 = inner.get("ciphertext_b64", "")
            if ciphertext_b64:
                encrypted = json.loads(base64.b64decode(ciphertext_b64).decode())
                if not isinstance(encrypted, dict):
                    return False
                plaintext = self.session_manager.decrypt_message(sender, encrypted)
                
                # Check message type
                if self._is_group_invite(plaintext):
                    await self._handle_group_invite(sender, plaintext, source=source)
                elif self._is_group_rekey_announce(plaintext):
                    await self._handle_group_rekey_announce(sender, plaintext, source=source)
                elif self._is_group_sender_key_update(plaintext):
                    self._handle_group_sender_key_update(sender, plaintext, source=source)
                elif self._is_group_sender_key(plaintext):
                    self._handle_group_sender_key(sender, plaintext, source=source)
                elif self._is_group_msg(plaintext):
                    self._handle_group_msg(sender, plaintext, source=source)
                else:
                    # Regular message - save and display
                    self.db.save_message(sender, "received", plaintext, inner.get("timestamp"))
                    self._print_async_section(f"Direct message from {sender}", source=source)
                    self._print_message(
                        sender,
                        plaintext,
                        inner.get("timestamp"),
                        "received",
                        source=source,
                    )
                self.db.mark_message_seen(msg_id)
                return True
            return False
        except Exception as e:
            self._print_error(f"Failed to process message: {e}")
            return False
    
    def _is_group_invite(self, plaintext: str) -> bool:
        """Check if a message is a group invite."""
        try:
            data = json.loads(plaintext)
            if not isinstance(data, dict):
                return False
            return data.get("type") == "group_invite"
        except (json.JSONDecodeError, TypeError, AttributeError):
            return False
    
    def _is_group_sender_key(self, plaintext: str) -> bool:
        """Check if a message is a group sender key response."""
        try:
            data = json.loads(plaintext)
            if not isinstance(data, dict):
                return False
            return data.get("type") == "group_sender_key"
        except (json.JSONDecodeError, TypeError, AttributeError):
            return False

    def _is_group_rekey_announce(self, plaintext: str) -> bool:
        """Check if a message is a group rekey announcement."""
        try:
            data = json.loads(plaintext)
            if not isinstance(data, dict):
                return False
            return data.get("type") == "group_rekey_announce"
        except (json.JSONDecodeError, TypeError, AttributeError):
            return False

    def _is_group_sender_key_update(self, plaintext: str) -> bool:
        """Check if a message is a group sender key update."""
        try:
            data = json.loads(plaintext)
            if not isinstance(data, dict):
                return False
            return data.get("type") == "group_sender_key_update"
        except (json.JSONDecodeError, TypeError, AttributeError):
            return False
    
    def _is_group_msg(self, plaintext: str) -> bool:
        """Check if a message is a group message."""
        try:
            data = json.loads(plaintext)
            if not isinstance(data, dict):
                return False
            return data.get("type") == "group_msg"
        except (json.JSONDecodeError, TypeError, AttributeError):
            return False
    
    async def _handle_group_invite(self, sender: str, plaintext: str, source: str = "live") -> None:
        """Handle a group invite message - store as pending."""
        try:
            data = json.loads(plaintext)
            group_id = data["group_id"]
            group_name = data["group_name"]
            sender_key_b64 = data["sender_key_b64"]
            
            # Store as pending invite
            self._pending_invites[group_id] = {
                "inviter": sender,
                "group_name": group_name,
                "sender_key_b64": sender_key_b64,
            }

            self._print_async_section(f"Group invitation from {sender}", source=source)
            
            self._print_state(
                "Group invite",
                f"{sender} invited you to '{group_name}' ({group_id})",
            )
            self._print_state(
                "Group invite",
                f"Use /group accept {group_id} to join or /group reject {group_id} to decline",
            )
            self._reprint_prompt()
            
        except Exception as e:
            self._print_error(f"Failed to process group invite: {e}")
    
    async def _accept_group_invite(self, group_id: str) -> None:
        """Accept a pending group invite."""
        if group_id not in self._pending_invites:
            self._print_error(f"No pending invite for group {group_id}")
            return
        
        invite = self._pending_invites[group_id]
        sender = invite["inviter"]
        group_name = invite["group_name"]
        sender_key_b64 = invite["sender_key_b64"]
        
        try:
            self._print_state("Group invite", f"Joining '{group_name}' and exchanging keys...")
            # Join the group
            my_sender_key = self.group_manager.join_group(group_id, group_name, sender)
            
            # Store inviter's sender key
            sender_key = base64.b64decode(sender_key_b64)
            self.group_manager.receive_sender_key(group_id, sender, sender_key)
            
            # Send our sender key back to the inviter
            response = json.dumps({
                "type": "group_sender_key",
                "group_id": group_id,
                "sender_key_b64": base64.b64encode(my_sender_key).decode(),
            })
            key_exchange_sent = await self._send_encrypted_to(sender, response)
            
            # Remove from pending
            del self._pending_invites[group_id]
            
            self._print_state("Group", f"Joined '{group_name}'", level="success")
            if key_exchange_sent:
                self._print_state("Group key exchange", f"Shared your sender key with {sender}")
            else:
                self._print_warning(
                    "Joined locally, but could not send your sender key to inviter. "
                    "Reconnect and ask for a new invite if members cannot read your messages."
                )
            
        except Exception as e:
            self._print_error(f"Failed to accept invite: {e}")
    
    def _reject_group_invite(self, group_id: str) -> None:
        """Reject a pending group invite."""
        if group_id not in self._pending_invites:
            self._print_error(f"No pending invite for group {group_id}")
            return
        
        group_name = self._pending_invites[group_id]["group_name"]
        del self._pending_invites[group_id]
        self._print_state("Group invite", f"Rejected invite for '{group_name}'")
    
    def _handle_group_msg(self, sender: str, plaintext: str, source: str = "live") -> None:
        """Handle a group message."""
        try:
            data = json.loads(plaintext)
            group_id = data["group_id"]
            group_name = data["group_name"]
            encrypted = data["encrypted"]
            
            # Decrypt the group message
            decrypted = self.group_manager.decrypt_message(encrypted)
            
            # Save to history
            self.db.save_group_message(group_id, sender, decrypted)
            
            # Display with clear group indication
            time_str = self._format_timestamp()
            self._print_async_section(f"Group message · {group_name}", source=source)
            self.console.print(
                f"[dim]{time_str}[/dim] [bold green]{sender}[/bold green] "
                f"[dim]in[/dim] [bold magenta]{group_name}[/bold magenta]: {decrypted}"
            )
            self._reprint_prompt()
            
        except Exception as e:
            self._print_error(f"Failed to process group message: {e}")
    
    def _handle_group_sender_key(self, sender: str, plaintext: str, source: str = "live") -> None:
        """Handle a group sender key response."""
        try:
            data = json.loads(plaintext)
            group_id = data["group_id"]
            sender_key_b64 = data["sender_key_b64"]
            
            # Store the sender's key
            sender_key = base64.b64decode(sender_key_b64)
            self.group_manager.receive_sender_key(group_id, sender, sender_key)
            
            group = self.db.get_group(group_id)
            group_name = group["name"] if group else group_id
            self._print_async_section(f"Group key update · {group_name}", source=source)
            self._print_state(
                "Group key exchange",
                f"Received sender key from {sender} for '{group_name}'",
            )
            self._reprint_prompt()
            
        except Exception as e:
            self._print_error(f"Failed to process sender key: {e}")

    async def _handle_group_rekey_announce(self, sender: str, plaintext: str, source: str = "live") -> None:
        """Handle a group membership removal and rekey announcement."""
        try:
            data = json.loads(plaintext)
            group_id = data["group_id"]
            group_name = data.get("group_name", group_id)
            removed_username = data["removed_username"]
            remaining_members = data.get("remaining_members", [])
            sender_key_b64 = data["sender_key_b64"]
            rotation_epoch = data.get("rotation_epoch", "")

            if removed_username == self.client.username:
                self.group_manager.remove_member(group_id, removed_username)
                self._print_async_section(f"Group membership update · {group_name}", source=source)
                self._print_warning(f"You were removed from '{group_name}'")
                self._reprint_prompt()
                return

            if not self.db.get_group(group_id):
                self.db.create_group(group_id, group_name, sender)

            self.group_manager.remove_member(group_id, removed_username)
            self.group_manager.receive_sender_key(group_id, sender, base64.b64decode(sender_key_b64))

            my_rotated_key = self.group_manager.rotate_own_sender_key(group_id)
            sender_key_update = json.dumps(
                {
                    "type": "group_sender_key_update",
                    "group_id": group_id,
                    "group_name": group_name,
                    "sender_key_b64": base64.b64encode(my_rotated_key).decode(),
                    "removed_username": removed_username,
                    "rotation_epoch": rotation_epoch,
                }
            )

            failed_members: list[str] = []
            targets = [member for member in remaining_members if member != self.client.username]
            for member in targets:
                ok = await self._send_encrypted_to(member, sender_key_update)
                if not ok:
                    failed_members.append(member)

            self._print_async_section(f"Group rekey · {group_name}", source=source)
            self._print_state(
                "Group security",
                f"Processed member removal ({removed_username}) and rotated your sender key",
                level="success",
            )
            if failed_members:
                self._print_warning(
                    f"Could not share rotated key with: {', '.join(failed_members)}"
                )
            self._reprint_prompt()
        except Exception as e:
            self._print_error(f"Failed to process group rekey announcement: {e}")

    def _handle_group_sender_key_update(self, sender: str, plaintext: str, source: str = "live") -> None:
        """Handle sender key updates triggered by group rekey."""
        try:
            data = json.loads(plaintext)
            group_id = data["group_id"]
            group_name = data.get("group_name")
            removed_username = data.get("removed_username", "")
            sender_key_b64 = data["sender_key_b64"]

            if removed_username:
                self.group_manager.remove_member(group_id, removed_username)

            sender_key = base64.b64decode(sender_key_b64)
            self.group_manager.receive_sender_key(group_id, sender, sender_key)

            if not group_name:
                group = self.db.get_group(group_id)
                group_name = group["name"] if group else group_id

            self._print_async_section(f"Group key update · {group_name}", source=source)
            self._print_state(
                "Group security",
                f"Received rotated sender key from {sender}",
            )
            self._reprint_prompt()
        except Exception as e:
            self._print_error(f"Failed to process group sender key update: {e}")

    def _build_cached_bundle_for_session(self, username: str) -> tuple[dict | None, str]:
        """Build a peer bundle from locally cached contact data."""
        return self._app_service().build_cached_bundle_for_session(username)

    def _print_direct_delivery_recovery_hint(self, error: Exception) -> None:
        """Print actionable guidance for direct-delivery failures while offline."""
        error_text = str(error)
        if "Server disconnected and direct delivery to" not in error_text:
            return
        self._print_info(
            "Direct delivery requires the recipient to be online now with an active direct listener."
        )
        self._print_info(
            "If the recipient is offline, reconnect + /login and resend so the server can queue it."
        )
    
    async def _send_encrypted_to(self, recipient: str, plaintext: str) -> bool:
        """Send an encrypted message to a recipient (helper for internal messages)."""
        try:
            payload = await self._app_service().prepare_outbound_payload(recipient, plaintext)

            route, refreshed_bundle = await self.client.send_chat_message_routed(
                recipient=recipient,
                sender=self.client.username,
                ciphertext_b64=payload.ciphertext_b64,
                ephemeral_pub_b64=payload.context.ephemeral_pub_b64,
                opk_id=payload.context.opk_id,
                msg_id=payload.msg_id,
                peer_host=payload.context.peer_host,
                peer_port=payload.context.peer_port,
            )
            if refreshed_bundle:
                self.contact_manager.add_contact(recipient, refreshed_bundle, verify=True)
            if route == "server":
                self._print_state(
                    "Delivery",
                    f"Delivered via server relay ({recipient})",
                )
            return True
        except Exception:
            return False
    
    async def _cmd_contacts(self) -> None:
        """Handle /contacts command."""
        if not self.client.authenticated:
            self._print_error("Not logged in")
            return
        
        contacts = self.contact_manager.list_contacts()
        
        if not contacts:
            self._print_info("No contacts yet. Use /add <username> to add one")
            return
        
        table = Table(title="Contacts")
        table.add_column("Username", style="cyan")
        table.add_column("Session", style="green")
        
        for username in contacts:
            has_session = "✓" if self.session_manager.has_session(username) else "-"
            table.add_row(username, has_session)
        
        self.console.print(table)
    
    async def _cmd_add(self, username: str) -> None:
        """Handle /add command."""
        if not self.client.authenticated:
            self._print_error("Not logged in")
            return
        
        if username == self.client.username:
            self._print_error("Cannot add yourself")
            return
        
        already_contact = self.contact_manager.has_contact(username)
        if already_contact:
            self._print_info(f"{username} is already a contact; refreshing details")
        
        try:
            bundle = await self._app_service().fetch_and_cache_contact(username, verify=True)
            
            if not bundle:
                self._print_error(f"User '{username}' not found")
                return

            if already_contact:
                self._print_success(f"Updated {username} contact details")
            else:
                self._print_success(f"Added {username} to contacts")
            
        except Exception as e:
            self._print_error(f"Failed to add contact: {e}")
    
    async def _cmd_msg(self, username: str, text: str) -> None:
        """Handle /msg command."""
        if not self.client.authenticated:
            self._print_error("Not logged in")
            return
        
        if not text.strip():
            self._print_error("Message cannot be empty")
            return
        
        try:
            payload = await self._app_service().prepare_outbound_payload(username, text)
            
            # Send message
            route, refreshed_bundle = await self.client.send_chat_message_routed(
                recipient=username,
                sender=self.client.username,
                ciphertext_b64=payload.ciphertext_b64,
                ephemeral_pub_b64=payload.context.ephemeral_pub_b64,
                opk_id=payload.context.opk_id,
                msg_id=payload.msg_id,
                peer_host=payload.context.peer_host,
                peer_port=payload.context.peer_port,
            )
            if refreshed_bundle:
                self.contact_manager.add_contact(username, refreshed_bundle, verify=True)
            if route == "direct":
                self._print_state("Delivery", "Delivered via direct peer connection")
            else:
                self._print_state("Delivery", "Delivered via server relay")
            
            # Save to history
            self.db.save_message(username, "sent", text)
            self._print_message(username, text, direction="sent")
            
        except Exception as e:
            self._print_error(f"Failed to send message: {e}")
            self._print_direct_delivery_recovery_hint(e)
    
    async def _cmd_history(self, username: str) -> None:
        """Handle /history command."""
        if not self.client.authenticated:
            self._print_error("Not logged in")
            return
        
        messages = self.db.get_messages(username, limit=20)
        
        if not messages:
            self._print_info(f"No messages with {username}")
            return
        
        self.console.print(Panel.fit(f"[bold]Chat with {username}[/bold]", border_style="bright_cyan"))
        self.console.print(Rule("[dim]Recent messages[/dim]", style="bright_black"))
        
        for msg in messages:
            self._print_message(
                username,
                msg["plaintext"],
                msg["timestamp"],
                msg["direction"],
                source="history",
                reprint_prompt=False,
            )
    
    async def _cmd_offline(self) -> None:
        """Handle /offline command."""
        if not self.client.authenticated:
            self._print_error("Not logged in")
            return
        
        try:
            self._print_state("Offline inbox", "Checking server for pending messages...")
            messages = await self.client.fetch_offline_messages()
            
            if not messages:
                self._print_state("Offline inbox", "No pending messages")
                return
            
            self._print_state("Offline inbox", f"Received {len(messages)} message(s)")
            
            for index, msg_json in enumerate(messages, start=1):
                try:
                    # Process like incoming message; parser/validation happens downstream
                    deliver_msg = DeliverMessage(payload_json=msg_json)
                    ok = await self._process_delivered_message(deliver_msg, source="offline")
                    if not ok:
                        self._print_warning(
                            f"Offline message {index}/{len(messages)} could not be decrypted/validated"
                        )
                except Exception as e:
                    self._print_error(f"Failed to process offline message: {e}")
        except asyncio.TimeoutError:
            self._print_state("Offline inbox", "No pending messages (timeout)")
            self._print_info("Tip: if this persists, verify server availability with /connect")
        except ConnectionError as e:
            self._print_error(f"Failed to fetch offline messages: {e}")
            if "Not connected" in str(e):
                self._print_info("Tip: run /connect and /login before /offline")
        except Exception as e:
            if str(e).strip():
                self._print_error(f"Failed to fetch offline messages: {e}")
                self._print_info("Tip: retry /offline after reconnecting if needed")
            else:
                self._print_state("Offline inbox", "No pending messages")
    
    async def _cmd_quit(self) -> None:
        """Handle /quit command."""
        self._running = False
        # Disconnect will be done in run()'s finally block
    
    async def _cmd_group(self, args: list[str]) -> None:
        """Handle /group commands."""
        if not self.client.authenticated:
            self._print_error("Not logged in")
            return
        
        if not args:
            self._print_error("Usage: /group <create|list|members|invite|remove|accept|reject|invites|msg|history> ...")
            return
        
        # Combine all args and re-parse since maxsplit=2 may have combined them incorrectly
        # e.g. args might be ["invite", "id user"] or ["msg", "id Hello world"]
        full_args = ' '.join(args)
        
        # For msg command, we need to preserve the message (which can have spaces)
        # Format: "msg <group_id> <message...>"
        if full_args.startswith("msg "):
            parts = full_args.split(maxsplit=2)  # ["msg", "group_id", "message..."]
            if len(parts) < 3:
                self._print_error("Usage: /group msg <group_id> <message>")
                return
            await self._cmd_group_msg(parts[1], parts[2])
            return
        
        # For other commands, split normally
        all_parts = full_args.split()
        subcmd = all_parts[0].lower()
        subargs = all_parts[1:] if len(all_parts) > 1 else []
        
        if subcmd == "create":
            if not subargs:
                self._print_error("Usage: /group create <name>")
                return
            await self._cmd_group_create(subargs[0])
            
        elif subcmd == "list":
            await self._cmd_group_list()
            
        elif subcmd == "members":
            if not subargs:
                self._print_error("Usage: /group members <group_id>")
                return
            await self._cmd_group_members(subargs[0])
            
        elif subcmd == "invite":
            if len(subargs) < 2:
                self._print_error("Usage: /group invite <group_id> <username>")
                return
            await self._cmd_group_invite(subargs[0], subargs[1])

        elif subcmd == "remove":
            if len(subargs) < 2:
                self._print_error("Usage: /group remove <group_id> <username>")
                return
            await self._cmd_group_remove(subargs[0], subargs[1])
        
        elif subcmd == "accept":
            if not subargs:
                self._print_error("Usage: /group accept <group_id>")
                return
            await self._accept_group_invite(subargs[0])
        
        elif subcmd == "reject":
            if not subargs:
                self._print_error("Usage: /group reject <group_id>")
                return
            self._reject_group_invite(subargs[0])
        
        elif subcmd == "invites":
            self._cmd_group_invites()
        
        elif subcmd == "history":
            if not subargs:
                self._print_error("Usage: /group history <group_id>")
                return
            await self._cmd_group_history(subargs[0])
        else:
            self._print_error(f"Unknown group command: {subcmd}")
    
    async def _cmd_group_create(self, name: str) -> None:
        """Create a new group."""
        try:
            group_id = self.group_manager.create_group(name)
            self._print_state("Group", f"Created '{name}' with ID {group_id}", level="success")
        except Exception as e:
            self._print_error(f"Failed to create group: {e}")
    
    async def _cmd_group_list(self) -> None:
        """List all groups."""
        groups = self.group_manager.list_groups()
        
        if not groups:
            self._print_state("Group", "No groups yet. Use /group create <name>")
            return
        
        table = Table(title="Your Groups")
        table.add_column("ID", style="cyan")
        table.add_column("Name", style="white")
        table.add_column("Members", style="green")
        
        for g in groups:
            members = self.group_manager.get_group_members(g["group_id"])
            table.add_row(g["group_id"], g["name"], str(len(members)))
        
        self.console.print(table)
    
    def _cmd_group_invites(self) -> None:
        """List pending group invites."""
        if not self._pending_invites:
            self._print_state("Group invites", "No pending invites")
            return
        
        table = Table(title="Pending Group Invites")
        table.add_column("ID", style="cyan")
        table.add_column("Group Name", style="white")
        table.add_column("From", style="green")
        
        for group_id, invite in self._pending_invites.items():
            table.add_row(group_id, invite["group_name"], invite["inviter"])
        
        self.console.print(table)
        self._print_state("Group invites", "Use /group accept <id> or /group reject <id>")
    
    async def _cmd_group_members(self, group_id: str) -> None:
        """List group members."""
        group = self.db.get_group(group_id)
        if not group:
            self._print_error(f"Group {group_id} not found")
            return
        
        members = self.group_manager.get_group_members(group_id)
        
        table = Table(title=f"Members of '{group['name']}'")
        table.add_column("Username", style="cyan")
        
        for m in members:
            table.add_row(m)
        
        self.console.print(table)
    
    async def _cmd_group_invite(self, group_id: str, username: str) -> None:
        """Invite a user to a group."""
        group = self.db.get_group(group_id)
        if not group:
            self._print_error(f"Group {group_id} not found")
            return
        if username == self.client.username:
            self._print_error("You are already in the group")
            return
        
        try:
            self._print_state("Group invite", f"Preparing invitation for {username}...")
            # Get our sender key for this group
            my_sender_key = self.group_manager.get_my_sender_key(group_id)
            if not my_sender_key:
                self._print_error("You are not a member of this group")
                return
            
            # Create invite message
            invite_msg = json.dumps({
                "type": "group_invite",
                "group_id": group_id,
                "group_name": group["name"],
                "sender_key_b64": base64.b64encode(my_sender_key).decode(),
            })
            
            # Send invite as E2EE message
            success = await self._send_encrypted_to(username, invite_msg)
            
            if success:
                # Add member locally (without sender key yet - will receive it in response)
                self.group_manager.add_member(group_id, username)
                self._print_state(
                    "Group invite",
                    f"Invitation sent to {username} for '{group['name']}'",
                    level="success",
                )
            else:
                self._print_error(f"Failed to send invitation - could not establish session with {username}")
                if not self.client.connected:
                    self._print_info("Server is disconnected. Reconnect and /login, then retry.")
        except Exception as e:
            self._print_error(f"Failed to invite: {e}")

    async def _cmd_group_remove(self, group_id: str, username: str) -> None:
        """Remove a member from a group and trigger sender-key rotation."""
        group = self.db.get_group(group_id)
        if not group:
            self._print_error(f"Group {group_id} not found")
            return
        if group["creator"] != self.client.username:
            self._print_error("Only the group creator can remove members")
            return
        if username == self.client.username:
            self._print_error("Creator self-removal is not supported")
            return

        members = self.group_manager.get_group_members(group_id)
        if username not in members:
            self._print_error(f"User '{username}' is not a member of this group")
            return

        try:
            self.group_manager.remove_member(group_id, username)
            rotated_sender_key = self.group_manager.rotate_own_sender_key(group_id)
            remaining_members = [member for member in members if member != username]
            rotation_epoch = uuid.uuid4().hex

            announce_payload = json.dumps(
                {
                    "type": "group_rekey_announce",
                    "group_id": group_id,
                    "group_name": group["name"],
                    "removed_username": username,
                    "remaining_members": remaining_members,
                    "rotation_epoch": rotation_epoch,
                    "sender_key_b64": base64.b64encode(rotated_sender_key).decode(),
                }
            )

            failed_members: list[str] = []
            targets = [member for member in remaining_members if member != self.client.username]
            for member in targets:
                ok = await self._send_encrypted_to(member, announce_payload)
                if not ok:
                    failed_members.append(member)

            self._print_state(
                "Group security",
                f"Removed {username} and rotated your sender key",
                level="success",
            )
            if failed_members:
                self._print_warning(
                    f"Could not deliver rekey announcement to: {', '.join(failed_members)}"
                )
        except Exception as e:
            self._print_error(f"Failed to remove member: {e}")
    
    async def _cmd_group_msg(self, group_id: str, text: str) -> None:
        """Send a message to a group."""
        group = self.db.get_group(group_id)
        if not group:
            self._print_error(f"Group {group_id} not found")
            return
        
        try:
            encrypted = self.group_manager.encrypt_message(group_id, text)
            
            # Save to history
            self.db.save_group_message(group_id, self.client.username, text)
            
            self.console.print(
                f"[dim]{datetime.now().strftime('%H:%M')}[/dim] "
                f"[cyan]You → {group['name']}[/cyan]: {text}"
            )
            
            # Send to all group members (except ourselves)
            members = self.group_manager.get_group_members(group_id)
            sent_count = 0
            targets = [m for m in members if m != self.client.username]
            failed_members: list[str] = []
            for member in members:
                if member != self.client.username:
                    # Create group message payload
                    group_msg = json.dumps({
                        "type": "group_msg",
                        "group_id": group_id,
                        "group_name": group["name"],
                        "encrypted": encrypted,
                    })
                    success = await self._send_encrypted_to(member, group_msg)
                    if success:
                        sent_count += 1
                    else:
                        failed_members.append(member)
            
            if sent_count > 0:
                self._print_state("Group delivery", f"Delivered to {sent_count}/{len(targets)} member(s)")
            elif not targets:
                self._print_info("No other members to deliver to yet")
            else:
                self._print_warning("Could not deliver to any members")
            if failed_members:
                self._print_warning(
                    f"Delivery pending for: {', '.join(failed_members)}. They may need to reconnect."
                )
            
        except Exception as e:
            self._print_error(f"Failed to send: {e}")
    
    async def _cmd_group_history(self, group_id: str) -> None:
        """View group message history."""
        group = self.db.get_group(group_id)
        if not group:
            self._print_error(f"Group {group_id} not found")
            return
        
        messages = self.db.get_group_messages(group_id, limit=20)
        
        if not messages:
            self._print_info(f"No messages in group '{group['name']}'")
            return
        
        self.console.print(
            Panel.fit(f"[bold]Group: {group['name']}[/bold]", border_style="bright_magenta")
        )
        self.console.print(Rule("[dim]Recent group messages[/dim]", style="bright_black"))
        
        for msg in messages:
            time_str = self._format_timestamp(msg.get("timestamp"))
            
            sender = msg["sender"]
            if sender == self.client.username:
                self.console.print(
                    f"[dim]{time_str}[/dim] [bold cyan]You[/bold cyan]: {msg['plaintext']}"
                )
            else:
                self.console.print(
                    f"[dim]{time_str}[/dim] [bold green]{sender}[/bold green]: {msg['plaintext']}"
                )
    
    async def _process_command(self, line: str) -> None:
        """Process a command line."""
        line = line.strip()
        
        if not line:
            return
        
        if not line.startswith("/"):
            self._print_error("Commands start with /. Use /help for list")
            return
        
        parts = line[1:].split(maxsplit=2)
        cmd = parts[0].lower() if parts else ""
        args = parts[1:] if len(parts) > 1 else []
        
        if cmd == "help":
            self._print_help()
        elif cmd == "connect":
            await self._cmd_connect()
        elif cmd == "disconnect":
            await self._cmd_disconnect()
        elif cmd == "register":
            if not args:
                self._print_error("Usage: /register <username>")
            else:
                await self._cmd_register(args[0])
        elif cmd == "login":
            if not args:
                self._print_error("Usage: /login <username>")
            else:
                await self._cmd_login(args[0])
        elif cmd == "contacts":
            await self._cmd_contacts()
        elif cmd == "add":
            if not args:
                self._print_error("Usage: /add <username>")
            else:
                await self._cmd_add(args[0])
        elif cmd == "msg":
            if len(args) < 2:
                self._print_error("Usage: /msg <username> <message>")
            else:
                await self._cmd_msg(args[0], args[1])
        elif cmd == "history":
            if not args:
                self._print_error("Usage: /history <username>")
            else:
                await self._cmd_history(args[0])
        elif cmd == "offline":
            await self._cmd_offline()
        elif cmd == "group":
            await self._cmd_group(args)
        elif cmd == "quit" or cmd == "exit":
            await self._cmd_quit()
        else:
            self._print_error(f"Unknown command: /{cmd}. Use /help")
    
    async def run(self) -> None:
        """Run the interactive CLI loop."""
        self._print_banner()
        self._running = True
        self._configure_readline_completion()
        
        try:
            while self._running:
                try:
                    prompt = self._build_prompt()
                    
                    # Get input (readline handles arrow keys automatically)
                    loop = asyncio.get_event_loop()
                    line = await loop.run_in_executor(None, lambda: input(prompt))
                    if line.strip():
                        self._print_command_section(line)
                    
                    await self._process_command(line)
                    
                except EOFError:
                    break
                except KeyboardInterrupt:
                    break
                    
        except Exception as e:
            self._print_error(f"Unexpected error: {e}")
        finally:
            # Clean shutdown
            self._running = False
            self._restore_readline_completion()
            if self.client.connected:
                await self.client.disconnect()
            self.console.print()
            self.console.print("[bold cyan]Goodbye![/bold cyan]")


async def main() -> None:
    """Entry point for CLI."""
    cli = ChatCLI()
    await cli.run()


if __name__ == "__main__":
    asyncio.run(main())
