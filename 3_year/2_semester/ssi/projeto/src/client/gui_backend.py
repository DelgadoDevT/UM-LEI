"""Local HTTP/WebSocket backend for the desktop GUI."""
from __future__ import annotations

from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI, HTTPException, WebSocket, WebSocketDisconnect
from fastapi.exceptions import RequestValidationError
from fastapi.responses import HTMLResponse, JSONResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field

from src.client.gui_controller import GUIController


class CredentialsRequest(BaseModel):
    username: str = Field(min_length=1)
    password: str = Field(min_length=1)


class UsernameRequest(BaseModel):
    username: str = Field(min_length=1)


class MessageRequest(BaseModel):
    username: str = Field(min_length=1)
    text: str = Field(min_length=1)


class GroupNameRequest(BaseModel):
    name: str = Field(min_length=1)


class GroupMemberRequest(BaseModel):
    username: str = Field(min_length=1)


class GroupMessageRequest(BaseModel):
    text: str = Field(min_length=1)


def build_app(controller: GUIController | None = None) -> FastAPI:
    gui = controller or GUIController()

    @asynccontextmanager
    async def lifespan(_app: FastAPI):
        yield
        await gui.shutdown()

    app = FastAPI(title="Secure Chat GUI Backend", lifespan=lifespan)

    static_dir = Path(__file__).parent / "gui" / "static"
    app.mount("/static", StaticFiles(directory=str(static_dir)), name="static")

    @app.exception_handler(RequestValidationError)
    async def validation_exception_handler(request, exc: RequestValidationError):
        messages = []
        for error in exc.errors():
            field = error.get("loc", [""])[-1]
            field_name = str(field).capitalize() if field else "Campo"
            err_type = error.get("type")
            
            if err_type == "string_too_short":
                messages.append(f"{field_name} não pode estar vazio.")
            elif err_type == "missing":
                messages.append(f"{field_name} é obrigatório.")
            else:
                messages.append(f"{field_name} é inválido.")
                
        return JSONResponse(
            status_code=422,
            content={"detail": " ".join(messages)}
        )

    async def run_action(action):
        try:
            return await gui.run_locked(action)
        except ValueError as exc:
            raise HTTPException(status_code=400, detail=str(exc)) from exc
        except ConnectionError as exc:
            raise HTTPException(status_code=503, detail=str(exc)) from exc
        except Exception as exc:
            raise HTTPException(status_code=500, detail=str(exc)) from exc

    @app.get("/", response_class=HTMLResponse)
    async def index() -> str:
        return (static_dir / "index.html").read_text(encoding="utf-8")

    @app.get("/api/state")
    async def api_state():
        return gui.state_snapshot()

    @app.post("/api/connect")
    async def api_connect():
        return await run_action(gui.connect())

    @app.post("/api/disconnect")
    async def api_disconnect():
        return await run_action(gui.disconnect())

    @app.post("/api/register")
    async def api_register(req: CredentialsRequest):
        print(f"[API] Register request for: {req.username}")
        return await run_action(gui.register(req.username, req.password))

    @app.post("/api/login")
    async def api_login(req: CredentialsRequest):
        print(f"[API] Login request for: {req.username}")
        return await run_action(gui.login(req.username, req.password))

    @app.get("/api/contacts")
    async def api_contacts():
        return gui.get_contacts()

    @app.post("/api/contacts")
    async def api_add_contact(req: UsernameRequest):
        return await run_action(gui.add_contact(req.username))

    @app.post("/api/messages")
    async def api_send_message(req: MessageRequest):
        return await run_action(gui.send_message(req.username, req.text))

    @app.get("/api/history/{username}")
    async def api_history(username: str, limit: int = 50):
        try:
            return gui.get_history(username, limit=limit)
        except ValueError as exc:
            raise HTTPException(status_code=400, detail=str(exc)) from exc

    @app.post("/api/offline/fetch")
    async def api_fetch_offline():
        return await run_action(gui.fetch_offline())

    @app.get("/api/groups")
    async def api_groups():
        try:
            return gui.list_groups()
        except ValueError as exc:
            raise HTTPException(status_code=400, detail=str(exc)) from exc

    @app.post("/api/groups")
    async def api_create_group(req: GroupNameRequest):
        return await run_action(gui.create_group(req.name))

    @app.get("/api/groups/invites")
    async def api_group_invites():
        return gui.list_group_invites()

    @app.post("/api/groups/{group_id}/accept")
    async def api_group_accept(group_id: str):
        return await run_action(gui.accept_group_invite(group_id))

    @app.post("/api/groups/{group_id}/reject")
    async def api_group_reject(group_id: str):
        try:
            return gui.reject_group_invite(group_id)
        except ValueError as exc:
            raise HTTPException(status_code=400, detail=str(exc)) from exc

    @app.get("/api/groups/{group_id}/members")
    async def api_group_members(group_id: str):
        try:
            return gui.group_members(group_id)
        except ValueError as exc:
            raise HTTPException(status_code=400, detail=str(exc)) from exc

    @app.post("/api/groups/{group_id}/invite")
    async def api_group_invite(group_id: str, req: GroupMemberRequest):
        return await run_action(gui.invite_group_member(group_id, req.username))

    @app.post("/api/groups/{group_id}/remove")
    async def api_group_remove(group_id: str, req: GroupMemberRequest):
        return await run_action(gui.remove_group_member(group_id, req.username))

    @app.post("/api/groups/{group_id}/messages")
    async def api_group_message(group_id: str, req: GroupMessageRequest):
        return await run_action(gui.send_group_message(group_id, req.text))

    @app.get("/api/groups/{group_id}/history")
    async def api_group_history(group_id: str, limit: int = 50):
        try:
            return gui.get_group_history(group_id, limit=limit)
        except ValueError as exc:
            raise HTTPException(status_code=400, detail=str(exc)) from exc

    @app.websocket("/ws/events")
    async def ws_events(ws: WebSocket):
        await ws.accept()
        try:
            while True:
                event = await gui.next_event(timeout=25.0)
                await ws.send_json(event)
        except WebSocketDisconnect:
            return

    return app


app = build_app()
