/*
 *    Copyright 2023 KPG-TB
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.kpgtb.ktools;

import com.github.kpgtb.ktools.manager.CacheManager;
import com.github.kpgtb.ktools.manager.DebugManager;
import com.github.kpgtb.ktools.manager.LanguageManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

public final class Ktools extends JavaPlugin {

    private BukkitAudiences adventure;
    private DebugManager debug;
    private LanguageManager globalLanguageManager;
    private CacheManager cacheManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.debug = new DebugManager(getConfig(),getLogger());

        long startMillis = System.currentTimeMillis();
        debug.sendInfo(DebugType.START, "Enabling plugin...");

        debug.sendInfo(DebugType.START, "Loading audience...");
        this.adventure = BukkitAudiences.create(this);
        debug.sendInfo(DebugType.START, "Loaded audience.");

        debug.sendInfo(DebugType.START, "Loading language...");
        String lang = getConfig().getString("lang");
        if(lang == null) lang = "en";
        this.globalLanguageManager = new LanguageManager(getDataFolder(), lang, debug);
        this.globalLanguageManager.saveDefaultLanguage("lang/en.yml", this);
        this.globalLanguageManager.refreshMessages();
        debug.sendInfo(DebugType.START, "Loaded "+lang+" language.");

        debug.sendInfo(DebugType.START, "Loading cache...");
        this.cacheManager = new CacheManager(getConfig(), getDataFolder(), debug);
        this.cacheManager.setupCacheFile();
        debug.sendInfo(DebugType.START, "Loaded cache.");

        debug.sendInfo(DebugType.START, "Enabled plugin in " + (System.currentTimeMillis() - startMillis) + "ms.");
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

    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
