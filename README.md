# EconomyPlus

**EconomyPlus** is a highly configurable, multi-currency economy plugin designed for Hytale. It features robust player balance management, customizable taxation for player-to-player payments, and an integrated HUD for real-time balance tracking.

## Features

* **Multi-Currency Support**: Define an unlimited number of currencies with unique symbols, decimal places, and starting balances.
* **Persistent Storage**: Player data is stored in a structured JSON format with configurable auto-save intervals to prevent data loss.
* **Integrated HUD**: A real-time "Wallet" HUD that displays current balances for the primary currency or all currencies simultaneously.
* **Dynamic Tax System**: Apply customizable flat or percentage-based taxes to player-to-player payments with various rounding options.
* **Interactive Leaderboard**: An in-game UI for viewing top balances (`/baltop`) with support for pagination and player search.
* **Developer API**: A comprehensive `EconomyAPI` for external plugins to interact with player wallets, manage balances, and handle transactions.
* **VaultUnlocked Compatibility**: When VaultUnlocked is present, EconomyPlus registers as an economy provider so other mods can interact with EconomyPlus balances through the VaultUnlocked API.

## Installation

1. Place the `EconomyPlus` JAR file into your server's plugin directory.
2. Restart the server to generate the default configuration files.
3. Configure your currencies in `currencies.yml` and plugin settings in `config.yml`.

## Commands

| Command | Description | Permission Group |
| --- | --- | --- |
| `/bal [player] [currency]` | Check your balance or another player's balance. | Default |
| `/pay <player> <amount> [currency]` | Send money to another player (taxes may apply). | Default |
| `/baltop [currency] [page]` | View the richest players for a specific currency. | Adventure |
| `/wallet` | Toggles the visibility of the on-screen Wallet HUD. | Adventure |
| `/eco set <player> <amount> [currency]` | Sets a player's balance. | Admin |
| `/eco add <player> <amount> [currency]` | Adds currency to a player's balance. | Admin |
| `/eco remove <player> <amount> [currency]` | Removes currency from a player's balance. | Admin |
| `/eco reload` | Reloads all configuration files and messages. | Admin |

## Configuration

### `config.yml`

Handles general settings, including default currencies, tax logic, and storage frequency.

```yaml
defaults:
  primary-currency: "coins"
  starting-balances:
    coins: 100
tax:
  pay:
    percent: 0
    flat: 0
    rounding: "down"
storage:
  autosave-seconds: 30

```

### `currencies.yml`

Define your server's economic units.

```yaml
currencies:
  coins:
    name: "Coins"
    symbol: "¢"
    decimals: 0
  gems:
    name: "Gems"
    symbol: "♦"
    decimals: 0

```

## Developer API

Developers can use the `EconomyAPI` class to integrate with the economy system:

```java
// Get a player's balance
long balance = EconomyAPI.getBalance(playerUuid, "coins");

// Add balance to a player
TransactionResult result = EconomyAPI.addBalance(playerUuid, "coins", 500);

// Process a payment between players
PayResult payResult = EconomyAPI.pay(senderUuid, receiverUuid, "coins", 100);

```

## VaultUnlocked Integration

EconomyPlus includes a VaultUnlocked-compatible economy provider so mods that rely on VaultUnlocked can interact with EconomyPlus-managed balances.

### Installation

1. Download VaultUnlocked from CurseForge: https://www.curseforge.com/hytale/mods/vaultunlocked
2. Place the VaultUnlocked JAR into your server's mods directory (for local runs you can place it in `run/mods`).
3. Restart the server. EconomyPlus will automatically register as an economy provider with VaultUnlocked when both are present.

### Notes

* No extra configuration is required in EconomyPlus to enable VaultUnlocked support.
* If you run into issues, ensure VaultUnlocked is loaded and enabled before plugins that depend on an economy provider, then restart the server.

## Authors

* **Itamar Behar**
* **Omer Behar**

**Website**: [lucastudios.com](http://lucastudios.com)