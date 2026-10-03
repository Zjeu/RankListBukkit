# RankList (Bukkit / Spigot / Paper)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Minecraft Versions](https://img.shields.io/badge/Minecraft-1.16%20--%201.21%2B-brightgreen.svg)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/)

A Bukkit / Spigot / Paper port of **[RankListVelocity](https://github.com/Zjeu/RankListVelocity)** by [Zjeu](https://github.com/Zjeu).

Customizes the `/list`, `/who`, and `/online` commands on standalone Minecraft servers using **LuckPerms** groups and prefixes, with optional **PlaceholderAPI** support and full Hex color formatting.

---

## 🌟 Features

- **Fully Customizable Player List**: Easily configure the message format, header, delimiter, and player layout.
- **LuckPerms Group Sorting**: Sorts online players hierarchically by their primary group defined in `config.yml`.
- **Prefix Support**: Automatically fetches prefixes from LuckPerms metadata with fallback support.
- **Player Limit & Overflow Counter**: Limits displayed players (`max-display`) and displays a customizable overflow tag (e.g. `(+X more)`).
- **Hex & Legacy Color Codes**: Supports standard color codes (`&a`, `&6`, etc.) and hex colors (`&#RRGGBB`).
- **PlaceholderAPI Integration**: If installed, Placeholders are parsed in player formats and headers.
- **In-Game Reload**: Update configuration on the fly using `/list reload`.
- **Command Aliases**: Works with `/list`, `/who`, and `/online`.

---

## 📋 Commands & Permissions

| Command | Aliases | Description | Permission | Default |
|---|---|---|---|---|
| `/list` | `/who`, `/online` | Displays the formatted list of online players | `ranklist.use` | Everyone (`true`) |
| `/list reload` | - | Reloads configuration from `config.yml` | `ranklist.reload` | OP (`op`) |

---

## ⚙️ Configuration (`config.yml`)

```yaml
# Configuration for RankList (Bukkit / Spigot / Paper)
# Forked and ported from RankListVelocity (https://github.com/Zjeu/RankListVelocity)

# The default prefix used when luckperms prefix is missing or null
default-prefix: "&7[Default]"

# The format for displaying the player list
# {header} will be replaced by the groups-header text
# {players} will be replaced by the player list
# {limit} will be replaced by max-players
# {online} will be replaced by online-players
list-format:
  - "{header}"
  - " "
  - "{players}"

# Formatting for individual players in the list
# {prefix} - the player's prefix
# {name} - the player's name
player-format: "{prefix} {name}"

# Delimiter between players
player-delimiter: "&8, "

# Format for when there are more than 'max-display' players
# {more} will be replaced by the number of extra players
more-format: "&7(+{more} more)"

# Maximum number of players to display before grouping them into 'more-format'
max-display: 30

# The header string to display the order of ranks
groups-header: "&cOwner&7, &4Manager&7, &cAdmin&7, &5Moderator&7, &9Helper&7, &aBuilder&7, &dPartner&7, &6Famous&7, &dMedia&7, &bACE&7, &bMVP&7, &bPRO&7, &bVIP&7, &7Default &8(&e{online}&8/&e{limit}&8)&8:"

# Sort order for ranks, from highest (first) to lowest (last)
# Players in groups not listed here will be sorted to the end
sort-order:
  - "owner"
  - "manager"
  - "admin"
  - "moderator"
  - "helper"
  - "builder"
  - "partner"
  - "famous"
  - "media"
  - "ace"
  - "mvp"
  - "pro"
  - "vip"
  - "default"
```

---

## 🔌 Dependencies

- **Server Software**: Paper, Purpur, Spigot, or CraftBukkit (1.16 - 1.21+)
- **Java**: Java 17 or higher
- **LuckPerms**: Required for rank and prefix resolution
- **PlaceholderAPI** *(Optional)*: Supported for custom placeholders

---

## 🛠️ Building from Source

To compile the plugin yourself:

```bash
git clone https://github.com/Zjeu/RankListBukkit.git
cd RankListBukkit
mvn clean package
```

The compiled jar file will be generated in `target/RankListBukkit-1.0.0.jar`.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
Original proxy concept from [RankListVelocity](https://github.com/Zjeu/RankListVelocity).
