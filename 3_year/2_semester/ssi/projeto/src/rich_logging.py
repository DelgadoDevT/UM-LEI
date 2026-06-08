"""Rich-based structured logging helpers for terminal output."""

from __future__ import annotations

from typing import Any

from rich.console import Console
from rich.markup import escape
from rich.rule import Rule

_console = Console()

_EVENT_STYLES = {
    "info": "bold blue",
    "success": "bold green",
    "error": "bold red",
    "warn": "bold yellow",
    "network": "bold cyan",
    "security": "bold magenta",
}

_EVENT_ICONS = {
    "info": "ℹ",
    "success": "✓",
    "error": "✗",
    "warn": "⚠",
    "network": "⇄",
    "security": "🔐",
}

_ALERT_CATEGORIES = {"warn", "error", "security"}
_SEPARATOR_STYLE = "bright_black"
_SEPARATOR_TITLES = {
    "connection": "connection lifecycle",
    "alerts": "warning/error/security activity",
}
_last_separator_group: str | None = None


def get_console() -> Console:
    """Return the shared Rich console instance used for logs."""
    return _console


def reset_presentation_state() -> None:
    """Reset separator grouping state between independent log flows."""
    global _last_separator_group
    _last_separator_group = None


def _context_value(key: str, value: Any) -> str:
    """Normalize context values to compact printable text."""
    if key == "peer" and isinstance(value, tuple) and len(value) >= 2:
        return f"{value[0]}:{value[1]}"
    return str(value)


def _format_context(context: dict[str, Any]) -> str:
    """Render structured context as escaped dimmed key=value suffix."""
    parts: list[str] = []
    for key, value in context.items():
        if value is None:
            continue
        value_text = _context_value(key, value).strip()
        if not value_text:
            continue
        parts.append(f"{escape(str(key))}={escape(value_text)}")
    return f" [dim]{' '.join(parts)}[/dim]" if parts else ""


def _separator_group(category: str, event: str) -> str | None:
    """Map an event to a visual separator group when applicable."""
    if category in _ALERT_CATEGORIES:
        return "alerts"
    if event.lower().startswith("connection."):
        return "connection"
    return None


def _maybe_print_separator(category: str, event: str) -> None:
    """Print group separator when transitioning across event groups."""
    global _last_separator_group

    group = _separator_group(category, event)
    if group is None:
        _last_separator_group = None
        return

    if group != _last_separator_group:
        title = _SEPARATOR_TITLES[group]
        _console.print(Rule(f"[dim]{title}[/dim]", style=_SEPARATOR_STYLE))
    _last_separator_group = group


def log_event(category: str, event: str, message: str, **context: Any) -> None:
    """Emit one structured log line with optional contextual metadata."""
    _maybe_print_separator(category, event)
    style = _EVENT_STYLES.get(category, _EVENT_STYLES["info"])
    icon = _EVENT_ICONS.get(category, _EVENT_ICONS["info"])
    context_text = _format_context(context)
    _console.print(
        f"[{style}]{icon} {escape(event.upper())}[/] {escape(message)}{context_text}"
    )


def log_info(event: str, message: str, **context: Any) -> None:
    """Log informational event."""
    log_event("info", event, message, **context)


def log_success(event: str, message: str, **context: Any) -> None:
    """Log successful operation event."""
    log_event("success", event, message, **context)


def log_error(event: str, message: str, **context: Any) -> None:
    """Log error event."""
    log_event("error", event, message, **context)


def log_warn(event: str, message: str, **context: Any) -> None:
    """Log warning event."""
    log_event("warn", event, message, **context)


def log_network(event: str, message: str, **context: Any) -> None:
    """Log network lifecycle event."""
    log_event("network", event, message, **context)


def log_security(event: str, message: str, **context: Any) -> None:
    """Log security-relevant event."""
    log_event("security", event, message, **context)
