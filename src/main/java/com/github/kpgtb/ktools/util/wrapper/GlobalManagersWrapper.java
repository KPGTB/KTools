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

package com.github.kpgtb.ktools.util.wrapper;

import com.github.kpgtb.ktools.manager.cache.CacheManager;
import com.github.kpgtb.ktools.manager.command.parser.ParamParserManager;
import com.github.kpgtb.ktools.manager.data.DataManager;
import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.item.ItemManager;
import com.github.kpgtb.ktools.manager.language.LanguageManager;
import com.github.kpgtb.ktools.manager.resourcepack.ResourcePackManager;
import com.github.kpgtb.ktools.manager.ui.UiManager;
import com.github.kpgtb.ktools.manager.ui.bar.BarManager;

/**
 * Wrapper with all objects that should have only one instance - from KTools
 */
public class GlobalManagersWrapper {
    private final DebugManager debugManager;
    private final LanguageManager globalLanguageManager;
    private final CacheManager cacheManager;
    private final ParamParserManager paramParserManager;
    private final DataManager dataManager;
    private final UiManager uiManager;
    private final ResourcePackManager resourcepackManager;
    private final ItemManager itemManager;
    private final BarManager barManager;
    private final boolean legacy;

    public GlobalManagersWrapper(DebugManager debugManager, LanguageManager globalLanguageManager, CacheManager cacheManager, ParamParserManager paramParserManager, DataManager dataManager, UiManager uiManager, ResourcePackManager resourcepackManager, ItemManager itemManager, BarManager barManager, boolean legacy) {
        this.debugManager = debugManager;
        this.globalLanguageManager = globalLanguageManager;
        this.cacheManager = cacheManager;
        this.paramParserManager = paramParserManager;
        this.dataManager = dataManager;
        this.uiManager = uiManager;
        this.resourcepackManager = resourcepackManager;
        this.itemManager = itemManager;
        this.barManager = barManager;
        this.legacy = legacy;
    }

    public BarManager getBarManager() {
        return barManager;
    }

    public DebugManager getDebugManager() {
        return debugManager;
    }

    public LanguageManager getGlobalLanguageManager() {
        return globalLanguageManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public ParamParserManager getParamParserManager() {
        return paramParserManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public UiManager getUiManager() {
        return uiManager;
    }

    public ResourcePackManager getResourcepackManager() {
        return resourcepackManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public boolean isLegacy() {
        return legacy;
    }
}
