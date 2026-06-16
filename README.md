# MobHealth

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight Paper plugin that shows a **top-center HUD** with the health of the mob you're looking at.

---

## Features

| Feature | Description |
|---|---|
| **Top-center HUD** | Uses a boss bar at the top of the screen. |
| **Mob name** | Shows the mob's name or translated type (e.g., "Zombie"). |
| **Health** | Displays current health / max health (e.g., `10 / 20`). |
| **Health bar** | Boss-bar progress and color reflect remaining health. |
| **Per-player** | Each player sees their own target's health. |

---

## How It Works

The plugin ray-traces from the player's eyes every tick to find the living entity they're looking at within 6 blocks. When a mob is found:

1. A boss bar appears at the top center.
2. The title shows the mob name and health.
3. The bar fills and changes color based on health percentage.
4. The bar hides when looking at air, blocks, other players, or beyond range.

---

## Requirements

| Requirement | Version |
|---|---|
| Server software | [Paper](https://papermc.io/downloads/paper) |
| Minecraft / Paper API | 26.1 (Minecraft 1.21.x) |
| Java | 25 |

---

## Installation

1. Download the latest `MobHealth-*.jar` from the [Releases](../../releases) page.
2. Place the JAR in your server's `plugins/` directory.
3. Restart your server.
4. Look at any mob — the HUD appears automatically.

---

## Building from Source

```bash
git clone <repo-url>
cd MobHealth
mvn package
```

The compiled JAR will be at `target/MobHealth-1.0.0.jar`.

---

## License

This project is released under the [MIT License](LICENSE).
