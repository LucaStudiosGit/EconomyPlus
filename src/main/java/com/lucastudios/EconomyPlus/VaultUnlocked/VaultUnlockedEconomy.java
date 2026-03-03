package com.lucastudios.EconomyPlus.VaultUnlocked;
import com.lucastudios.EconomyPlus.Main;
import com.lucastudios.EconomyPlus.api.EconomyAPI;
import com.lucastudios.EconomyPlus.model.TransactionResult;
import com.lucastudios.EconomyPlus.model.Wallet;
import net.milkbowl.vault2.economy.AccountPermission;
import net.milkbowl.vault2.economy.Economy;
import net.milkbowl.vault2.economy.EconomyResponse;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class VaultUnlockedEconomy implements Economy {
    private Main plugin;

    public VaultUnlockedEconomy(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return plugin != null && plugin.economy != null;
    }

    
    @Override
    public String getName() {
        return "EconomyPlus (VaultUnlocked)";
    }

    @Override
    public boolean hasSharedAccountSupport() {
        plugin.getLogger().atInfo().log("Checking for shared account support: false");
        return false;
    }

    @Override
    public boolean hasMultiCurrencySupport() {
        plugin.getLogger().atInfo().log("Checking for multi-currency support: true");
        return true;
    }

    @Override
    public int fractionalDigits(String pluginName) {
        plugin.getLogger().atInfo().log("Economy Plus plugin has different fractional digits for different currencies. Returning the default currency configuration.");
        return plugin.currencies().get(plugin.config().defaults().primaryCurrency()).decimals();
    }

    
    @Override
    public String format( BigDecimal amount) {
        return plugin.currencies().get(plugin.config().defaults().primaryCurrency()).format(amount);
    }

    
    @Override
    public String format( String pluginName,  BigDecimal amount) {
        return plugin.currencies().get(plugin.config().defaults().primaryCurrency()).format(amount);
    }

    
    @Override
    public String format( BigDecimal amount,  String currency) {
        return plugin.currencies().get(currency).format(amount);
    }

    
    @Override
    public String format( String pluginName,  BigDecimal amount,  String currency) {
        return plugin.currencies().get(currency).format(amount);
    }

    @Override
    public boolean hasCurrency( String currency) {
        return plugin.currencies().exists(currency);
    }

    
    @Override
    public String getDefaultCurrency( String pluginName) {
        return plugin.config().defaults().primaryCurrency();
    }

    
    @Override
    public String defaultCurrencyNamePlural( String pluginName) {
        plugin.getLogger().atInfo().log("VaultUnlocked is requesting the default currency name plural. No plural name, Returning the primary currency name from the config.");
        return plugin.config().defaults().primaryCurrency();
    }

    
    @Override
    public String defaultCurrencyNameSingular( String pluginName) {
        return plugin.config().defaults().primaryCurrency();
    }

    @Override
    public  Collection<String> currencies() {
        return  plugin.currencies().keys();
    }

    @Override
    public boolean createAccount( UUID accountID,  String name) {
        plugin.getLogger().atInfo().log("Attempting to create account with UUID " + accountID + " and name " + name + ". This method is not supported by EconomyPlus, Accounts are created automatically on new player login returning false.");
        return false;
    }

    @Override
    public boolean createAccount( UUID accountID,  String name, boolean player) {
        plugin.getLogger().atInfo().log("Attempting to create account with UUID " + accountID + " and name " + name + ". This method is not supported by EconomyPlus, Accounts are created automatically on new player login returning false.");
        return false;
    }

    @Override
    public boolean createAccount( UUID accountID,  String name,  String worldName) {
        plugin.getLogger().atInfo().log("Attempting to create account with UUID " + accountID + " and name " + name + ". This method is not supported by EconomyPlus, Accounts are created automatically on new player login returning false.");
        return false;
    }

    @Override
    public boolean createAccount( UUID accountID,  String name,  String worldName, boolean player) {
        plugin.getLogger().atInfo().log("Attempting to create account with UUID " + accountID + " and name " + name + ". This method is not supported by EconomyPlus, Accounts are created automatically on new player login returning false.");
        return false;
    }

    @Override
    public  Map<UUID, String> getUUIDNameMap() {
        Map<UUID, String> map = new HashMap<>();
        for (Wallet wallet : plugin.economy.wallets().values()) {
            map.put(wallet.playerUuid(), wallet.lastKnownName());
        }
        return map;
    }

    @Override
    public Optional<String> getAccountName( UUID accountID) {
        return Optional.ofNullable(plugin.economy.wallets().get(accountID).lastKnownName());
    }

    @Override
    public boolean hasAccount( UUID accountID) {
        return plugin.economy.wallets().containsKey(accountID);
    }

    @Override
    public boolean hasAccount( UUID accountID,  String worldName) {
        return plugin.economy.wallets().containsKey(accountID);
    }

    @Override
    public boolean renameAccount( UUID accountID,  String name) {
        plugin.getLogger().atInfo().log("Attempting to rename account with UUID " + accountID + " to name " + name + ". This method is not supported by EconomyPlus, Account names are set to player name automatically on player login returning false.");
        return false;
    }

    @Override
    public boolean renameAccount( String pluginName,  UUID accountID,  String name) {
        plugin.getLogger().atInfo().log("Attempting to rename account with UUID " + accountID + " to name " + name + ". This method is not supported by EconomyPlus, Account names are set to player name automatically on player login returning false.");
        return false;
    }

    @Override
    public boolean deleteAccount( String pluginName,  UUID accountID) {
        plugin.getLogger().atInfo().log("Attempting to delete account with UUID " + accountID + ". This method is not supported by EconomyPlus, returning false.");
        return false;
    }

    @Override
    public boolean accountSupportsCurrency( String pluginName,  UUID accountID,  String currency) {
        return plugin.currencies().exists(currency);
    }

    @Override
    public boolean accountSupportsCurrency( String pluginName,  UUID accountID,  String currency,  String world) {
        return plugin.currencies().exists(currency);
    }

    
    @Override
    public BigDecimal getBalance( String pluginName,  UUID accountID) {
        return plugin.economy.wallets().get(accountID).getBalance(plugin.config().defaults().primaryCurrency());
    }

    
    @Override
    public BigDecimal getBalance( String pluginName,  UUID accountID,  String world) {
        return plugin.economy.wallets().get(accountID).getBalance(plugin.config().defaults().primaryCurrency());
    }

    
    @Override
    public BigDecimal getBalance( String pluginName,  UUID accountID,  String world,  String currency) {
        return plugin.economy.wallets().get(accountID).getBalance(currency);
    }

    @Override
    public boolean has( String pluginName,  UUID accountID,  BigDecimal amount) {
        return EconomyAPI.getBalance(accountID, plugin.config().defaults().primaryCurrency()).compareTo(amount) >= 0;
    }

    @Override
    public boolean has( String pluginName,  UUID accountID,  String worldName,  BigDecimal amount) {
        return EconomyAPI.getBalance(accountID, plugin.config().defaults().primaryCurrency()).compareTo(amount) >= 0;
    }

    @Override
    public boolean has( String pluginName,  UUID accountID,  String worldName,  String currency,  BigDecimal amount) {
        return EconomyAPI.getBalance(accountID, currency).compareTo(amount) >= 0;
    }

    @Override
    public  EconomyResponse withdraw( String pluginName,  UUID accountID,  BigDecimal amount) {
        return toEconomyResponse(EconomyAPI.withdraw(accountID, plugin.config().defaults().primaryCurrency(), amount));
    }

    
    @Override
    public EconomyResponse withdraw( String pluginName,  UUID accountID,  String worldName,  BigDecimal amount) {
        return toEconomyResponse(EconomyAPI.withdraw(accountID,plugin.config().defaults().primaryCurrency(), amount));
    }

    
    @Override
    public EconomyResponse withdraw( String pluginName,  UUID accountID,  String worldName,  String currency,  BigDecimal amount) {
        return toEconomyResponse(EconomyAPI.withdraw(accountID, currency, amount));
    }

    
    @Override
    public EconomyResponse deposit( String pluginName,  UUID accountID,  BigDecimal amount) {
        return toEconomyResponse(EconomyAPI.deposit(accountID,
                plugin.config().defaults().primaryCurrency(), amount));
    }

    
    @Override
    public EconomyResponse deposit( String pluginName,  UUID accountID,  String worldName,  BigDecimal amount) {
        return toEconomyResponse(EconomyAPI.deposit(accountID,
                plugin.config().defaults().primaryCurrency(), amount));
    }

    
    @Override
    public EconomyResponse deposit( String pluginName,  UUID accountID,  String worldName,  String currency,  BigDecimal amount) {
        return toEconomyResponse(EconomyAPI.deposit(accountID, currency, amount));
    }

    @Override
    public boolean createSharedAccount( String pluginName,  UUID accountID,  String name,  UUID owner) {
        plugin.getLogger().atInfo().log("Attempting to create shared account with UUID " + accountID + ", name "
                + name + " and owner " + owner + ". This method is not supported by EconomyPlus, returning false.");
        return false;
    }

    @Override
    public boolean isAccountOwner( String pluginName,  UUID accountID,  UUID uuid) {
        plugin.getLogger().atInfo().log("Checking if UUID " + uuid + " is owner of account with UUID " + accountID + ". This method is not supported by EconomyPlus, returning false.");
        return false;
    }

    @Override
    public boolean setOwner( String pluginName,  UUID accountID,  UUID uuid) {
        plugin.getLogger().atInfo().log("Attempting to set owner of account with UUID " + accountID + " to UUID " + uuid + ". This method is not supported by EconomyPlus, returning false.");
        return false;
    }

    @Override
    public boolean isAccountMember( String pluginName,  UUID accountID,  UUID uuid) {
        plugin.getLogger().atInfo().log("Checking if UUID " + uuid + " is member of account with UUID " + accountID + ". This method is not supported by EconomyPlus, returning false.");
        return false;
    }

    @Override
    public boolean addAccountMember( String pluginName,  UUID accountID,  UUID uuid) {
        plugin.getLogger().atInfo().log("Attempting to add UUID " + uuid + " as member of account with UUID " + accountID + ". This method is not supported by EconomyPlus, returning false.");
        return false;
    }

    @Override
    public boolean addAccountMember( String pluginName,  UUID accountID,  UUID uuid,  AccountPermission... initialPermissions) {
        plugin.getLogger().atInfo().log("Attempting to add UUID " + uuid + " as member of account with UUID " + accountID + " with permissions " + Arrays.toString(initialPermissions) + ". This method is not supported by EconomyPlus, returning false.");
        return false;
    }

    @Override
    public boolean removeAccountMember( String pluginName,  UUID accountID,  UUID uuid) {
        plugin.getLogger().atInfo().log("Attempting to remove UUID " + uuid + " as member of account with UUID " + accountID + ". This method is not supported by EconomyPlus, returning false.");
        return false;
    }

    @Override
    public boolean hasAccountPermission( String pluginName,  UUID accountID,  UUID uuid,  AccountPermission permission) {
        plugin.getLogger().atInfo().log("Checking if UUID " + uuid + " has permission " + permission + " on account with UUID " + accountID + ". This method is not supported by EconomyPlus, returning false.");
        return false;
    }

    @Override
    public boolean updateAccountPermission( String pluginName,  UUID accountID,  UUID uuid,  AccountPermission permission, boolean value) {
        plugin.getLogger().atInfo().log("Attempting to update permission " + permission + " for UUID " + uuid + " on account with UUID " + accountID + " to value " + value + ". This method is not supported by EconomyPlus, returning false.");
        return false;
    }

    @Override
    public EconomyResponse set( String pluginName,  UUID accountID,  BigDecimal amount) {
        return toEconomyResponse(EconomyAPI.setBalance(accountID, plugin.config().defaults().primaryCurrency(), amount));
    }

    @Override
    public EconomyResponse set( String pluginName,  UUID accountID,  String worldName,  BigDecimal amount) {
        return toEconomyResponse(EconomyAPI.setBalance(accountID, plugin.config().defaults().primaryCurrency(), amount));
    }

    @Override
    public EconomyResponse set( String pluginName,  UUID accountID,  String worldName,  String currency,  BigDecimal amount) {
        return toEconomyResponse(EconomyAPI.setBalance(accountID, currency, amount));
    }

    private EconomyResponse toEconomyResponse(TransactionResult result) {
        if (result.isSuccess()) {
            TransactionResult.Success success = (TransactionResult.Success) result;
            return new EconomyResponse(success.amount(), success.newBalance(), EconomyResponse.ResponseType.SUCCESS, "");
        } else {
            TransactionResult.Failure failure = (TransactionResult.Failure) result;
            EconomyResponse.ResponseType responseType;
            switch (failure.reason()) {
                case CURRENCY_NOT_FOUND -> responseType = EconomyResponse.ResponseType.FAILURE;
                case PLAYER_NOT_FOUND -> responseType = EconomyResponse.ResponseType.FAILURE;
                case INSUFFICIENT_FUNDS -> responseType = EconomyResponse.ResponseType.FAILURE;
                case INVALID_AMOUNT -> responseType = EconomyResponse.ResponseType.FAILURE;
                case CANNOT_PAY_SELF -> responseType = EconomyResponse.ResponseType.FAILURE;
                case FLAT_TAX_TOO_HIGH -> responseType = EconomyResponse.ResponseType.FAILURE;
                default -> responseType = EconomyResponse.ResponseType.FAILURE;
            }
            return new EconomyResponse(failure.amount(), failure.balance(), responseType, failure.message());
        }
    }
}
