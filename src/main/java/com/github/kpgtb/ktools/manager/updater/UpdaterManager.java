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

package com.github.kpgtb.ktools.manager.updater;

import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * UpdaterManager handles checking updates in plugin
 */
public class UpdaterManager {
    private final PluginDescriptionFile description;
    private final IUpdater updater;
    private final DebugManager debug;

    /**
     * Constructor of this manager
     * @param description Description of plugin JavaPlugin#getDescription()
     * @param updater Instance of IUpdater interface
     * @param debug Instance of DebugManager
     */
    public UpdaterManager(PluginDescriptionFile description, IUpdater updater, DebugManager debug) {
        this.description = description;
        this.updater = updater;
        this.debug = debug;
    }

    /**
     * Check if there are some updates
     */
    public void checkUpdate() {
        double version;
        try {
            String versionName = description.getVersion();
            String[] versionParts = versionName.split("-");
            version = Double.parseDouble(versionParts[0]);
        } catch (Exception e) {
            this.debug.sendWarning(DebugType.UPDATER, "Error while checking plugin version");
            return;
        }
        if(this.updater.hasUpdate(version)) {
            String url = this.updater.getDownloadLink();
            this.debug.sendInfo(DebugType.UPDATER, "Detected new version of this plugin! Download it here -> " + url, true);
            return;
        }
        this.debug.sendInfo(DebugType.UPDATER, "You have the newest version of this plugin");
    }
}
