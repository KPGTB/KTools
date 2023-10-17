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

package com.github.kpgtb.ktools.listener;

import com.github.kpgtb.ktools.manager.cache.CacheManager;
import com.github.kpgtb.ktools.manager.listener.KListener;
import com.github.kpgtb.ktools.manager.resourcepack.ResourcePackManager;
import com.github.kpgtb.ktools.manager.ui.bar.BarManager;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * JoinListener handles setting resourcepack when player join to the server
 */
public class JoinListener extends KListener {
    private final CacheManager cacheManager;
    private final ResourcePackManager resourcepackManager;
    private final JavaPlugin plugin;
    private final boolean legacy;
    private final BarManager barManager;
    /**
     * Constructor of listener.
     *
     * @param wrapper ToolsObjectWrapper or object that extends it.
     */
    public JoinListener(ToolsObjectWrapper wrapper) {
        super(wrapper);
        this.cacheManager = wrapper.getCacheManager();
        this.resourcepackManager = wrapper.getResourcePackManager();
        this.plugin = wrapper.getPlugin();
        this.legacy = wrapper.isLegacy();
        this.barManager = wrapper.getBarManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(legacy) {
            return;
        }
        Player player = event.getPlayer();
        if(resourcepackManager.isEnabled()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        return;
                    }
                    String url = cacheManager.getServerData("ktools", "resourcepackUrl", String.class);
                    if (url == null) {
                        return;
                    }
                    player.setResourcePack(url);
                }
            }.runTaskLater(plugin, 60);
        }

        this.barManager.getBars().values().forEach(bar -> {
            if(bar.isDefaultShow()) {
                this.barManager.showBar(bar,player);
            }
        });
    }
}
