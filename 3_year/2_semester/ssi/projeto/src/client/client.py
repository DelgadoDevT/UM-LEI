"""Implement the asynchronous encrypted chat client runtime.

Manage server transport setup, account authentication, bundle retrieval,
message submission, and background receive-loop orchestration.
"""
from __future__ import annotations

import asyncio
import base64
import json
from typing import Callable, Awaitable

from src.crypto.asymmetric import (
    generate_x25519_keypair,
    x25519_exchange,
    serialize_public_key,
    deserialize_x25519_public,
)
from src.crypto.kdf import hkdf_derive
from src.protocol.messages import (
    MsgType,
    BaseMessage,
    ClientHello,
    ServerHello,
    Register,
    ClientAuth,
    AuthOK,
    AuthFail,
    FetchBundle,
    BundleResponse,
    BundleNotFound,
    SendMessage,
    DirectAck,
    DeliverMessage,
    FetchOffline,
    OfflineMessages,
)
from src.protocol.transport import send_message, recv_message

import config


class ChatClient:
    """Coordinate client networking and authenticated message transport.

    Expose high-level operations for connecting, registering, logging in,
    fetching key bundles, sending encrypted payloads, and receiving inbound
    protocol messages through a callback.

    :ivar reader: Stream reader for inbound server frames.
    :vartype reader: asyncio.StreamReader | None
    :ivar writer: Stream writer for outbound server frames.
    :vartype writer: asyncio.StreamWriter | None
    :ivar transport_key_send: Symmetric key for client-to-server encryption.
    :vartype transport_key_send: bytes | None
    :ivar transport_key_recv: Symmetric key for server-to-client decryption.
    :vartype transport_key_recv: bytes | None
    :ivar username: Authenticated account username when logged in.
    :vartype username: str | None
    :ivar ca_cert_pem: Server-provided CA certificate in PEM encoding.
    :vartype ca_cert_pem: str | None
    """
    
    def __init__(self):
        """Initialize connection and receive-loop state.

        :returns: None.
        :rtype: None
        """
        self.reader: asyncio.StreamReader | None = None
        self.writer: asyncio.StreamWriter | None = None
        self.transport_key_send: bytes | None = None
        self.transport_key_recv: bytes | None = None
        self.username: str | None = None
        self.ca_cert_pem: str | None = None
        self._message_handler: Callable[[BaseMessage], Awaitable[bool | None]] | None = None
        self._running = False
        self._receive_task: asyncio.Task | None = None
        # For pausing the receive loop during request-response
        self._request_in_progress = False
        self._paused_event: asyncio.Event | None = None
        self._resume_event: asyncio.Event | None = None
        self._direct_server: asyncio.AbstractServer | None = None
        self._direct_host: str = ""
        self._direct_port: int = 0
    
    def _ensure_events(self) -> None:
        """Create asyncio events if they don't exist (lazy init for correct event loop)."""
        if self._paused_event is None:
            self._paused_event = asyncio.Event()
        if self._resume_event is None:
            self._resume_event = asyncio.Event()
    
    @property
    def connected(self) -> bool:
        """Return whether the TCP connection is currently open."""
        if self.writer is None:
            return False
        if self.writer.is_closing():
            return False

        if self.reader is None:
            return True

        reader_at_eof = getattr(self.reader, "at_eof", None)
        if callable(reader_at_eof):
            return not bool(reader_at_eof())

        # Some tests stub reader with lightweight objects that do not expose
        # StreamReader APIs; treat these as connected when writer is open.
        return True
    
    @property
    def authenticated(self) -> bool:
        """Return whether a user account is currently authenticated."""
        return self.username is not None
    
    async def connect(
        self,
        host: str = None,
        port: int = None,
    ) -> None:
        """Connect to the server and complete transport key agreement.

        :param host: Server hostname override; defaults to configured host.
        :type host: str | None
        :param port: Server port override; defaults to configured port.
        :type port: int | None
        :returns: None.
        :rtype: None
        :raises OSError: If opening the TCP connection fails.
        :raises ConnectionError: If handshake response validation fails.

        Example::

            client = ChatClient()
            await client.connect("127.0.0.1", 9999)
        """
        host = host or config.SERVER_HOST
        port = port or config.SERVER_PORT

        # Authentication is bound to a specific server connection.
        # If we reconnect after a server-only disconnect, require re-login.
        self.username = None
        
        # Open TCP connection
        self.reader, self.writer = await asyncio.open_connection(host, port)
        
        # Perform transport handshake
        await self._handshake()
    
    async def _handshake(self) -> None:
        """Perform ECDH handshake to establish transport keys."""
        # Generate ephemeral key pair
        eph_priv, eph_pub = generate_x25519_keypair()
        eph_pub_bytes = serialize_public_key(eph_pub)
        
        # Send ClientHello
        client_hello = ClientHello(
            ephemeral_pub_b64=base64.b64encode(eph_pub_bytes).decode()
        )
        await send_message(self.writer, None, client_hello)
        
        # Receive ServerHello
        server_hello = await recv_message(self.reader, None)
        if not isinstance(server_hello, ServerHello):
            raise ConnectionError("Expected ServerHello from server")
        
        # Derive shared secret
        server_eph_pub_bytes = base64.b64decode(server_hello.ephemeral_pub_b64)
        server_eph_pub = deserialize_x25519_public(server_eph_pub_bytes)
        shared_secret = x25519_exchange(eph_priv, server_eph_pub)
        
        # Derive transport keys (same as server)
        transcript = client_hello.ephemeral_pub_b64.encode() + server_eph_pub_bytes
        derived_keys = hkdf_derive(shared_secret, 64, salt=b"", info=transcript)
        self.transport_key_send = derived_keys[:32]  # Client to Server
        self.transport_key_recv = derived_keys[32:]  # Server to Client
        
        # Store CA certificate for later verification
        self.ca_cert_pem = server_hello.ca_cert_pem
    
    async def register(
        self,
        username: str,
        password: str,
        key_bundle: dict,
        p2p_host: str = "",
        p2p_port: int = 0,
    ) -> bool:
        """Register a new account and upload the public key bundle.

        :param username: Desired account username.
        :type username: str
        :param password: Plaintext password that will be hashed locally.
        :type password: str
        :param key_bundle: Public key material serialized for upload.
        :type key_bundle: dict
        :param p2p_host: Advertised host for optional peer-to-peer delivery.
        :type p2p_host: str
        :param p2p_port: Advertised port for optional peer-to-peer delivery.
        :type p2p_port: int
        :returns: ``True`` when registration succeeds; otherwise ``False``.
        :rtype: bool
        :raises ConnectionError: If not connected or response type is invalid.

        Example::

            ok = await client.register("alice", "password", bundle)
            if ok:
                print("Registered")
        """
        if not self.connected:
            raise ConnectionError("Not connected to server")
        
        # Send registration message
        register_msg = Register(
            username=username,
            password=password,
            key_bundle_json=json.dumps(key_bundle),
            p2p_host=p2p_host,
            p2p_port=p2p_port,
        )
        await send_message(self.writer, self.transport_key_send, register_msg)

        try:
            # Wait for response
            response = await recv_message(self.reader, self.transport_key_recv)
        except asyncio.IncompleteReadError:
            # Server may close immediately after auth failure; treat as failed auth.
            await self.disconnect_server_only()
            return False
        except ConnectionResetError:
            await self.disconnect_server_only()
            return False
        except Exception as exc:
            await self.disconnect_server_only()
            raise ConnectionError(f"Registration failed: {exc}") from exc

        if isinstance(response, AuthOK):
            self.username = username
            return True
        if isinstance(response, AuthFail):
            await self.disconnect_server_only()
            return False

        await self.disconnect_server_only()
        raise ConnectionError(f"Unexpected response: {type(response)}")
    
    async def login(
        self,
        username: str,
        password: str,
        p2p_host: str = "",
        p2p_port: int = 0,
    ) -> bool:
        """Authenticate an existing account.

        :param username: Account username.
        :type username: str
        :param password: Plaintext password that will be hashed locally.
        :type password: str
        :param p2p_host: Advertised host for optional peer-to-peer delivery.
        :type p2p_host: str
        :param p2p_port: Advertised port for optional peer-to-peer delivery.
        :type p2p_port: int
        :returns: ``True`` when authentication succeeds; otherwise ``False``.
        :rtype: bool
        :raises ConnectionError: If not connected or response type is invalid.

        Example::

            ok = await client.login("alice", "password")
            if not ok:
                print("Login failed")
        """
        if not self.connected:
            raise ConnectionError("Not connected to server")
        
        # Send auth message
        auth_msg = ClientAuth(
            username=username,
            password=password,
            p2p_host=p2p_host,
            p2p_port=p2p_port,
        )
        await send_message(self.writer, self.transport_key_send, auth_msg)

        try:
            # Wait for response
            response = await recv_message(self.reader, self.transport_key_recv)
        except asyncio.IncompleteReadError:
            # Server may close immediately after auth failure; treat as failed auth.
            await self.disconnect_server_only()
            return False
        except ConnectionResetError:
            await self.disconnect_server_only()
            return False
        except Exception as exc:
            await self.disconnect_server_only()
            raise ConnectionError(f"Login failed: {exc}") from exc

        if isinstance(response, AuthOK):
            self.username = username
            return True
        if isinstance(response, AuthFail):
            await self.disconnect_server_only()
            return False

        await self.disconnect_server_only()
        raise ConnectionError(f"Unexpected response: {type(response)}")
    
    async def fetch_bundle(self, username: str) -> dict | None:
        """Fetch the target user's server-side key bundle.

        Pause background receiving while a deterministic request/response round
        trip is in progress.

        :param username: Username whose bundle is requested.
        :type username: str
        :returns: Parsed bundle dictionary, or ``None`` when absent.
        :rtype: dict | None
        :raises ConnectionError: If not authenticated/connected or flow is invalid.
        :raises TimeoutError: If no bundle response arrives before the deadline.

        Example::

            bundle = await client.fetch_bundle("bob")
            if bundle is None:
                print("Recipient has no bundle")
        """
        if not self.authenticated:
            raise ConnectionError("Not authenticated")
        if not self.connected:
            raise ConnectionError("Not connected to server")
        
        # Pause the receive loop and wait for it to acknowledge
        await self._pause_receiving()
        
        try:
            # Send fetch request
            fetch_msg = FetchBundle(username=username)
            await send_message(self.writer, self.transport_key_send, fetch_msg)

            # Read until we get the bundle response; ignore pending async notifications
            # (e.g., OfflineMessages sent by server immediately after authentication).
            loop = asyncio.get_running_loop()
            deadline = loop.time() + 10.0

            while True:
                timeout_left = deadline - loop.time()
                if timeout_left <= 0:
                    raise TimeoutError("Timed out waiting for bundle response")

                response = await asyncio.wait_for(
                    recv_message(self.reader, self.transport_key_recv),
                    timeout=timeout_left,
                )

                if isinstance(response, (BundleResponse, BundleNotFound)):
                    break

                if isinstance(response, (OfflineMessages, DeliverMessage)):
                    await self._dispatch_notification(response)
                    continue

                raise ConnectionError(f"Unexpected response: {type(response)}")
        finally:
            # Resume receiving
            self._resume_receiving()
        
        if isinstance(response, BundleResponse):
            return json.loads(response.bundle_json)
        elif isinstance(response, BundleNotFound):
            return None

        return None
    
    async def send_chat_message(
        self,
        recipient: str,
        ciphertext_b64: str,
        ephemeral_pub_b64: str = "",
        opk_id: int | None = None,
        msg_id: str = "",
    ) -> None:
        """Send one encrypted direct-message payload through the server.

        :param recipient: Recipient username.
        :type recipient: str
        :param ciphertext_b64: Base64-encoded ciphertext payload.
        :type ciphertext_b64: str
        :param ephemeral_pub_b64: Optional X3DH ephemeral public key.
        :type ephemeral_pub_b64: str
        :param opk_id: Optional one-time prekey identifier consumed by X3DH.
        :type opk_id: int | None
        :param msg_id: Application-level message identifier.
        :type msg_id: str
        :returns: None.
        :rtype: None
        :raises ConnectionError: If no authenticated server connection exists.

        Example::

            await client.send_chat_message("bob", payload_b64, msg_id="m1")
        """
        if not self.authenticated:
            raise ConnectionError("Not authenticated")
        if not self.connected:
            raise ConnectionError("Not connected to server")
        
        msg = SendMessage(
            msg_id=msg_id,
            recipient=recipient,
            sender=self.username,
            ciphertext_b64=ciphertext_b64,
            ephemeral_pub_b64=ephemeral_pub_b64,
            opk_id=opk_id,
        )
        await send_message(self.writer, self.transport_key_send, msg)

    async def ensure_direct_listener(
        self,
        host: str | None = None,
        port: int | None = None,
        *,
        force_rebind: bool = False,
    ) -> tuple[str, int]:
        """Start direct peer listener and return bound endpoint.

        If ``force_rebind`` is true and the requested endpoint differs from the
        current listener, the existing listener is restarted.
        """
        bind_host = host if host is not None else config.CLIENT_P2P_HOST
        bind_port = port if port is not None else config.CLIENT_P2P_PORT

        if self._direct_server and not force_rebind:
            return self._direct_host, self._direct_port

        if self._direct_server and force_rebind:
            # If already bound to the requested endpoint, keep it.
            if self._direct_host == bind_host and int(self._direct_port) == int(bind_port):
                return self._direct_host, self._direct_port
            self._direct_server.close()
            await self._direct_server.wait_closed()
            self._direct_server = None
            self._direct_host = ""
            self._direct_port = 0

        self._direct_server = await asyncio.start_server(self._handle_direct_client, bind_host, bind_port)

        sock = self._direct_server.sockets[0]
        bound_host, bound_port = sock.getsockname()[:2]
        self._direct_host = bound_host
        self._direct_port = int(bound_port)
        return self._direct_host, self._direct_port

    def get_advertised_endpoint(self) -> tuple[str, int]:
        """Return the endpoint advertised to peers for direct delivery.

        :returns: Advertised host/port pair, or empty endpoint when disabled.
        :rtype: tuple[str, int]
        """
        if not self._direct_server:
            return "", 0

        explicit_host = (config.CLIENT_P2P_ADVERTISE_HOST or "").strip()
        if explicit_host:
            return explicit_host, self._direct_port

        if self.writer:
            local = self.writer.get_extra_info("sockname")
            if local and len(local) >= 2:
                return local[0], self._direct_port

        return "127.0.0.1", self._direct_port

    async def send_chat_message_direct(
        self,
        *,
        peer_host: str,
        peer_port: int,
        recipient: str,
        sender: str,
        ciphertext_b64: str,
        ephemeral_pub_b64: str = "",
        opk_id: int | None = None,
        msg_id: str = "",
        timeout: float = 2.0,
    ) -> bool:
        """Send one encrypted message directly to a peer endpoint.

        :param peer_host: Peer host to connect to.
        :type peer_host: str
        :param peer_port: Peer port to connect to.
        :type peer_port: int
        :param recipient: Intended message recipient username.
        :type recipient: str
        :param sender: Sender username.
        :type sender: str
        :param ciphertext_b64: Base64-encoded ciphertext payload.
        :type ciphertext_b64: str
        :param ephemeral_pub_b64: Optional X3DH ephemeral public key.
        :type ephemeral_pub_b64: str
        :param opk_id: Optional one-time prekey identifier.
        :type opk_id: int | None
        :param msg_id: Application-level message identifier.
        :type msg_id: str
        :param timeout: Timeout in seconds for each network step.
        :type timeout: float
        :returns: ``True`` when a valid positive ACK is received.
        :rtype: bool
        """
        if not peer_host or not peer_port:
            return False

        reader: asyncio.StreamReader | None = None
        writer: asyncio.StreamWriter | None = None
        try:
            reader, writer = await asyncio.wait_for(
                asyncio.open_connection(peer_host, int(peer_port)),
                timeout=timeout,
            )

            direct_msg = SendMessage(
                msg_id=msg_id,
                recipient=recipient,
                sender=sender,
                ciphertext_b64=ciphertext_b64,
                ephemeral_pub_b64=ephemeral_pub_b64,
                opk_id=opk_id,
            )
            await asyncio.wait_for(send_message(writer, None, direct_msg), timeout=timeout)
            ack = await asyncio.wait_for(recv_message(reader, None), timeout=timeout)
            return (
                isinstance(ack, DirectAck)
                and ack.accepted
                and ack.msg_id == msg_id
            )
        except Exception:
            return False
        finally:
            if writer:
                writer.close()
                try:
                    await writer.wait_closed()
                except Exception:
                    pass

    async def send_chat_message_routed(
        self,
        *,
        recipient: str,
        sender: str,
        ciphertext_b64: str,
        ephemeral_pub_b64: str = "",
        opk_id: int | None = None,
        msg_id: str = "",
        peer_host: str = "",
        peer_port: int = 0,
    ) -> tuple[str, dict | None]:
        """Send through server first and fallback to direct peer on failure.

        :param recipient: Intended recipient username.
        :type recipient: str
        :param sender: Sender username used for direct fallback.
        :type sender: str
        :param ciphertext_b64: Base64-encoded ciphertext payload.
        :type ciphertext_b64: str
        :param ephemeral_pub_b64: Optional X3DH ephemeral public key.
        :type ephemeral_pub_b64: str
        :param opk_id: Optional one-time prekey identifier.
        :type opk_id: int | None
        :param msg_id: Application-level message identifier.
        :type msg_id: str
        :param peer_host: Optional direct peer host for fallback.
        :type peer_host: str
        :param peer_port: Optional direct peer port for fallback.
        :type peer_port: int
        :returns: Transport used and optional metadata.
        :rtype: tuple[str, dict | None]
        :raises ConnectionError: If server is disconnected and direct fallback fails.
        """
        server_error: Exception | None = None

        if self.connected:
            try:
                await self.send_chat_message(
                    recipient=recipient,
                    ciphertext_b64=ciphertext_b64,
                    ephemeral_pub_b64=ephemeral_pub_b64,
                    opk_id=opk_id,
                    msg_id=msg_id,
                )
                return "server", None
            except Exception as exc:
                server_error = exc
        else:
            server_error = ConnectionError("Not connected to server")

        has_direct_endpoint = bool(peer_host and peer_port)
        if has_direct_endpoint and await self.send_chat_message_direct(
            peer_host=peer_host,
            peer_port=peer_port,
            recipient=recipient,
            sender=sender,
            ciphertext_b64=ciphertext_b64,
            ephemeral_pub_b64=ephemeral_pub_b64,
            opk_id=opk_id,
            msg_id=msg_id,
        ):
            return "direct", None

        if not self.connected:
            if has_direct_endpoint:
                raise ConnectionError(
                    f"Server disconnected and direct delivery to {peer_host}:{peer_port} failed"
                ) from server_error
            raise ConnectionError(
                "Server disconnected and no direct endpoint is available for this contact"
            ) from server_error

        raise server_error if server_error is not None else ConnectionError("Message delivery failed")
    
    async def fetch_offline_messages(self) -> list[str]:
        """Fetch and return queued offline messages for the account.

        Pause background receiving while request/response processing is active.

        :returns: Raw message payload JSON strings.
        :rtype: list[str]
        :raises ConnectionError: If no authenticated server connection exists.
        :raises TimeoutError: If the server does not reply before timeout.
        """
        if not self.authenticated:
            raise ConnectionError("Not authenticated")
        if not self.connected:
            raise ConnectionError("Not connected to server")
        
        # Pause the receive loop and wait for it to acknowledge
        await self._pause_receiving()
        
        try:
            fetch_msg = FetchOffline()
            await send_message(self.writer, self.transport_key_send, fetch_msg)
            
            loop = asyncio.get_running_loop()
            deadline = loop.time() + 10.0

            while True:
                timeout_left = deadline - loop.time()
                if timeout_left <= 0:
                    raise TimeoutError("Timed out waiting for offline messages")

                response = await asyncio.wait_for(
                    recv_message(self.reader, self.transport_key_recv),
                    timeout=timeout_left,
                )

                if isinstance(response, OfflineMessages):
                    break

                if isinstance(response, DeliverMessage):
                    await self._dispatch_notification(response)
                    continue

                raise ConnectionError(f"Unexpected response: {type(response)}")
        finally:
            # Resume receiving
            self._resume_receiving()
        
        if isinstance(response, OfflineMessages):
            return response.messages or []
        else:
            return []
    
    def set_message_handler(
        self,
        handler: Callable[[BaseMessage], Awaitable[bool | None]],
    ) -> None:
        """Set the asynchronous callback for inbound protocol messages.

        :param handler: Coroutine function invoked for each received message.
        :type handler: Callable[[BaseMessage], Awaitable[bool | None]]
        :returns: None.
        :rtype: None
        """
        self._message_handler = handler

    async def _dispatch_notification(self, msg: BaseMessage) -> None:
        """Dispatch out-of-band notifications without interrupting request/response flows."""
        if not self._message_handler:
            return
        try:
            await self._message_handler(msg)
        except Exception:
            pass
    
    def _start_receive_task(self) -> None:
        """Start the background receive task."""
        if self._receive_task is None or self._receive_task.done():
            self._ensure_events()
            self._running = True
            self._request_in_progress = False
            self._paused_event.clear()
            self._resume_event.clear()
            self._receive_task = asyncio.create_task(self._receive_loop())
    
    async def _pause_receiving(self) -> None:
        """Pause the receive loop and wait for it to acknowledge."""
        if not self._receive_task or self._receive_task.done():
            return  # Nothing to pause
        
        self._ensure_events()
        
        # Signal that we want to pause
        self._request_in_progress = True
        self._paused_event.clear()
        
        # Wait for receive loop to acknowledge pause (max ~1.5s for timeout to expire)
        try:
            await asyncio.wait_for(self._paused_event.wait(), timeout=2.0)
        except asyncio.TimeoutError:
            pass  # Continue anyway
    
    def _resume_receiving(self) -> None:
        """Signal the receive loop to resume."""
        self._request_in_progress = False
        if self._resume_event:
            self._resume_event.set()
    
    async def _stop_receive_task(self) -> None:
        """Stop the background receive task completely."""
        self._running = False
        self._request_in_progress = False
        if self._resume_event:
            self._resume_event.set()  # Unblock if paused
        
        if self._receive_task and not self._receive_task.done():
            self._receive_task.cancel()
            try:
                await self._receive_task
            except asyncio.CancelledError:
                pass
        self._receive_task = None
    
    async def _receive_loop(self) -> None:
        """Background loop that receives messages from server."""
        try:
            while self._running and self.connected:
                # Check if we should pause for a request-response operation
                if self._request_in_progress:
                    self._paused_event.set()  # Signal that we've paused
                    self._resume_event.clear()
                    await self._resume_event.wait()  # Wait for resume signal
                    continue
                
                try:
                    msg = await asyncio.wait_for(
                        recv_message(self.reader, self.transport_key_recv),
                        timeout=0.5,  # Short timeout for responsiveness
                    )
                    
                    # Handle message if we have a handler
                    if msg:
                        await self._dispatch_notification(msg)
                        
                except asyncio.TimeoutError:
                    continue  # Check flags again
                except asyncio.IncompleteReadError:
                    break
                except asyncio.CancelledError:
                    raise
        except asyncio.CancelledError:
            pass
        except Exception:
            pass
        finally:
            self._running = False
    
    async def start_receiving(self) -> None:
        """Start the background receive loop.

        :returns: None.
        :rtype: None
        :raises ConnectionError: If no authenticated session exists.
        """
        if not self.authenticated:
            raise ConnectionError("Not authenticated")
        
        self._start_receive_task()

    async def _handle_direct_client(
        self,
        reader: asyncio.StreamReader,
        writer: asyncio.StreamWriter,
    ) -> None:
        """Handle inbound direct P2P message and forward to message handler."""
        ack = DirectAck(accepted=False, reason="invalid")
        try:
            msg = await recv_message(reader, None)
            if not isinstance(msg, SendMessage):
                ack.reason = "invalid_type"
            elif not self.username or msg.recipient != self.username:
                ack.msg_id = msg.msg_id
                ack.reason = "wrong_recipient"
            elif not msg.sender or not msg.ciphertext_b64 or not msg.msg_id:
                ack.msg_id = msg.msg_id
                ack.reason = "invalid_payload"
            elif not self._message_handler:
                ack.msg_id = msg.msg_id
                ack.reason = "no_handler"
            else:
                result = await self._message_handler(DeliverMessage(payload_json=msg.to_json()))
                ack.msg_id = msg.msg_id
                if result is False:
                    ack.accepted = False
                    ack.reason = "rejected"
                else:
                    ack.accepted = True
                    ack.reason = "ok"
        except Exception:
            ack.reason = "parse_error"
        try:
            await send_message(writer, None, ack)
        except Exception:
            pass
        finally:
            writer.close()
            try:
                await writer.wait_closed()
            except Exception:
                pass
    
    def stop_receiving(self) -> None:
        """Stop the background receive loop.

        :returns: None.
        :rtype: None
        """
        self._running = False
        if self._receive_task and not self._receive_task.done():
            self._receive_task.cancel()
    
    async def disconnect(self) -> None:
        """Close server and direct connections and clear runtime state.

        :returns: None.
        :rtype: None
        """
        await self._stop_receive_task()

        if self._direct_server:
            self._direct_server.close()
            await self._direct_server.wait_closed()
            self._direct_server = None
            self._direct_host = ""
            self._direct_port = 0
        
        if self.writer:
            self.writer.close()
            try:
                await self.writer.wait_closed()
            except Exception:
                pass
        
        self.reader = None
        self.writer = None
        self.transport_key_send = None
        self.transport_key_recv = None
        self.username = None

    async def disconnect_server_only(self) -> None:
        """Close only the server connection and keep direct listener state.

        :returns: None.
        :rtype: None
        """
        await self._stop_receive_task()

        if self.writer:
            self.writer.close()
            try:
                await self.writer.wait_closed()
            except Exception:
                pass

        self.reader = None
        self.writer = None
        self.transport_key_send = None
        self.transport_key_recv = None
