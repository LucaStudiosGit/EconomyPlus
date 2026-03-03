# Changelog

## [1.0.1] - 2026-03-03

### Fixed
- Fixed a `StackOverflowError` crash when VaultUnlocked was present. `VaultUnlockedEconomy.isEnabled()` was delegating to `VaultUnlocked.economyObj().isEnabled()`, which iterated all registered economy providers and called `isEnabled()` on each — including EconomyPlus itself, causing infinite recursion. The method now checks `plugin != null && plugin.economy != null` directly.

## [1.0.0] - Initial Release

- Multi-currency support with configurable symbols, decimal places, and starting balances.
- Persistent JSON storage with configurable auto-save intervals.
- Integrated Wallet HUD for real-time balance display.
- Dynamic tax system (flat and percentage-based) for player-to-player payments.
- Interactive `/baltop` leaderboard with pagination and player search.
- Developer `EconomyAPI` for external plugin integration.
- VaultUnlocked compatibility layer for cross-plugin economy interoperability.
