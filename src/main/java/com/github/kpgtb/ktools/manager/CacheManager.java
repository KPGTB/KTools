package com.github.kpgtb.ktools.manager;

import com.github.kpgtb.ktools.manager.cache.CacheSource;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CacheManager {
    private final FileConfiguration config;
    private final File dataFolder;
    private final DebugManager debug;

    private File cacheFile;
    private FileConfiguration cacheConfiguration;

    public CacheManager(FileConfiguration config, File dataFolder, DebugManager debug) {
        this.config = config;
        this.dataFolder = dataFolder;
        this.debug = debug;
    }

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

    public void setData(CacheSource cacheSource, Object target, String pluginName, String key, Object data) {
        String finalKey = pluginName + "-" + key;
        switch (cacheSource) {
            case SERVER:
                cacheConfiguration.set("server."+finalKey, data);
                try {
                    cacheConfiguration.save(cacheFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
        }
    }

    public void setServerData(String pluginName, String key, Object data) {
        this.setData(CacheSource.SERVER, null, pluginName,key,data);
    }

}
