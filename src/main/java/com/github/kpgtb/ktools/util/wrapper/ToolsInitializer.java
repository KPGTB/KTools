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

package com.github.kpgtb.ktools.util.wrapper;

import com.github.kpgtb.ktools.Ktools;
import com.github.kpgtb.ktools.manager.language.LanguageManager;
import com.github.kpgtb.ktools.util.file.PackageUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Class that handles plugin initialization
 * @since 2.0.0
 */
public class ToolsInitializer {
    private final JavaPlugin plugin;

    private final GlobalManagersWrapper globalManagersWrapper;
    private final BukkitAudiences adventure;
    private final PackageUtil packageUtil;

    private LanguageManager languageManager;

    /**
     * Constructor
     * @param plugin JavaPlugin instance
     */
    public ToolsInitializer(JavaPlugin plugin) {
        this.plugin = plugin;

        Ktools tools = (Ktools) Bukkit.getPluginManager().getPlugin("Ktools");
        this.globalManagersWrapper = tools.getGlobalManagersWrapper();
        this.adventure = BukkitAudiences.create(plugin);
        this.packageUtil = new PackageUtil(
                plugin.getClass().getPackage().getName(),
                plugin.getName().toLowerCase()
        );
    }

    /**
     * Prepare language manager
     * @param lang Lang key. It is a name of file with messages IN resources/lang
     * @return This initializer
     */
    public ToolsInitializer prepareLanguage(String lang) {
        this.languageManager = new LanguageManager(plugin.getDataFolder(), lang, globalManagersWrapper.getDebugManager(),globalManagersWrapper.getGlobalLanguageManager());
        this.languageManager.saveDefaultLanguage(String.format("lang/%s.yml", lang), plugin);
        this.languageManager.refreshMessages();
        return this;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public GlobalManagersWrapper getGlobalManagersWrapper() {
        return globalManagersWrapper;
    }

    public BukkitAudiences getAdventure() {
        return adventure;
    }

    public PackageUtil getPackageUtil() {
        return packageUtil;
    }

    public LanguageManager getLanguageManager() {
        return languageManager != null ? languageManager : globalManagersWrapper.getGlobalLanguageManager();
    }
}
