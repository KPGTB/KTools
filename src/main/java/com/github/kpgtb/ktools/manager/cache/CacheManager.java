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

package com.github.kpgtb.ktools.manager.cache;

import com.github.kpgtb.ktools.manager.debug.DebugManager;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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
     * With this method you can save data to cache
     * @param target Object where you want to save data (or null if cacheSource is SERVER)
     * @param pluginName Name of plugin that save this data
     * @param key Key of data
     * @param data Object with data
     */
    @SuppressWarnings("unchecked")
    public <T> void setData(Object target, String pluginName, String key, T data) {
        CacheSource cacheSource = getSource(target);

        String finalKey = pluginName + "-" + key;
        debug.sendInfo(DebugType.CACHE, "Saving cache with type " + cacheSource.name() + " key: " + finalKey + " data ("+data.getClass().getSimpleName()+"): " + data+ "...");

        Class<T> clazz = (Class<T>) data.getClass();
        PersistentDataType<T,T> pdcType = getPdcType(clazz);

        if(pdcType == null) {
            throw new IllegalArgumentException("You try to save wrong type!");
        }

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
                Player player = (Player) target;
                boolean playerSaveMeta = config.getString("cache.player").equalsIgnoreCase("metadata");

                if(playerSaveMeta) {
                    PersistentDataContainer playerPDC = player.getPersistentDataContainer();
                    NamespacedKey playerKey = new NamespacedKey(pluginName,key);
                    playerPDC.set(playerKey, pdcType, data);
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
                Entity entity = (Entity) target;
                boolean entitySaveMeta = config.getString("cache.entity").equalsIgnoreCase("metadata");

                if(entitySaveMeta) {
                    PersistentDataContainer entityPDC = entity.getPersistentDataContainer();
                    NamespacedKey entityKey = new NamespacedKey(pluginName,key);
                    entityPDC.set(entityKey, pdcType, data);
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
                ItemStack itemStack = (ItemStack) target;

                if(itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    debug.sendWarning(DebugType.CACHE, "Item stack is empty.");
                    return;
                }

                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer itemPDC = meta.getPersistentDataContainer();
                NamespacedKey itemKey = new NamespacedKey(pluginName,key);
                itemPDC.set(itemKey, pdcType, data);
                itemStack.setItemMeta(meta);

                debug.sendInfo(DebugType.CACHE, "Saved item stack cache in metadata.");

                break;
        }
    }

    /**
     * With this method you can save string data to server's cache
     * @param pluginName Name of plugin that save this data
     * @param key Key of data
     * @param data Object with data
     */
    public <T> void setServerData(String pluginName, String key, T data) {
        this.setData(null, pluginName,key,data);
    }

    /**
     * With this method you can remove data from cache
     * @param target Object where you want to save data (or null if cacheSource is SERVER)
     * @param pluginName Name of plugin that save this data
     * @param key Key of data
     * @since 1.3.0
     */
    public void removeData( Object target, String pluginName, String key) {
        CacheSource cacheSource = getSource(target);
        String finalKey = pluginName + "-" + key;
        debug.sendInfo(DebugType.CACHE, "Removing cache with type " + cacheSource.name() + " key: " + finalKey + "...");

        switch (cacheSource) {
            case SERVER:
                cacheConfiguration.set("server."+finalKey, null);
                try {
                    cacheConfiguration.save(cacheFile);
                    debug.sendInfo(DebugType.CACHE, "Removed server cache!");
                } catch (IOException e) {
                    debug.sendWarning(DebugType.CACHE, "Error while removing server cache!");
                    throw new RuntimeException(e);
                }
                break;
            case PLAYER:
                Player player = (Player) target;
                boolean playerSaveMeta = config.getString("cache.player").equalsIgnoreCase("metadata");

                if(playerSaveMeta) {
                    PersistentDataContainer playerPDC = player.getPersistentDataContainer();
                    NamespacedKey playerKey = new NamespacedKey(pluginName,key);
                    playerPDC.remove(playerKey);
                    debug.sendInfo(DebugType.CACHE, "Removed player cache from metadata.");
                    return;
                }

                cacheConfiguration.set("player." + player.getUniqueId().toString() + "." + finalKey, null);
                try {
                    cacheConfiguration.save(cacheFile);
                    debug.sendInfo(DebugType.CACHE, "Removed player cache from files.");
                } catch (IOException e) {
                    debug.sendWarning(DebugType.CACHE, "Error while removing player cache from files.");
                    throw new RuntimeException(e);
                }
                break;
            case ENTITY:
                Entity entity = (Entity) target;
                boolean entitySaveMeta = config.getString("cache.entity").equalsIgnoreCase("metadata");

                if(entitySaveMeta) {
                    PersistentDataContainer entityPDC = entity.getPersistentDataContainer();
                    NamespacedKey entityKey = new NamespacedKey(pluginName,key);
                    entityPDC.remove(entityKey);
                    debug.sendInfo(DebugType.CACHE, "Removing entity cache from metadata.");
                    return;
                }

                cacheConfiguration.set("entity." + entity.getUniqueId() + "." + finalKey, null);
                try {
                    cacheConfiguration.save(cacheFile);
                    debug.sendInfo(DebugType.CACHE, "Removed entity cache from files.");
                } catch (IOException e) {
                    debug.sendWarning(DebugType.CACHE, "Error while removing entity cache from files.");
                    throw new RuntimeException(e);
                }
                break;
            case ITEMSTACK:
                ItemStack itemStack = (ItemStack) target;

                if(itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    debug.sendWarning(DebugType.CACHE, "Item stack is empty.");
                    return;
                }

                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer itemPDC = meta.getPersistentDataContainer();
                NamespacedKey itemKey = new NamespacedKey(pluginName,key);
                itemPDC.remove(itemKey);
                itemStack.setItemMeta(meta);

                debug.sendInfo(DebugType.CACHE, "Removed item stack cache from metadata.");

                break;
        }
    }

    /**
     * With this method you can remove data from server's cache
     * @param pluginName Name of plugin that save this data
     * @param key Key of data
     * @since 1.3.0
     */
    public void removeServerData( String pluginName, String key) {
        this.removeData(null, pluginName,key);
    }

    /**
     * This method returns data from cache
     * @param target Object from you want to get data (or null if cacheSource is SERVER)
     * @param pluginName Name of plugin that saves this data
     * @param key Key of data
     * @param expected Class that is expected in return
     * @return Object with data or null if there isn't any data
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getData(Object target, String pluginName, String key, Class<T> expected) {
        CacheSource cacheSource = getSource(target);
        PersistentDataType<T,T> pdcType = getPdcType(expected);

        if(pdcType == null) {
            throw new IllegalArgumentException("You try to save wrong type!");
        }

        String finalKey = pluginName + "-" + key;
        debug.sendInfo(DebugType.CACHE, "Getting cache with type " + cacheSource.name() + " key: " + finalKey +  "...");
        switch (cacheSource) {
            case SERVER:
                return (T) cacheConfiguration.get("server."+finalKey);
            case PLAYER:
                Player player = (Player) target;
                boolean playerSaveMeta = config.getString("cache.player").equalsIgnoreCase("metadata");

                if(playerSaveMeta) {
                    PersistentDataContainer playerPDC = player.getPersistentDataContainer();
                    NamespacedKey playerKey = new NamespacedKey(pluginName,key);
                    return playerPDC.get(playerKey, pdcType);
                }

                return (T) cacheConfiguration.get("player." + player.getUniqueId().toString() + "." + finalKey);
            case ENTITY:
                Entity entity = (Entity) target;
                boolean entitySaveMeta = config.getString("cache.entity").equalsIgnoreCase("metadata");

                if(entitySaveMeta) {
                    PersistentDataContainer entityPDC = entity.getPersistentDataContainer();
                    NamespacedKey entityKey = new NamespacedKey(pluginName,key);
                    return entityPDC.get(entityKey, pdcType);
                }

                return (T) cacheConfiguration.get("entity." + entity.getUniqueId() + "." + finalKey);
            case ITEMSTACK:
                ItemStack itemStack = (ItemStack) target;

                if(itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    debug.sendWarning(DebugType.CACHE, "Item stack is empty.");
                    return null;
                }

                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer itemPDC = meta.getPersistentDataContainer();
                NamespacedKey itemKey = new NamespacedKey(pluginName,key);
                return itemPDC.get(itemKey,pdcType);
        }
        return null;
    }

    /**
     * This method returns data from server's cache
     * @param pluginName Name of plugin that saves this data
     * @param key Key of data
     * @param expected Class that is expected in return
     * @return Object with data or null if there isn't any data
     */
    @Nullable
    public <T> T getServerData(String pluginName, String key, Class<T> expected) {
        return this.getData(null,pluginName,key, expected);
    }

    /**
     * This method returns data from cache or defined data
     * @param target Object from you want to get data (or null if cacheSource is SERVER)
     * @param pluginName Name of plugin that saves this data
     * @param key Key of data
     * @param or Data that should be returned when data is null
     * @return Object with data
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> T getDataOr(Object target, String pluginName, String key, T or) {
        Class<T> expected = (Class<T>) or.getClass();
        if(!hasData(target, pluginName,key,expected)) {
            return or;
        }

        T result = getData(target,pluginName,key,expected);
        if(result == null) {
            return or;
        }

        return result;
    }

    /**
     * This method returns data from server's cache or defined data
     * @param pluginName Name of plugin that saves this data
     * @param key Key of data
     * @param or Data that should be returned when data is null
     * @return Object with data
     */
    @NotNull
    public <T> T getServerDataOr(String pluginName, String key, T or) {
        return getDataOr(null,pluginName,key,or);
    }

    /**
     * This method checks if cache contains data
     * @param target Object from you want to get data (or null if cacheSource is SERVER)
     * @param pluginName Name of plugin that saves this data
     * @param key Key of data
     * @param expected Class that should be checked
     * @return true if exists
     * @since 1.3.0
     */
    public <T> boolean hasData(Object target, String pluginName, String key, Class<T> expected) {
        try {
            T data = getData(target,pluginName,key,expected);

            if(data == null) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This method checks if server's cache contains data
     * @param pluginName Name of plugin that saves this data
     * @param key Key of data
     * @param expected Class that should be checked
     * @return true if exists
     * @since 1.3.0
     */
    public <T> boolean hasServerData(String pluginName, String key, Class<T> expected) {
        return this.hasData( null,pluginName,key,expected);
    }

    /**
     * This method returns PersistentDataType from class
     * @param clazz Class that is expected
     * @return PersistentDataType of this class or null if there isn't any PDT with this class
     */
    @Nullable
    @SuppressWarnings("unchecked")
    private <Z> PersistentDataType<Z,Z> getPdcType(Class<Z> clazz) {
        HashMap<Class<?>, PersistentDataType<?,?>> acceptedTypes = new HashMap<>();
        acceptedTypes.put(Byte.class, PersistentDataType.BYTE);
        acceptedTypes.put(Short.class, PersistentDataType.SHORT);
        acceptedTypes.put(Integer.class, PersistentDataType.INTEGER);
        acceptedTypes.put(Long.class, PersistentDataType.LONG);
        acceptedTypes.put(Float.class, PersistentDataType.FLOAT);
        acceptedTypes.put(Double.class, PersistentDataType.DOUBLE);
        acceptedTypes.put(String.class, PersistentDataType.STRING);
        acceptedTypes.put(byte[].class, PersistentDataType.BYTE_ARRAY);
        acceptedTypes.put(int[].class, PersistentDataType.INTEGER_ARRAY);
        acceptedTypes.put(long[].class, PersistentDataType.LONG_ARRAY);
        return (PersistentDataType<Z, Z>) acceptedTypes.get(clazz);
    }

    /**
     * THis method returns cachesource from target
     * @param target Object that is the target
     * @return CacheSource of target
     * @throws IllegalArgumentException when target can't be converted to cache source
     */
    private CacheSource getSource(Object target) {
        if(target == null) {
            return CacheSource.SERVER;
        }
        if (target instanceof Player) return CacheSource.PLAYER;
        if (target instanceof Entity) return CacheSource.ENTITY;
        if (target instanceof ItemStack) return CacheSource.ITEMSTACK;

        throw new IllegalArgumentException("Wrong target in cache. Excepted: null, player, entity, itemstack. Recived: " + target.getClass());
    }
}
