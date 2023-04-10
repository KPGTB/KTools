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

import com.github.kpgtb.ktools.manager.command.filter.IFilter;
import com.github.kpgtb.ktools.manager.command.parser.ParamParserManager;
import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.language.LanguageManager;
import com.github.kpgtb.ktools.manager.command.annotation.*;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.manager.language.LanguageLevel;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Abstract class that handles process of preparing command
 */
public abstract class KCommand extends Command {
    private final String cmdName;

    private final ToolsObjectWrapper wrapper;
    private final DebugManager debug;
    private final LanguageManager language;
    private final BukkitAudiences adventure;
    private final ParamParserManager parser;

    private final LinkedList<Subcommand> mainCommands;
    private final LinkedHashMap<String, Subcommand> subcommands;

    /**
     * Constructor of command. It also handles getting information about command
     * @param toolsObjectWrapper ToolsObjectWrapper or object that extends it.
     * @param groupPath Path to command from commands package
     */
    public KCommand(ToolsObjectWrapper toolsObjectWrapper, String groupPath) {
        super("");

        this.wrapper = toolsObjectWrapper;
        this.debug = toolsObjectWrapper.getDebugManager();
        this.language = toolsObjectWrapper.getLanguageManager();
        this.adventure = toolsObjectWrapper.getAdventure();
        this.parser = toolsObjectWrapper.getParamParserManager();

        File dataFolder = toolsObjectWrapper.getPlugin().getDataFolder();
        File commandsFile = new File(dataFolder, "commands.yml");
        YamlConfiguration commandsConfig = YamlConfiguration.loadConfiguration(commandsFile);

        cmdName = getClass().getSimpleName()
                .toLowerCase()
                .replace("command","");

        commandsConfig.set(cmdName+".command", "/" + cmdName);

        String description = "";
        Description descriptionAnnotation = getClass().getDeclaredAnnotation(Description.class);
        if(descriptionAnnotation != null) {
            description = descriptionAnnotation.value();
        }

        commandsConfig.set(cmdName+".description", description);

        String[] aliases = new String[0];
        CommandAliases commandAliases = getClass().getDeclaredAnnotation(CommandAliases.class);
        if(commandAliases != null) {
            aliases = commandAliases.value();
        }

        commandsConfig.set(cmdName+".aliases", aliases);

        super.setName(cmdName);
        super.setDescription(description);
        super.setAliases(Arrays.asList(aliases));

        this.mainCommands = new LinkedList<>();
        this.subcommands = new LinkedHashMap<>();

        String customCommandPermission = "";
        boolean commandWithoutPermission = getClass().getDeclaredAnnotation(WithoutPermission.class) != null;

        CustomPermission customCommandPermissionAnnotation = getClass().getDeclaredAnnotation(CustomPermission.class);
        if(customCommandPermissionAnnotation != null) {
            customCommandPermission = customCommandPermissionAnnotation.value();
        }

        this.debug.sendInfo(DebugType.COMMAND, "Registering command " + cmdName);

        for(Method method : getClass().getDeclaredMethods()) {
            if(method.isSynthetic()) {
                continue;
            }

            this.debug.sendInfo(DebugType.COMMAND, "Trying method " + method.getName());
            if(method.getAnnotation(NoCommand.class) != null) {
                this.debug.sendInfo(DebugType.COMMAND, "This method is not a subcommand! Cancelling");
                continue;
            }
            this.debug.sendInfo(DebugType.COMMAND, "This method is a subcommand!");

            String subName = method.getName().toLowerCase();
            String subDescription = "";

            this.debug.sendInfo(DebugType.COMMAND, "Name: " + subName);

            Description subcommandDescription = method.getDeclaredAnnotation(Description.class);
            if(subcommandDescription != null) {
                subDescription = subcommandDescription.value();
            }

            this.debug.sendInfo(DebugType.COMMAND, "Description: " + subDescription);

            String customPermission = "";
            boolean withoutPermission = method.getDeclaredAnnotation(WithoutPermission.class) != null;

            CustomPermission customPermissionAnnotation = method.getDeclaredAnnotation(CustomPermission.class);
            if(customPermissionAnnotation != null) {
                customPermission = customPermissionAnnotation.value();
            }

            ArrayList<String> permissions = new ArrayList<>();
            if(!withoutPermission && !commandWithoutPermission) {
                // Method permission ex. command.admin.give.item
                permissions.add(
                        ("command." + groupPath + "." + cmdName + "." + subName).replace("..", ".")
                );
                // Command permission ex. command.admin.give.*
                permissions.add(
                        ("command." + groupPath + "." + cmdName + ".*").replace("..", ".")
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
                if(!customCommandPermission.isEmpty()) {
                    permissions.add(
                            customCommandPermission
                    );
                }
                if(!customPermission.isEmpty()) {
                    permissions.add(
                            customPermission
                    );
                }
            }

            permissions.forEach(perm -> {
                this.debug.sendInfo(DebugType.COMMAND, "Loaded permission: " + perm);
            });

            boolean playerRequired;
            Class<? extends IFilter<?>>[] senderOrFilters = array();
            Class<? extends IFilter<?>>[] senderAndFilters = array();
            if(method.getParameterCount() == 0) {
                continue;
            }

            Parameter[] parametersRaw = method.getParameters();
            Parameter typeParam = parametersRaw[0];
            Class<?> typeClazz = typeParam.getType();

            if (Player.class.equals(typeClazz)) {
                playerRequired = true;
            } else if (CommandSender.class.equals(typeClazz)) {
                playerRequired = false;
            } else {
                this.debug.sendWarning(DebugType.COMMAND, "This command isn't for console or player! Cancelling!");
                continue;
            }

            Filter senderFilter = typeParam.getDeclaredAnnotation(Filter.class);
            if(senderFilter != null) {
                senderOrFilters = senderFilter.orFilters();
                senderAndFilters = senderFilter.andFilters();
            }

            this.debug.sendInfo(DebugType.COMMAND, "This command is only for player: " + (playerRequired ? "yes" : "no"));

            LinkedHashMap<String, CommandArgument> parameters = new LinkedHashMap<>();
            for (int i = 1; i < parametersRaw.length; i++) {
                this.debug.sendInfo(DebugType.COMMAND, "Loaded parameter " + parametersRaw[i].getName() + " with type " + parametersRaw[i].getType().getSimpleName());
                Parameter param = parametersRaw[i];

                String paramName = param.getName();
                Class<?> paramClass = param.getType();
                Class<? extends IFilter<?>>[] paramOrFilters = array();
                Class<? extends IFilter<?>>[] paramAndFilters = array();

                Filter paramFilter = param.getDeclaredAnnotation(Filter.class);
                if(paramFilter != null) {
                    paramOrFilters = paramFilter.orFilters();
                    paramAndFilters = paramFilter.andFilters();
                }

                CommandArgument argument = new CommandArgument(paramName, paramClass, paramOrFilters, paramAndFilters);
                parameters.put(paramName, argument);
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

            boolean isMain = method.getDeclaredAnnotation(MainCommand.class) != null;

            StringBuilder cmdString = new StringBuilder("/" + cmdName);
            if(!isMain) {
                cmdString.append(" ")
                        .append(subName);
            }
            AtomicInteger i = new AtomicInteger(1);
            boolean finalEndless = endless;
            parameters.forEach((argName, argsType) -> {
                String start = "<";
                String end = ">";
                if(finalEndless && i.get() == parameters.size()) {
                    start = "[<";
                    end = ">]";
                }
                i.getAndIncrement();
                cmdString
                        .append(" ")
                        .append(start)
                        .append(argName)
                        .append(end);
            });


            boolean hidden = method.getDeclaredAnnotation(Hidden.class) != null;
            this.debug.sendInfo(DebugType.COMMAND, "This command is hidden: " + ((hidden) ? "yes" : "no"));

            commandsConfig.set(cmdName+".variants."+subName+".command", cmdString.toString());
            commandsConfig.set(cmdName+".variants."+subName+".description", subDescription);
            commandsConfig.set(cmdName+".variants."+subName+".permissions", permissions);
            commandsConfig.set(cmdName+".variants."+subName+".onlyPlayer", playerRequired);
            commandsConfig.set(cmdName+".variants."+subName+".hidden", hidden);

            Subcommand subcommand = new Subcommand(subName, subDescription, permissions,playerRequired, senderOrFilters, senderAndFilters, parameters,method, endless, hidden);
            if(isMain) {
                this.debug.sendInfo(DebugType.COMMAND, "This command is main command!");
                this.mainCommands.add(subcommand);
                continue;
            }
            this.debug.sendInfo(DebugType.COMMAND, "This command is sub command!");
            subcommands.put(subName, subcommand);
        }
        try {
            commandsConfig.save(commandsFile);
        } catch (IOException e) {
            this.debug.sendWarning(DebugType.COMMAND, "Error while saving commands list");
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
                if(!hasPermission(sender,mainCommand)) {
                    language.getComponent(LanguageLevel.GLOBAL, "noPermission").forEach(audience::sendMessage);
                    return false;
                }
                if(!passFilters(mainCommand.getSenderOrFilters(), mainCommand.getSenderAndFilters(), sender,sender)) {
                    sendFilterMessages(mainCommand.getSenderOrFilters(), mainCommand.getSenderAndFilters(), sender,sender,audience);
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

            sendHelp(sender);
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
        CommandArgument notPassArg = null;
        Object notPassObj = null;

        for(Subcommand subcommand : possibleSubcommands) {

            ArrayList<CommandArgument> subCommandArgs = new ArrayList<>(subcommand.getArgsType().values());
            ArrayList<Class<?>> subCommandClasses = new ArrayList<>();
            subCommandArgs.forEach(arg -> {
                subCommandClasses.add(arg.getClazz());
            });
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
            for(Class<?> expectedClass : subCommandClasses) {
                if(subcommand.isEndless()) {
                    if(subCommandArgs.size() == (i+1)) {
                        if (expectedClass.equals(String.class)) {
                            break;
                        }
                    }
                }
                if(!parser.canConvert(fixedArgs.get(i), expectedClass,wrapper)) {
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

            if(!hasPermission(sender,subcommand)) {
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
                commandArgs[j] = parser.convert(fixedArgs.get(fixedJ), subCommandArgs.get(fixedJ).getClazz(),wrapper);
            }

            boolean passSenderFilters = passFilters(subcommand.getSenderOrFilters(), subcommand.getSenderAndFilters(), sender,sender);
            if(!passSenderFilters) {
                found = true;
                notPassArg = new CommandArgument("",null,subcommand.getSenderOrFilters(), subcommand.getSenderAndFilters());
                notPassObj = sender;
                continue;
            }
            boolean passArgsFilters = true;

            for (int i1 = 1; i1 < commandArgs.length; i1++) {
                passArgsFilters = passFilters(subCommandArgs.get((i1-1)), commandArgs[i1],sender);
                if(!passArgsFilters) {
                    found = true;
                    notPassArg = subCommandArgs.get((i1-1));
                    notPassObj = commandArgs[i1];
                    break;
                }
            }

            if(!passArgsFilters) {
                continue;
            }

            try {
                subcommand.getMethod().invoke(this, commandArgs);
                return true;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        if(found) {
            if(notPassArg != null && notPassObj != null) {
                sendFilterMessages(notPassArg, notPassObj, sender,audience);
                return false;
            }
            language.getComponent(LanguageLevel.GLOBAL, "onlyPlayer").forEach(audience::sendMessage);
            return false;
        }
        sendHelp(sender);
        return false;
    }

    /**
     * Method that handles sending help info to sender. It can be overridden
     * @param sender Instance of CommandSender
     */
    protected void sendHelp(CommandSender sender) {
        Audience audience = this.adventure.sender(sender);

        HashMap<String, Subcommand> commands = new HashMap<>();
        for (int i = 0; i < mainCommands.size(); i++) {
            String key = "mcmd_" + i;
            commands.put(key,mainCommands.get(i));
        }
        commands.putAll(subcommands);

        ArrayList<Component> toSend = new ArrayList<>();

        commands.forEach((subName, command) -> {
            if(!hasPermission(sender,command)) {
                return;
            }
            if(command.isHidden()) {
                return;
            }

            StringBuilder cmdLine = subName.startsWith("mcmd_") ? new StringBuilder(cmdName + " ") : new StringBuilder(cmdName + " " + subName + " ");
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
            toSend.addAll(language.getComponent(LanguageLevel.GLOBAL, "helpLine", placeholders.toArray(new TagResolver[0])));
        });

        if(toSend.size() > 0) {
            toSend.add(0,Component.text(" "));
            toSend.add(Component.text(" "));
        }

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
                    subcommands.values()
                        .stream()
                        .filter(cmd -> hasPermission(sender,cmd))
                        .filter(cmd -> !cmd.isHidden())
                        .map(Subcommand::getName)
                        .filter(s -> s.startsWith(lastArg))
                        .collect(Collectors.toList())
            );

            mainCommands.forEach(cmd -> {
                if(cmd.getArgsType().isEmpty()) {
                    return;
                }
                if(!hasPermission(sender,cmd)) {
                    return;
                }
                if(cmd.isHidden()) {
                    return;
                }
                Optional<CommandArgument> clazz = cmd.getArgsType().values().stream().findFirst();
                if(!clazz.isPresent()) {
                    return;
                }

                List<String> complete = parser.complete(lastArg,sender,clazz.get().getClazz(),wrapper);
                result.add("<" + clazz.get().getName() + ">");
                result.addAll(getCompleterThatPass(complete,clazz.get(),sender));
            });

            return result;
        }

        HashMap<String, Subcommand> commands = new HashMap<>();
        for (int i = 0; i < mainCommands.size(); i++) {
            String key = "mcmd_" + i;
            commands.put(key,mainCommands.get(i));
        }
        commands.putAll(subcommands);

        commands.forEach((subName, cmd) -> {
            if(!hasPermission(sender,cmd)) {
                return;
            }
            if(cmd.isHidden()) {
                return;
            }
            boolean mainCmd = subName.startsWith("mcmd_");

            List<String> trueArgs = Arrays.stream(args).collect(Collectors.toList());
            if(!mainCmd) {
                if(!subName.equalsIgnoreCase(args[0])) {
                    return;
                }
                trueArgs.remove(0);
            }
            int trueLastIdx = trueArgs.size() -1;

            if(cmd.getArgsType().size() >= trueArgs.size()) {
                boolean isIt = true;
                for (int i = 0; i < trueLastIdx; i++) {
                    CommandArgument argument = (CommandArgument) cmd.getArgsType().values().toArray()[i];
                    Class<?> clazz = argument.getClazz();
                    if(!parser.canConvert(trueArgs.get(i), clazz,wrapper)) {
                        isIt = false;
                        break;
                    }
                }
                if(!isIt) {
                    return;
                }
                CommandArgument argument = (CommandArgument) cmd.getArgsType().values().toArray()[trueLastIdx];
                Class<?> clazz = argument.getClazz();
                List<String> complete = parser.complete(lastArg,sender,clazz,wrapper);
                result.add("<" + argument.getName() + ">");
                result.addAll(getCompleterThatPass(complete,argument,sender));
            } else {
                if(!cmd.isEndless()) {
                    return;
                }

                boolean isIt = true;
                for (int i = 0; i < cmd.getArgsType().size(); i++) {
                    CommandArgument argument = (CommandArgument) cmd.getArgsType().values().toArray()[i];
                    Class<?> clazz = argument.getClazz();
                    if(!parser.canConvert(trueArgs.get(i), clazz,wrapper)) {
                        isIt = false;
                        break;
                    }
                }
                if(!isIt) {
                    return;
                }
                CommandArgument argument = (CommandArgument) cmd.getArgsType().values().toArray()[cmd.getArgsType().size() - 1];
                result.add("[<" + argument.getName() + ">]");
            }
        });

        return result;
    }

    /**
     * Check if sender has permissions to use command
     * @param sender Command sender
     * @param command Instance of Subcommand object
     * @return true if it has one of required permissions
     */
    protected boolean hasPermission(CommandSender sender, Subcommand command) {
        boolean hasPermission = command.getPermissions().isEmpty();
        for(String perm : command.getPermissions()) {
            if(sender.hasPermission(perm)) {
                hasPermission = true;
                break;
            }
        }
        return hasPermission;
    }

    /**
     * Check if obj pass filters
     * @param orFilterClasses Or filters
     * @param andFilterClasses And filters
     * @param obj Object that should pass the test
     * @return true if object pass tests
     */
    @SuppressWarnings("unchecked")
    protected <T> boolean passFilters(Class<? extends IFilter<?>>[] orFilterClasses, Class<? extends IFilter<?>>[] andFilterClasses, T obj, CommandSender sender){
        IFilter<T>[] orFilters = (IFilter<T>[]) convertFilterClassesToArray(orFilterClasses, obj.getClass());
        IFilter<T>[] andFilters = (IFilter<T>[]) convertFilterClassesToArray(andFilterClasses, obj.getClass());

        boolean passOr = true;
        boolean passAnd = true;

        for (IFilter<T> filter : orFilters) {
            if (filter.filter(obj, wrapper,sender)) {
                passOr = true;
                break;
            }
            passOr = false;
        }

        for (IFilter<T> filter : andFilters) {
            if (!filter.filter(obj, wrapper,sender)) {
                passAnd = false;
                break;
            }
        }
        return passOr && passAnd;
    }


    /**
     * Check if obj pass filters
     * @param argument Instance of CommandArgument
     * @param obj Object that should pass the test
     * @return true if object pass tests
     */
    protected <T> boolean passFilters(CommandArgument argument, T obj, CommandSender sender){
        Class<? extends IFilter<?>>[] orFilterClasses = argument.getOrFilters();
        Class<? extends IFilter<?>>[] andFilterClasses = argument.getAndFilters();

        return passFilters(orFilterClasses,andFilterClasses,obj, sender);
    }

    @SuppressWarnings("unchecked")
    protected <T> void sendFilterMessages(Class<? extends IFilter<?>>[] orFilterClasses, Class<? extends IFilter<?>>[] andFilterClasses, T obj, CommandSender sender, Audience audience) {
        List<Component> message = new ArrayList<>();
        int lastWeight = -1;

        IFilter<T>[] orFilters = (IFilter<T>[]) convertFilterClassesToArray(orFilterClasses, obj.getClass());
        IFilter<T>[] andFilters = (IFilter<T>[]) convertFilterClassesToArray(andFilterClasses, obj.getClass());

        boolean passOr = false;

        for (IFilter<T> filter : orFilters) {
            if (filter.filter(obj, wrapper,sender)) {
                passOr = true;
                break;
            }
            if(filter.weight() > lastWeight) {
                message = filter.notPassMessage(obj,wrapper,sender);
                lastWeight = filter.weight();
            }
        }

        if(passOr) {
            message = new ArrayList<>();
            lastWeight = -1;
        }

        for (IFilter<T> filter : andFilters) {
            if (!filter.filter(obj, wrapper,sender)) {
                if(filter.weight() > lastWeight) {
                    message = filter.notPassMessage(obj,wrapper,sender);
                    lastWeight = filter.weight();
                }
            }
        }

        message.forEach(audience::sendMessage);
    }

    protected <T> void sendFilterMessages(CommandArgument argument, T obj, CommandSender sender, Audience audience) {
        Class<? extends IFilter<?>>[] orFilterClasses = argument.getOrFilters();
        Class<? extends IFilter<?>>[] andFilterClasses = argument.getAndFilters();

       sendFilterMessages(orFilterClasses,andFilterClasses,obj,sender,audience);
    }

    protected List<String> getCompleterThatPass(List<String> complete, CommandArgument argument, CommandSender sender) {
        List<String> result = new ArrayList<>();
        complete.forEach(s -> {
            Object obj = parser.convert(s, argument.getClazz(), wrapper);
            if(passFilters(argument,obj,sender)) {
                result.add(s);
            }
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> IFilter<T>[] convertFilterClassesToArray(Class<? extends IFilter<?>>[] filters, Class<T> expected) {
        return Arrays.stream(filters)
                .filter(clazz -> {
                    if(clazz.getGenericInterfaces().length == 0) {
                        return false;
                    }
                    ParameterizedType type = (ParameterizedType) clazz.getGenericInterfaces()[0];
                    if(type.getActualTypeArguments().length == 0) {
                        return false;
                    }
                    Class<T> typeArgClazz = (Class<T>) type.getActualTypeArguments()[0];
                    return typeArgClazz.isAssignableFrom(expected);
                })
                .map(clazz -> {
                    try {
                        return clazz.newInstance();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(IFilter[]::new);
    }

    private <T> T[] array(T... arr) {
        return arr;
    }
}