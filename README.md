# Pixelmon TPA Brand

<p align="center">
	A clean, modern teleport request system for NeoForge servers.<br>
	Fast requests • Click-to-accept UI • Anti-spam flow
</p>

<p align="center">
	<img src="https://img.shields.io/badge/Minecraft-1.21.1-3C8527?style=for-the-badge" alt="Minecraft 1.21.1" />
	<img src="https://img.shields.io/badge/NeoForge-21.1.117-5B3FD0?style=for-the-badge" alt="NeoForge 21.1.117" />
	<img src="https://img.shields.io/badge/Java-21-F89820?style=for-the-badge" alt="Java 21" />
	<img src="https://img.shields.io/badge/License-MIT-2EA043?style=for-the-badge" alt="MIT License" />
</p>

---

## ✨ Features

- `/tpa <player>` — request teleporting to another player
- `/tpahere <player>` — request that player teleport to you
- `/tpaccept` / `/tpdeny` + aliases under `/tpa`
- `/tpa toggle` to opt in/out of incoming requests
- Configurable delays via server config file
- Clickable in-chat actions: **[ACCEPT] [DENY]**
- Request protections:
	- **5s** sender cooldown
	- **60s** request timeout

---

## 🧭 Command Reference

| Command | Description |
|---|---|
| `/tpa <player>` | Ask to teleport to a player |
| `/tpahere <player>` | Ask a player to teleport to you |
| `/tpaccept` | Accept your pending request |
| `/tpdeny` | Deny your pending request |
| `/tpa accept` | Alias of `/tpaccept` |
| `/tpa deny` | Alias of `/tpdeny` |
| `/tpa toggle` | Enable/disable incoming TPA requests |
| `/tpa reload` | Reload server data/config flow (admin only) |
| `/tpa help` | Show command help |

---

## ⚙️ Compatibility

| Requirement | Version |
|---|---|
| Minecraft | `1.21.1` |
| NeoForge | `21.1.117` |
| Java | `21` |

---

## 🔧 Delay Configuration

Both delays are configurable in the server config file:

- `world/serverconfig/mistixtpa-server.toml`

Settings:

```toml
[tpa]
# How long a request stays valid (seconds)
requestTimeoutSeconds = 60

# Delay between sending requests (seconds)
commandCooldownSeconds = 5
```

After editing, run `/tpa reload` (or restart the server).

---

## 🛠 Build from Source

### Windows (PowerShell)

```powershell
.\gradlew.bat clean build
```

### Linux/macOS

```bash
./gradlew clean build
```

Output jar:

- `build/libs/mistixtpa-1.0.0.jar`

---

##  Contributing

Issues and PRs are welcome. Keep changes focused and tested for NeoForge `1.21.1`.

---

##  License

MIT — see `LICENSE`.

---

##  Credits

- Author: **Mistix**
