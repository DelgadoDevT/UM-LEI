"""Handle one authenticated client connection on the server.

Coordinate transport-handshake completion, per-client command dispatch,
online/offline message delivery, and cleanup of connection registry state.
"""
import asyncio
import json
import base64

from src.protocol.messages import MsgType, DeliverMessage, BundleResponse, BundleNotFound, OfflineMessages
from src.protocol.handshake import server_handshake
from src.protocol.transport import recv_message, send_message
from src.rich_logging import log_error, log_info, log_network, log_success, log_warn
import config


class ConnectionHandler:
    """Manage message flow for one connected client.

    Route incoming protocol messages to dedicated handlers, deliver outgoing
    responses over the encrypted transport channel, and update connected-user
    bookkeeping in the parent server.

    :ivar reader: Async reader for the underlying TCP stream.
    :vartype reader: asyncio.StreamReader
    :ivar writer: Async writer for the underlying TCP stream.
    :vartype writer: asyncio.StreamWriter
    :ivar addr: Client socket address pair.
    :vartype addr: tuple[str, int]
    :ivar server: Parent server object providing shared state and storage.
    :vartype server: object
    :ivar username: Authenticated username associated with this connection.
    :vartype username: str | None
    :ivar transport_key_send: Key used to encrypt server-to-client frames.
    :vartype transport_key_send: bytes | None
    :ivar transport_key_recv: Key used to decrypt client-to-server frames.
    :vartype transport_key_recv: bytes | None
    """

    def __init__(self, reader, writer, addr, server):
        """Initialize per-connection runtime state.

        :param reader: Async stream reader for inbound bytes.
        :type reader: asyncio.StreamReader
        :param writer: Async stream writer for outbound bytes.
        :type writer: asyncio.StreamWriter
        :param addr: Client endpoint as host/port tuple.
        :type addr: tuple[str, int]
        :param server: Parent server exposing db, lock, and client registry.
        :type server: object
        :returns: None.
        :rtype: None
        """
        self.reader = reader
        self.writer = writer
        self.addr = addr
        self.server = server
        self.username = None
        self.transport_key_send = None
        self.transport_key_recv = None

    async def run(self):
        """Run the authenticated connection lifecycle until disconnect.

        Perform server-side transport handshake, register the authenticated
        client, dispatch incoming protocol messages, and ensure cleanup on
        connection termination.

        :returns: None.
        :rtype: None
        """
        try:
            self.transport_key_recv, self.transport_key_send, self.username = await server_handshake(
                self.reader, self.writer, self.server.db, self.server.ca
            )
            if self.username:
                log_success(
                    "auth.accepted",
                    "Client authenticated",
                    user=self.username,
                    peer=self.addr,
                )
                self.server.increment_runtime_counter("clients_authenticated")
                await self.server.register_connected_client(self.username, self)
                
                await self._handle_fetch_offline()

                while True:
                    try:
                        msg = await asyncio.wait_for(
                            recv_message(self.reader, self.transport_key_recv),
                            timeout=config.SERVER_IDLE_TIMEOUT_SECONDS,
                        )
                    except asyncio.TimeoutError:
                        log_warn(
                            "connection.idle",
                            "Closing idle client connection",
                            user=self.username,
                            peer=self.addr,
                            timeout_seconds=config.SERVER_IDLE_TIMEOUT_SECONDS,
                        )
                        break
                    await self._dispatch(msg)
        except (ConnectionResetError, asyncio.IncompleteReadError):
            log_network("connection.lost", "Connection closed unexpectedly", user=self.username, peer=self.addr)
        except Exception as e:
            self.server.increment_runtime_counter("connection_errors")
            log_error(
                "connection.error",
                "Unhandled connection error",
                user=self.username,
                peer=self.addr,
                error=type(e).__name__,
            )
        finally:
            if self.username:
                log_network(
                    "connection.close",
                    "User disconnected",
                    user=self.username,
                    peer=self.addr,
                )
                await self.server.unregister_connected_client(self.username, self)
            self.writer.close()
            await self.writer.wait_closed()

    async def _dispatch(self, msg):
        """Dispatch one received protocol message to its handler."""
        handlers = {
            MsgType.SEND_MSG: self._handle_send_message,
            MsgType.FETCH_BUNDLE: self._handle_fetch_bundle,
            MsgType.FETCH_OFFLINE: self._handle_fetch_offline,
        }
        handler = handlers.get(msg.type)
        if handler:
            await handler(msg)
        else:
            msg_type = msg.type.value if hasattr(msg.type, "value") else str(msg.type)
            log_warn("dispatch.unhandled", "Unhandled message type", user=self.username, msg_type=msg_type)

    async def _handle_send_message(self, msg):
        """Deliver a direct message or persist it for offline delivery."""
        recipient = msg.recipient
        recipient_handlers = await self.server.get_connected_client_handlers(recipient)

        delivered_count = 0
        stale_handlers: list[ConnectionHandler] = []

        if recipient_handlers:
            for recipient_handler in recipient_handlers:
                try:
                    delivery = DeliverMessage(payload_json=msg.to_json())
                    await recipient_handler.send_to_client(delivery)
                    delivered_count += 1
                except Exception:
                    stale_handlers.append(recipient_handler)

            for stale_handler in stale_handlers:
                await self.server.unregister_connected_client(recipient, stale_handler)

            if delivered_count > 0:
                self.server.increment_runtime_counter("messages_forwarded", delivered_count)
                log_network(
                    "message.forwarded",
                    "Delivered message to online recipient sessions",
                    sender=self.username,
                    recipient=recipient,
                    sessions=delivered_count,
                )
                return

        self.server.increment_runtime_counter("messages_queued_offline")
        self.server.db.store_offline_message(recipient, self.username, msg.to_json())
        log_info(
            "message.queued",
            "Stored message for offline delivery",
            sender=self.username,
            recipient=recipient,
        )

    async def _handle_fetch_bundle(self, msg):
        """Fetch and return a recipient public-key bundle."""
        target_username = msg.username
        bundle = self.server.db.get_user_bundle(target_username)
        if bundle:
            for key, value in bundle.items():
                if isinstance(value, bytes):
                    bundle[key] = base64.b64encode(value).decode()
            response = BundleResponse(username=target_username, bundle_json=json.dumps(bundle))
            await self.send_to_client(response)
            log_info(
                "bundle.fetch.hit",
                "Returned key bundle",
                requester=self.username,
                target=target_username,
            )
        else:
            response = BundleNotFound(username=target_username)
            await self.send_to_client(response)
            log_warn(
                "bundle.fetch.miss",
                "Requested bundle not found",
                requester=self.username,
                target=target_username,
            )

    async def _handle_fetch_offline(self, msg=None):
        """Deliver queued offline messages for the authenticated user."""
        messages = self.server.db.pop_offline_messages(self.username)
        if messages:
            self.server.increment_runtime_counter("offline_messages_delivered", len(messages))
            log_info(
                "offline.deliver",
                "Delivering queued offline messages",
                user=self.username,
                count=len(messages),
            )
        # Always send response so client knows check is complete
        response = OfflineMessages(messages=messages or [])
        await self.send_to_client(response)
        
    async def send_to_client(self, msg):
        """Send one encrypted protocol message to this client.

        :param msg: Protocol message envelope to serialize and send.
        :type msg: src.protocol.messages.BaseMessage
        :returns: None.
        :rtype: None
        """
        await send_message(self.writer, self.transport_key_send, msg)
