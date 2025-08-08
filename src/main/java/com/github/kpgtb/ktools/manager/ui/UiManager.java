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

package com.github.kpgtb.ktools.manager.ui;

import com.comphenix.protocol.ProtocolManager;
import com.github.kpgtb.ktools.KTools;
import com.github.kpgtb.ktools.util.ui.FontWidth;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import com.google.gson.JsonParser;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * UIManager handles custom UIs on server
 */
public class UiManager {
    private final HashMap<UUID, ArrayList<BaseUiObject>> ui;
    private final HashMap<UUID, ArrayList<String>> standardActionBars;

    @Getter
    private boolean sending;
    @Getter
    private boolean required;
    private final KTools plugin;
    private BukkitTask task;
    private PacketSendingListener packet;
    private final ProtocolManager protocolManager;

    /**
     * Constructor of this manager
     * @param plugin Instance of plugin
     * @param protocolManager Instance of ProtocolManager (ProtocolLib)
     */
    public UiManager(KTools plugin, ProtocolManager protocolManager) {
        this.plugin = plugin;
        this.ui = new HashMap<>();
        this.standardActionBars = new HashMap<>();
        this.sending = false;
        this.required = false;
        this.protocolManager = protocolManager;
    }

    /**
     * Mark ui manager as required
     * @param required
     */
    public void setRequired(boolean required) {
        this.required = required;
        if(required) {
            runActionBar();
            return;
        }
        stopActionBar();
    }

    private void runActionBar() {
        stopActionBar();
        packet = new PacketSendingListener(plugin,this,protocolManager);
        task = new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    Component component = Component.text("");

                    if(ui.containsKey(player.getUniqueId())) {
                        for(BaseUiObject obj : ui.get(player.getUniqueId())) {
                            component = component.append(obj.getComponentToShow());
                        }
                    }

                    if(standardActionBars.containsKey(player.getUniqueId())) {
                        String text = "";
                        for(String s : standardActionBars.get(player.getUniqueId())) {
                            text = s;
                        }

                        BaseUiObject obj = new BaseUiObject(plugin.getToolsObjectWrapper().getLanguageManager()
                            .convertLegacyStringToComponent(text), Alignment.CENTER, 0,plugin.getToolsObjectWrapper());
                        component.append(obj.getComponentToShow());
                    }

                    if(!plugin.getToolsObjectWrapper().getLanguageManager().convertComponentToString(component).isEmpty()) {
                        sending = true;
                        plugin.getToolsObjectWrapper().getAdventure().player(player).sendActionBar(component);
                        sending = false;
                    }
                }
            }
        }.runTaskTimer(plugin, 5,5);
    }
    private void stopActionBar() {
        if(packet != null) {
            packet.disable();
            packet = null;
        }
        if(task == null) {
            return;
        }
        task.cancel();
    }

    /**
     * Add custom UI to player
     * @param uuid UUID od player
     * @param BaseUiObject Object that represents UI
     */
    public void addUI(UUID uuid, BaseUiObject BaseUiObject) {
        if(!ui.containsKey(uuid)) {
            ui.put(uuid, new ArrayList<>());
        }

        ArrayList<BaseUiObject> uis = ui.get(uuid);
        uis.add(BaseUiObject);

        ui.replace(uuid, uis);
    }

    /**
     * Remove custom UI from player
     * @param uuid UUID od player
     * @param BaseUiObject Object that represents UI
     */
    public void removeUI(UUID uuid, BaseUiObject BaseUiObject) {
        if(!ui.containsKey(uuid)) {
            ui.put(uuid, new ArrayList<>());
        }
        ArrayList<BaseUiObject> uis = ui.get(uuid);
        uis.remove(BaseUiObject);

        ui.replace(uuid, uis);
    }

    /**
     * Send normal actionbar to player
     * @param uuid UUID of player
     * @param text Text of actionbar
     * @param time Time in ticks
     */
    public void addActionBar(UUID uuid, String text, int time) {
        if(!standardActionBars.containsKey(uuid)) {
            standardActionBars.put(uuid, new ArrayList<>());
        }

        ArrayList<String> actionBars = standardActionBars.get(uuid);
        actionBars.add(text);

        standardActionBars.replace(uuid, actionBars);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                removeActionBar(uuid, text);
            }
        }.runTaskLater(plugin, time);
    }

    /**
     * Remove normal actionbar from player
     * @param uuid UUID of player
     * @param text Text of actionbar
     */
    public void removeActionBar(UUID uuid, String text) {
        if(!standardActionBars.containsKey(uuid)) {
            standardActionBars.put(uuid, new ArrayList<>());
        }

        ArrayList<String> actionbars = standardActionBars.get(uuid);
        actionbars.remove(text);

        standardActionBars.replace(uuid, actionbars);
    }

    public ArrayList<BaseUiObject> getUI(UUID uuid) {
        if(!ui.containsKey(uuid)) {
            return new ArrayList<>();
        }

        return ui.get(uuid);
    }

    public ArrayList<String> getStandardActionBars(UUID uuid) {
        if(!standardActionBars.containsKey(uuid)) {
            return new ArrayList<>();
        }

        return standardActionBars.get(uuid);
    }

    public void removeAllUI(UUID uuid) {
        ui.remove(uuid);
    }

    public void removeAllActionBars(UUID uuid) {
        standardActionBars.remove(uuid);
    }
}
