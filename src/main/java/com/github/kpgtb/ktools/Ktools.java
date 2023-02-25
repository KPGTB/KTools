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

import com.github.kpgtb.ktools.manager.cache.CacheManager;
import com.github.kpgtb.ktools.manager.command.CommandManager;
import com.github.kpgtb.ktools.manager.command.parser.ParamParserManager;
import com.github.kpgtb.ktools.manager.data.DataManager;
import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.manager.language.LanguageManager;
import com.github.kpgtb.ktools.manager.listener.ListenerManager;
import com.github.kpgtb.ktools.manager.recipe.RecipeManager;
import com.github.kpgtb.ktools.util.ToolsObjectWrapper;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of plugin
 */
public final class Ktools extends JavaPlugin {

    private ToolsObjectWrapper toolsObjectWrapper;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        DebugManager debug = new DebugManager(getConfig(),getLogger());

        long startMillis = System.currentTimeMillis();
        debug.sendInfo(DebugType.START, "Enabling plugin...");

        debug.sendInfo(DebugType.START, "Loading audience...");
        BukkitAudiences adventure = BukkitAudiences.create(this);
        debug.sendInfo(DebugType.START, "Loaded audience.");

        debug.sendInfo(DebugType.START, "Loading language...");
        String lang = getConfig().getString("lang");
        if(lang == null) lang = "en";
        LanguageManager globalLanguageManager = new LanguageManager(getDataFolder(), lang, debug);
        globalLanguageManager.saveDefaultLanguage("lang/en.yml", this);
        globalLanguageManager.refreshMessages();
        debug.sendInfo(DebugType.START, "Loaded "+lang+" language.");

        debug.sendInfo(DebugType.START, "Loading cache...");
        CacheManager cacheManager = new CacheManager(getConfig(), getDataFolder(), debug);
        cacheManager.setupCacheFile();
        debug.sendInfo(DebugType.START, "Loaded cache.");

        debug.sendInfo(DebugType.START, "Loading command param parsers...");
        ParamParserManager paramParserManager = new ParamParserManager(debug);
        paramParserManager.registerParsers("com.github.kpgtb.ktools.manager.command.parser", getFile());
        debug.sendInfo(DebugType.START, "Loaded command param parsers.");

        debug.sendInfo(DebugType.START, "Loading database...");
        DataManager dataManager = new DataManager(debug,getConfig(),getDataFolder());
        dataManager.registerTables("com.github.kpgtb.ktools.database", getFile());
        debug.sendInfo(DebugType.START, "Loaded database.");

        debug.sendInfo(DebugType.START, "Loading tools object wrapper...");
        this.toolsObjectWrapper = new ToolsObjectWrapper(cacheManager,debug,globalLanguageManager,this,adventure,paramParserManager, dataManager);
        debug.sendInfo(DebugType.START, "Loaded tools object wrapper.");

        debug.sendInfo(DebugType.START, "Loading commands...");
        CommandManager commandManager = new CommandManager(toolsObjectWrapper, getFile(), "ktools");
        commandManager.registerCommands("com.github.kpgtb.ktools.command");
        debug.sendInfo(DebugType.START, "Loaded commands.");

        debug.sendInfo(DebugType.START, "Loading listeners...");
        ListenerManager listenerManager = new ListenerManager(toolsObjectWrapper, getFile());
        listenerManager.registerListeners("com.github.kpgtb.ktools.listener");
        debug.sendInfo(DebugType.START, "Loaded listeners.");

        debug.sendInfo(DebugType.START, "Loading recipes...");
        RecipeManager recipeManager = new RecipeManager(toolsObjectWrapper, getFile(), "ktools");
        recipeManager.registerRecipes("com.github.kpgtb.ktools.recipe");
        debug.sendInfo(DebugType.START, "Loaded recipes.");

        debug.sendInfo(DebugType.START, "Enabled plugin in " + (System.currentTimeMillis() - startMillis) + "ms.");
    }

    @Override
    public void onDisable() {
        BukkitAudiences adventure = this.toolsObjectWrapper.getAdventure();
        if(adventure != null) {
            adventure.close();
        }
        this.toolsObjectWrapper.getDataManager().close();
    }
}
