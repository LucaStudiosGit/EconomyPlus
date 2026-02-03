# Add and Remove Subcommands Implementation

## Overview
Added `/eco add` and `/eco remove` subcommands to allow administrators to add or remove currency from player balances.

## Files Created

### AddSubCommand.java
- Location: `src/main/java/com/lucastudios/EconomyPlus/commands/AddSubCommand.java`
- Extends `AbstractAsyncCommand`
- Adds a specified amount to a player's balance
- Validates:
  - Player exists
  - Currency exists
  - Amount is positive
- Provides color-coded feedback with formatted numbers

### RemoveSubCommand.java
- Location: `src/main/java/com/lucastudios/EconomyPlus/commands/RemoveSubCommand.java`
- Extends `AbstractAsyncCommand`
- Removes a specified amount from a player's balance
- Validates:
  - Player exists
  - Currency exists
  - Amount is positive
  - Player has sufficient funds
- Provides color-coded feedback with formatted numbers

## Files Modified

### EcoCommand.java
- Added `addSubCommand(new AddSubCommand(plugin));`
- Added `addSubCommand(new RemoveSubCommand(plugin));`

## Usage

### Add Currency
```
/eco add <player> <amount> [currency]
```

**Examples:**
- `/eco add Steve 100` - Adds 100 of the primary currency (coins) to Steve
- `/eco add Alex 50 gems` - Adds 50 gems to Alex
- `/eco add Notch 1000 shards` - Adds 1000 shards to Notch

### Remove Currency
```
/eco remove <player> <amount> [currency]
```

**Examples:**
- `/eco remove Steve 100` - Removes 100 of the primary currency (coins) from Steve
- `/eco remove Alex 50 gems` - Removes 50 gems from Alex
- `/eco remove Notch 1000 shards` - Removes 1000 shards from Notch

## Features

### Both Commands Support:
- **Optional Currency Parameter**: Defaults to primary currency if not specified
- **Automatic Wallet Creation**: Creates wallet if player doesn't have one
- **Currency Conversion**: Properly handles decimal places using `Currency.toMinorUnits()`
- **Number Formatting**: Uses locale-specific thousands separators (e.g., 1,000)
- **Color-Coded Messages**:
  - Green (§a): Success messages
  - Red (§c): Error messages
  - Yellow (§e): Warning messages

### Add Command Specific:
- Validates amount is positive
- No upper limit on additions (admin discretion)
- Success message format: "Added ¢1,000 Coins to Steve's balance. New balance: ¢2,500"

### Remove Command Specific:
- Validates amount is positive
- Checks for insufficient funds
- Success message format: "Removed ¢500 Coins from Steve's balance. New balance: ¢1,500"
- Error includes current balance if insufficient funds

## Error Handling

Both commands handle the following errors gracefully:
- **Player not found**: "§cPlayer not found."
- **Currency not found**: "§cCurrency not found: [currency_id]"
- **Invalid amount**: "§cAmount must be positive."
- **Insufficient funds** (remove only): "§cInsufficient funds. Player only has [balance] [currency]"

## Technical Details

### Command Architecture
- Both extend `AbstractAsyncCommand` for non-blocking execution
- Use `RequiredArg<String>` for player name
- Use `RequiredArg<Double>` for amount
- Use `OptionalArg<String>` for currency ID
- Leverage `EconomyAPI` for balance operations

### Integration
- Automatically triggers wallet autosave
- Updates baltop cache
- Wallet HUD updates in real-time
- Thread-safe operations

### Data Flow
1. Parse command arguments
2. Resolve player UUID using `Utility.GetUuidByPlayerName()`
3. Validate currency exists in `CurrencyRegistry`
4. Get or create player wallet
5. Convert amount to minor units using currency decimals
6. Call `EconomyAPI.addBalance()` or `EconomyAPI.takeBalance()`
7. Handle result and display formatted message

## Permission

Both commands inherit permissions from the parent `/eco` command collection, requiring admin privileges.

## Complete /eco Command Suite

With these additions, the `/eco` command now has four subcommands:
1. `/eco set <player> <amount> [currency]` - Set exact balance
2. `/eco add <player> <amount> [currency]` - Add to balance
3. `/eco remove <player> <amount> [currency]` - Remove from balance
4. `/eco reload` - Reload all configuration files
