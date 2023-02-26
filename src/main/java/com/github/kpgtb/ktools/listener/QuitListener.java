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

import com.github.kpgtb.ktools.manager.listener.Klistener;
import com.github.kpgtb.ktools.manager.ui.UiManager;
import com.github.kpgtb.ktools.util.ToolsObjectWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class QuitListener extends Klistener {
    private final UiManager uiManager;

    /**
     * Constructor of listener.
     *
     * @param toolsObjectWrapper ToolsObjectWrapper or object that extends it.
     */
    public QuitListener(ToolsObjectWrapper toolsObjectWrapper) {
        super(toolsObjectWrapper);
        this.uiManager = toolsObjectWrapper.getUiManager();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if(uiManager == null) {
            return;
        }
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        uiManager.removeAllUI(uuid);
        uiManager.removeAllActionBars(uuid);
    }
}
