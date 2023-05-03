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

package com.github.kpgtb.ktools.util.ui;

import com.github.kpgtb.ktools.manager.language.LanguageManager;
import com.github.kpgtb.ktools.manager.updater.version.KVersion;
import org.bukkit.Bukkit;

/**
 * Fix text to not show shadow
 * Thanks -> https://github.com/PuckiSilver/NoShadow
 * @since 1.6.0
 */
public class NoShadow {
    /**
     * Disable shadow in string (Resourcepack must be enabled!)
     * @param str Text
     * @param languageManager Instance of language manager
     * @return String without shadow
     */
    public static String disableShadow(String str, LanguageManager languageManager) {
        String version = Bukkit.getBukkitVersion()
                .split("-")[0];
        KVersion mcVersion = new KVersion(version);

        boolean fixShadow = mcVersion.isNewerThan(new KVersion("1.19.0"));

        if(fixShadow) {
            return languageManager.convertMmToString("<color:#4e5c24>" + str);
        } else {
            return str;
        }
    }
}
