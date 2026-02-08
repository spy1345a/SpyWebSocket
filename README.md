# 📡 SpyWebSocket

SpyWebSocket is a lightweight **Paper** plugin that exposes a secure **WebSocket API** for remotely interacting with a Minecraft server.

It allows external programs (such as Python scripts, bots, or automation tools) to authenticate, execute console commands, and retrieve live player data in real time using structured JSON messages.

> ⚠️ **Warning:**  
> This plugin allows remote console command execution.  
> Only use it on trusted servers and networks.

---

## ✨ Features

- 🔐 Authentication required before any action
- 📡 Built-in WebSocket server (no external services needed)
- 🧵 Thread-safe command execution on the main server thread
- 📍 Live player coordinate retrieval
- ⚙️ Configurable host, port, username, and password
- 🧩 Simple JSON-based protocol
- 📦 Shaded JAR (no runtime dependencies required)

---

## 📌 Compatibility

- **Platform:** Paper
- **Tested versions:** 1.21+
- **Java version:** 21
- **Also works on:** Spigot (untested)
- **Not compatible with:** Sponge, Fabric, Forge

---

## 📂 Installation

1. Download the plugin JAR
2. Place it into your server’s `plugins/` folder
3. Start the server once to generate the config
4. Edit `plugins/SpyWebSocket/config.yml`
5. Restart the server

---

## ⚙️ Configuration (`config.yml`)

```yaml
websocket:
  host: 127.0.0.1
  port: 8765

  username: admin
  password: strongpassword
  ```
**Configuration Notes:**
Use 127.0.0.1 to restrict access to the local machine (recommended)

Do not expose the port publicly unless properly secured

Use a strong password

## 🔐 Authentication
**Clients must authenticate before using any other action.**

## Request
```json
{
  "type": "auth",
  "username": "admin",
  "password": "strongpassword"
}
```
---
## Success Response
```json
{
  "type": "auth_ok"
}
```
---
## Failure Response
```json
{
  "type": "auth_fail"
}
```
---
## 🧾 Execute Console Commands
Executes a command as the server console.

Request
json
Copy code
{
  "type": "command",
  "command": "say Hello from WebSocket"
}
Commands are executed safely on the Minecraft main thread.

## 📍 Get Online Player Coordinates
Returns position and world data for all online players.

## Request
```json

{
  "type": "get_players"
}
```
## Response
```json
{
  "type": "players",
  "players": {
    "Steve": {
      "x": 120.5,
      "y": 64.0,
      "z": -32.1,
      "world": "world"
    }
  }
}
```
# ❌ Error Responses
**If a request is invalid or unauthorized:**
```json
{
  "type": "error",
  "reason": "not_authenticated"
}
```

## Common error reasons:

invalid_json

missing_type

unknown_type

not_authenticated

## 🧠 Use Cases
Python-controlled Minecraft automation

Admin dashboards or control panels

Server monitoring tools

Twitch / chat integrations

External overlays or bots

## 🛡️ Security Considerations
This plugin is intended for trusted environments

Keep the WebSocket bound to 127.0.0.1

Use firewalls when necessary

Do not expose this plugin to the public internet

## 📜 License
MIT License (or replace with your preferred license)

## ❤️ Contributing
Pull requests, issues, and feature ideas are welcome.

## ⚠️ Disclaimer
The author is not responsible for misuse of this plugin.
Use responsibly.







