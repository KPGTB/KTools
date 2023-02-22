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

package com.github.kpgtb.ktools.util;

import com.github.kpgtb.ktools.manager.CacheManager;
import com.github.kpgtb.ktools.manager.DebugManager;
import com.github.kpgtb.ktools.manager.LanguageManager;
import com.github.kpgtb.ktools.manager.ParamParserManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Wrapper with all necessary objects
 */
public class ToolsObjectWrapper {
    private final CacheManager cacheManager;
    private final DebugManager debugManager;
    private final LanguageManager languageManager;
    private final FileConfiguration config;
    private final BukkitAudiences adventure;
    private final ParamParserManager paramParserManager;

    public ToolsObjectWrapper(CacheManager cacheManager, DebugManager debugManager, LanguageManager languageManager, FileConfiguration config, BukkitAudiences adventure, ParamParserManager paramParserManager) {
        this.cacheManager = cacheManager;
        this.debugManager = debugManager;
        this.languageManager = languageManager;
        this.config = config;
        this.adventure = adventure;
        this.paramParserManager = paramParserManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public DebugManager getDebugManager() {
        return debugManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public BukkitAudiences getAdventure() {
        return adventure;
    }

    public ParamParserManager getParamParserManager() {
        return paramParserManager;
    }
}
