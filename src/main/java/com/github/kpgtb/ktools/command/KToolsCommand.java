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

package com.github.kpgtb.ktools.command;

import com.github.kpgtb.ktools.KTools;
import com.github.kpgtb.ktools.manager.command.KCommand;
import com.github.kpgtb.ktools.manager.command.annotation.Description;
import com.github.kpgtb.ktools.manager.command.annotation.MainCommand;
import com.github.kpgtb.ktools.manager.command.annotation.WithoutPermission;
import com.github.kpgtb.ktools.manager.language.LanguageLevel;
import com.github.kpgtb.ktools.manager.language.LanguageManager;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

@Description("Manage KTools")
public class KToolsCommand extends KCommand {
    private final ToolsObjectWrapper wrapper;

    public KToolsCommand(ToolsObjectWrapper toolsObjectWrapper, String groupPath) {
        super(toolsObjectWrapper, groupPath);
        this.wrapper = toolsObjectWrapper;
    }

    @MainCommand
    @WithoutPermission
    @Description("Information about plugin")
    public void info(CommandSender sender) {
        MiniMessage mm = MiniMessage.miniMessage();
        String pluginVersion = wrapper.getPlugin().getDescription().getVersion().split("-")[0];
        List<Component> messagesToSend = new ArrayList<>();

        messagesToSend.add(mm.deserialize("<#28e28b>Some plugins on this server uses free, open-source <underlined><bold><#a5dffc><click:open_url:'https://www.spigotmc.org/resources/ktools.108301/'><hover:show_text:'<#f9d521>Go to SpigotMC page'>KTools"));
        messagesToSend.add(mm.deserialize("<#28e28b>Author of tools: <underlined><bold><#a5dffc><click:open_url:'https://kpgtb.pl/'><hover:show_text:'<#f9d521>Check portfolio'>KPG-TB"));
        messagesToSend.add(
                KTools.HAS_UPDATE
                ?
                mm.deserialize("<#28e28b>Version of tools: <underlined><bold><#a5dffc><click:open_url:'https://www.spigotmc.org/resources/ktools.108301/'><hover:show_text:'<#ff8482>Server has outdated version of tools!'>"+pluginVersion)
                :
                mm.deserialize("<#28e28b>Version of tools: <underlined><bold><#a5dffc><hover:show_text:'<#caf9a1>Server has the newest version of tools!'>"+pluginVersion)
        );


        Audience audience = wrapper.getAdventure().sender(sender);
        messagesToSend.forEach(audience::sendMessage);
    }

    public class Messages {
        @Description("Reload all messages (Also in hooked plugins)")
        public void reload(CommandSender sender) {
            LanguageManager global = wrapper.getLanguageManager();
            global.refreshMessages();
            global.getHookedManagers().forEach(LanguageManager::refreshMessages);

            Audience audience = wrapper.getAdventure().sender(sender);
            global.getComponent(
                    LanguageLevel.GLOBAL,
                    "reloadedMessages",
                    Placeholder.unparsed("plugins", String.valueOf(global.getHookedManagers().size() + 1))
            ).forEach(audience::sendMessage);
        }
    }
}
