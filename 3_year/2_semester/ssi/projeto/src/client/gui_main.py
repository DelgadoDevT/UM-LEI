"""Desktop launcher for the Secure Chat GUI."""
from __future__ import annotations

import os
import socket
import threading
import time
import asyncio
import sys


def _find_free_port(start_port: int, max_tries: int = 100) -> int:
    for port in range(start_port, start_port + max_tries):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            try:
                sock.bind(("127.0.0.1", port))
                return port
            except OSError:
                continue
    raise OSError("Could not find a free port for the GUI backend")


def _wait_for_port(host: str, port: int, timeout: float = 8.0) -> bool:
    deadline = time.time() + timeout
    while time.time() < deadline:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            sock.settimeout(0.5)
            try:
                sock.connect((host, port))
                return True
            except OSError:
                time.sleep(0.1)
    return False


def main() -> None:
    try:
        import uvicorn
        import webview
    except Exception as exc:
        raise SystemExit(
            "GUI dependencies are missing. Install with Poetry and retry: "
            "fastapi, uvicorn, pywebview"
        ) from exc

    from src.client.gui_backend import build_app
    from src.client.gui_controller import GUIController

    host = os.getenv("GUI_BACKEND_HOST", "127.0.0.1")
    default_port = int(os.getenv("GUI_BACKEND_PORT", "8765"))
    
    try:
        port = _find_free_port(default_port)
    except OSError as exc:
        raise SystemExit(str(exc)) from exc

    controller = GUIController()
    app = build_app(controller)

    config = uvicorn.Config(app, host=host, port=port, log_level="info")
    server = uvicorn.Server(config)

    def run_server() -> None:
        server.run()

    server_thread = threading.Thread(target=run_server)
    server_thread.start()

    if not _wait_for_port(host, port, timeout=10.0):
        raise SystemExit("GUI backend failed to start")

    window_url = f"http://{host}:{port}/"
    webview.create_window(
        "SecureFlow | E2EE Messaging",
        window_url,
        min_size=(1100, 760),
        width=1280,
        height=860,
    )
    
    # Blocks until window is closed
    webview.start()
    
    # Graceful shutdown
    print("Closing SecureFlow...")
    server.should_exit = True
    
    # Wait for server thread to finish (triggers lifespan and controller shutdown)
    server_thread.join(timeout=5.0)
    
    # Final backup cleanup
    try:
        asyncio.run(controller.shutdown())
    except Exception:
        pass
    
    sys.exit(0)


if __name__ == "__main__":
    main()
