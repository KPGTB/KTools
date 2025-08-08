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

import com.github.kpgtb.ktools.manager.updater.version.KVersion;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Fix text to not show shadow
 * Thanks -> https://github.com/PuckiSilver/NoShadow
 * @since 1.6.0
 */
public class NoShadow {
    /**
     * Disable shadow in string (Resourcepack must be enabled!)
     * @param str Text
     * @param wrapper Instance of ToolsObjectWrapper
     * @return String without shadow
     */
    public static Component disableShadow(String str, ToolsObjectWrapper wrapper, Player player) {
        String version = Bukkit.getBukkitVersion()
                .split("-")[0];

        KVersion mcVersion = new KVersion(version);

        boolean shouldHandleShadow = mcVersion.isNewerThan(new KVersion("1.19.0"));
        boolean minecraftShadowHandling = mcVersion.isNewerOrEquals(new KVersion("1.21.4"));
        boolean clientShadowHandling = minecraftShadowHandling;


        if(Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
            ViaAPI api = Via.getAPI();
            int protVer = api.getPlayerVersion(player);

            shouldHandleShadow = protVer >= ProtocolVersion.v1_19.getVersion();
            clientShadowHandling = protVer >= ProtocolVersion.v1_21_4.getVersion();
        }

        boolean fixShadow = shouldHandleShadow && !clientShadowHandling && wrapper.getKTools().getConfig().getBoolean("fixShadowsOnActionBars");

        if(fixShadow) {
            return MiniMessage.miniMessage()
                .deserialize("<color:#4e5c24>" + str);
        } else if(clientShadowHandling && minecraftShadowHandling) {
            return MiniMessage.miniMessage()
                .deserialize("<shadow:yellow:0.1>" + str);
        } else {
            return MiniMessage.miniMessage()
                .deserialize(str);
        }
    }
}
