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

import com.github.kpgtb.ktools.manager.debug.DebugType;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Logger;

/**
 * DebugManager handles debug messages sent to console
 */
public class DebugManager {
    private final FileConfiguration config;
    private final Logger logger;

    /**
     * Creates new instance of DebugManager
     * @param config Config.yml of Ktools
     * @param logger Minecraft console logger
     */
    public DebugManager(FileConfiguration config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    /**
     * Check if this type of debug is enabled in config.yml
     * @param type {@link com.github.kpgtb.ktools.manager.debug.DebugType} enum
     * @return true if this type is enabled in config.yml
     */
    private boolean isEnabled(DebugType type) {
        return config.getBoolean("debug.enabled") && config.getBoolean(type.getConfigStr());
    }

    /**
     * Send info debug message to console
     * @param type {@link com.github.kpgtb.ktools.manager.debug.DebugType} enum
     * @param message Message to send
     */
    public void sendInfo(DebugType type, String message) {
        this.sendInfo(type,message,false);
    }

    /**
     * Send info debug message to console
     * @param type {@link com.github.kpgtb.ktools.manager.debug.DebugType} enum
     * @param message Message to send
     * @param force If true, message will be sent also if this type is disabled in config
     */
    public void sendInfo(DebugType type, String message, boolean force) {
        if(!isEnabled(type) && !force) {
            return;
        }
        logger.info("[DEBUG]" + message);
    }

    /**
     * Send warning debug message to console
     * @param type {@link com.github.kpgtb.ktools.manager.debug.DebugType} enum
     * @param message Message to send
     */
    public void sendWarning(DebugType type, String message) {
        this.sendWarning(type,message,false);
    }

    /**
     * Send warning debug message to console
     * @param type {@link com.github.kpgtb.ktools.manager.debug.DebugType} enum
     * @param message Message to send
     * @param force If true, message will be sent also if this type is disabled in config
     */
    public void sendWarning(DebugType type, String message, boolean force) {
        if(!isEnabled(type) && !force) {
            return;
        }
        logger.warning("[DEBUG]" + message);
    }
}
