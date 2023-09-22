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

package com.github.kpgtb.ktools.manager.gui;

import com.github.kpgtb.ktools.manager.gui.write.IWriteResponse;
import com.github.kpgtb.ktools.manager.language.LanguageLevel;
import com.github.kpgtb.ktools.util.item.ItemBuilder;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

/**
 * Write GUI is a gui with response when you can write sth
 */
public class KWriteGui implements Listener {
    private final IWriteResponse response;
    private final KGui lastGui;
    private final Player player;
    private final ToolsObjectWrapper wrapper;
    private boolean responsed;

    public KWriteGui(ToolsObjectWrapper wrapper,  KGui lastGui, Player player, IWriteResponse response) {
        this.response = response;
        this.lastGui = lastGui;
        this.player = player;
        this.wrapper = wrapper;
        this.responsed = false;
    }

    /**
     * Open GUI to player
     */
    public void open() {
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> {
                    if(responsed) {
                        return;
                    }
                    response.response("");
                    responsed = true;
                    if(lastGui != null) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                lastGui.open(stateSnapshot.getPlayer());
                            }
                        }.runTaskLater(wrapper.getPlugin(), 3);
                    }
                })
                .onClick((slot,stateSnapshot) -> {
                    if(!slot.equals(AnvilGUI.Slot.OUTPUT)) {
                        return Arrays.asList(AnvilGUI.ResponseAction.close());
                    }

                    response.response(stateSnapshot.getText());
                    responsed = true;
                    if(lastGui != null) {
                        lastGui.open(stateSnapshot.getPlayer());
                    }
                    return Arrays.asList(AnvilGUI.ResponseAction.close());
                })
                .text(
                        wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "writeGuiPlaceholder")
                )
                .itemLeft(
                        new ItemBuilder(Material.PAPER).build()
                )
                .title(
                        wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "writeGuiName")
                )
                .plugin(wrapper.getPlugin())
                .open(player);
    }
}
