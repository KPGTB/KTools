package com.github.kpgtb.ktools.manager.ui.bar;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BarPlaceholders extends PlaceholderExpansion {
    private final BarManager barManager;

    public BarPlaceholders(BarManager barManager) {
        this.barManager = barManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "kbar";
    }

    @Override
    public @NotNull String getAuthor() {
        return "KPG-TB";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] values = params.split("_",2);
        String barName = values[0];
        OfflinePlayer target = player;
        if(values.length>1) {
            target = Bukkit.getOfflinePlayer(values[1]);
        }

        KBar bar = barManager.getBar(barName);
        if(bar == null) {
            return null;
        }

        return barManager.getIconsForPosition(3,bar,target);
    }
}
