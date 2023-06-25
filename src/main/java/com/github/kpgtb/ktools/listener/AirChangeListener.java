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

import com.github.kpgtb.ktools.manager.listener.KListener;
import com.github.kpgtb.ktools.manager.ui.bar.BarManager;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AirChangeListener extends KListener {
    private final BarManager barManager;
    private final JavaPlugin plugin;

    public AirChangeListener(ToolsObjectWrapper toolsObjectWrapper) {
        super(toolsObjectWrapper);
        this.barManager = toolsObjectWrapper.getBarManager();
        this.plugin = toolsObjectWrapper.getPlugin();
    }

    @EventHandler
    public void onChange(EntityAirChangeEvent event) {
        if(!(event.getEntity() instanceof Player)) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                barManager.updateBars((Player) event.getEntity());
            }
        }.runTaskLater(plugin,3L);
    }
}
