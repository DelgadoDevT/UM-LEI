"""Runtime observability dashboard for the server process."""

from __future__ import annotations

import asyncio
from contextlib import suppress
from datetime import datetime, timezone
from typing import Callable

from rich.console import Console, Group
from rich.live import Live
from rich.panel import Panel
from rich.table import Table

from src.rich_logging import log_warn


class ServerRuntimeDashboard:
    """Render live runtime metrics for server observability in terminal sessions."""

    def __init__(
        self,
        console: Console,
        host: str,
        port: int,
        connected_clients_getter: Callable[[], int],
        refresh_interval: float = 1.0,
    ) -> None:
        """Initialize dashboard rendering dependencies and counters."""
        self.console = console
        self.host = host
        self.port = port
        self.connected_clients_getter = connected_clients_getter
        self.refresh_interval = refresh_interval

        self.started_at = datetime.now(timezone.utc)
        self._live: Live | None = None
        self._refresh_task: asyncio.Task[None] | None = None
        self._counters = {
            "connections_accepted": 0,
            "clients_authenticated": 0,
            "messages_forwarded": 0,
            "messages_queued_offline": 0,
            "offline_messages_delivered": 0,
            "connection_errors": 0,
        }

    def increment(self, counter_name: str, value: int = 1) -> None:
        """Increment a named counter when it exists in the dashboard state."""
        if counter_name in self._counters:
            self._counters[counter_name] += value

    async def start(self) -> None:
        """Start live dashboard rendering when terminal supports it."""
        if not self._supports_live_rendering():
            return

        try:
            self._live = Live(
                self._build_panel(),
                console=self.console,
                auto_refresh=False,
                transient=False,
            )
            self._live.start()
            self._refresh_task = asyncio.create_task(
                self._refresh_loop(),
                name="server-runtime-dashboard",
            )
        except Exception as exc:  # pragma: no cover - terminal dependent
            log_warn(
                "runtime.panel.unavailable",
                "Runtime panel unavailable, continuing without live UI",
                error=type(exc).__name__,
            )
            self._live = None
            self._refresh_task = None

    async def stop(self) -> None:
        """Stop live rendering task and release runtime UI resources."""
        if self._refresh_task is not None:
            self._refresh_task.cancel()
            with suppress(asyncio.CancelledError):
                await self._refresh_task
            self._refresh_task = None

        if self._live is not None:
            with suppress(Exception):  # pragma: no cover - terminal dependent
                self._live.stop()
            self._live = None

    def _supports_live_rendering(self) -> bool:
        """Return whether current terminal can safely render live updates."""
        if not self.console.is_terminal:
            return False
        if getattr(self.console, "is_dumb_terminal", False):
            return False
        return True

    async def _refresh_loop(self) -> None:
        """Refresh panel periodically while the live dashboard is active."""
        try:
            while True:
                await asyncio.sleep(self.refresh_interval)
                if self._live is not None:
                    self._live.update(self._build_panel(), refresh=True)
        except asyncio.CancelledError:
            return
        except Exception as exc:  # pragma: no cover - terminal dependent
            log_warn(
                "runtime.panel.failed",
                "Runtime panel update failed, disabling live UI",
                error=type(exc).__name__,
            )

    def _build_panel(self) -> Panel:
        """Build composed runtime panel with connectivity and counter tables."""
        runtime_table = Table(show_header=False, box=None, pad_edge=False)
        runtime_table.add_row("Host", self.host)
        runtime_table.add_row("Port", str(self.port))
        runtime_table.add_row("Uptime", self._uptime_text())
        runtime_table.add_row("Connected clients", str(self.connected_clients_getter()))

        counters_table = Table(title="Event counters", box=None, pad_edge=False)
        counters_table.add_column("Event", style="bold cyan")
        counters_table.add_column("Count", justify="right", style="bold")
        counters_table.add_row("Connections accepted", str(self._counters["connections_accepted"]))
        counters_table.add_row("Clients authenticated", str(self._counters["clients_authenticated"]))
        counters_table.add_row("Messages forwarded", str(self._counters["messages_forwarded"]))
        counters_table.add_row("Messages queued offline", str(self._counters["messages_queued_offline"]))
        counters_table.add_row("Offline messages delivered", str(self._counters["offline_messages_delivered"]))
        counters_table.add_row("Connection errors", str(self._counters["connection_errors"]))

        return Panel(
            Group(runtime_table, counters_table),
            title="Server Runtime",
            border_style="cyan",
        )

    def _uptime_text(self) -> str:
        """Return HH:MM:SS formatted uptime from dashboard start."""
        elapsed = datetime.now(timezone.utc) - self.started_at
        total_seconds = int(elapsed.total_seconds())
        hours, rem = divmod(total_seconds, 3600)
        minutes, seconds = divmod(rem, 60)
        return f"{hours:02d}:{minutes:02d}:{seconds:02d}"
