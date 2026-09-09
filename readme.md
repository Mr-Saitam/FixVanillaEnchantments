# FixVanillaEnchantments

<img width="1600" height="500" alt="image" src="https://github.com/user-attachments/assets/ee173526-908e-4ad6-879e-586842e4222c" />


**Automatically fixes items with illegal enchantments — no lag, no manual commands, no drama.**

Sharpness X on a sword. Protection IV + Fire Protection IV on the same armor piece. Fortune and Silk Touch together on a pickaxe. If your server ever had an exploit, a misconfigured enchanting plugin, or items imported from another server, you've probably got items like this floating around — and they break the balance for everyone else.

FixVanillaEnchantments detects and corrects them on its own, the moment they appear: on join, when moved around in the inventory, when picked up off the ground, and even **inside shulker boxes and containers** — all designed to add zero extra load to the server.

<img width="1400" height="660" alt="image" src="https://github.com/user-attachments/assets/efe58c14-edfb-44df-b68a-f8fd49f9de71" />


## Features
<img width="1500" height="260" alt="image" src="https://github.com/user-attachments/assets/addcd7cf-f87b-426d-9bfc-0cbb42e83155" />


- **Configurable vanilla limits** per enchantment (Sharpness V, Protection IV, etc.)
- **Incompatibility detection** (Sharpness + Smite, Fortune + Silk Touch, Riptide + Loyalty...) with configurable priority rules
- **Removes invalid enchantments** for the item type (e.g. Aqua Affinity on a sword)
- **Checks shulker boxes** — contents are fixed along with the box itself, no world scanning
- **Checks chests, barrels, ender chests, dispensers and droppers** when they're closed
- **Automatic correction** on: join, inventory click, drag, picking up from the ground, hotbar switch, hand swap, inventory close
- **Manual command** `/fixmyitems` so players can fix their own inventory
- **Asynchronous file logging** — zero impact on the server's main thread
- **100% configurable**: toggle each trigger, each message, and each enchantment's limits independently

## Fully configurable language

Every piece of text the plugin shows or writes (chat messages, log lines, even debug messages) comes from `plugins/FixVanillaEnchantments/lang/<language>.yml`, never hardcoded in the code. Pick it with a single line in `config.yml`:

```yaml
language: es   # or "en"
```

Spanish (`es`) and English (`en`) are included out of the box. To add another language: copy `lang/es.yml` to `lang/<code>.yml`, translate it, and change `language:` — no need to recompile the plugin. If a translation is missing a key, the plugin automatically falls back to the embedded English default, so it never breaks over a missing message.

## Installation

1. Download the `.jar` and drop it into your Paper/Spigot server's `plugins/` folder.
2. Restart the server (or load it with your favorite plugin manager).
3. Edit `plugins/FixVanillaEnchantments/config.yml` to your liking.
4. Done — no external dependencies required.

**Requires:** Paper/Spigot 1.13 or higher · Java 8+

## Commands

| Command | Aliases | Permission | Description |
|---|---|---|---|
| `/fixmyitems` | `/fixme`, `/fixitems` | `fixvanillaenchants.use` (default: everyone) | Manually fixes the player's own inventory |
| `/fixenchants-reload` | `/fixench-reload`, `/fereload` | `fixvanillaenchants.reload` (default: op) | Reloads the configuration without restarting |

## Configuration

```yaml
settings:
  notify-players: true
  fix-on-join: true
  fix-on-inventory-move: true
  fix-on-pickup: true
  remove-invalid-enchants: true
  enable-file-logging: true
  fix-shulker-contents: true
  fix-containers: true
  debug-mode: false

  enchantment-priority:
    - sharpness
    - protection
    - power
    # ...

limits:
  sharpness: 5
  protection: 4
  # ... every vanilla enchantment

incompatible:
  protection: [fire_protection, blast_protection, projectile_protection]
  sharpness: [smite, bane_of_arthropods]
  # ...
```

Each trigger can be disabled individually if you prefer a lighter or a more aggressive setup.

## Performance

Explicitly designed for high-population servers:

- Logging is **asynchronous** (in-memory queue + periodic flush), never blocks the main thread.
- Shulker boxes are filtered by item type before their NBT is ever deserialized — zero cost for non-shulker items.
- Containers (chests, barrels, etc.) are only checked when **closed**, never through a hopper listener or a periodic world scan.
- Inventory handlers discard irrelevant events before scheduling any task.

## Building from source

```bash
git clone https://github.com/<your-username>/FixVanillaEnchantments.git
cd FixVanillaEnchantments
mvn clean package
```

The final `.jar` will be in `target/FixVanillaEnchantments-<version>.jar`.

## Contributing

Issues and PRs are welcome. If you find an item that slips through the fixer, open an issue with the item type and context (chest, shulker, villager trade, etc.).

## License

MIT — use it, modify it, redistribute it freely.
