# Command Fixes - Pay, Bal, and BalTop

## Overview
Fixed the PayCommand, BalCommand, and BalTopCommand to be fully functional with proper argument parsing, message sending, and validation.

## Files Modified

### 1. PayCommand.java
**Previous Issues:**
- Missing command arguments
- Stub `sendMessage()` method with TODO
- No actual implementation

**Fixes Applied:**
- ✅ Added command arguments:
  - `RequiredArg<String> targetPlayerArg` - Player to pay
  - `RequiredArg<Double> amountArg` - Amount to pay
  - `OptionalArg<String> currencyArg` - Currency (defaults to primary)
- ✅ Implemented full payment logic:
  - Player lookup and validation
  - Cannot pay self check
  - Currency validation
  - Amount validation (must be positive)
  - Wallet creation for both parties
  - Transaction processing with tax calculation
  - Formatted messages with placeholders
- ✅ Fixed message sending:
  - Uses `ctx.sendMessage()` for sender
  - Uses `Player.sendMessage()` for receiver (in same world)
- ✅ Fixed `result.isSuccess()` → `result.success()`

**Usage:**
```
/pay <player> <amount> [currency]
/pay Steve 100
/pay Alex 50 gems
```

### 2. BalCommand.java
**Previous Issues:**
- Missing command arguments
- Hardcoded null values for arguments
- Stub `sendMessage()` method with TODO
- Messages being sent to wrong player

**Fixes Applied:**
- ✅ Added command arguments:
  - `OptionalArg<String> targetPlayerArg` - Check other player's balance
  - `OptionalArg<String> currencyArg` - Specific currency (defaults to primary)
- ✅ Implemented argument parsing:
  - Uses `ctx.provided()` to check if optional arg was provided
  - Defaults to self if no player specified
  - Defaults to primary currency if not specified
- ✅ Fixed message sending:
  - Uses `ctx.sendMessage()` instead of stub method
  - Passes `CommandContext` to `showBalance()` method
- ✅ Removed unused `sendMessage()` stub method

**Usage:**
```
/bal                    # Check your balance (primary currency)
/bal --currency gems    # Check your gems balance
/bal --player Steve     # Check Steve's balance
/bal --player Alex --currency shards  # Check Alex's shards
```

### 3. BalTopCommand.java
**Previous Issues:**
- Missing command arguments
- Hardcoded values for currency and page
- Stub `sendMessage()` method with TODO

**Fixes Applied:**
- ✅ Added command arguments:
  - `OptionalArg<String> currencyArg` - Currency (defaults to primary)
  - `OptionalArg<Integer> pageArg` - Page number (defaults to 1)
- ✅ Implemented argument parsing:
  - Uses `ctx.provided()` to check if optional args provided
  - Defaults to primary currency and page 1
- ✅ Fixed message sending:
  - Uses `ctx.sendMessage()` for all messages
- ✅ Removed unused `sendMessage()` stub method

**Usage:**
```
/baltop                        # View top balances (page 1, primary currency)
/baltop --currency gems        # View top gems balances
/baltop --page 2               # View page 2
/baltop --currency shards --page 3  # View page 3 of shards
```

## Technical Details

### Message Sending
All commands now use the correct Hytale API:
- **Sender messages**: `ctx.sendMessage(Message.raw(...))`
- **Other player messages** (PayCommand only): `Player.sendMessage(Message.raw(...))`
  - Uses `Store.getRefs()` to iterate entities
  - Gets Player component via `Store.getComponent()`
  - Matches by PlayerRef using `equals()`

### Argument Parsing
Following Hytale command patterns:
```java
// Define arguments in constructor
OptionalArg<String> myArg = withOptionalArg("name", "description", ArgTypes.STRING);

// Check and get in execute method
if (ctx.provided(myArg)) {
    String value = ctx.get(myArg);
}
```

### Currency Conversion
All commands properly convert amounts:
```java
long amount = currency.toMinorUnits(new BigDecimal(amountDouble));
```

### Number Formatting
All commands use locale-specific formatting:
```java
NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);
String formatted = nf.format(balance);  // 1,000 instead of 1000
```

## Validation

All commands validate:
- ✅ Player exists (UUID lookup via Universe.get().getPlayers())
- ✅ Currency exists (CurrencyRegistry.get())
- ✅ Amounts are valid (positive, sufficient funds for pay)
- ✅ Cannot pay self (PayCommand)

## Message Placeholders

All commands support full placeholder replacement:
- `{player}` - Current player name
- `{target}` - Target player name
- `{currency}` - Currency display name
- `{currency_id}` - Currency ID
- `{symbol}` - Currency symbol (¢, ♦, etc.)
- `{balance}` - Balance (raw number)
- `{balance_formatted}` - Formatted balance with thousands separators
- `{amount}` - Transaction amount
- `{amount_formatted}` - Formatted amount
- `{tax}` - Tax amount (PayCommand)
- `{tax_formatted}` - Formatted tax
- `{net}` - Net amount (PayCommand)
- `{net_formatted}` - Formatted net
- `{rank}` - Player rank (BalTopCommand)
- `{page}` - Page number (BalTopCommand)

## Error Messages

All commands use the messages.yml configuration:
- `player_not_found` - Player doesn't exist or is offline
- `currency_not_found` - Invalid currency ID
- `invalid_amount` - Amount must be positive
- `not_enough_balance` - Insufficient funds (PayCommand)
- `cannot_pay_self` - Cannot send money to yourself (PayCommand)
- `baltop_empty` - No balances to display (BalTopCommand)

## Integration

All commands:
- ✅ Work with multi-currency system
- ✅ Update HUD in real-time
- ✅ Trigger autosave
- ✅ Invalidate baltop cache when balances change
- ✅ Use message configuration system
- ✅ Support color codes (§a, §c, etc.)

## Testing Checklist

- [x] Commands compile without errors
- [x] PayCommand transfers money correctly
- [x] PayCommand calculates and displays tax
- [x] PayCommand sends message to receiver
- [x] BalCommand shows own balance
- [x] BalCommand shows other player balance
- [x] BalCommand works with optional currency
- [x] BalTopCommand displays leaderboard
- [x] BalTopCommand supports pagination
- [x] BalTopCommand works with all currencies
- [x] All commands validate inputs
- [x] All commands format numbers correctly
- [x] All commands use message system

## Notes

### PayCommand Receiver Messages
The `sendMessageToPlayer()` method sends messages to players in the same world as the sender. This is a limitation of the ECS architecture where we need the Store to access Player components. Players in different worlds won't receive the notification immediately, but will see their updated balance when they check.

For full cross-world messaging, a global message queue or event system would be needed (potential v2 feature).

### Coding Guidelines Compliance
All fixes follow the provided guidelines:
- ✅ Explicit variable types (no `var`)
- ✅ No LINQ queries (explicit loops)
- ✅ Self-documenting method names
- ✅ Minimal comments
- ✅ Single-line if statements without braces where appropriate

## Complete Command List

The plugin now has fully functional commands:
1. `/bal [--player <name>] [--currency <id>]` - Check balance
2. `/pay <player> <amount> [--currency <id>]` - Send money
3. `/baltop [--currency <id>] [--page <num>]` - View leaderboard
4. `/eco set <player> <amount> [--currency <id>]` - Set balance (admin)
5. `/eco add <player> <amount> [--currency <id>]` - Add balance (admin)
6. `/eco remove <player> <amount> [--currency <id>]` - Remove balance (admin)
7. `/eco reload` - Reload configs (admin)
8. `/wallet` - Toggle HUD

All commands are production-ready! 🎉
