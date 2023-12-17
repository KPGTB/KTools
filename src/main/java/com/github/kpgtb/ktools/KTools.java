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
import com.github.kpgtb.ktools.manager.data.GsonAdapterManager;
import com.github.kpgtb.ktools.manager.data.adapter.ItemStackAdapter;
import com.github.kpgtb.ktools.manager.data.adapter.LocationAdapter;
import com.github.kpgtb.ktools.manager.data.adapter.OfflinePlayerAdapter;
import com.github.kpgtb.ktools.manager.data.adapter.WorldAdapter;
import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.manager.item.ItemManager;
import com.github.kpgtb.ktools.manager.item.builder.KItemBuilder;
import com.github.kpgtb.ktools.manager.language.LanguageManager;
import com.github.kpgtb.ktools.manager.listener.ListenerManager;
import com.github.kpgtb.ktools.manager.recipe.RecipeManager;
import com.github.kpgtb.ktools.manager.resourcepack.ResourcePackManager;
import com.github.kpgtb.ktools.manager.resourcepack.ResourcePackServer;
import com.github.kpgtb.ktools.manager.resourcepack.uploader.SelfUploader;
import com.github.kpgtb.ktools.manager.ui.UiManager;
import com.github.kpgtb.ktools.manager.ui.bar.BarManager;
import com.github.kpgtb.ktools.manager.updater.SpigotUpdater;
import com.github.kpgtb.ktools.manager.updater.UpdaterManager;
import com.github.kpgtb.ktools.manager.updater.version.KVersion;
import com.github.kpgtb.ktools.util.bstats.Metrics;
import com.github.kpgtb.ktools.util.file.PackageUtil;
import com.github.kpgtb.ktools.util.time.KTime;
import com.github.kpgtb.ktools.util.ui.FontWidth;
import com.github.kpgtb.ktools.util.wrapper.GlobalManagersWrapper;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import com.google.gson.JsonParser;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

/**
 * Main class of plugin
 */
public final class KTools extends JavaPlugin {

    private ToolsObjectWrapper toolsObjectWrapper;
    private GlobalManagersWrapper globalManagersWrapper;
    private ResourcePackServer resourcePackServer;

    public static boolean HAS_UPDATE;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        DebugManager debug = new DebugManager(getConfig(),getLogger());

        long startMillis = System.currentTimeMillis();
        debug.sendInfo(DebugType.START, "Enabling plugin...");

        boolean legacy = !new KVersion(
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
        GsonAdapterManager.getInstance()
                .registerAdapter(ItemStack.class, new ItemStackAdapter())
                .registerAdapter(Location.class, new LocationAdapter())
                .registerAdapter(OfflinePlayer.class, new OfflinePlayerAdapter())
                .registerAdapter(World.class, new WorldAdapter());

        DataManager dataManager = new DataManager(debug,getConfig(),getDataFolder(), this);
        dataManager.registerPersisters(packageUtil.get("manager.data.persister.base"), getFile());
        debug.sendInfo(DebugType.START, "Loaded database.");

        debug.sendInfo(DebugType.START, "Loading bars...");
        BarManager barManager = new BarManager(getConfig().getInt("bar.startChar"));
        debug.sendInfo(DebugType.START, "Loaded bars.");

        UiManager uiManager = null;
        FontWidth.initWidth (new JsonParser().parse(getTextResource("spaces.json")));
        if(Bukkit.getPluginManager().getPlugin("ProtocolLib") != null && !legacy) {
            debug.sendInfo(DebugType.START, "Loading ui...");
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            uiManager = new UiManager(this,protocolManager);
            debug.sendInfo(DebugType.START, "Loaded ui.");
        }

        ResourcePackManager resourcepackManager = null;
        if(!legacy) {
            debug.sendInfo(DebugType.START, "Loading resourcepack...");
            if(getConfig().getBoolean("resourcePackSelfHost.enabled")) {
                debug.sendInfo(DebugType.START, "Starting resourcepack server...");
                this.resourcePackServer = new ResourcePackServer(this);
                try {
                    this.resourcePackServer.start();
                    debug.sendInfo(DebugType.START, "Started resourcepack server...");
                } catch (IOException e) {
                    debug.sendWarning(DebugType.START, "Error while starting resourcepack server...", true);
                    e.printStackTrace();
                }
            }

            resourcepackManager = new ResourcePackManager(this, debug, cacheManager);
            ResourcePackManager finalResourcepackManager = resourcepackManager;
            UiManager finalUiManager = uiManager;
            JavaPlugin plugin = this;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(!barManager.getBars().isEmpty()) {
                        barManager.prepareBars();
                    }

                    if(finalResourcepackManager.areSpacesRegistered() || (finalUiManager != null && finalUiManager.isRequired())) {
                        finalResourcepackManager.registerPlugin(packageUtil.getTag(), getDescription().getVersion());

                        //NegativeSpaces
                        finalResourcepackManager.registerCustomChar("space", "\uF801", "space_split.png", getResource("txt/space_split.png"), -3, -32768, -1);
                        finalResourcepackManager.registerCustomChar("space", "\uF802", "space_split.png", getResource("txt/space_split.png"), -4, -32768, -2);
                        finalResourcepackManager.registerCustomChar("space", "\uF803", "space_split.png", getResource("txt/space_split.png"), -5, -32768, -3);
                        finalResourcepackManager.registerCustomChar("space", "\uF804", "space_split.png", getResource("txt/space_split.png"), -6, -32768, -4);
                        finalResourcepackManager.registerCustomChar("space", "\uF805", "space_split.png", getResource("txt/space_split.png"), -7, -32768, -5);
                        finalResourcepackManager.registerCustomChar("space", "\uF806", "space_split.png", getResource("txt/space_split.png"), -8, -32768, -6);
                        finalResourcepackManager.registerCustomChar("space", "\uF807", "space_split.png", getResource("txt/space_split.png"), -9, -32768, -7);
                        finalResourcepackManager.registerCustomChar("space", "\uF808", "space_split.png", getResource("txt/space_split.png"), -10, -32768, -8);
                        finalResourcepackManager.registerCustomChar("space", "\uF809", "space_split.png", getResource("txt/space_split.png"), -18, -32768, -16);
                        finalResourcepackManager.registerCustomChar("space", "\uF80A", "space_split.png", getResource("txt/space_split.png"), -34, -32768, -32);
                        finalResourcepackManager.registerCustomChar("space", "\uF80B", "space_split.png", getResource("txt/space_split.png"), -66, -32768, -64);
                        finalResourcepackManager.registerCustomChar("space", "\uF80C", "space_split.png", getResource("txt/space_split.png"), -130, -32768, -128);
                        finalResourcepackManager.registerCustomChar("space", "\uF80D", "space_split.png", getResource("txt/space_split.png"), -258, -32768, -256);
                        finalResourcepackManager.registerCustomChar("space", "\uF80E", "space_split.png", getResource("txt/space_split.png"), -514, -32768, -512);
                        finalResourcepackManager.registerCustomChar("space", "\uF80F", "space_split.png", getResource("txt/space_split.png"), -1026, -32768, -1024);

                        finalResourcepackManager.registerCustomChar("space", "\uF811", "space_nosplit.png", getResource("txt/space_nosplit.png"), -3, -32768, -1);
                        finalResourcepackManager.registerCustomChar("space", "\uF812", "space_nosplit.png", getResource("txt/space_nosplit.png"), -4, -32768, -2);
                        finalResourcepackManager.registerCustomChar("space", "\uF813", "space_nosplit.png", getResource("txt/space_nosplit.png"), -5, -32768, -3);
                        finalResourcepackManager.registerCustomChar("space", "\uF814", "space_nosplit.png", getResource("txt/space_nosplit.png"), -6, -32768, -4);
                        finalResourcepackManager.registerCustomChar("space", "\uF815", "space_nosplit.png", getResource("txt/space_nosplit.png"), -7, -32768, -5);
                        finalResourcepackManager.registerCustomChar("space", "\uF816", "space_nosplit.png", getResource("txt/space_nosplit.png"), -8, -32768, -6);
                        finalResourcepackManager.registerCustomChar("space", "\uF817", "space_nosplit.png", getResource("txt/space_nosplit.png"), -9, -32768, -7);
                        finalResourcepackManager.registerCustomChar("space", "\uF818", "space_nosplit.png", getResource("txt/space_nosplit.png"), -10, -32768, -8);
                        finalResourcepackManager.registerCustomChar("space", "\uF819", "space_nosplit.png", getResource("txt/space_nosplit.png"), -18, -32768, -16);
                        finalResourcepackManager.registerCustomChar("space", "\uF81A", "space_nosplit.png", getResource("txt/space_nosplit.png"), -34, -32768, -32);
                        finalResourcepackManager.registerCustomChar("space", "\uF81B", "space_nosplit.png", getResource("txt/space_nosplit.png"), -66, -32768, -64);
                        finalResourcepackManager.registerCustomChar("space", "\uF81C", "space_nosplit.png", getResource("txt/space_nosplit.png"), -130, -32768, -128);
                        finalResourcepackManager.registerCustomChar("space", "\uF81D", "space_nosplit.png", getResource("txt/space_nosplit.png"), -258, -32768, -256);
                        finalResourcepackManager.registerCustomChar("space", "\uF81E", "space_nosplit.png", getResource("txt/space_nosplit.png"), -514, -32768, -512);
                        finalResourcepackManager.registerCustomChar("space", "\uF81F", "space_nosplit.png", getResource("txt/space_nosplit.png"), -1026, -32768, -1024);

                        finalResourcepackManager.registerCustomChar("space", "\uF821", "space_split.png", getResource("txt/space_split.png"), 0, -32768, 1);
                        finalResourcepackManager.registerCustomChar("space", "\uF822", "space_split.png", getResource("txt/space_split.png"), 1, -32768, 2);
                        finalResourcepackManager.registerCustomChar("space", "\uF823", "space_split.png", getResource("txt/space_split.png"), 2, -32768, 3);
                        finalResourcepackManager.registerCustomChar("space", "\uF824", "space_split.png", getResource("txt/space_split.png"), 3, -32768, 4);
                        finalResourcepackManager.registerCustomChar("space", "\uF825", "space_split.png", getResource("txt/space_split.png"), 4, -32768, 5);
                        finalResourcepackManager.registerCustomChar("space", "\uF826", "space_split.png", getResource("txt/space_split.png"), 5, -32768, 6);
                        finalResourcepackManager.registerCustomChar("space", "\uF827", "space_split.png", getResource("txt/space_split.png"), 6, -32768, 7);
                        finalResourcepackManager.registerCustomChar("space", "\uF828", "space_split.png", getResource("txt/space_split.png"), 7, -32768, 8);
                        finalResourcepackManager.registerCustomChar("space", "\uF829", "space_split.png", getResource("txt/space_split.png"), 15, -32768, 16);
                        finalResourcepackManager.registerCustomChar("space", "\uF82A", "space_split.png", getResource("txt/space_split.png"), 31, -32768, 32);
                        finalResourcepackManager.registerCustomChar("space", "\uF82B", "space_split.png", getResource("txt/space_split.png"), 63, -32768, 64);
                        finalResourcepackManager.registerCustomChar("space", "\uF82C", "space_split.png", getResource("txt/space_split.png"), 127, -32768, 128);
                        finalResourcepackManager.registerCustomChar("space", "\uF82D", "space_split.png", getResource("txt/space_split.png"), 255, -32768, 256);
                        finalResourcepackManager.registerCustomChar("space", "\uF82E", "space_split.png", getResource("txt/space_split.png"), 511, -32768, 512);
                        finalResourcepackManager.registerCustomChar("space", "\uF82F", "space_split.png", getResource("txt/space_split.png"), 1023, -32768, 1024);

                        finalResourcepackManager.registerCustomChar("space", "\uF831", "space_nosplit.png", getResource("txt/space_nosplit.png"), 0, -32768, 1);
                        finalResourcepackManager.registerCustomChar("space", "\uF832", "space_nosplit.png", getResource("txt/space_nosplit.png"), 1, -32768, 2);
                        finalResourcepackManager.registerCustomChar("space", "\uF833", "space_nosplit.png", getResource("txt/space_nosplit.png"), 2, -32768, 3);
                        finalResourcepackManager.registerCustomChar("space", "\uF834", "space_nosplit.png", getResource("txt/space_nosplit.png"), 3, -32768, 4);
                        finalResourcepackManager.registerCustomChar("space", "\uF835", "space_nosplit.png", getResource("txt/space_nosplit.png"), 4, -32768, 5);
                        finalResourcepackManager.registerCustomChar("space", "\uF836", "space_nosplit.png", getResource("txt/space_nosplit.png"), 5, -32768, 6);
                        finalResourcepackManager.registerCustomChar("space", "\uF837", "space_nosplit.png", getResource("txt/space_nosplit.png"), 6, -32768, 7);
                        finalResourcepackManager.registerCustomChar("space", "\uF838", "space_nosplit.png", getResource("txt/space_nosplit.png"), 7, -32768, 8);
                        finalResourcepackManager.registerCustomChar("space", "\uF839", "space_nosplit.png", getResource("txt/space_nosplit.png"), 15, -32768, 16);
                        finalResourcepackManager.registerCustomChar("space", "\uF83A", "space_nosplit.png", getResource("txt/space_nosplit.png"), 31, -32768, 32);
                        finalResourcepackManager.registerCustomChar("space", "\uF83B", "space_nosplit.png", getResource("txt/space_nosplit.png"), 63, -32768, 64);
                        finalResourcepackManager.registerCustomChar("space", "\uF83C", "space_nosplit.png", getResource("txt/space_nosplit.png"), 127, -32768, 128);
                        finalResourcepackManager.registerCustomChar("space", "\uF83D", "space_nosplit.png", getResource("txt/space_nosplit.png"), 255, -32768, 256);
                        finalResourcepackManager.registerCustomChar("space", "\uF83E", "space_nosplit.png", getResource("txt/space_nosplit.png"), 511, -32768, 512);
                        finalResourcepackManager.registerCustomChar("space", "\uF83F", "space_nosplit.png", getResource("txt/space_nosplit.png"), 1023, -32768, 1024);
                    }

                    if(finalUiManager != null && finalUiManager.isRequired()) {
                        //Shader
                        finalResourcepackManager.registerCustomFile("noShadow", "assets"+ File.separator+"minecraft"+ File.separator+"shaders"+ File.separator+"core" + File.separator, "rendertype_text.vsh", getResource("txt/rendertype_text.vsh"));
                    }

                    if(finalResourcepackManager.isEnabled()) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                finalResourcepackManager.prepareResourcepack(false);
                            }
                        }.runTaskTimerAsynchronously(plugin,1L,new KTime(getConfig().getString("resourcePackRefreshRate")).getTicks());
                    }
                }
            }.runTaskLater(this, 1L);
            debug.sendInfo(DebugType.START, "Loaded resourcepack.");
        }

        debug.sendInfo(DebugType.START, "Loading items...");
        ItemManager itemManager = new ItemManager(debug,this);
        debug.sendInfo(DebugType.START, "Loaded items.");

        debug.sendInfo(DebugType.START, "Loading tools object wrapper...");
        this.toolsObjectWrapper = new ToolsObjectWrapper(cacheManager,debug,globalLanguageManager,this,adventure,paramParserManager, dataManager, resourcepackManager, uiManager, itemManager, legacy, packageUtil, barManager, this);
        debug.sendInfo(DebugType.START, "Loaded tools object wrapper.");

        barManager.setWrapper(toolsObjectWrapper);

        debug.sendInfo(DebugType.START, "Loading commands...");
        CommandManager commandManager = new CommandManager(toolsObjectWrapper, getFile(), packageUtil.getTag());
        commandManager.registerCommands(packageUtil.get("command"));
        debug.sendInfo(DebugType.START, "Loaded commands.");

        debug.sendInfo(DebugType.START, "Loading listeners...");
        ListenerManager listenerManager = new ListenerManager(toolsObjectWrapper, getFile());
        listenerManager.registerListeners(packageUtil.get("listener"));
        debug.sendInfo(DebugType.START, "Loaded listeners.");

        debug.sendInfo(DebugType.START, "Loading global managers wrapper...");
        this.globalManagersWrapper = new GlobalManagersWrapper(this, debug, globalLanguageManager,cacheManager,paramParserManager,dataManager,uiManager,resourcepackManager, itemManager, barManager, legacy);
        debug.sendInfo(DebugType.START, "Loaded global managers wrapper.");

        debug.sendInfo(DebugType.START, "Checking updates...");
        UpdaterManager updaterManager = new UpdaterManager(getDescription(), new SpigotUpdater("108301"),debug);
        HAS_UPDATE = updaterManager.checkUpdate();

        new Metrics(this, 18408);

        debug.sendInfo(DebugType.START, "Enabled plugin in " + (System.currentTimeMillis() - startMillis) + "ms.");
    }

    @Override
    public void onDisable() {
        BukkitAudiences adventure = this.toolsObjectWrapper.getAdventure();
        if(adventure != null) {
            adventure.close();
        }
        if(this.resourcePackServer != null) {
            this.resourcePackServer.stop();
        }
        this.toolsObjectWrapper.getDataManager().close();
    }

    public GlobalManagersWrapper getGlobalManagersWrapper() {
        return globalManagersWrapper;
    }
}
