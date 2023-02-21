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

import com.github.kpgtb.ktools.manager.*;
import com.github.kpgtb.ktools.manager.command.parser.java.*;
import com.github.kpgtb.ktools.manager.command.parser.spigot.OfflinePlayerParser;
import com.github.kpgtb.ktools.manager.command.parser.spigot.PlayerParser;
import com.github.kpgtb.ktools.manager.command.parser.spigot.WorldParser;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.util.ToolsObjectWrapper;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
        ParamParserManager paramParserManager = new ParamParserManager();
        // JAVA TYPES
        paramParserManager.registerParser(Boolean.class, new BooleanParser());
        paramParserManager.registerParser(Byte.class, new ByteParser());
        paramParserManager.registerParser(Double.class, new DoubleParser());
        paramParserManager.registerParser(Float.class, new FloatParser());
        paramParserManager.registerParser(Integer.class, new IntegerParser());
        paramParserManager.registerParser(Long.class, new LongParser());
        paramParserManager.registerParser(Short.class, new ShortParser());
        paramParserManager.registerParser(String.class, new StringParser());
        // SPIGOT
        paramParserManager.registerParser(OfflinePlayer.class, new OfflinePlayerParser());
        paramParserManager.registerParser(Player.class, new PlayerParser());
        paramParserManager.registerParser(World.class, new WorldParser());
        debug.sendInfo(DebugType.START, "Loaded command param parsers.");

        debug.sendInfo(DebugType.START, "Loading tools object wrapper...");
        this.toolsObjectWrapper = new ToolsObjectWrapper(cacheManager,debug,globalLanguageManager,getConfig(),adventure,paramParserManager);
        debug.sendInfo(DebugType.START, "Loaded tools object wrapper.");

        debug.sendInfo(DebugType.START, "Loading commands...");
        CommandManager commandManager = new CommandManager(toolsObjectWrapper, getFile(), "ktools");
        commandManager.registerCommands("com.github.kpgtb.ktools.command");
        debug.sendInfo(DebugType.START, "Loaded commands.");

        debug.sendInfo(DebugType.START, "Enabled plugin in " + (System.currentTimeMillis() - startMillis) + "ms.");
    }

    @Override
    public void onDisable() {
        BukkitAudiences adventure = this.toolsObjectWrapper.getAdventure();
        if(adventure != null) {
            adventure.close();
        }
    }

    public ToolsObjectWrapper getToolsObjectWrapper() {
        return toolsObjectWrapper;
    }
}
