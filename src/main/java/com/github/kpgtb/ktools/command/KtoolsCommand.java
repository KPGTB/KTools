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

import com.github.kpgtb.ktools.Ktools;
import com.github.kpgtb.ktools.manager.command.KCommand;
import com.github.kpgtb.ktools.manager.command.annotation.Description;
import com.github.kpgtb.ktools.manager.command.annotation.MainCommand;
import com.github.kpgtb.ktools.manager.command.annotation.WithoutPermission;
import com.github.kpgtb.ktools.manager.language.LanguageLevel;
import com.github.kpgtb.ktools.manager.language.LanguageManager;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

@Description("Manage ktools")
public class KtoolsCommand extends KCommand {
    private final ToolsObjectWrapper wrapper;

    public KtoolsCommand(ToolsObjectWrapper toolsObjectWrapper, String groupPath) {
        super(toolsObjectWrapper, groupPath);
        this.wrapper = toolsObjectWrapper;
    }

    @MainCommand
    @WithoutPermission
    @Description("Information about plugin")
    public void info(CommandSender sender) {
        Component firstLine = Component.text("Some plugins on this server uses free, open-source ")
                .color(TextColor.color(40,226,139))
                .append(
                        Component.text("Ktools")
                                .color(TextColor.color(165,223,252))
                                .decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED)
                                .clickEvent(ClickEvent.openUrl("https://www.spigotmc.org/resources/ktools.108301/"))
                                .hoverEvent(
                                        Component.text("Go to SpigotMC page")
                                                .color(TextColor.color(249,213,33))
                                )
                );

        Component secondLine = Component.text("Author of tools: ")
                .color(TextColor.color(40,226,139))
                .append(
                        Component.text("KPG-TB")
                                .color(TextColor.color(165,223,252))
                                .decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED)
                                .clickEvent(ClickEvent.openUrl("https://kpgtb.pl/"))
                                .hoverEvent(
                                        Component.text("Check portfolio")
                                                .color(TextColor.color(249,213,33))
                                )
                );

        Component version = Component.text(wrapper.getPlugin().getDescription().getVersion().split("-")[0])
                .color(TextColor.color(165,223,252))
                .decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED)
                .hoverEvent(
                        Component.text("Server has the newest version of tools!")
                                .color(TextColor.color(202, 249, 161))
                );

        if(Ktools.HAS_UPDATE) {
            version = version.hoverEvent(
                        Component.text("Server has outdated version of tools!")
                                .color(TextColor.color(255, 132, 130))
                    )
                    .clickEvent(ClickEvent.openUrl("https://www.spigotmc.org/resources/ktools.108301/"));
        }

        Component thirdLine = Component.text("Version of tools: ")
                .color(TextColor.color(40,226,139))
                .append(
                    version
                );

        Audience audience = wrapper.getAdventure().sender(sender);
        audience.sendMessage(firstLine);
        audience.sendMessage(secondLine);
        audience.sendMessage(thirdLine);
    }

    @Description("Reload all messages (Also in hooked plugins)")
    public void reloadMessages(CommandSender sender) {
        LanguageManager global = wrapper.getLanguageManager();
        global.refreshMessages();
        global.getHookedManagers().forEach(LanguageManager::refreshMessages);

        Audience audience = wrapper.getAdventure().sender(sender);
        global.getComponent(
                LanguageLevel.GLOBAL,
                "reloadedMessages",
                Placeholder.parsed("plugins", (global.getHookedManagers().size() + 1)+"")
        ).forEach(audience::sendMessage);
    }

}
