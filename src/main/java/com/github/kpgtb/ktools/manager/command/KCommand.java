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

package com.github.kpgtb.ktools.manager.command;

import com.github.kpgtb.ktools.manager.DebugManager;
import com.github.kpgtb.ktools.manager.LanguageManager;
import com.github.kpgtb.ktools.manager.command.annotation.*;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.manager.language.LanguageLevel;
import com.github.kpgtb.ktools.util.ToolsObjectWrapper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class KCommand extends Command {
    private final String cmdName;

    private final DebugManager debug;
    private final LanguageManager language;
    private final BukkitAudiences adventure;

    private final ArrayList<Subcommand> mainCommands;
    private final HashMap<String, Subcommand> subcommands;

    public KCommand(ToolsObjectWrapper toolsObjectWrapper, String groupPath) {
        super("");

        this.debug = toolsObjectWrapper.getDebugManager();
        this.language = toolsObjectWrapper.getLanguageManager();
        this.adventure = toolsObjectWrapper.getAdventure();

        cmdName = getClass().getSimpleName()
                .toLowerCase()
                .replace("command","");

        String description = "";
        CommandDescription descriptionAnnotation = getClass().getDeclaredAnnotation(CommandDescription.class);
        if(descriptionAnnotation != null) {
            description = descriptionAnnotation.description();
        }

        String[] aliases = new String[0];
        CommandAliases commandAliases = getClass().getDeclaredAnnotation(CommandAliases.class);
        if(commandAliases != null) {
            aliases = commandAliases.aliases();
        }

        super.setName(cmdName);
        super.setDescription(description);
        super.setAliases(Arrays.asList(aliases));

        this.mainCommands = new ArrayList<>();
        this.subcommands = new HashMap<>();

        for(Method method : getClass().getDeclaredMethods()) {
            if(method.getAnnotation(NoCommand.class) != null) {
                continue;
            }

            String subName = method.getName().toLowerCase();
            String subDescription = "";

            SubcommandDescription subcommandDescription = method.getAnnotation(SubcommandDescription.class);
            if(subcommandDescription != null) {
                subDescription = subcommandDescription.description();
            }

            ArrayList<String> permissions = new ArrayList<>();
            // Method permission ex. command.admin.give.item
            permissions.add(
                    "command." + groupPath + "." + cmdName + "." + subName
            );
            // Command permission ex. command.admin.give.*
            permissions.add(
                    "command." + groupPath + "." + cmdName + ".*"
            );
            // Group permission ex. command.admin.*
            if(!groupPath.isEmpty()) {
                permissions.add(
                        "command." + groupPath + ".*"
                );
            }
            // Global permission (command.*)
            permissions.add(
                    "command.*"
            );

            boolean playerRequired;
            if(method.getParameterCount() == 0) {
                continue;
            }
            Parameter[] parametersRaw = method.getParameters();
            Class<?> typeClazz = parametersRaw[0].getType().getSuperclass();

            if (Player.class.equals(typeClazz)) {
                playerRequired = true;
            } else if (CommandSender.class.equals(typeClazz)) {
                playerRequired = false;
            } else {
                continue;
            }

            HashMap<String, Class<?>> parameters = new HashMap<>();
            for (int i = 1; i < parametersRaw.length; i++) {
                parameters.put(parametersRaw[i].getName(), parametersRaw[i].getType());
            }

            Subcommand subcommand = new Subcommand(subName, subDescription, permissions,playerRequired,parameters,method);
            if(method.getDeclaredAnnotation(MainCommand.class) != null) {
                this.mainCommands.add(subcommand);
                continue;
            }
            subcommands.put(subName, subcommand);
        }
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(args.length == 0) {
            boolean found = false;
            for (Subcommand mainCommand : mainCommands) {
                if(!mainCommand.getArgsType().isEmpty()) {
                    continue;
                }
                if(mainCommand.isPlayerRequired() && (!(sender instanceof Player))) {
                    found = true;
                    continue;
                }
                try {
                    mainCommand.getMethod().invoke(sender);
                    return true;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    this.debug.sendWarning(DebugType.COMMAND, "Error while invoking main command");
                    throw new RuntimeException(e);
                }
            }

            Audience audience = adventure.sender(sender);
            if(found) {
                language.getComponent(LanguageLevel.GLOBAL, "onlyPlayer", (Player) sender).forEach(audience::sendMessage);
                return false;
            }

            sendHelp(audience);
            return false;
        }



        return true;
    }

    private void sendHelp(Audience audience) {
        ArrayList<Component> toSend = new ArrayList<>();
        mainCommands.forEach(command -> {
            StringBuilder cmdLine = new StringBuilder(cmdName + " ");
            command.getArgsType().forEach((argName, argsType) -> {
                cmdLine
                        .append("<")
                        .append(argName)
                        .append(">")
                        .append(" ");
            });
            cmdLine.deleteCharAt(cmdLine.length() - 1);
            ArrayList<TagResolver> placeholders = new ArrayList<>();
            placeholders.add(Placeholder.parsed("command", cmdLine.toString()));
            placeholders.add(Placeholder.parsed("description", command.getDescription()));
            toSend.addAll(language.getComponent(LanguageLevel.GLOBAL, "helpLine", placeholders));
        });

        subcommands.forEach((subName, command) -> {
            StringBuilder cmdLine = new StringBuilder(cmdName + " " + subName + " ");
            command.getArgsType().forEach((argName, argsType) -> {
                cmdLine
                        .append("<")
                        .append(argName)
                        .append(">")
                        .append(" ");
            });
            cmdLine.deleteCharAt(cmdLine.length() - 1);
            ArrayList<TagResolver> placeholders = new ArrayList<>();
            placeholders.add(Placeholder.parsed("command", cmdLine.toString()));
            placeholders.add(Placeholder.parsed("description", command.getDescription()));
            toSend.addAll(language.getComponent(LanguageLevel.GLOBAL, "helpLine", placeholders));
        });

        toSend.forEach(audience::sendMessage);
    }
}
