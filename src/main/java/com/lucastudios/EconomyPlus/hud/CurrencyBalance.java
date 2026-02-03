package com.lucastudios.EconomyPlus.hud;

/**
 * displayName, what the player sees, example "Coins"
 * symbol, example "$" or "⛃"
 * amount, example 12500
 */
public record CurrencyBalance(String displayName, String symbol, long amount) {}
