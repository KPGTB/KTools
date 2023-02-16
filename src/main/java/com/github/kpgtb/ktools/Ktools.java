package com.github.kpgtb.ktools;

import com.github.kpgtb.ktools.manager.DebugManager;
import com.github.kpgtb.ktools.manager.LanguageManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

public final class Ktools extends JavaPlugin {

    private BukkitAudiences adventure;
    private DebugManager debug;
    private LanguageManager globalLanguageManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.debug = new DebugManager(getConfig(),getLogger());
        debug.sendInfo(DebugType.START, "Enabling plugin...");

        debug.sendInfo(DebugType.START, "Loading audience...");
        this.adventure = BukkitAudiences.create(this);
        debug.sendInfo(DebugType.START, "Loaded audience.");

        debug.sendInfo(DebugType.START, "Loading language...");
        String lang = getConfig().getString("lang");
        if(lang == null) lang = "en";
        this.globalLanguageManager = new LanguageManager(getDataFolder(), lang);
        debug.sendInfo(DebugType.START, "Loaded "+lang+" language.");
    }

    @Override
    public void onDisable() {
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    public DebugManager getDebug() {
        return debug;
    }

    public LanguageManager getGlobalLanguageManager() {
        return globalLanguageManager;
    }
}
