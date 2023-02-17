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

package com.github.kpgtb.ktools.manager;

import com.github.kpgtb.ktools.manager.cache.CacheSource;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * CacheManager stores small data in object metadata or in files
 */
public class CacheManager {
    private final FileConfiguration config;
    private final File dataFolder;
    private final DebugManager debug;

    private File cacheFile;
    private FileConfiguration cacheConfiguration;

    /**
     * CacheManager constructor
     * @param config Config.yml from Ktools
     * @param dataFolder Data folder of Ktools
     * @param debug DebugManager instance
     */
    public CacheManager(FileConfiguration config, File dataFolder, DebugManager debug) {
        this.config = config;
        this.dataFolder = dataFolder;
        this.debug = debug;
    }

    /**
     * This method creates and configure cache file
     */
    public void setupCacheFile() {
        debug.sendInfo(DebugType.CACHE, "Starting cache file setup...");
        if(!dataFolder.exists()) {
            dataFolder.mkdirs();
            debug.sendInfo(DebugType.CACHE, "Created data folder.");
        }

        cacheFile = new File(dataFolder, "cache.yml");
        if(!cacheFile.exists()) {
            try {
                cacheFile.createNewFile();
                debug.sendInfo(DebugType.CACHE, "Created cache file.");
            } catch (IOException e) {
                debug.sendWarning(DebugType.CACHE, "Error while creating cache file!");
                throw new RuntimeException(e);
            }
        }

        cacheConfiguration = YamlConfiguration.loadConfiguration(cacheFile);

        debug.sendInfo(DebugType.CACHE, "Finished cache file setup.");
    }

    /**
     * With this method you can save string data to cache
     * @param cacheSource {@link com.github.kpgtb.ktools.manager.cache.CacheSource}
     * @param target Object where you want to save data (or null if cacheSource is SERVER)
     * @param pluginName Name of plugin that save this data
     * @param key Key of data
     * @param data String of data
     */
    public void setData(CacheSource cacheSource, Object target, String pluginName, String key, String data) {
        String finalKey = pluginName + "-" + key;
        debug.sendInfo(DebugType.CACHE, "Saving cache with type " + cacheSource.name() + " key: " + finalKey + " data (String): " + data+ "...");
        switch (cacheSource) {
            case SERVER:
                cacheConfiguration.set("server."+finalKey, data);
                try {
                    cacheConfiguration.save(cacheFile);
                    debug.sendInfo(DebugType.CACHE, "Saved server cache!");
                } catch (IOException e) {
                    debug.sendWarning(DebugType.CACHE, "Error while saving server cache!");
                    throw new RuntimeException(e);
                }
                break;
            case PLAYER:
                if(!(target instanceof Player)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't a player!");
                    return;
                }
                Player player = (Player) target;
                boolean playerSaveMeta = config.getString("cache.player").equalsIgnoreCase("metadata");

                if(playerSaveMeta) {
                    PersistentDataContainer playerPDC = player.getPersistentDataContainer();
                    NamespacedKey playerKey = new NamespacedKey(pluginName,key);
                    playerPDC.set(playerKey, PersistentDataType.STRING, data);
                    debug.sendInfo(DebugType.CACHE, "Saved player cache in metadata.");
                    return;
                }

                cacheConfiguration.set("player." + player.getUniqueId().toString() + "." + finalKey, data);
                try {
                    cacheConfiguration.save(cacheFile);
                    debug.sendInfo(DebugType.CACHE, "Saved player cache in files.");
                } catch (IOException e) {
                    debug.sendWarning(DebugType.CACHE, "Error while saving player cache in files.");
                    throw new RuntimeException(e);
                }
                break;
            case ENTITY:
                if(!(target instanceof Entity)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't an entity.");
                    return;
                }
                Entity entity = (Entity) target;
                boolean entitySaveMeta = config.getString("cache.entity").equalsIgnoreCase("metadata");

                if(entitySaveMeta) {
                    PersistentDataContainer entityPDC = entity.getPersistentDataContainer();
                    NamespacedKey entityKey = new NamespacedKey(pluginName,key);
                    entityPDC.set(entityKey, PersistentDataType.STRING, data);
                    debug.sendInfo(DebugType.CACHE, "Saved entity cache in metadata.");
                    return;
                }

                cacheConfiguration.set("entity." + entity.getUniqueId() + "." + finalKey, data);
                try {
                    cacheConfiguration.save(cacheFile);
                    debug.sendInfo(DebugType.CACHE, "Saved entity cache in files.");
                } catch (IOException e) {
                    debug.sendWarning(DebugType.CACHE, "Error while saving entity cache in files.");
                    throw new RuntimeException(e);
                }
                break;
            case ITEMSTACK:
                if(!(target instanceof ItemStack)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't an item stack.");
                    return;
                }
                ItemStack itemStack = (ItemStack) target;

                if(itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    debug.sendWarning(DebugType.CACHE, "Item stack is empty.");
                    return;
                }

                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer itemPDC = meta.getPersistentDataContainer();
                NamespacedKey itemKey = new NamespacedKey(pluginName,key);
                itemPDC.set(itemKey,PersistentDataType.STRING, data);
                itemStack.setItemMeta(meta);

                debug.sendInfo(DebugType.CACHE, "Saved item stack cache in metadata.");

                break;
        }
    }

    /**
     * With this method you can save int data to cache
     * @param cacheSource {@link com.github.kpgtb.ktools.manager.cache.CacheSource}
     * @param target Object where you want to save data (or null if cacheSource is SERVER)
     * @param pluginName Name of plugin that save this data
     * @param key Key of data
     * @param data Int of data
     */
    public void setData(CacheSource cacheSource, Object target, String pluginName, String key, int data) {
        String finalKey = pluginName + "-" + key;
        debug.sendInfo(DebugType.CACHE, "Saving cache with type " + cacheSource.name() + " key: " + finalKey + " data (String): " + data+ "...");
        switch (cacheSource) {
            case SERVER:
                cacheConfiguration.set("server."+finalKey, data);
                try {
                    cacheConfiguration.save(cacheFile);
                    debug.sendInfo(DebugType.CACHE, "Saved server cache!");
                } catch (IOException e) {
                    debug.sendWarning(DebugType.CACHE, "Error while saving server cache!");
                    throw new RuntimeException(e);
                }
                break;
            case PLAYER:
                if(!(target instanceof Player)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't a player!");
                    return;
                }
                Player player = (Player) target;
                boolean playerSaveMeta = config.getString("cache.player").equalsIgnoreCase("metadata");

                if(playerSaveMeta) {
                    PersistentDataContainer playerPDC = player.getPersistentDataContainer();
                    NamespacedKey playerKey = new NamespacedKey(pluginName,key);
                    playerPDC.set(playerKey, PersistentDataType.INTEGER, data);
                    debug.sendInfo(DebugType.CACHE, "Saved player cache in metadata.");
                    return;
                }

                cacheConfiguration.set("player." + player.getUniqueId().toString() + "." + finalKey, data);
                try {
                    cacheConfiguration.save(cacheFile);
                    debug.sendInfo(DebugType.CACHE, "Saved player cache in files.");
                } catch (IOException e) {
                    debug.sendWarning(DebugType.CACHE, "Error while saving player cache in files.");
                    throw new RuntimeException(e);
                }
                break;
            case ENTITY:
                if(!(target instanceof Entity)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't an entity.");
                    return;
                }
                Entity entity = (Entity) target;
                boolean entitySaveMeta = config.getString("cache.entity").equalsIgnoreCase("metadata");

                if(entitySaveMeta) {
                    PersistentDataContainer entityPDC = entity.getPersistentDataContainer();
                    NamespacedKey entityKey = new NamespacedKey(pluginName,key);
                    entityPDC.set(entityKey, PersistentDataType.INTEGER, data);
                    debug.sendInfo(DebugType.CACHE, "Saved entity cache in metadata.");
                    return;
                }

                cacheConfiguration.set("entity." + entity.getUniqueId() + "." + finalKey, data);
                try {
                    cacheConfiguration.save(cacheFile);
                    debug.sendInfo(DebugType.CACHE, "Saved entity cache in files.");
                } catch (IOException e) {
                    debug.sendWarning(DebugType.CACHE, "Error while saving entity cache in files.");
                    throw new RuntimeException(e);
                }
                break;
            case ITEMSTACK:
                if(!(target instanceof ItemStack)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't an item stack.");
                    return;
                }
                ItemStack itemStack = (ItemStack) target;

                if(itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    debug.sendWarning(DebugType.CACHE, "Item stack is empty.");
                    return;
                }

                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer itemPDC = meta.getPersistentDataContainer();
                NamespacedKey itemKey = new NamespacedKey(pluginName,key);
                itemPDC.set(itemKey,PersistentDataType.INTEGER, data);
                itemStack.setItemMeta(meta);

                debug.sendInfo(DebugType.CACHE, "Saved item stack cache in metadata.");

                break;
        }
    }

    /**
     * With this method you can save double data to cache
     * @param cacheSource {@link com.github.kpgtb.ktools.manager.cache.CacheSource}
     * @param target Object where you want to save data (or null if cacheSource is SERVER)
     * @param pluginName Name of plugin that save this data
     * @param key Key of data
     * @param data double of data
     */
    public void setData(CacheSource cacheSource, Object target, String pluginName, String key, double data) {
        String finalKey = pluginName + "-" + key;
        debug.sendInfo(DebugType.CACHE, "Saving cache with type " + cacheSource.name() + " key: " + finalKey + " data (String): " + data+ "...");
        switch (cacheSource) {
            case SERVER:
                cacheConfiguration.set("server."+finalKey, data);
                try {
                    cacheConfiguration.save(cacheFile);
                    debug.sendInfo(DebugType.CACHE, "Saved server cache!");
                } catch (IOException e) {
                    debug.sendWarning(DebugType.CACHE, "Error while saving server cache!");
                    throw new RuntimeException(e);
                }
                break;
            case PLAYER:
                if(!(target instanceof Player)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't a player!");
                    return;
                }
                Player player = (Player) target;
                boolean playerSaveMeta = config.getString("cache.player").equalsIgnoreCase("metadata");

                if(playerSaveMeta) {
                    PersistentDataContainer playerPDC = player.getPersistentDataContainer();
                    NamespacedKey playerKey = new NamespacedKey(pluginName,key);
                    playerPDC.set(playerKey, PersistentDataType.DOUBLE, data);
                    debug.sendInfo(DebugType.CACHE, "Saved player cache in metadata.");
                    return;
                }

                cacheConfiguration.set("player." + player.getUniqueId().toString() + "." + finalKey, data);
                try {
                    cacheConfiguration.save(cacheFile);
                    debug.sendInfo(DebugType.CACHE, "Saved player cache in files.");
                } catch (IOException e) {
                    debug.sendWarning(DebugType.CACHE, "Error while saving player cache in files.");
                    throw new RuntimeException(e);
                }
                break;
            case ENTITY:
                if(!(target instanceof Entity)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't an entity.");
                    return;
                }
                Entity entity = (Entity) target;
                boolean entitySaveMeta = config.getString("cache.entity").equalsIgnoreCase("metadata");

                if(entitySaveMeta) {
                    PersistentDataContainer entityPDC = entity.getPersistentDataContainer();
                    NamespacedKey entityKey = new NamespacedKey(pluginName,key);
                    entityPDC.set(entityKey, PersistentDataType.DOUBLE, data);
                    debug.sendInfo(DebugType.CACHE, "Saved entity cache in metadata.");
                    return;
                }

                cacheConfiguration.set("entity." + entity.getUniqueId() + "." + finalKey, data);
                try {
                    cacheConfiguration.save(cacheFile);
                    debug.sendInfo(DebugType.CACHE, "Saved entity cache in files.");
                } catch (IOException e) {
                    debug.sendWarning(DebugType.CACHE, "Error while saving entity cache in files.");
                    throw new RuntimeException(e);
                }
                break;
            case ITEMSTACK:
                if(!(target instanceof ItemStack)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't an item stack.");
                    return;
                }
                ItemStack itemStack = (ItemStack) target;

                if(itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    debug.sendWarning(DebugType.CACHE, "Item stack is empty.");
                    return;
                }

                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer itemPDC = meta.getPersistentDataContainer();
                NamespacedKey itemKey = new NamespacedKey(pluginName,key);
                itemPDC.set(itemKey,PersistentDataType.DOUBLE, data);
                itemStack.setItemMeta(meta);

                debug.sendInfo(DebugType.CACHE, "Saved item stack cache in metadata.");

                break;
        }
    }

    /**
     * With this method you can save string data to server's cache
     * @param pluginName Name of plugin that save this data
     * @param key Key of data
     * @param data double of data
     */
    public void setServerData(String pluginName, String key, String data) {
        this.setData(CacheSource.SERVER, null, pluginName,key,data);
    }

    /**
     * With this method you can save int data to server's cache
     * @param pluginName Name of plugin that save this data
     * @param key Key of data
     * @param int double of data
     */
    public void setServerData(String pluginName, String key, int data) {
        this.setData(CacheSource.SERVER, null, pluginName,key,data);
    }

    /**
     * With this method you can save double data to server's cache
     * @param pluginName Name of plugin that save this data
     * @param key Key of data
     * @param data double of data
     */
    public void setServerData(String pluginName, String key, double data) {
        this.setData(CacheSource.SERVER, null, pluginName,key,data);
    }

    /**
     * This method returns data from cache
     * @param cacheSource {@link com.github.kpgtb.ktools.manager.cache.CacheSource}
     * @param target Object from you want to get data (or null if cacheSource is SERVER)
     * @param pluginName Name of plugin that saves this data
     * @param key Key of data
     * @return String of data or null if there isn't any data
     */
    @Nullable
    public String getStringData(CacheSource cacheSource, Object target, String pluginName, String key) {
        String finalKey = pluginName + "-" + key;
        debug.sendInfo(DebugType.CACHE, "Getting cache with type " + cacheSource.name() + " key: " + finalKey +  "...");
        switch (cacheSource) {
            case SERVER:
                return cacheConfiguration.getString("server."+finalKey);
            case PLAYER:
                if(!(target instanceof Player)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't a player!");
                    return null;
                }
                Player player = (Player) target;
                boolean playerSaveMeta = config.getString("cache.player").equalsIgnoreCase("metadata");

                if(playerSaveMeta) {
                    PersistentDataContainer playerPDC = player.getPersistentDataContainer();
                    NamespacedKey playerKey = new NamespacedKey(pluginName,key);
                    return playerPDC.get(playerKey, PersistentDataType.STRING);
                }

                return cacheConfiguration.getString("player." + player.getUniqueId().toString() + "." + finalKey);
            case ENTITY:
                if(!(target instanceof Entity)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't an entity.");
                    return null;
                }
                Entity entity = (Entity) target;
                boolean entitySaveMeta = config.getString("cache.entity").equalsIgnoreCase("metadata");

                if(entitySaveMeta) {
                    PersistentDataContainer entityPDC = entity.getPersistentDataContainer();
                    NamespacedKey entityKey = new NamespacedKey(pluginName,key);
                    return entityPDC.get(entityKey, PersistentDataType.STRING);
                }

                return cacheConfiguration.getString("entity." + entity.getUniqueId() + "." + finalKey);
            case ITEMSTACK:
                if(!(target instanceof ItemStack)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't an item stack.");
                    return null;
                }
                ItemStack itemStack = (ItemStack) target;

                if(itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    debug.sendWarning(DebugType.CACHE, "Item stack is empty.");
                    return null;
                }

                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer itemPDC = meta.getPersistentDataContainer();
                NamespacedKey itemKey = new NamespacedKey(pluginName,key);
                return itemPDC.get(itemKey,PersistentDataType.STRING);
        }
        return null;
    }

    /**
     * This method returns data from cache
     * @param cacheSource {@link com.github.kpgtb.ktools.manager.cache.CacheSource}
     * @param target Object from you want to get data (or null if cacheSource is SERVER)
     * @param pluginName Name of plugin that saves this data
     * @param key Key of data
     * @return int of data or 0 if there isn't any data
     */
    public int getIntData(CacheSource cacheSource, Object target, String pluginName, String key) {
        String finalKey = pluginName + "-" + key;
        debug.sendInfo(DebugType.CACHE, "Getting cache with type " + cacheSource.name() + " key: " + finalKey +  "...");
        switch (cacheSource) {
            case SERVER:
                return cacheConfiguration.getInt("server."+finalKey);
            case PLAYER:
                if(!(target instanceof Player)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't a player!");
                    return 0;
                }
                Player player = (Player) target;
                boolean playerSaveMeta = config.getString("cache.player").equalsIgnoreCase("metadata");

                if(playerSaveMeta) {
                    PersistentDataContainer playerPDC = player.getPersistentDataContainer();
                    NamespacedKey playerKey = new NamespacedKey(pluginName,key);
                    return playerPDC.get(playerKey, PersistentDataType.INTEGER);
                }

                return cacheConfiguration.getInt("player." + player.getUniqueId().toString() + "." + finalKey);
            case ENTITY:
                if(!(target instanceof Entity)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't an entity.");
                    return 0;
                }
                Entity entity = (Entity) target;
                boolean entitySaveMeta = config.getString("cache.entity").equalsIgnoreCase("metadata");

                if(entitySaveMeta) {
                    PersistentDataContainer entityPDC = entity.getPersistentDataContainer();
                    NamespacedKey entityKey = new NamespacedKey(pluginName,key);
                    return entityPDC.get(entityKey, PersistentDataType.INTEGER);
                }

                return cacheConfiguration.getInt("entity." + entity.getUniqueId() + "." + finalKey);
            case ITEMSTACK:
                if(!(target instanceof ItemStack)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't an item stack.");
                    return 0;
                }
                ItemStack itemStack = (ItemStack) target;

                if(itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    debug.sendWarning(DebugType.CACHE, "Item stack is empty.");
                    return 0;
                }

                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer itemPDC = meta.getPersistentDataContainer();
                NamespacedKey itemKey = new NamespacedKey(pluginName,key);
                return itemPDC.get(itemKey,PersistentDataType.INTEGER);
        }
        return 0;
    }

    /**
     * This method returns data from cache
     * @param cacheSource {@link com.github.kpgtb.ktools.manager.cache.CacheSource}
     * @param target Object from you want to get data (or null if cacheSource is SERVER)
     * @param pluginName Name of plugin that saves this data
     * @param key Key of data
     * @return double of data or 0.0 if there isn't any data
     */
    public double getDoubleData(CacheSource cacheSource, Object target, String pluginName, String key) {
        String finalKey = pluginName + "-" + key;
        debug.sendInfo(DebugType.CACHE, "Getting cache with type " + cacheSource.name() + " key: " + finalKey +  "...");
        switch (cacheSource) {
            case SERVER:
                return cacheConfiguration.getDouble("server."+finalKey);
            case PLAYER:
                if(!(target instanceof Player)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't a player!");
                    return 0.0;
                }
                Player player = (Player) target;
                boolean playerSaveMeta = config.getString("cache.player").equalsIgnoreCase("metadata");

                if(playerSaveMeta) {
                    PersistentDataContainer playerPDC = player.getPersistentDataContainer();
                    NamespacedKey playerKey = new NamespacedKey(pluginName,key);
                    return playerPDC.get(playerKey, PersistentDataType.DOUBLE);
                }

                return cacheConfiguration.getDouble("player." + player.getUniqueId().toString() + "." + finalKey);
            case ENTITY:
                if(!(target instanceof Entity)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't an entity.");
                    return 0.0;
                }
                Entity entity = (Entity) target;
                boolean entitySaveMeta = config.getString("cache.entity").equalsIgnoreCase("metadata");

                if(entitySaveMeta) {
                    PersistentDataContainer entityPDC = entity.getPersistentDataContainer();
                    NamespacedKey entityKey = new NamespacedKey(pluginName,key);
                    return entityPDC.get(entityKey, PersistentDataType.DOUBLE);
                }

                return cacheConfiguration.getDouble("entity." + entity.getUniqueId() + "." + finalKey);
            case ITEMSTACK:
                if(!(target instanceof ItemStack)) {
                    debug.sendWarning(DebugType.CACHE, "Target isn't an item stack.");
                    return 0.0;
                }
                ItemStack itemStack = (ItemStack) target;

                if(itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    debug.sendWarning(DebugType.CACHE, "Item stack is empty.");
                    return 0.0;
                }

                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer itemPDC = meta.getPersistentDataContainer();
                NamespacedKey itemKey = new NamespacedKey(pluginName,key);
                return itemPDC.get(itemKey,PersistentDataType.DOUBLE);
        }
        return 0.0;
    }

    /**
     * This method returns data from server's cache
     * @param pluginName Name of plugin that saves this data
     * @param key Key of data
     * @return String of data or null if there isn't any data
     */
    public String getStringServerData(String pluginName, String key) {
        return this.getStringData(CacheSource.SERVER, null,pluginName,key);
    }

    /**
     * This method returns data from cache
     * @param pluginName Name of plugin that saves this data
     * @param key Key of data
     * @return int of data or 0 if there isn't any data
     */
    public int getIntServerData(String pluginName, String key) {
        return this.getIntData(CacheSource.SERVER, null,pluginName,key);
    }

    /**
     * This method returns data from cache
     * @param pluginName Name of plugin that saves this data
     * @param key Key of data
     * @return double of data or 0.0 if there isn't any data
     */
    public double getDoubleServerData(String pluginName, String key) {
        return this.getDoubleData(CacheSource.SERVER, null,pluginName,key);
    }
}
