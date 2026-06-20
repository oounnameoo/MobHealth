# MobHealth

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight Paper plugin that shows a **floating health label above every mob**.

---

## Features

| Feature | Description |
|---|---|
| **Floating health labels** | Each mob has a visible health tag above its head. |
| **Current / max health** | Shows remaining health and maximum health (e.g., `❤ 10 / 20`). |
| **Color-coded health** | Label is green → yellow → red based on remaining health percentage. |
| **Mobs only** | Players and non-living entities are ignored. |
| **Distance culling** | Labels are hidden when no player is within 32 blocks, reducing clutter. |
| **Armor-stand based** | Uses an invisible, marker armor stand so the label is smooth and collision-free. |
| **Automatic cleanup** | Labels are removed when a mob dies or the plugin disables. |
| **No configuration needed** | Works out of the box. |

---

## How It Works

Every tick the plugin scans all loaded living entities (excluding players). For each mob it creates or updates an invisible armor stand floating above the mob's head. The stand's name shows a heart icon plus the mob's current and maximum health, colored by how injured the mob is. The label hides automatically when no player is nearby and is removed when the mob dies.

---

## Usage

1. Install the plugin on your Paper server.
2. Spawn or find any mob.
3. A health label appears above the mob.

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
4. Look at any mob — its health appears above its head.

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
