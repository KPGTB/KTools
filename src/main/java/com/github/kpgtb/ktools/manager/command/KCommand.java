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

import com.github.kpgtb.ktools.manager.command.parser.ParamParserManager;
import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.language.LanguageManager;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Abstract class that handles process of preparing command
 */
public abstract class KCommand extends Command {
    private final String cmdName;

    private final DebugManager debug;
    private final LanguageManager language;
    private final BukkitAudiences adventure;
    private final ParamParserManager parser;

    private final ArrayList<Subcommand> mainCommands;
    private final HashMap<String, Subcommand> subcommands;

    /**
     * Constructor of command. It also handles getting information about command
     * @param toolsObjectWrapper ToolsObjectWrapper or object that extends it.
     * @param groupPath Path to command from commands package
     */
    public KCommand(ToolsObjectWrapper toolsObjectWrapper, String groupPath) {
        super("");

        this.debug = toolsObjectWrapper.getDebugManager();
        this.language = toolsObjectWrapper.getLanguageManager();
        this.adventure = toolsObjectWrapper.getAdventure();
        this.parser = toolsObjectWrapper.getParamParserManager();

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

        this.debug.sendInfo(DebugType.COMMAND, "Registering command " + cmdName);

        for(Method method : getClass().getDeclaredMethods()) {
            this.debug.sendInfo(DebugType.COMMAND, "Trying method " + method.getName());
            if(method.getAnnotation(NoCommand.class) != null) {
                this.debug.sendInfo(DebugType.COMMAND, "This method is not a subcommand! Cancelling");
                continue;
            }
            this.debug.sendInfo(DebugType.COMMAND, "This method is a subcommand!");

            String subName = method.getName().toLowerCase();
            String subDescription = "";

            this.debug.sendInfo(DebugType.COMMAND, "Name: " + subName);

            SubcommandDescription subcommandDescription = method.getAnnotation(SubcommandDescription.class);
            if(subcommandDescription != null) {
                subDescription = subcommandDescription.description();
            }

            this.debug.sendInfo(DebugType.COMMAND, "Description: " + subDescription);

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

            permissions.forEach(perm -> {
                this.debug.sendInfo(DebugType.COMMAND, "Loaded permission: " + perm);
            });

            boolean playerRequired;
            if(method.getParameterCount() == 0) {
                continue;
            }

            Parameter[] parametersRaw = method.getParameters();
            Class<?> typeClazz = parametersRaw[0].getType();

            if (Player.class.equals(typeClazz)) {
                playerRequired = true;
            } else if (CommandSender.class.equals(typeClazz)) {
                playerRequired = false;
            } else {
                this.debug.sendWarning(DebugType.COMMAND, "This command isn't for console or player! Cancelling!");
                continue;
            }

            this.debug.sendInfo(DebugType.COMMAND, "This command is only for player: " + (playerRequired ? "yes" : "no"));

            LinkedHashMap<String, Class<?>> parameters = new LinkedHashMap<>();
            for (int i = 1; i < parametersRaw.length; i++) {
                this.debug.sendInfo(DebugType.COMMAND, "Loaded parameter " + parametersRaw[i].getName() + " with type " + parametersRaw[i].getType().getSimpleName());
                parameters.put(parametersRaw[i].getName(), parametersRaw[i].getType());
            }

            boolean endless = false;

            if(!parameters.isEmpty()) {
                Parameter lastParameter = parametersRaw[parametersRaw.length - 1];
                if(lastParameter.getType().equals(String.class) &&
                        lastParameter.getDeclaredAnnotation(LongString.class) != null) {
                    endless = true;
                }
            }

            this.debug.sendInfo(DebugType.COMMAND, "This command is endless: " + (endless ? "yes" : "no"));

            Subcommand subcommand = new Subcommand(subName, subDescription, permissions,playerRequired,parameters,method, endless);
            if(method.getDeclaredAnnotation(MainCommand.class) != null) {
                this.debug.sendInfo(DebugType.COMMAND, "This command is main command!");
                this.mainCommands.add(subcommand);
                continue;
            }
            this.debug.sendInfo(DebugType.COMMAND, "This command is sub command!");
            subcommands.put(subName, subcommand);
        }
        this.debug.sendInfo(DebugType.COMMAND, "Registered command " + cmdName);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        Audience audience = adventure.sender(sender);
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
                boolean hasPermission = false;
                for(String perm : mainCommand.getPermissions()) {
                    if(sender.hasPermission(perm)) {
                        hasPermission = true;
                        break;
                    }
                }
                if(!hasPermission) {
                    language.getComponent(LanguageLevel.GLOBAL, "noPermission").forEach(audience::sendMessage);
                    return false;
                }
                try {
                    mainCommand.getMethod().invoke(this, sender);
                    return true;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    this.debug.sendWarning(DebugType.COMMAND, "Error while invoking main command");
                    throw new RuntimeException(e);
                }
            }

            if(found) {
                language.getComponent(LanguageLevel.GLOBAL, "onlyPlayer").forEach(audience::sendMessage);
                return false;
            }

            sendHelp(audience);
            return false;
        }

        ArrayList<Subcommand> possibleSubcommands = new ArrayList<>();
        subcommands.forEach((name, subcommand) -> {
            if(args[0].equalsIgnoreCase(name)) {
                possibleSubcommands.add(subcommand);
            }
        });
        possibleSubcommands.addAll(mainCommands);

        boolean found = false;
        for(Subcommand subcommand : possibleSubcommands) {

            ArrayList<Class<?>> subCommandArgs = new ArrayList<>(subcommand.getArgsType().values());
            ArrayList<String> fixedArgs = new ArrayList<>(Arrays.asList(args));
            if(subcommand.getName().equalsIgnoreCase(args[0])) {
                fixedArgs.remove(0);
            }

            if(!subcommand.isEndless() && fixedArgs.size() != subCommandArgs.size()) {
                continue;
            }

            if(subcommand.isEndless() && fixedArgs.size() < subCommandArgs.size()) {
                continue;
            }

            boolean isIt = true;
            int i = 0;
            for(Class<?> expectedClass : subCommandArgs) {
                if(subcommand.isEndless()) {
                    if(subCommandArgs.size() == (i+1)) {
                        if (expectedClass.equals(String.class)) {
                            break;
                        }
                    }
                }
                if(!parser.canConvert(fixedArgs.get(i), expectedClass)) {
                    isIt = false;
                    break;
                }
                i++;
            }

            if(!isIt) {
                continue;
            }

            if(subcommand.isPlayerRequired() && (!(sender instanceof Player))) {
                found = true;
                continue;
            }

            boolean hasPermission = false;
            for(String perm : subcommand.getPermissions()) {
                if(sender.hasPermission(perm)) {
                    hasPermission = true;
                    break;
                }
            }
            if(!hasPermission) {
                language.getComponent(LanguageLevel.GLOBAL, "noPermission").forEach(audience::sendMessage);
                return false;
            }

            Object[] commandArgs = new Object[subCommandArgs.size() + 1];
            commandArgs[0] = sender;
            for (int j = 1; j < commandArgs.length; j++) {
                int fixedJ = j - 1;
                if(subcommand.isEndless()) {
                    if(commandArgs.length == (j+1)) {
                        StringBuilder stringBuilder = new StringBuilder(fixedArgs.get(fixedJ));
                        for (int k = j; k < fixedArgs.size(); k++) {
                            stringBuilder
                                .append(" ")
                                .append(fixedArgs.get(k));
                        }
                        commandArgs[j] = stringBuilder.toString();
                        break;
                    }
                }
                commandArgs[j] = parser.convert(fixedArgs.get(fixedJ), subCommandArgs.get(fixedJ));
            }
            try {
                subcommand.getMethod().invoke(this, commandArgs);
                return true;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        if(found) {
            language.getComponent(LanguageLevel.GLOBAL, "onlyPlayer").forEach(audience::sendMessage);
            return false;
        }
        sendHelp(audience);
        return false;
    }

    /**
     * Method that handles sending help info to sender. It can be overridden
     * @param audience Audience instance from AdventureAPI
     */
    protected void sendHelp(Audience audience) {
        ArrayList<Component> toSend = new ArrayList<>();
        mainCommands.forEach(command -> {
            StringBuilder cmdLine = new StringBuilder(cmdName + " ");
            AtomicInteger i = new AtomicInteger(1);
            command.getArgsType().forEach((argName, argsType) -> {
                String start = "<";
                String end = ">";
                if(command.isEndless() && i.get() == command.getArgsType().size()) {
                    start = "[<";
                    end = ">]";
                }
                i.getAndIncrement();
                cmdLine
                        .append(start)
                        .append(argName)
                        .append(end)
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
            AtomicInteger i = new AtomicInteger(1);
            command.getArgsType().forEach((argName, argsType) -> {
                String start = "<";
                String end = ">";
                if(command.isEndless() && i.get() == command.getArgsType().size()) {
                    start = "[<";
                    end = ">]";
                }
                i.getAndIncrement();
                cmdLine
                        .append(start)
                        .append(argName)
                        .append(end)
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

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        ArrayList<String> result = new ArrayList<>();

        if(args.length == 0) {
            return result;
        }

        int lastArgIdx = args.length - 1;
        String lastArg = args[lastArgIdx];

        if(args.length == 1) {
            result.addAll(
                    subcommands.keySet()
                    .stream()
                    .filter(s -> s.startsWith(lastArg))
                    .collect(Collectors.toList())
            );

            mainCommands.forEach(mcmd -> {
                if(mcmd.getArgsType().isEmpty()) {
                    return;
                }
                Optional<Class<?>> clazz = mcmd.getArgsType().values().stream().findFirst();
                if(!clazz.isPresent()) {
                    return;
                }
                result.addAll(parser.complete(lastArg,sender,clazz.get()));
            });

            return result;
        }

        mainCommands.forEach(mcmd -> {
            if(mcmd.getArgsType().size() < args.length) {
                return;
            }
            boolean isIt = true;
            for (int i = 0; i < lastArgIdx; i++) {
                Class<?> clazz = (Class<?>) mcmd.getArgsType().values().toArray()[i];
                if(!parser.canConvert(args[i], clazz)) {
                    isIt = false;
                    break;
                }
            }
            if(!isIt) {
                return;
            }
            Class<?> clazz = (Class<?>) mcmd.getArgsType().values().toArray()[lastArgIdx];
            result.addAll(parser.complete(lastArg,sender,clazz));
        });

        subcommands.forEach((name,subcmd) -> {
            if(!name.equalsIgnoreCase(args[0])) {
                return;
            }
            List<String> trueArgs = Arrays.stream(args).collect(Collectors.toList());
            trueArgs.remove(0);
            int trueLastIdx = trueArgs.size() -1;

            if(subcmd.getArgsType().size() < trueArgs.size()) {
                return;
            }
            boolean isIt = true;
            for (int i = 0; i < trueLastIdx; i++) {
                Class<?> clazz = (Class<?>) subcmd.getArgsType().values().toArray()[i];
                if(!parser.canConvert(trueArgs.get(i), clazz)) {
                    isIt = false;
                    break;
                }
            }
            if(!isIt) {
                return;
            }
            Class<?> clazz = (Class<?>) subcmd.getArgsType().values().toArray()[trueLastIdx];
            result.addAll(parser.complete(lastArg,sender,clazz));
        });

        return result;
    }
}
