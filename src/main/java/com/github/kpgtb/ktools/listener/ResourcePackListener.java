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

import com.github.kpgtb.ktools.manager.language.LanguageLevel;
import com.github.kpgtb.ktools.manager.listener.KListener;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class ResourcePackListener extends KListener {
    private final ToolsObjectWrapper wrapper;
    public ResourcePackListener(ToolsObjectWrapper toolsObjectWrapper) {
        super(toolsObjectWrapper);
        this.wrapper = toolsObjectWrapper;
    }

    @EventHandler
    public void onResourcePack(PlayerResourcePackStatusEvent event) {
        if(wrapper.getResourcePackManager() == null) {
            return;
        }
        if(!wrapper.getResourcePackManager().isEnabled()) {
            return;
        }
        if(event.getStatus().equals(PlayerResourcePackStatusEvent.Status.DECLINED)) {
            StringBuilder reason = new StringBuilder();
            wrapper.getLanguageManager().getString(LanguageLevel.GLOBAL, "resourcePackDeny").forEach(msg -> {
                reason.append(msg).append("\n");
            });
            event.getPlayer().kickPlayer(reason.toString());
        }
    }
}
