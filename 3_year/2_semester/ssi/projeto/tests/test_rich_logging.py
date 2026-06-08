"""Tests for rich logging presentation separators."""

from __future__ import annotations

import io

from rich.console import Console

import src.rich_logging as rich_logging


def _setup_captured_console(monkeypatch) -> io.StringIO:
    """Swap global Rich console with in-memory capture console for assertions."""
    output = io.StringIO()
    console = Console(
        file=output,
        force_terminal=False,
        color_system=None,
        width=80,
    )
    monkeypatch.setattr(rich_logging, "_console", console)
    rich_logging.reset_presentation_state()
    return output


def test_connection_lifecycle_separator_is_printed_at_boundaries(monkeypatch) -> None:
    """Ensure connection separator is printed once per connection event burst."""
    output = _setup_captured_console(monkeypatch)

    rich_logging.log_network("connection.open", "New connection accepted")
    rich_logging.log_info("server.idle", "Waiting for connections")
    rich_logging.log_network("connection.close", "User disconnected")

    captured = output.getvalue()
    assert captured.count("connection lifecycle") == 2


def test_warning_error_security_burst_uses_single_separator(monkeypatch) -> None:
    """Ensure alert burst keeps a single warning/error/security separator."""
    output = _setup_captured_console(monkeypatch)

    rich_logging.log_warn("handshake.failed", "Handshake/authentication failed")
    rich_logging.log_error("connection.error", "Unhandled connection error")
    rich_logging.log_security("auth.login", "User login successful")

    captured = output.getvalue()
    assert captured.count("warning/error/security activity") == 1
