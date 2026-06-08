const state = {
  selectedContact: null,
  selectedGroup: null,
  activeTab: 'chats',
  currentUser: null,
  authenticated: false
};

const el = {
  authOverlay: document.getElementById("authOverlay"),
  connectionBadge: document.getElementById("connectionBadge"),
  connectionText: document.getElementById("connectionText"),
  authUsername: document.getElementById("authUsername"),
  authPassword: document.getElementById("authPassword"),
  currentUsername: document.getElementById("currentUsername"),
  authBadgeText: document.getElementById("authBadgeText"),
  userAvatar: document.getElementById("userAvatar"),
  
  contactInput: document.getElementById("contactInput"),
  contactsList: document.getElementById("contactsList"),
  
  groupName: document.getElementById("groupName"),
  groupsList: document.getElementById("groupsList"),
  invitesList: document.getElementById("invitesList"),
  inviteCount: document.getElementById("inviteCount"),
  
  emptyState: document.getElementById("emptyState"),
  chatInterface: document.getElementById("chatInterface"),
  activeChatName: document.getElementById("activeChatName"),
  activeAvatar: document.getElementById("activeAvatar"),
  activeGroupActions: document.getElementById("activeGroupActions"),
  activeChatStatus: document.getElementById("activeChatStatus"),
  
  messageTimeline: document.getElementById("messageTimeline"),
  msgInput: document.getElementById("msgInput"),
  btnSend: document.getElementById("btnSend"),
  
  groupUserInput: document.getElementById("groupUserInput"),
  
  // Hidden inputs for state persistence (reusing existing logic)
  msgRecipient: document.getElementById("msgRecipient"),
  groupIdInput: document.getElementById("groupIdInput"),
  historyUser: document.getElementById("historyUser"),
  
  btnConnect: document.getElementById("btnConnect"),
  toast: document.getElementById("notificationToast")
};

// UI Helpers
function showToast(message, duration = 3000) {
  el.toast.textContent = message;
  el.toast.classList.add("show");
  setTimeout(() => el.toast.classList.remove("show"), duration);
}

function scrollToBottom() {
  el.messageTimeline.scrollTop = el.messageTimeline.scrollHeight;
}

function getInitials(name) {
  return (name || "?").substring(0, 1).toUpperCase();
}

function formatTime(timestamp) {
  const date = timestamp ? new Date(timestamp) : new Date();
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function renderMessage(text, direction, sender, timestamp) {
  const div = document.createElement("div");
  div.className = `message ${direction === 'sent' ? 'sent' : 'received'}`;
  
  const content = document.createElement("div");
  content.className = "msg-content";
  
  if (state.selectedGroup && direction !== 'sent') {
    const senderEl = document.createElement("div");
    senderEl.className = "msg-sender";
    senderEl.textContent = sender;
    content.appendChild(senderEl);
  }
  
  const textEl = document.createElement("span");
  textEl.textContent = text;
  content.appendChild(textEl);
  
  const meta = document.createElement("div");
  meta.className = "msg-meta";
  meta.innerHTML = `<span>${formatTime(timestamp)}</span>`;
  if (direction === 'sent') meta.innerHTML += ' <span>✓✓</span>';
  
  div.appendChild(content);
  div.appendChild(meta);
  el.messageTimeline.appendChild(div);
  scrollToBottom();
}

// API Wrapper
async function api(path, options = {}) {
  const response = await fetch(path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.detail || `${response.status} ${response.statusText}`);
  }
  return data;
}

// State Management
function setConnected(connected) {
  el.connectionBadge.className = `status-dot ${connected ? "connected" : "disconnected"}`;
  el.connectionText.textContent = connected ? "System Ready" : "Disconnected";
  if (el.btnConnect) {
    el.btnConnect.style.display = connected ? "none" : "inline-block";
  }
}

function setAuth(authenticated, username = "") {
  state.authenticated = authenticated;
  state.currentUser = username;
  
  if (authenticated) {
    el.authOverlay.classList.add("hidden");
    el.currentUsername.textContent = username;
    el.authBadgeText.textContent = "Secure Session Active";
    el.userAvatar.textContent = getInitials(username);
  } else {
    el.authOverlay.classList.remove("hidden");
  }
}

async function selectContact(username) {
  state.selectedContact = username;
  state.selectedGroup = null;
  
  el.msgRecipient.value = username;
  el.historyUser.value = username;
  
  el.emptyState.style.display = "none";
  el.chatInterface.style.display = "flex";
  el.activeGroupActions.style.display = "none";
  el.activeChatName.textContent = username;
  el.activeAvatar.textContent = getInitials(username);
  el.activeChatStatus.textContent = "End-to-End Encrypted";
  
  el.messageTimeline.innerHTML = "";
  await loadHistory();
  
  // Update sidebar selection
  document.querySelectorAll('.conversation-item').forEach(i => i.classList.remove('active'));
  const activeItem = document.querySelector(`.conversation-item[data-id="${username}"]`);
  if (activeItem) activeItem.classList.add('active');
}

async function selectGroup(group) {
  state.selectedGroup = group.group_id;
  state.selectedContact = null;
  
  el.groupIdInput.value = group.group_id;
  
  el.emptyState.style.display = "none";
  el.chatInterface.style.display = "flex";
  el.activeGroupActions.style.display = "flex";
  el.activeChatName.textContent = group.name;
  el.activeAvatar.textContent = "G";
  el.activeChatStatus.textContent = `${group.member_count} members • Secure`;
  
  el.messageTimeline.innerHTML = "";
  await loadGroupHistory();

  document.querySelectorAll('.conversation-item').forEach(i => i.classList.remove('active'));
  const activeItem = document.querySelector(`.conversation-item[data-id="${group.group_id}"]`);
  if (activeItem) activeItem.classList.add('active');
}

// Rendering
function renderContacts(items) {
  el.contactsList.innerHTML = "";
  for (const contact of items) {
    const li = document.createElement("li");
    li.className = "conversation-item";
    li.dataset.id = contact.username;
    if (state.selectedContact === contact.username) li.classList.add("active");
    
    li.innerHTML = `
      <div class="conv-avatar">${getInitials(contact.username)}</div>
      <div class="conv-details">
        <div class="conv-top">
          <h4>${contact.username}</h4>
          <span>${contact.has_session ? "Locked" : ""}</span>
        </div>
        <div class="conv-preview">Click to open secure chat</div>
      </div>
    `;
    li.onclick = () => selectContact(contact.username);
    el.contactsList.appendChild(li);
  }
}

function renderGroups(items) {
  el.groupsList.innerHTML = "";
  for (const group of items) {
    const li = document.createElement("li");
    li.className = "conversation-item";
    li.dataset.id = group.group_id;
    if (state.selectedGroup === group.group_id) li.classList.add("active");
    
    li.innerHTML = `
      <div class="conv-avatar" style="background:#202c33">G</div>
      <div class="conv-details">
        <div class="conv-top">
          <h4>${group.name}</h4>
          <span>${group.member_count} members</span>
        </div>
        <div class="conv-preview">Group ID: ${group.group_id}</div>
      </div>
    `;
    li.onclick = () => selectGroup(group);
    el.groupsList.appendChild(li);
  }
}

function renderInvites(items) {
  el.invitesList.innerHTML = "";
  el.inviteCount.textContent = items.length;
  el.inviteCount.style.display = items.length > 0 ? "inline-block" : "none";
  
  for (const invite of items) {
    const li = document.createElement("li");
    li.className = "conversation-item";
    li.innerHTML = `
      <div class="conv-avatar" style="background:var(--danger)">!</div>
      <div class="conv-details">
        <div class="conv-top">
          <h4>Invite: ${invite.group_name}</h4>
        </div>
        <div class="conv-preview">From: ${invite.inviter}</div>
        <div class="add-action" style="padding: 8px 0 0 0">
           <button class="btn-primary mini-btn accept-btn">Accept</button>
           <button class="btn-secondary mini-btn reject-btn">Reject</button>
        </div>
      </div>
    `;
    
    li.querySelector('.accept-btn').onclick = async (e) => {
      e.stopPropagation();
      try {
        await api(`/api/groups/${invite.group_id}/accept`, { method: "POST" });
        showToast("Joined group");
        await loadInvites();
        await loadGroups();
      } catch (err) { showToast(`Error: ${err.message}`); }
    };
    
    li.querySelector('.reject-btn').onclick = async (e) => {
      e.stopPropagation();
      try {
        await api(`/api/groups/${invite.group_id}/reject`, { method: "POST" });
        showToast("Invite rejected");
        await loadInvites();
      } catch (err) { showToast(`Error: ${err.message}`); }
    };
    
    el.invitesList.appendChild(li);
  }
}

// Data Loaders
async function refreshState() {
  try {
    const data = await api("/api/state");
    setConnected(data.connected);
    setAuth(data.authenticated, data.username || "");
    renderInvites(data.pending_invites || []);
  } catch (err) { console.error("State refresh failed", err); }
}

async function loadContacts() {
  try {
    const data = await api("/api/contacts");
    renderContacts(data.contacts || []);
  } catch (err) { showToast(`Contacts error: ${err.message}`); }
}

async function loadHistory() {
  const username = state.selectedContact;
  if (!username) return;
  try {
    const data = await api(`/api/history/${encodeURIComponent(username)}?limit=50`);
    el.messageTimeline.innerHTML = "";
    (data.messages || []).forEach(msg => {
      renderMessage(
        msg.plaintext,
        msg.direction,
        msg.direction === 'sent' ? 'You' : username,
        msg.timestamp
      );
    });
    scrollToBottom();
  } catch (err) { console.error("History load failed", err); }
}

async function loadGroups() {
  try {
    const data = await api("/api/groups");
    renderGroups(data.groups || []);
  } catch (err) { console.error("Groups load failed", err); }
}

async function loadInvites() {
  try {
    const data = await api("/api/groups/invites");
    renderInvites(data.invites || []);
  } catch (err) { console.error("Invites load failed", err); }
}

async function loadGroupHistory() {
  const groupId = state.selectedGroup;
  if (!groupId) return;
  try {
    const data = await api(`/api/groups/${encodeURIComponent(groupId)}/history?limit=50`);
    el.messageTimeline.innerHTML = "";
    (data.messages || []).forEach(msg => {
      renderMessage(
        msg.plaintext,
        msg.sender === state.currentUser ? 'sent' : 'received',
        msg.sender,
        msg.timestamp
      );
    });
    scrollToBottom();
  } catch (err) { console.error("Group history failed", err); }
}

// Real-time
function startEventSocket() {
  const protocol = window.location.protocol === "https:" ? "wss" : "ws";
  const ws = new WebSocket(`${protocol}://${window.location.host}/ws/events`);

  ws.onmessage = (raw) => {
    const event = JSON.parse(raw.data);
    if (event.type === "heartbeat") return;

    if (event.type === "message") {
      if (state.selectedContact === event.sender) {
        renderMessage(event.text, 'received', event.sender);
      } else {
        showToast(`New message from ${event.sender}`);
      }
    }
    if (event.type === "message_sent") {
      if (state.selectedContact === event.to) {
        // Already rendered by optimistic UI or reload
      }
    }
    if (event.type === "group_message") {
      if (state.selectedGroup === event.group_id) {
        renderMessage(event.text, event.sender === state.currentUser ? 'sent' : 'received', event.sender);
      } else {
        showToast(`New message in ${event.group_name}`);
      }
    }
    if (event.type === "group_invite" || event.type === "group_joined") {
      loadInvites();
      loadGroups();
    }
  };

  ws.onclose = () => setTimeout(startEventSocket, 2000);
}

// Action Wiring
async function wireActions() {
  // Tabs
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.onclick = () => {
      const target = btn.dataset.tab;
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
      btn.classList.add('active');
      document.getElementById(`tab-${target}`).classList.add('active');
    };
  });

  // Force lowercase username for better UX
  el.authUsername.oninput = () => {
    el.authUsername.value = el.authUsername.value.toLowerCase();
  };
  
  el.contactInput.oninput = () => {
    el.contactInput.value = el.contactInput.value.toLowerCase();
  };
  
  el.groupUserInput.oninput = () => {
    el.groupUserInput.value = el.groupUserInput.value.toLowerCase();
  };

  document.getElementById("btnConnect").onclick = async () => {
    try {
      await api("/api/connect", { method: "POST" });
      await refreshState();
      showToast("Connected to server");
    } catch (err) { showToast(`Failed: ${err.message}`); }
  };

  document.getElementById("btnRegister").onclick = async () => {
    try {
      await api("/api/register", {
        method: "POST",
        body: JSON.stringify({
          username: el.authUsername.value.trim(),
          password: el.authPassword.value,
        }),
      });
      await refreshState();
      await boot();
    } catch (err) { showToast(`Register failed: ${err.message}`); }
  };

  document.getElementById("btnLogin").onclick = async () => {
    try {
      await api("/api/login", {
        method: "POST",
        body: JSON.stringify({
          username: el.authUsername.value.trim(),
          password: el.authPassword.value,
        }),
      });
      await refreshState();
      await boot();
    } catch (err) { showToast(`Login failed: ${err.message}`); }
  };

  document.getElementById("btnAddContact").onclick = async () => {
    try {
      await api("/api/contacts", {
        method: "POST",
        body: JSON.stringify({ username: el.contactInput.value.trim() }),
      });
      el.contactInput.value = "";
      await loadContacts();
      showToast("Contact added");
    } catch (err) { showToast(`Error: ${err.message}`); }
  };

  const sendMessage = async () => {
    const text = el.msgInput.value.trim();
    if (!text) return;
    
    try {
      if (state.selectedContact) {
        await api("/api/messages", {
          method: "POST",
          body: JSON.stringify({ username: state.selectedContact, text }),
        });
        renderMessage(text, 'sent', 'You');
      } else if (state.selectedGroup) {
        await api(`/api/groups/${encodeURIComponent(state.selectedGroup)}/messages`, {
          method: "POST",
          body: JSON.stringify({ text }),
        });
        renderMessage(text, 'sent', 'You');
      }
      el.msgInput.value = "";
    } catch (err) { showToast(`Send failed: ${err.message}`); }
  };

  el.btnSend.onclick = sendMessage;
  el.msgInput.onkeydown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  document.getElementById("btnFetchOffline").onclick = async () => {
    try {
      await api("/api/offline/fetch", { method: "POST" });
      if (state.selectedContact) await loadHistory();
      if (state.selectedGroup) await loadGroupHistory();
      showToast("Sync complete");
    } catch (err) { showToast(`Fetch failed: ${err.message}`); }
  };

  document.getElementById("btnCreateGroup").onclick = async () => {
    try {
      await api("/api/groups", {
        method: "POST",
        body: JSON.stringify({ name: el.groupName.value.trim() }),
      });
      el.groupName.value = "";
      await loadGroups();
      showToast("Group created");
    } catch (err) { showToast(`Error: ${err.message}`); }
  };

  document.getElementById("btnInvite").onclick = async () => {
    try {
      await api(`/api/groups/${encodeURIComponent(state.selectedGroup)}/invite`, {
        method: "POST",
        body: JSON.stringify({ username: el.groupUserInput.value.trim() }),
      });
      el.groupUserInput.value = "";
      showToast("Invite sent");
    } catch (err) { showToast(`Error: ${err.message}`); }
  };

  document.getElementById("btnRemove").onclick = async () => {
    try {
      await api(`/api/groups/${encodeURIComponent(state.selectedGroup)}/remove`, {
        method: "POST",
        body: JSON.stringify({ username: el.groupUserInput.value.trim() }),
      });
      el.groupUserInput.value = "";
      showToast("Member removed");
    } catch (err) { showToast(`Error: ${err.message}`); }
  };
}

async function boot() {
  // Auto-connect on startup
  try {
    await api("/api/connect", { method: "POST" });
  } catch (err) {
    console.warn("Initial auto-connect failed", err);
  }
  
  await refreshState();
  if (state.authenticated) {
    await loadContacts();
    await loadGroups();
    await loadInvites();
  }
}

// Initial Boot
wireActions();
boot();
startEventSocket();
