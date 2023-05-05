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

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.github.kpgtb.ktools.manager.cache.CacheManager;
import com.github.kpgtb.ktools.manager.command.CommandManager;
import com.github.kpgtb.ktools.manager.command.parser.ParamParserManager;
import com.github.kpgtb.ktools.manager.data.DataManager;
import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.manager.item.ItemManager;
import com.github.kpgtb.ktools.manager.item.builder.KitemBuilder;
import com.github.kpgtb.ktools.manager.language.LanguageManager;
import com.github.kpgtb.ktools.manager.listener.ListenerManager;
import com.github.kpgtb.ktools.manager.resourcepack.ResourcepackManager;
import com.github.kpgtb.ktools.manager.ui.UiManager;
import com.github.kpgtb.ktools.manager.updater.SpigotUpdater;
import com.github.kpgtb.ktools.manager.updater.UpdaterManager;
import com.github.kpgtb.ktools.manager.updater.version.KVersion;
import com.github.kpgtb.ktools.util.file.PackageUtil;
import com.github.kpgtb.ktools.util.item.ItemBuilder;
import com.github.kpgtb.ktools.util.wrapper.GlobalManagersWrapper;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Main class of plugin
 */
public final class Ktools extends JavaPlugin {

    private ToolsObjectWrapper toolsObjectWrapper;
    private GlobalManagersWrapper globalManagersWrapper;
    private boolean legacy;

    public static boolean HAS_UPDATE;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        DebugManager debug = new DebugManager(getConfig(),getLogger());

        long startMillis = System.currentTimeMillis();
        debug.sendInfo(DebugType.START, "Enabling plugin...");

        legacy = !new KVersion(
                Bukkit.getBukkitVersion()
                .split("-")[0]
        ).isNewerOrEquals("1.14");
        if(legacy) {
            debug.sendWarning(DebugType.START, "You are using legacy version! Not everything can be available!", true);
        }

        debug.sendInfo(DebugType.START, "Loading packages...");
        PackageUtil packageUtil = new PackageUtil("com.github.kpgtb.ktools", "ktools");
        debug.sendInfo(DebugType.START, "Loaded packages.");

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

        CacheManager cacheManager = null;
        if(!legacy) {
            debug.sendInfo(DebugType.START, "Loading cache...");
            cacheManager = new CacheManager(getConfig(), getDataFolder(), debug);
            cacheManager.setupCacheFile();
            debug.sendInfo(DebugType.START, "Loaded cache.");
        }

        debug.sendInfo(DebugType.START, "Loading command param parsers...");
        ParamParserManager paramParserManager = new ParamParserManager(debug);
        paramParserManager.registerParsers(packageUtil.get("manager.command.parser"), getFile());
        debug.sendInfo(DebugType.START, "Loaded command param parsers.");

        debug.sendInfo(DebugType.START, "Loading database...");
        DataManager dataManager = new DataManager(debug,getConfig(),getDataFolder(), this);
        dataManager.registerPersisters(packageUtil.get("manager.data.persister"), getFile());
        debug.sendInfo(DebugType.START, "Loaded database.");

        UiManager uiManager = null;
        if(Bukkit.getPluginManager().getPlugin("ProtocolLib") != null && !legacy) {
            debug.sendInfo(DebugType.START, "Loading ui...");
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            uiManager = new UiManager(this,protocolManager, getTextResource("spaces.json"));
            debug.sendInfo(DebugType.START, "Loaded ui.");
        }

        ResourcepackManager resourcepackManager = null;
        if(!legacy) {
            debug.sendInfo(DebugType.START, "Loading resourcepack...");
            resourcepackManager = new ResourcepackManager(this, debug, cacheManager);
            ResourcepackManager finalResourcepackManager = resourcepackManager;
            UiManager finalUiManager = uiManager;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!legacy && finalResourcepackManager.isEnabled()) {
                        finalResourcepackManager.registerPlugin(packageUtil.tag(), getDescription().getVersion());

                        if(finalUiManager != null && finalUiManager.isRequired()) {
                            //NegativeSpaces
                            finalResourcepackManager.registerCustomChar("space", "\uF801", "space_split", getResource("txt/space_split.png"), -3, -32768, -1);
                            finalResourcepackManager.registerCustomChar("space", "\uF802", "space_split", getResource("txt/space_split.png"), -4, -32768, -2);
                            finalResourcepackManager.registerCustomChar("space", "\uF803", "space_split", getResource("txt/space_split.png"), -5, -32768, -3);
                            finalResourcepackManager.registerCustomChar("space", "\uF804", "space_split", getResource("txt/space_split.png"), -6, -32768, -4);
                            finalResourcepackManager.registerCustomChar("space", "\uF805", "space_split", getResource("txt/space_split.png"), -7, -32768, -5);
                            finalResourcepackManager.registerCustomChar("space", "\uF806", "space_split", getResource("txt/space_split.png"), -8, -32768, -6);
                            finalResourcepackManager.registerCustomChar("space", "\uF807", "space_split", getResource("txt/space_split.png"), -9, -32768, -7);
                            finalResourcepackManager.registerCustomChar("space", "\uF808", "space_split", getResource("txt/space_split.png"), -10, -32768, -8);
                            finalResourcepackManager.registerCustomChar("space", "\uF809", "space_split", getResource("txt/space_split.png"), -18, -32768, -16);
                            finalResourcepackManager.registerCustomChar("space", "\uF80A", "space_split", getResource("txt/space_split.png"), -34, -32768, -32);
                            finalResourcepackManager.registerCustomChar("space", "\uF80B", "space_split", getResource("txt/space_split.png"), -66, -32768, -64);
                            finalResourcepackManager.registerCustomChar("space", "\uF80C", "space_split", getResource("txt/space_split.png"), -130, -32768, -128);
                            finalResourcepackManager.registerCustomChar("space", "\uF80D", "space_split", getResource("txt/space_split.png"), -258, -32768, -256);
                            finalResourcepackManager.registerCustomChar("space", "\uF80E", "space_split", getResource("txt/space_split.png"), -514, -32768, -512);
                            finalResourcepackManager.registerCustomChar("space", "\uF80F", "space_split", getResource("txt/space_split.png"), -1026, -32768, -1024);

                            finalResourcepackManager.registerCustomChar("space", "\uF811", "space_nosplit", getResource("txt/space_nosplit.png"), -3, -32768, -1);
                            finalResourcepackManager.registerCustomChar("space", "\uF812", "space_nosplit", getResource("txt/space_nosplit.png"), -4, -32768, -2);
                            finalResourcepackManager.registerCustomChar("space", "\uF813", "space_nosplit", getResource("txt/space_nosplit.png"), -5, -32768, -3);
                            finalResourcepackManager.registerCustomChar("space", "\uF814", "space_nosplit", getResource("txt/space_nosplit.png"), -6, -32768, -4);
                            finalResourcepackManager.registerCustomChar("space", "\uF815", "space_nosplit", getResource("txt/space_nosplit.png"), -7, -32768, -5);
                            finalResourcepackManager.registerCustomChar("space", "\uF816", "space_nosplit", getResource("txt/space_nosplit.png"), -8, -32768, -6);
                            finalResourcepackManager.registerCustomChar("space", "\uF817", "space_nosplit", getResource("txt/space_nosplit.png"), -9, -32768, -7);
                            finalResourcepackManager.registerCustomChar("space", "\uF818", "space_nosplit", getResource("txt/space_nosplit.png"), -10, -32768, -8);
                            finalResourcepackManager.registerCustomChar("space", "\uF819", "space_nosplit", getResource("txt/space_nosplit.png"), -18, -32768, -16);
                            finalResourcepackManager.registerCustomChar("space", "\uF81A", "space_nosplit", getResource("txt/space_nosplit.png"), -34, -32768, -32);
                            finalResourcepackManager.registerCustomChar("space", "\uF81B", "space_nosplit", getResource("txt/space_nosplit.png"), -66, -32768, -64);
                            finalResourcepackManager.registerCustomChar("space", "\uF81C", "space_nosplit", getResource("txt/space_nosplit.png"), -130, -32768, -128);
                            finalResourcepackManager.registerCustomChar("space", "\uF81D", "space_nosplit", getResource("txt/space_nosplit.png"), -258, -32768, -256);
                            finalResourcepackManager.registerCustomChar("space", "\uF81E", "space_nosplit", getResource("txt/space_nosplit.png"), -514, -32768, -512);
                            finalResourcepackManager.registerCustomChar("space", "\uF81F", "space_nosplit", getResource("txt/space_nosplit.png"), -1026, -32768, -1024);

                            finalResourcepackManager.registerCustomChar("space", "\uF821", "space_split", getResource("txt/space_split.png"), 0, -32768, 1);
                            finalResourcepackManager.registerCustomChar("space", "\uF822", "space_split", getResource("txt/space_split.png"), 1, -32768, 2);
                            finalResourcepackManager.registerCustomChar("space", "\uF823", "space_split", getResource("txt/space_split.png"), 2, -32768, 3);
                            finalResourcepackManager.registerCustomChar("space", "\uF824", "space_split", getResource("txt/space_split.png"), 3, -32768, 4);
                            finalResourcepackManager.registerCustomChar("space", "\uF825", "space_split", getResource("txt/space_split.png"), 4, -32768, 5);
                            finalResourcepackManager.registerCustomChar("space", "\uF826", "space_split", getResource("txt/space_split.png"), 5, -32768, 6);
                            finalResourcepackManager.registerCustomChar("space", "\uF827", "space_split", getResource("txt/space_split.png"), 6, -32768, 7);
                            finalResourcepackManager.registerCustomChar("space", "\uF828", "space_split", getResource("txt/space_split.png"), 7, -32768, 8);
                            finalResourcepackManager.registerCustomChar("space", "\uF829", "space_split", getResource("txt/space_split.png"), 15, -32768, 16);
                            finalResourcepackManager.registerCustomChar("space", "\uF82A", "space_split", getResource("txt/space_split.png"), 31, -32768, 32);
                            finalResourcepackManager.registerCustomChar("space", "\uF82B", "space_split", getResource("txt/space_split.png"), 63, -32768, 64);
                            finalResourcepackManager.registerCustomChar("space", "\uF82C", "space_split", getResource("txt/space_split.png"), 127, -32768, 128);
                            finalResourcepackManager.registerCustomChar("space", "\uF82D", "space_split", getResource("txt/space_split.png"), 255, -32768, 256);
                            finalResourcepackManager.registerCustomChar("space", "\uF82E", "space_split", getResource("txt/space_split.png"), 511, -32768, 512);
                            finalResourcepackManager.registerCustomChar("space", "\uF82F", "space_split", getResource("txt/space_split.png"), 1023, -32768, 1024);

                            finalResourcepackManager.registerCustomChar("space", "\uF831", "space_nosplit", getResource("txt/space_nosplit.png"), 0, -32768, 1);
                            finalResourcepackManager.registerCustomChar("space", "\uF832", "space_nosplit", getResource("txt/space_nosplit.png"), 1, -32768, 2);
                            finalResourcepackManager.registerCustomChar("space", "\uF833", "space_nosplit", getResource("txt/space_nosplit.png"), 2, -32768, 3);
                            finalResourcepackManager.registerCustomChar("space", "\uF834", "space_nosplit", getResource("txt/space_nosplit.png"), 3, -32768, 4);
                            finalResourcepackManager.registerCustomChar("space", "\uF835", "space_nosplit", getResource("txt/space_nosplit.png"), 4, -32768, 5);
                            finalResourcepackManager.registerCustomChar("space", "\uF836", "space_nosplit", getResource("txt/space_nosplit.png"), 5, -32768, 6);
                            finalResourcepackManager.registerCustomChar("space", "\uF837", "space_nosplit", getResource("txt/space_nosplit.png"), 6, -32768, 7);
                            finalResourcepackManager.registerCustomChar("space", "\uF838", "space_nosplit", getResource("txt/space_nosplit.png"), 7, -32768, 8);
                            finalResourcepackManager.registerCustomChar("space", "\uF839", "space_nosplit", getResource("txt/space_nosplit.png"), 15, -32768, 16);
                            finalResourcepackManager.registerCustomChar("space", "\uF83A", "space_nosplit", getResource("txt/space_nosplit.png"), 31, -32768, 32);
                            finalResourcepackManager.registerCustomChar("space", "\uF83B", "space_nosplit", getResource("txt/space_nosplit.png"), 63, -32768, 64);
                            finalResourcepackManager.registerCustomChar("space", "\uF83C", "space_nosplit", getResource("txt/space_nosplit.png"), 127, -32768, 128);
                            finalResourcepackManager.registerCustomChar("space", "\uF83D", "space_nosplit", getResource("txt/space_nosplit.png"), 255, -32768, 256);
                            finalResourcepackManager.registerCustomChar("space", "\uF83E", "space_nosplit", getResource("txt/space_nosplit.png"), 511, -32768, 512);
                            finalResourcepackManager.registerCustomChar("space", "\uF83F", "space_nosplit", getResource("txt/space_nosplit.png"), 1023, -32768, 1024);
                        }

                        finalResourcepackManager.prepareResourcepack();
                    }
                }
            }.runTaskLater(this, 1L);
            debug.sendInfo(DebugType.START, "Loaded resourcepack.");
        }

        debug.sendInfo(DebugType.START, "Loading items...");
        ItemManager itemManager = new ItemManager(debug,this);
        debug.sendInfo(DebugType.START, "Loaded items.");

        debug.sendInfo(DebugType.START, "Loading tools object wrapper...");
        this.toolsObjectWrapper = new ToolsObjectWrapper(cacheManager,debug,globalLanguageManager,this,adventure,paramParserManager, dataManager, resourcepackManager, uiManager, itemManager, legacy, packageUtil);
        debug.sendInfo(DebugType.START, "Loaded tools object wrapper.");

        debug.sendInfo(DebugType.START, "Loading commands...");
        CommandManager commandManager = new CommandManager(toolsObjectWrapper, getFile(), packageUtil.tag());
        commandManager.registerCommands(packageUtil.get("command"));
        debug.sendInfo(DebugType.START, "Loaded commands.");

        debug.sendInfo(DebugType.START, "Loading listeners...");
        ListenerManager listenerManager = new ListenerManager(toolsObjectWrapper, getFile());
        listenerManager.registerListeners(packageUtil.get("listener"));
        debug.sendInfo(DebugType.START, "Loaded listeners.");

        debug.sendInfo(DebugType.START, "Loading global managers wrapper...");
        this.globalManagersWrapper = new GlobalManagersWrapper(debug, globalLanguageManager,cacheManager,paramParserManager,dataManager,uiManager,resourcepackManager, itemManager, legacy);
        debug.sendInfo(DebugType.START, "Loaded global managers wrapper.");

        debug.sendInfo(DebugType.START, "Checking updates...");
        UpdaterManager updaterManager = new UpdaterManager(getDescription(), new SpigotUpdater("108301"),debug);
        HAS_UPDATE = updaterManager.checkUpdate();

        new KitemBuilder(toolsObjectWrapper,packageUtil.tag(), "test", new ItemBuilder(Material.STONE).displayname(globalLanguageManager.convertMmToString("<gradient:red:blue>BAaarrakeopjaedmaeodmae[")).build()).register();

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

    public GlobalManagersWrapper getGlobalManagersWrapper() {
        return globalManagersWrapper;
    }
}
