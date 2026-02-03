# Reload Command Implementation

## Overview
Added `/eco reload` subcommand to allow administrators to reload all plugin configuration files without restarting the server.

## Files Created

### ReloadSubCommand.java
- Location: `src/main/java/com/lucastudios/EconomyPlus/commands/ReloadSubCommand.java`
- Extends `AbstractAsyncCommand`
- Calls `Main.reloadAll()` to reload all configurations
- Provides feedback messages to the admin:
  - Yellow: "Reloading EconomyPlus configuration..."
  - Green: "EconomyPlus configuration reloaded successfully!"
  - Gray: Shows list of loaded currencies
  - Red: Error message if reload fails

## Files Modified

### EcoCommand.java
- Added `addSubCommand(new ReloadSubCommand(plugin));` to register the reload subcommand

## Usage

```
/eco reload
```

This command will reload:
- `config.yml` - Core plugin settings (defaults, tax, HUD, baltop)
- `currencies.yml` - Currency definitions
- `messages.yml` - All player-facing messages
- `balances.json` - Wallet data (re-synced from disk)

## Notes

- The command is async and won't block the game thread
- All online players' wallets are updated after reload to ensure consistency
- The wallet HUD is already connected to the real economy system and will reflect changes automatically
- Errors are logged to both the command sender and the server log

## Permission

The command inherits permissions from the parent `/eco` command collection, requiring admin privileges.
