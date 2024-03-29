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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.github.kpgtb.ktools.manager.updater.version.KVersion;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles all actionbars on server
 */
public class PacketSendingListener {
    private final ProtocolManager protocolManager;
    private final List<PacketAdapter> packetAdapter;

    public PacketSendingListener(JavaPlugin plugin, UiManager uiManager, ProtocolManager protocolManager) {
        this.protocolManager = protocolManager;
        packetAdapter = new ArrayList<>();
        if(new KVersion(Bukkit.getBukkitVersion().split("-")[0])
                .isNewerOrEquals("1.17")) {
            packetAdapter.add(new Manager_1_17(plugin,uiManager));
        } else {
            packetAdapter.add(new Manager_1_16(plugin,uiManager));
        }
        if(new KVersion(Bukkit.getBukkitVersion().split("-")[0])
                .isNewerOrEquals("1.19")) {
            packetAdapter.add(new Manager_System(plugin,uiManager));
        } else {
            packetAdapter.add(new Manager_Chat(plugin,uiManager));
        }

        packetAdapter.forEach(protocolManager::addPacketListener);
    }

    public void disable() { packetAdapter.forEach(protocolManager::removePacketListener); }

    private static class Manager_1_17 extends PacketAdapter {
        private final UiManager uiManager;
        public Manager_1_17(JavaPlugin plugin, UiManager uiManager) {
            super(plugin, PacketType.Play.Server.SET_ACTION_BAR_TEXT);
            this.uiManager = uiManager;
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if (event.isCancelled() || uiManager.isSending()) { return; }
            PacketContainer packet = event.getPacket();
            StructureModifier<WrappedChatComponent> chatComponents = packet.getChatComponents();
            if(chatComponents.size() == 0) {return;}
            WrappedChatComponent component = chatComponents.read(0);
            String text = "";
            if(component != null) {
                text = ComponentSerializer.parse(component.getJson())[0].toLegacyText();
            }
           uiManager.addActionBar(
                    event.getPlayer().getUniqueId(),
                    text,
                    60
            );

            event.setCancelled(true);
        }
    }

    private static class Manager_System extends PacketAdapter {
        private final UiManager uiManager;
        public Manager_System(JavaPlugin plugin, UiManager uiManager) {
            super(plugin, PacketType.Play.Server.SYSTEM_CHAT);
            this.uiManager = uiManager;
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if (event.isCancelled() || uiManager.isSending()) { return; }
            PacketContainer packet = event.getPacket();

            boolean isActionBar = packet.getBooleans().read(0);
            if(!isActionBar) {
                return;
            }

            String text = "";

            StructureModifier<String> strings = packet.getStrings();
            if(strings.size() != 0) {
                String json = strings.read(0);
                if(json != null) {
                    text = ComponentSerializer.parse(json)[0].toLegacyText();
                }
            } else {
                StructureModifier<WrappedChatComponent> chatComponents = packet.getChatComponents();
                if(chatComponents.size() == 0) {return;}
                WrappedChatComponent component = chatComponents.read(0);
                if(component != null) {
                    text = ComponentSerializer.parse(component.getJson())[0].toLegacyText();
                }
            }

            uiManager.addActionBar(
                    event.getPlayer().getUniqueId(),
                    text,
                    60
            );
            event.setCancelled(true);
        }
    }

    private static class Manager_Chat extends PacketAdapter {
        private final UiManager uiManager;
        public Manager_Chat(JavaPlugin plugin, UiManager uiManager) {
            super(plugin, PacketType.Play.Server.CHAT);
            this.uiManager = uiManager;
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if (event.isCancelled() || uiManager.isSending()) { return; }
            PacketContainer packet = event.getPacket();

            if(packet.getChatTypes().read(0) != EnumWrappers.ChatType.GAME_INFO) {
                return;
            }

            StructureModifier<WrappedChatComponent> chatComponents = packet.getChatComponents();
            StructureModifier<Object> modifiers = packet.getModifier();

            if(chatComponents.size() == 0) {
                return;
            }
            WrappedChatComponent component = chatComponents.read(0);

            String text = "";
            if(component != null) {
                text = ComponentSerializer.parse(component.getJson())[0].toLegacyText();
            } else {
                if(modifiers.size() >= 2) {
                    BaseComponent baseComponent = ((BaseComponent[]) modifiers.read(1))[0];
                    text = baseComponent.toLegacyText();
                }
            }
            uiManager.addActionBar(
                    event.getPlayer().getUniqueId(),
                    text,
                    60
            );
            event.setCancelled(true);
        }
    }

    private static class Manager_1_16 extends PacketAdapter {
        private final UiManager uiManager;
        public Manager_1_16(JavaPlugin plugin, UiManager uiManager) {
            super(plugin, PacketType.Play.Server.TITLE);
            this.uiManager = uiManager;
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if (event.isCancelled() || uiManager.isSending()) { return; }
            PacketContainer packet = event.getPacket();
            if (!packet.getTitleActions().read(0).equals(EnumWrappers.TitleAction.ACTIONBAR)) { return; }

            StructureModifier<WrappedChatComponent> chatComponents = packet.getChatComponents();
            if(chatComponents.size() == 0) {return;}
            WrappedChatComponent component = chatComponents.read(0);
            String text = "";
            if(component != null) {
                text = ComponentSerializer.parse(component.getJson())[0].toLegacyText();
            }
            uiManager.addActionBar(
                    event.getPlayer().getUniqueId(),
                    text,
                    60
            );
            event.setCancelled(true);
        }
    }
}
