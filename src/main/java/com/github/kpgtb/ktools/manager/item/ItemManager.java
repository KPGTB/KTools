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

package com.github.kpgtb.ktools.manager.item;

import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.util.file.ReflectionUtil;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * ItemManager handles process of creating custom items
 * @since 1.3.0
 */
public class ItemManager {
    private final HashMap<String, KItem> customItems;
    private final File itemsFile;

    /**
     * Constructor of ItemManager
     */
    public ItemManager(DebugManager debug, JavaPlugin plugin) {
        this.customItems = new HashMap<>();
        this.itemsFile = loadFile(debug,plugin);
    }

    /**
     * Register all items (Kitem) from package
     * @param wrapper Instance of ToolsObjectWrapper
     * @param jarFile File of plugin
     * @param pluginTag Tag of plugin
     * @param itemsPackage Package that should be scanned
     * @return List of items tag
     */
    public ArrayList<String> registerItems(ToolsObjectWrapper wrapper, File jarFile, String pluginTag, String itemsPackage) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        DebugManager debug = wrapper.getDebugManager();

        ArrayList<String> tags = new ArrayList<>();

        for(Class<?> clazz : ReflectionUtil.getAllClassesInPackage(jarFile,itemsPackage, KItem.class)) {
            try {

                debug.sendInfo(DebugType.ITEM, "Registering item " + clazz.getSimpleName() + "...");

                String itemName = pluginTag + ":" + camelToSnake(
                        clazz.getSimpleName()
                        .replace("Item", "")
                );

                KItem item = (KItem) clazz.getDeclaredConstructor(ToolsObjectWrapper.class, String.class)
                        .newInstance(wrapper, itemName);

                ItemStack bukkitItem = item.getItem();
                if(bukkitItem == null) {
                    debug.sendWarning(DebugType.ITEM, "Item is null! Cancelling!");
                    continue;
                }
                pluginManager.registerEvents(item, wrapper.getPlugin());
                item.generateItemInFile();

                this.customItems.put(itemName, item);
                tags.add(itemName);
                debug.sendInfo(DebugType.ITEM, "Registered item " + itemName);

            } catch (Exception e) {
                debug.sendWarning(DebugType.ITEM, "Error while loading item from class " + clazz.getName());
                e.printStackTrace();
            }
        }

        return tags;
    }

    /**
     * Register item
     * @param wrapper Instance of ToolsObjectWrapper
     * @param item Instance of Kitem
     * @return tag of item
     */
    public String registerItem(ToolsObjectWrapper wrapper, KItem item) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        DebugManager debug = wrapper.getDebugManager();

        debug.sendInfo(DebugType.ITEM, "Registering item " + item.getFullItemTag());
        ItemStack bukkitItem = item.getItem();
        if(bukkitItem == null) {
            debug.sendWarning(DebugType.ITEM, "Item is null! Cancelling!");
            return "";
        }
        pluginManager.registerEvents(item, wrapper.getPlugin());
        item.generateItemInFile();

        this.customItems.put(item.getFullItemTag(), item);
        debug.sendInfo(DebugType.ITEM, "Registered item " + item.getFullItemTag());

        return item.getFullItemTag();
    }

    /**
     * Unregister item
     * @param fullItemName Full name of item
     * @since 1.4.4
     */
    public void unregisterItem(String fullItemName) {
        if(!this.customItems.containsKey(fullItemName)) {
            return;
        }
        KItem item = this.customItems.get(fullItemName);
        this.customItems.remove(fullItemName);
        HandlerList.unregisterAll(item);
    }

    private File loadFile(DebugManager debug, JavaPlugin plugin) {
        File dataFolder = plugin.getDataFolder();
        dataFolder.mkdirs();
        File itemsFile = new File(dataFolder, "items.yml");
        if(itemsFile.exists()) {
            itemsFile.delete();
        }
        try {
            itemsFile.createNewFile();
        } catch (IOException e) {
            debug.sendWarning(DebugType.ITEM, "Error while creating file...");
        }
        debug.sendInfo(DebugType.COMMAND, "Loaded items list file.");
        return itemsFile;
    }

    /**
     * Get custom item
     * @param fullItemName Name of registered item (plugin:item)
     * @return ItemStack or null when not exists
     */
    @Nullable
    public ItemStack getCustomItem(String fullItemName) {
        return customItems.get(fullItemName).getItem().clone();
    }

    /**
     * Get custom item
     * @param pluginTag Tag of plugin
     * @param itemName Name of item
     * @return ItemStack or null when not exists
     */
    @Nullable
    public ItemStack getCustomItem(String pluginTag, String itemName) {
        return customItems.get(pluginTag.toLowerCase() + ":" + itemName).getItem().clone();
    }

    /**
     * Get custom item
     * @param pluginTag Tag of plugin
     * @param itemClass Class that contains item
     * @return ItemStack or null when not exists
     */
    @Nullable
    public ItemStack getCustomItem(String pluginTag, Class<? extends KItem> itemClass) {
        String itemName = pluginTag + ":" + camelToSnake(
                itemClass.getSimpleName()
                .replace("Item", "")
        );
        return customItems.get(itemName).getItem().clone();
    }

    /**
     * Get custom item
     * @param fullItemName Name of registered item (plugin:item)
     * @return KItem or null when not exists
     */
    @Nullable
    public KItem getCustomItemObj(String fullItemName) {
        return customItems.get(fullItemName);
    }

    /**
     * Get custom item
     * @param pluginTag Tag of plugin
     * @param itemName Name of item
     * @return KItem or null when not exists
     */
    @Nullable
    public KItem getCustomItemObj(String pluginTag, String itemName) {
        return customItems.get(pluginTag.toLowerCase() + ":" + itemName);
    }

    /**
     * Get custom item
     * @param pluginTag Tag of plugin
     * @param itemClass Class that contains item
     * @return KItem or null when not exists
     */
    @Nullable
    public KItem getCustomItemObj(String pluginTag, Class<? extends KItem> itemClass) {
        String itemName = pluginTag + ":" + camelToSnake(
                itemClass.getSimpleName()
                        .replace("Item", "")
        );
        return customItems.get(itemName);
    }

    /**
     * Get all registered items
     * @return HashMap of registered items
     */
    public HashMap<String, KItem> getCustomItems() {
        return customItems;
    }

    /**
     * Get file with items
     * @return File with all items
     */
    public File getItemsFile() {
        return itemsFile;
    }

    private String camelToSnake(String str) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        str = str.replaceAll(regex, replacement).toLowerCase();
        return str;
    }
}
