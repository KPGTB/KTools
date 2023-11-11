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

package com.github.kpgtb.ktools.manager.language;

import com.github.kpgtb.ktools.KTools;
import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.manager.updater.version.KVersion;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LanguageManager handles all message translations
 */
public class LanguageManager {
    private final File dataFolder;
    private final String lang;
    private final DebugManager debug;
    private final LanguageManager globalManager;
    private final HashMap<String, String> pluginMessages;
    private final HashMap<String, ArrayList<String>> pluginMessagesLong;
    private final ArrayList<LanguageManager> hookedManagers;

    /**
     * Constructor of Global LanguageManager. Use only in Ktools!
     * @param dataFolder Folder with plugin's data. Use JavaPlugin#getDataFolder() to get this
     * @param lang Language of messages. Get this from plugin's config
     * @param debug Instance of {@link DebugManager}
     */
    public LanguageManager(File dataFolder, String lang, DebugManager debug) {
        this.dataFolder = dataFolder;
        this.lang = lang;
        this.debug = debug;
        this.globalManager = this;
        this.pluginMessages = new HashMap<>();
        this.pluginMessagesLong = new HashMap<>();
        this.hookedManagers = new ArrayList<>();
    }

    /**
     * Constructor of Plugin's LanguageManager. Use in other plugins!
     * @param dataFolder Folder with plugin's data. Use JavaPlugin#getDataFolder() to get this
     * @param lang Language of messages. Get this from plugin's config
     * @param debug Instance of {@link DebugManager}
     * @param globalManager Instance of Global LanguageManager. Take it from {@link KTools}
     */
    public LanguageManager(File dataFolder, String lang, DebugManager debug, LanguageManager globalManager) {
        this.dataFolder = dataFolder;
        this.lang = lang;
        this.debug = debug;
        this.globalManager = globalManager;
        this.pluginMessages = new HashMap<>();
        this.pluginMessagesLong = new HashMap<>();
        this.hookedManagers = new ArrayList<>();

        globalManager.getHookedManagers().add(this);
    }

    /**
     * Save default language file
     * @param path Path to file in resources folder (lang/{lang_code}.yml)
     * @param plugin Instance of plugin
     */
    public void saveDefaultLanguage(String path, JavaPlugin plugin) {
        File defaultLangFile = new File(dataFolder, path);
        if(!defaultLangFile.exists()) {
            plugin.saveResource(path,false);
        }

        FileConfiguration langConfig = YamlConfiguration.loadConfiguration(defaultLangFile);

        FileConfiguration pluginLangConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(
                plugin.getResource(path)
        ));

        AtomicBoolean changed = new AtomicBoolean(false);
        pluginLangConfig.getConfigurationSection("").getKeys(true).forEach(langKey -> {
            if(!langConfig.getConfigurationSection("").contains(langKey)) {
                langConfig.set(langKey, pluginLangConfig.get(langKey));
                changed.set(true);
            }
        });
        if(changed.get()) {
            try {
                langConfig.save(defaultLangFile);
            } catch (IOException e) {
                debug.sendWarning(DebugType.LANGUAGE, "Error while saving changes to default language file ["+path+"].");
            }
            debug.sendInfo(DebugType.LANGUAGE, "Saved default language file with changes ["+path+"].");
        } else {
            debug.sendInfo(DebugType.LANGUAGE, "Saved default language file ["+path+"].");
        }
    }

    /**
     * Refresh messages from this plugin
     */
    public void refreshMessages() {
        debug.sendInfo(DebugType.LANGUAGE, "Refreshing messages...");
        this.pluginMessages.clear();
        this.pluginMessagesLong.clear();

        File langFile = new File(dataFolder, "lang/"+lang+".yml");
        if(!langFile.exists()) {
            debug.sendWarning(DebugType.LANGUAGE, "Language file doesn't exists! ["+langFile.getAbsolutePath()+"].");
            return;
        }

        FileConfiguration configuration = YamlConfiguration.loadConfiguration(langFile);
        ConfigurationSection messagesSection = configuration.getConfigurationSection("");

        if(messagesSection == null) {
            debug.sendWarning(DebugType.LANGUAGE, "Language dile doesn't have message section ["+langFile.getAbsolutePath()+"].");
            return;
        }

        messagesSection
                .getKeys(true)
                .forEach(msgCode -> {
                    debug.sendInfo(DebugType.LANGUAGE, "Loading "+msgCode+"...");
                    Object msg = messagesSection.get(msgCode);

                    String[] codeTemp = msgCode.split("\\.");
                    msgCode = codeTemp[codeTemp.length-1];

                    if(msg instanceof String) {
                        this.pluginMessages.put(msgCode, (String) msg);
                        debug.sendInfo(DebugType.LANGUAGE, "Loaded "+msgCode+" as single line.");
                        return;
                    }
                    if(msg instanceof ArrayList<?>) {
                        ArrayList<?> msgList = (ArrayList<?>) msg;
                        if(msgList.isEmpty() || !(msgList.get(0) instanceof String)) {
                            debug.sendWarning(DebugType.LANGUAGE, "Message "+msgCode+" is an empty list!");
                            return;
                        }
                        ArrayList<String> msgStringList = (ArrayList<String>) msgList;
                        this.pluginMessagesLong.put(msgCode, msgStringList);
                        debug.sendInfo(DebugType.LANGUAGE, "Loaded "+msgCode+" as multiple lines.");
                        return;
                    }
                    if(msg instanceof ConfigurationSection) {
                        debug.sendInfo(DebugType.LANGUAGE, "It's a section. Ignoring");
                        return;
                    }
                    debug.sendWarning(DebugType.LANGUAGE, "Could not load "+msgCode+"!");
                });
        debug.sendInfo(DebugType.LANGUAGE, "Refreshed messages.");
    }

    /**
     * Get message translation as list of {@link net.kyori.adventure.text.Component}
     * @param languageLevel {@link com.github.kpgtb.ktools.manager.language.LanguageLevel} PLUGIN (from lang) or GLOBAL (from Ktools)
     * @param code Code of message
     * @param player {@link org.bukkit.entity.Player} to PlaceholderAPI
     * @param placeholders Array of {@link net.kyori.adventure.text.minimessage.tag.resolver.Placeholder}
     * @return A list of translated components with PAPI and plugin's placeholders
     */
    public ArrayList<Component> getComponent(LanguageLevel languageLevel, String code, Player player, TagResolver... placeholders) {
        ArrayList<String> messages = new ArrayList<>();

        LanguageManager manager = languageLevel == LanguageLevel.PLUGIN ? this : this.globalManager;

        if(manager.pluginMessages.containsKey(code)) {
            messages.add(manager.pluginMessages.get(code));
        } else if (manager.pluginMessagesLong.containsKey(code)) {
            messages.addAll(manager.pluginMessagesLong.get(code));
        } else {
            messages.add("<red>Translation not found! [lang="+manager.lang+", level="+languageLevel.name()+", code="+code+"]");
        }

        ArrayList<Component> result = new ArrayList<>();
        MiniMessage mm = MiniMessage.miniMessage();

        boolean hasPAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

        messages.forEach(msg -> {
            if(hasPAPI && player != null) {
                msg = PlaceholderAPI.setPlaceholders(player, msg);
            }
            result.add(
                    mm.deserialize(msg, placeholders)
            );
        });

        return result;
    }

    /**
     * Get message translation as list of {@link net.kyori.adventure.text.Component}
     * @param languageLevel {@link com.github.kpgtb.ktools.manager.language.LanguageLevel} PLUGIN (from lang) or GLOBAL (from Ktools)
     * @param code Code of message
     * @param placeholders Array of {@link net.kyori.adventure.text.minimessage.tag.resolver.Placeholder}
     * @return A list of translated components with plugin's placeholders
     */
    public ArrayList<Component> getComponent(LanguageLevel languageLevel, String code, TagResolver... placeholders) {
        return this.getComponent(languageLevel, code,null,placeholders);
    }

    /**
     * Get first line of message translation as {@link net.kyori.adventure.text.Component}
     * @param languageLevel {@link com.github.kpgtb.ktools.manager.language.LanguageLevel} PLUGIN (from lang) or GLOBAL (from Ktools)
     * @param code Code of message
     * @param player {@link org.bukkit.entity.Player} to PlaceholderAPI
     * @param placeholders Array of {@link net.kyori.adventure.text.minimessage.tag.resolver.Placeholder}
     * @return A first line of translated components with plugin's placeholders
     */
    public Component getSingleComponent(LanguageLevel languageLevel, String code, Player player, TagResolver... placeholders) {
        return this.getComponent(languageLevel,code,player,placeholders).get(0);
    }

    /**
     * Get first line of message translation as {@link net.kyori.adventure.text.Component}
     * @param languageLevel {@link com.github.kpgtb.ktools.manager.language.LanguageLevel} PLUGIN (from lang) or GLOBAL (from Ktools)
     * @param code Code of message
     * @param placeholders Array of {@link net.kyori.adventure.text.minimessage.tag.resolver.Placeholder}
     * @return A first line of translated components with plugin's placeholders
     */
    public Component getSingleComponent(LanguageLevel languageLevel, String code, TagResolver... placeholders) {
        return this.getComponent(languageLevel, code, placeholders).get(0);
    }

    /**
     * Get message translation as list of {@link String}
     * @param languageLevel {@link com.github.kpgtb.ktools.manager.language.LanguageLevel} PLUGIN (from lang) or GLOBAL (from Ktools)
     * @param code Code of message
     * @param player {@link org.bukkit.entity.Player} to PlaceholderAPI
     * @param placeholders Array of {@link net.kyori.adventure.text.minimessage.tag.resolver.Placeholder}
     * @return A list of translated strings with PAPI and plugin's placeholders
     */
    public ArrayList<String> getString(LanguageLevel languageLevel, String code, Player player, TagResolver... placeholders) {
        ArrayList<Component> components = this.getComponent(languageLevel, code,player,placeholders);
        ArrayList<String> result = new ArrayList<>();

        components.forEach(component -> {
            result.add(convertComponentToString(component));
        });

        return result;
    }

    /**
     * Get message translation as list of {@link String}
     * @param languageLevel {@link com.github.kpgtb.ktools.manager.language.LanguageLevel} PLUGIN (from lang) or GLOBAL (from Ktools)
     * @param code Code of message
     * @param placeholders Array of {@link net.kyori.adventure.text.minimessage.tag.resolver.Placeholder}
     * @return A list of translated strings with plugin's placeholders
     */
    public ArrayList<String> getString(LanguageLevel languageLevel, String code, TagResolver... placeholders) {
        return this.getString(languageLevel, code,null,placeholders);
    }


    /**
     * Get first line of message translation as {@link String}
     * @param languageLevel {@link com.github.kpgtb.ktools.manager.language.LanguageLevel} PLUGIN (from lang) or GLOBAL (from Ktools)
     * @param code Code of message
     * @param player {@link org.bukkit.entity.Player} to PlaceholderAPI
     * @param placeholders Array of {@link net.kyori.adventure.text.minimessage.tag.resolver.Placeholder}
     * @return A first line of translated strings with plugin's placeholders
     */
    public String getSingleString(LanguageLevel languageLevel, String code, Player player, TagResolver... placeholders) {
        return this.getString(languageLevel,code,player,placeholders).get(0);
    }

    /**
     * Get first line of message translation as {@link String}
     * @param languageLevel {@link com.github.kpgtb.ktools.manager.language.LanguageLevel} PLUGIN (from lang) or GLOBAL (from Ktools)
     * @param code Code of message
     * @param placeholders Array of {@link net.kyori.adventure.text.minimessage.tag.resolver.Placeholder}
     * @return A first line of translated strings with plugin's placeholders
     */
    public String getSingleString(LanguageLevel languageLevel, String code, TagResolver... placeholders) {
        return this.getString(languageLevel, code, placeholders).get(0);
    }

    /**
     * Convert Component to String
     * @param component Component
     * @return String from component
     */
    public String convertComponentToString(Component component) {
        boolean isHexSupport = new KVersion(
                Bukkit.getBukkitVersion()
                        .split("-")[0]
        ).isNewerOrEquals("1.16");

        if (isHexSupport) {
            return LegacyComponentSerializer
                    .builder()
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build()
                    .serialize(component);
        }
        return LegacyComponentSerializer
                .builder()
                .build()
                .serialize(component);
    }

    /**
     * Convert mini message string to formatted string
     * @param mm Mini message string
     * @param placeholders Placeholders
     * @return formatted string
     */
    public String convertMmToString(String mm, TagResolver... placeholders) {
        return convertComponentToString(MiniMessage.miniMessage().deserialize(mm, placeholders));
    }

    /**
     * Convert legacy string to component
     * @param s Legacy string
     * @return TextComponent from string
     */
    public TextComponent convertLegacyStringToComponent(String s) {
        boolean isHexSupport = new KVersion(
                Bukkit.getBukkitVersion()
                        .split("-")[0]
        ).isNewerOrEquals("1.16");

        if (isHexSupport) {
            return LegacyComponentSerializer
                    .builder()
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build()
                    .deserialize(s);
        }
        return LegacyComponentSerializer
                .builder()
                .build()
                .deserialize(s);
    }

    /**
     * Get list of hooked language managers. ! Only global manager has filled list !
     * @return List of hooked managers
     */
    public ArrayList<LanguageManager> getHookedManagers() {
        return hookedManagers;
    }
}