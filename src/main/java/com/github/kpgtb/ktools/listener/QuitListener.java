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
import com.github.kpgtb.ktools.manager.ui.UiManager;
import com.github.kpgtb.ktools.manager.ui.bar.BarManager;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * QuitListener handles removing ui from player when they leave the server
 */
public class QuitListener extends KListener {
    private final UiManager uiManager;
    private final BarManager barManager;

    /**
     * Constructor of listener.
     *
     * @param wrapper ToolsObjectWrapper or object that extends it.
     */
    public QuitListener(ToolsObjectWrapper wrapper) {
        super(wrapper);
        this.uiManager = wrapper.getUiManager();
        this.barManager = wrapper.getBarManager();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if(uiManager == null) {
            return;
        }
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        barManager.hideAllBars(player);
        uiManager.removeAllUI(uuid);
        uiManager.removeAllActionBars(uuid);
    }
}
