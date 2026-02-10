# EconomyPlus

**EconomyPlus** is a highly configurable, multi-currency economy plugin designed for Hytale. It features robust player balance management, customizable taxation for player-to-player payments, and an integrated HUD for real-time balance tracking.

## Features

* **Multi-Currency Support**: Define an unlimited number of currencies with unique symbols, decimal places, and starting balances.
* **Persistent Storage**: Player data is stored in a structured JSON format with configurable auto-save intervals to prevent data loss.
* **Integrated HUD**: A real-time "Wallet" HUD that displays current balances for the primary currency or all currencies simultaneously.
* **Dynamic Tax System**: Apply customizable flat or percentage-based taxes to player-to-player payments with various rounding options.
* **Interactive Leaderboard**: An in-game UI for viewing top balances (`/baltop`) with support for pagination and player search.
* **Developer API**: A comprehensive `EconomyAPI` for external plugins to interact with player wallets, manage balances, and handle transactions.

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

## Authors

* **Itamar Behar**
* **Omer Behar**

**Website**: [lucastudios.com](http://lucastudios.com)