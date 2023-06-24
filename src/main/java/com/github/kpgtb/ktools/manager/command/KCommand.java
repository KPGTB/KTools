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

import com.github.kpgtb.ktools.manager.command.annotation.*;
import com.github.kpgtb.ktools.manager.command.filter.FilterWrapper;
import com.github.kpgtb.ktools.manager.command.filter.IFilter;
import com.github.kpgtb.ktools.manager.command.parser.IParamParser;
import com.github.kpgtb.ktools.manager.command.parser.ParamParserManager;
import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.language.LanguageLevel;
import com.github.kpgtb.ktools.manager.language.LanguageManager;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
import java.util.*;

public abstract class KCommand extends Command {
    private final ToolsObjectWrapper wrapper;
    private final String groupPath;
    private String cmdName;

    private final LanguageManager language;
    private final BukkitAudiences adventure;
    private final ParamParserManager parser;

    private final File commandsFile;
    private final YamlConfiguration commandsConf;
    private int variantIdx = 0;

    private final Map<CommandPath, List<CommandInfo>> subCommands;

    public KCommand(ToolsObjectWrapper wrapper, String groupPath) {
        super("");

        this.wrapper = wrapper;
        this.language = wrapper.getLanguageManager();
        this.adventure = wrapper.getAdventure();
        this.parser = wrapper.getParamParserManager();
        this.groupPath = groupPath;

        this.commandsFile = new File(wrapper.getPlugin().getDataFolder(), "commands.yml");
        this.commandsConf = YamlConfiguration.loadConfiguration(this.commandsFile);

        this.subCommands = new LinkedHashMap<>();
    }

    //
    //  Creating command
    //

    public final void prepareCommand() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.cmdName = getClass().getSimpleName().toLowerCase().replace("command", "");
        super.setName(this.cmdName);

        Description descriptionAnn = getClass().getDeclaredAnnotation(Description.class);
        String description = descriptionAnn != null ? descriptionAnn.value() : "Command created using Ktools";
        super.setDescription(description);

        CommandAliases aliasesAnn = getClass().getDeclaredAnnotation(CommandAliases.class);
        String[] aliases = aliasesAnn != null ? aliasesAnn.value() : new String[0];
        super.setAliases(Arrays.asList(aliases));

        CustomPermission customGlobalPermissionAnn = getClass().getDeclaredAnnotation(CustomPermission.class);
        String customGlobalPermission = customGlobalPermissionAnn != null ? customGlobalPermissionAnn.value() : null;
        boolean globalWithoutPermission = getClass().getDeclaredAnnotation(WithoutPermission.class) != null;

        this.setCommandInfo("command", "/"+cmdName);
        this.setCommandInfo("description", description);
        this.setCommandInfo("aliases", aliases);

        scanClass(new CommandPath(), this.getClass(), this, customGlobalPermission, globalWithoutPermission);
    }

    private void scanClass(CommandPath path, Class<?> clazz, Object invoker, String customGlobalPermission, boolean globalWithoutPermission) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for (Method method : clazz.getDeclaredMethods()) {
            // === Check if it is command
            if(method.isSynthetic()) {
                continue;
            }
            if(method.getDeclaredAnnotation(NoCommand.class) != null) {
                continue;
            }
            if(method.getParameterCount() == 0) {
                continue;
            }

            // === Name & Path
            String name = method.getName().toLowerCase();

            CommandPath newPath = path.clone();
            boolean mainCommand = method.getDeclaredAnnotation(MainCommand.class) != null;
            String methodPath = mainCommand ? "" : name;
            newPath.add(methodPath);
            if(!this.subCommands.containsKey(newPath)) {
                this.subCommands.put(newPath,new ArrayList<>());
            }
            List<CommandInfo> commands = this.subCommands.get(newPath);

            // === Description
            Description descriptionAnn = method.getDeclaredAnnotation(Description.class);
            String description = descriptionAnn != null ? descriptionAnn.value() : "Subcommand created using Ktools";

            // === Permissions
            CustomPermission customPermissionAnn = method.getDeclaredAnnotation(CustomPermission.class);
            String customPermission = customPermissionAnn != null ? customPermissionAnn.value() : null;
            boolean withoutPermission = method.getDeclaredAnnotation(WithoutPermission.class) != null;

            List<String> permissions = new ArrayList<>();
            if(!withoutPermission && !globalWithoutPermission) {
                if(mainCommand) {
                    permissions.add(
                            ("command."+this.groupPath+"."+this.cmdName+"."+newPath.getPermissionStr()+"."+name).replace("..", ".")
                    );
                } else {
                    permissions.add(
                            ("command." + this.groupPath + "." + this.cmdName + "." + newPath.getPermissionStr()).replace("..", ".")
                    );
                }
                CommandPath permissionsPath = new CommandPath();
                int permissionsPathMaxLength = newPath.getPath().length;
                if(!mainCommand) {
                    permissionsPathMaxLength--;
                }
                for (int i = 0; i < permissionsPathMaxLength; i++) {
                    permissionsPath.add(newPath.getPath()[i]);
                    permissions.add(
                            ("command." + this.groupPath + "." + this.cmdName + "." + permissionsPath.getPermissionStr()+".*").replace("..", ".")
                    );
                }
                permissions.add(
                        ("command."+this.groupPath+"."+this.cmdName+".*").replace("..", ".")
                );
                if(!this.groupPath.isEmpty()) {
                    permissions.add(
                            ("command."+this.groupPath+".*").replace("..", ".")
                    );
                }
                permissions.add("command.*");

                if(customGlobalPermission != null) {
                    permissions.add(customGlobalPermission);
                }
                if(customPermission != null) {
                    permissions.add(customPermission);
                }
            }

            // === Source
            Parameter[] parameters = method.getParameters();
            Parameter sourceParam = parameters[0];
            Class<?> sourceClass = sourceParam.getType();

            boolean playerRequired = Player.class.equals(sourceClass);
            if(!playerRequired && !CommandSender.class.equals(sourceClass)) {
                continue;
            }

            Filter sourceFiltersAnn = sourceParam.getDeclaredAnnotation(Filter.class);
            FilterWrapper sourceFilters = null;
            if(sourceFiltersAnn != null) {
                sourceFilters = new FilterWrapper(sourceFiltersAnn);
            }

            // === Arguments
            List<CommandArg> args = new LinkedList<>();
            for (int i = 1; i < parameters.length; i++) {
                Parameter param = parameters[i];

                String paramName = param.getName();
                Class<?> paramClass = param.getType();

                Filter paramFiltersAnn = param.getDeclaredAnnotation(Filter.class);
                FilterWrapper filters = null;
                if(paramFiltersAnn != null) {
                    filters = new FilterWrapper(paramFiltersAnn);
                }

                Parser customParserAnn = param.getDeclaredAnnotation(Parser.class);
                Class<? extends IParamParser<?>> customParser = null;
                if(customParserAnn != null) {
                    customParser = customParserAnn.value();
                }

                CommandArg arg = new CommandArg(paramName,paramClass, customParser, filters);
                args.add(arg);
            }

            // === Endless
            boolean endless = false;
            if(!args.isEmpty()) {
                Parameter lastParam = parameters[parameters.length-1];
                if(lastParam.getType().equals(String.class) &&
                lastParam.getDeclaredAnnotation(LongString.class) != null) {
                    endless = true;
                    String paramName = lastParam.getName();
                    String newParamName = "[" + paramName + "]";
                    args.get(args.size()-1).setName(newParamName);
                }
            }

            // === Hidden
            boolean hidden = method.getDeclaredAnnotation(Hidden.class) != null;

            // === Save
            CommandInfo info = new CommandInfo(newPath,description,permissions,playerRequired,sourceFilters,args, invoker, method,endless,hidden);
            commands.add(info);

            String variantName = mainCommand ? newPath.getPermissionStr() + "." + name : newPath.getPermissionStr();
            setVariantInfo(variantName, "command", getCommandStr(info));
            setVariantInfo(variantName, "description", description);
            setVariantInfo(variantName, "permissions", permissions);
            setVariantInfo(variantName, "onlyPlayer", playerRequired);
            setVariantInfo(variantName, "hidden", hidden);
            this.variantIdx++;
        }

        // === Scan another classes
        for (Class<?> c : clazz.getDeclaredClasses()) {
            CommandPath newPath = path.clone();
            newPath.add(c.getSimpleName().toLowerCase());

            scanClass(
                newPath,
                c,
                c.getDeclaredConstructor(clazz).newInstance(invoker),
                customGlobalPermission,
                globalWithoutPermission
            );
        }

        saveCommandsFile();
    }

    //
    //  Overrides
    //

    @Override
    public final boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        Audience audience = this.adventure.sender(sender);
        List<CommandPath> possiblePaths = new ArrayList<>();

        this.subCommands.keySet().forEach(path -> {
            String[] pathArr = path.getPath();
            if(args.length < pathArr.length) {
                return;
            }
            for (int i = 0; i < pathArr.length; i++) {
                if(!args[i].equalsIgnoreCase(pathArr[i])) {
                    return;
                }
            }
            possiblePaths.add(path);
        });

        boolean found = false;
        CommandArg notPassArg = null;
        Object notPassObj = null;

        for (CommandPath path : possiblePaths) {
            List<CommandInfo> commands = this.subCommands.get(path);
            List<String> fixedArgs = new LinkedList<>(Arrays.asList(args).subList(path.getPath().length, args.length));

            for (CommandInfo command : commands) {
                if(!command.isEndless() && command.getArgs().size() != fixedArgs.size()) {
                    continue;
                }
                if(command.isEndless() && command.getArgs().size() > fixedArgs.size()) {
                    continue;
                }

                boolean correctTypes = true;
                for (int i = 0; i < command.getArgs().size(); i++) {
                    if(command.isEndless() && command.getArgs().size() == i+1) {
                        break;
                    }
                    CommandArg arg = command.getArgs().get(i);

                    if(arg.hasCustomParser()) {
                        if(!arg.getCustomParser().canConvert(fixedArgs.get(i), wrapper)) {
                            correctTypes = false;
                            break;
                        }
                    } else {
                        if(!parser.canConvert(fixedArgs.get(i), arg.getClazz(), wrapper)) {
                            correctTypes = false;
                            break;
                        }
                    }

                }
                if(!correctTypes) {
                    continue;
                }

                if(command.isPlayerRequired() && !(sender instanceof Player)) {
                    found = true;
                    continue;
                }

                if(!hasPermission(sender,command)) {
                    language.getComponent(LanguageLevel.GLOBAL, "noPermission").forEach(audience::sendMessage);
                    return false;
                }

                Object[] finalArgs = new Object[command.getArgs().size() + 1];
                finalArgs[0] = sender;
                for (int i = 1; i < finalArgs.length; i++) {
                    int j = i-1;
                    if(command.isEndless() && finalArgs.length == i+1) {
                        List<String> longStr = new ArrayList<>();
                        for (int k = j; k < fixedArgs.size(); k++) {
                            longStr.add(fixedArgs.get(k));
                        }
                        finalArgs[i] = String.join(" ", longStr);
                        break;
                    }
                    CommandArg arg = command.getArgs().get(j);
                    if(arg.hasCustomParser()) {
                        finalArgs[i] = arg.getCustomParser().convert(fixedArgs.get(j), wrapper);
                    } else {
                        finalArgs[i] = parser.convert(fixedArgs.get(j), arg.getClazz(), wrapper);
                    }

                }

                boolean passSourceFilters = passFilters(command.getSourceFilters(), sender,sender);
                if(!passSourceFilters) {
                    found = true;
                    notPassArg = new CommandArg("",null, null, command.getSourceFilters());
                    notPassObj = sender;
                    continue;
                }

                boolean passArgsFilters = true;
                for (int j = 1; j < finalArgs.length; j++) {
                    CommandArg arg = command.getArgs().get(j-1);
                    passArgsFilters = passFilters(arg.getFilters(), finalArgs[j],sender);
                    if(!passArgsFilters) {
                        found = true;
                        notPassArg = arg;
                        notPassObj = finalArgs[j];
                        break;
                    }
                }

                if(!passArgsFilters) {
                    continue;
                }

                try {
                    command.getMethod().invoke(command.getMethodInvoker(), finalArgs);
                    return true;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if(found) {
            if(notPassArg != null && notPassObj != null) {
                sendFilterMessages(notPassArg.getFilters(), notPassObj, sender,audience);
                return false;
            }
            language.getComponent(LanguageLevel.GLOBAL, "onlyPlayer").forEach(audience::sendMessage);
            return false;
        }
        sendHelp(sender);
        return true;
    }

    @NotNull
    @Override
    public final List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        List<String> result = new LinkedList<>();
        if(args.length == 0) return result;

        this.subCommands.forEach((path, commands) -> {
            commands.forEach(command -> {
                if(!hasPermission(sender,command)) {
                    return;
                }
                if(command.isHidden()) {
                    return;
                }
                if(command.isPlayerRequired() && !(sender instanceof Player)) {
                    return;
                }
                if(args.length > command.getArgs().size() + path.getPath().length && !command.isEndless()) {
                    return;
                }

                boolean correctPath = true;
                boolean inPath = false;
                for (int i = 0; i < path.getPath().length; i++) {
                    if(i < args.length-1) {
                        if(!args[i].equalsIgnoreCase(path.getPath()[i])) {
                            correctPath = false;
                            break;
                        }
                    }
                    if(i == args.length-1) {
                        inPath = true;
                        String resultPath = path.getPath()[i];
                        if(resultPath.startsWith(args[args.length-1])) {
                            result.add(path.getPath()[i]);
                        }
                        break;
                    }
                }

                if(!correctPath) {
                    return;
                }

                if(inPath) {
                    return;
                }

                List<String> fixedArgs = new ArrayList<>(Arrays.asList(args).subList(path.getPath().length, args.length));

                boolean correctTypes = true;
                for (int i = 0; i < Math.min(fixedArgs.size()-1, command.getArgs().size()-1); i++) {
                    CommandArg arg = command.getArgs().get(i);
                    if(arg.hasCustomParser()) {
                        if (!arg.getCustomParser().canConvert(fixedArgs.get(i), wrapper)) {
                            correctTypes = false;
                            break;
                        }
                    } else {
                        if (!parser.canConvert(fixedArgs.get(i), arg.getClazz(), wrapper)) {
                            correctTypes = false;
                            break;
                        }
                    }
                }

                if(!correctTypes) {
                    return;
                }

                CommandArg finalArg = command.isEndless() ? command.getArgs().get(command.getArgs().size()-1) : null;
                if(fixedArgs.size() <= command.getArgs().size()) {
                    finalArg = command.getArgs().get(fixedArgs.size()-1);
                }
                if(finalArg == null) {
                    return;
                }
                List<String> complete;
                if(finalArg.hasCustomParser()) {
                    complete = finalArg.getCustomParser().complete(args[args.length-1],sender,wrapper);
                } else {
                    complete = parser.complete(args[args.length-1],sender,finalArg.getClazz(),wrapper);;
                }
                result.add("<"+finalArg.getName()+">");
                result.addAll(getCompleterThatPass(complete,finalArg,sender));
            });
        });

        return result;
    }

    //
    // Help Command
    //

    public void sendHelp(CommandSender sender) {
        Audience audience = adventure.sender(sender);
        List<Component> componentsToSend = new LinkedList<>();

        subCommands.forEach((path, commands) -> {
            commands.forEach(command -> {
                if(!hasPermission(sender,command)) {
                    return;
                }
                if(command.isHidden()) {
                    return;
                }
                if(command.isPlayerRequired() && !(sender instanceof Player)) {
                    return;
                }

                componentsToSend.addAll(wrapper.getLanguageManager().getComponent(
                        LanguageLevel.PLUGIN,
                        "helpLine",
                        Placeholder.parsed("command", getCommandStr(command)),
                        Placeholder.unparsed("description", command.getDescription())
                ));
            });
        });

        if(componentsToSend.isEmpty()) {
            componentsToSend.addAll(language.getComponent(LanguageLevel.PLUGIN, "helpNoInfo"));
        }

        componentsToSend.addAll(0,language.getComponent(LanguageLevel.PLUGIN, "helpInfoStart", Placeholder.parsed("command", cmdName)));
        componentsToSend.add(0, Component.text(" "));

        componentsToSend.addAll(language.getComponent(LanguageLevel.PLUGIN, "helpInfoEnd", Placeholder.parsed("command", cmdName)));
        componentsToSend.add( Component.text(" "));

        componentsToSend.forEach(audience::sendMessage);
    }

    //
    // Utilities
    //

    private String getCommandStr(CommandInfo command) {
        CommandPath path = command.getPath();
        StringBuilder cmdStr = new StringBuilder("/");
        cmdStr.append(this.cmdName);
        if(path.getPath().length > 0) {
            cmdStr
                    .append(" ")
                    .append(path.getPathStr());
        }

        command.getArgs().forEach(arg -> {
            cmdStr.append(" ")
                    .append("<")
                    .append(arg.getName())
                    .append(">");
        });
        return cmdStr.toString();
    }

    private List<String> getCompleterThatPass(List<String> complete, CommandArg arg, CommandSender sender) {
        List<String> result = new ArrayList<>();
        complete.forEach(s -> {
            Object obj;
            if(arg.hasCustomParser()) {
                obj = arg.getCustomParser().convert(s, wrapper);;
            } else {
                obj = parser.convert(s, arg.getClazz(), wrapper);;
            }
            if(passFilters(arg.getFilters(),obj,sender)) {
                result.add(s);
            }
        });
        return result;
    }

    private boolean hasPermission(CommandSender sender, CommandInfo command) {
        for (String permission : command.getPermissions()) {
            if(sender.hasPermission(permission)) {
                return true;
            }
        }
        return command.getPermissions().isEmpty();
    }

    @SuppressWarnings("unchecked")
    private <T> boolean passFilters(FilterWrapper filters, T obj, CommandSender sender) {
        if(filters == null) {
            return true;
        }
        IFilter<T>[] orFilters = (IFilter<T>[]) filters.getOrFilters(obj.getClass());
        IFilter<T>[] andFilters = (IFilter<T>[]) filters.getAndFilters(obj.getClass());

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

    @SuppressWarnings("unchecked")
    private <T> void sendFilterMessages(FilterWrapper filters, T obj, CommandSender sender, Audience audience) {
        if(filters == null) {
            return;
        }
        List<Component> message = new ArrayList<>();
        int lastWeight = -1;

        IFilter<T>[] orFilters = (IFilter<T>[]) filters.getOrFilters(obj.getClass());
        IFilter<T>[] andFilters = (IFilter<T>[]) filters.getAndFilters(obj.getClass());

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

    private void setCommandInfo(String key, Object value) {
        this.commandsConf.set(this.cmdName+"."+key, value);
    }

    private void setVariantInfo(String variant, String key, Object value) {
        this.setCommandInfo("variants."+variantIdx+"_"+variant.replace(".", "_")+"."+key, value);
    }

    private void saveCommandsFile() {
        try {
            this.commandsConf.save(this.commandsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
