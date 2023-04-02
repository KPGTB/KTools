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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;

/**
 * ItemManager handles process of creating custom items
 * @since 1.3.0
 */
public class ItemManager {
    private final HashMap<String, Kitem> customItems;

    /**
     * Constructor of ItemManager
     */
    public ItemManager() {
        this.customItems = new HashMap<>();
    }

    /**
     * Register all items (Kitem) from package
     * @param toolsObjectWrapper Instance of ToolsObjectWrapper
     * @param jarFile File of plugin
     * @param pluginTag Tag of plugin
     * @param itemsPackage Package that should be scanned
     */
    public void registerItems(ToolsObjectWrapper toolsObjectWrapper, File jarFile, String pluginTag, String itemsPackage) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        DebugManager debug = toolsObjectWrapper.getDebugManager();

        for(Class<?> clazz : ReflectionUtil.getAllClassesInPackage(jarFile,itemsPackage, Kitem.class)) {
            try {

                debug.sendInfo(DebugType.ITEM, "Registering item " + clazz.getSimpleName() + "...");

                String itemName = pluginTag + ":" + clazz.getSimpleName()
                        .toLowerCase()
                        .replace("Item", "");

                Kitem item = (Kitem) clazz.getDeclaredConstructor(ToolsObjectWrapper.class, String.class)
                        .newInstance(toolsObjectWrapper, itemName);

                ItemStack bukkitItem = item.getItem();
                if(bukkitItem == null) {
                    debug.sendWarning(DebugType.ITEM, "Item is null! Cancelling!");
                    continue;
                }
                pluginManager.registerEvents(item, toolsObjectWrapper.getPlugin());

                this.customItems.put(itemName, item);
                debug.sendInfo(DebugType.ITEM, "Registered item " + itemName);

            } catch (Exception e) {
                debug.sendWarning(DebugType.ITEM, "Error while loading item from class " + clazz.getName());
                e.printStackTrace();
            }
        }
    }

    /**
     * Register item
     * @param toolsObjectWrapper Instance of ToolsObjectWrapper
     * @param item Instance of Kitem
     */
    public void registerItem(ToolsObjectWrapper toolsObjectWrapper, Kitem item) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        DebugManager debug = toolsObjectWrapper.getDebugManager();

        debug.sendInfo(DebugType.ITEM, "Registering item " + item.getFullItemTag());
        ItemStack bukkitItem = item.getItem();
        if(bukkitItem == null) {
            debug.sendWarning(DebugType.ITEM, "Item is null! Cancelling!");
            return;
        }
        pluginManager.registerEvents(item, toolsObjectWrapper.getPlugin());

        this.customItems.put(item.getFullItemTag(), item);
        debug.sendInfo(DebugType.ITEM, "Registered item " + item.getFullItemTag());
    }

    /**
     * Get custom item
     * @param fullItemName Name of registered item (plugin:item)
     * @return ItemStack or null when not exists
     */
    @Nullable
    public ItemStack getCustomItem(String fullItemName) {
        return customItems.get(fullItemName).getItem();
    }

    /**
     * Get custom item
     * @param pluginTag Tag of plugin
     * @param itemName Name of item
     * @return ItemStack or null when not exists
     */
    @Nullable
    public ItemStack getCustomItem(String pluginTag, String itemName) {
        return customItems.get(pluginTag.toLowerCase() + ":" + itemName).getItem();
    }

    /**
     * Get custom item
     * @param pluginTag Tag of plugin
     * @param itemClass Class that contains item
     * @return ItemStack or null when not exists
     */
    @Nullable
    public ItemStack getCustomItem(String pluginTag, Class<? extends Kitem> itemClass) {
        String itemName = pluginTag + ":" + itemClass.getSimpleName()
                .toLowerCase()
                .replace("Item", "");
        return customItems.get(itemName).getItem();
    }

    /**
     * Get all registered items
     * @return HashMap of registered items
     */
    public HashMap<String, Kitem> getCustomItems() {
        return customItems;
    }
}
