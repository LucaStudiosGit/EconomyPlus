# Wallet Auto-Save Implementation

## Changes Made

### Problem
Previously, when a new wallet was created for a player, it was only stored in memory. The wallet would only be persisted to the JSON file during the periodic autosave or when the server shut down. This meant that if the server crashed, new players could lose their starting balance.

### Solution
Implemented immediate JSON persistence when creating new wallets.

## Modified Files

### 1. InMemoryEconomyService.java

**Added fields:**
- `JsonWalletStore walletStore` - Reference to the wallet persistence layer
- `Path storagePath` - Path to the balances.json file

**Updated constructor:**
```java
public InMemoryEconomyService(
    CurrencyRegistry currencyRegistry, 
    PluginConfig config, 
    Map<UUID, Wallet> loadedWallets,
    JsonWalletStore walletStore,  // NEW
    Path storagePath              // NEW
)
```

**Updated `getOrCreateWallet` method:**
- Changed from using `computeIfAbsent` to explicit check and create
- Added immediate save after creating new wallet:
  ```java
  wallets.put(playerId, wallet);
  
  try {
      walletStore.save(storagePath, wallets);
      wallet.markClean();
  } catch (Exception e) {
      // Log error but don't fail - wallet is still in memory
  }
  ```

### 2. JsonWalletStore.java

**Updated `save` method:**
- Now loads existing wallets from file first
- Merges existing wallets with dirty wallets
- This ensures new wallets (marked as dirty during creation) are saved along with all existing wallets

**Benefits:**
- Prevents data loss if wallet creation happens between autosaves
- Maintains all existing wallet data
- Only writes dirty wallets + preserves clean wallets from file

### 3. Main.java

**Updated `reloadAll` method:**
```java
this.walletStore = new JsonWalletStore();
Path storagePath = dataDir.resolve(config.storage().file());  // Store path
Map<UUID, Wallet> loaded = walletStore.load(storagePath);
this.economy = new InMemoryEconomyService(
    currencyRegistry, 
    config, 
    loaded, 
    walletStore,   // Pass wallet store
    storagePath    // Pass storage path
);
```

## How It Works

### Creating a New Wallet

1. Player joins for the first time
2. `getOrCreateWallet()` is called
3. Wallet doesn't exist, so new wallet is created
4. Starting balances are applied based on config
5. Wallet is added to in-memory map
6. **Immediately saved to JSON file** ← NEW!
7. Wallet marked as clean

### JSON File Structure

```json
{
  "schema": 1,
  "players": {
    "player-uuid-here": {
      "lastKnownName": "PlayerName",
      "coins": 100,
      "gems": 0
    }
  }
}
```

### Periodic Saves

The existing autosave mechanism continues to work:
- Runs every X seconds (configured in config.yml)
- Saves all dirty wallets (wallets that have been modified)
- Preserves existing clean wallets from the file

## Benefits

✅ **Immediate Persistence** - New wallets saved immediately
✅ **Crash Safety** - No loss of starting balance if server crashes
✅ **Performance** - Still uses dirty-tracking for regular saves
✅ **Data Integrity** - Atomic writes with temp file + rename
✅ **Backwards Compatible** - Works with existing JSON files

## Testing

To test this implementation:

1. Start server
2. Join with a new player
3. Check `balances.json` file - should contain new player immediately
4. Player should have starting balances from config
5. Make transactions - changes saved during autosave
6. Create another new player - both players should be in JSON

## Configuration

Starting balances are configured in `config.yml`:

```yaml
defaults:
  starting-balances:
    coins: 100
    gems: 0
```

When a new wallet is created, these amounts are automatically applied and immediately saved.
