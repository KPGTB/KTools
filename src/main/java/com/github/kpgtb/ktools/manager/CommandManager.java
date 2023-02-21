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

package com.github.kpgtb.ktools.manager;

import com.github.kpgtb.ktools.manager.command.KCommand;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.util.ReflectionUtil;
import com.github.kpgtb.ktools.util.ToolsObjectWrapper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;

import java.io.File;
import java.lang.reflect.Field;

public class CommandManager {
    private final ToolsObjectWrapper toolsObjectWrapper;
    private final File jarFile;
    private final String pluginTag;

    private final DebugManager debug;

    public CommandManager(ToolsObjectWrapper toolsObjectWrapper, File jarFile, String pluginTag) {
        this.toolsObjectWrapper = toolsObjectWrapper;
        this.jarFile = jarFile;
        this.pluginTag = pluginTag;

        this.debug = toolsObjectWrapper.getDebugManager();
    }

    public void registerCommands(String commandsPackage) {
        Field f;
        CommandMap commandMap;

        try {
            f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            debug.sendWarning(DebugType.COMMAND, "Error while loading commands");
            throw new RuntimeException(e);
        }

        for(Class<?> clazz : ReflectionUtil.getAllClassesInPackage(jarFile,commandsPackage, KCommand.class)) {
            try {

                String[] groupPath = clazz.getName().split("\\.");
                StringBuilder finalPath = new StringBuilder();

                for (int i = commandsPackage.split("\\.").length; i < (groupPath.length - 1); i++) {
                    finalPath
                            .append(groupPath[i])
                            .append(".");
                }

                if(finalPath.length() > 0) {
                    finalPath.deleteCharAt(finalPath.length() - 1);
                }

                KCommand command = (KCommand) clazz.getDeclaredConstructor(ToolsObjectWrapper.class, String.class)
                        .newInstance(toolsObjectWrapper, finalPath.toString());
                commandMap.register(pluginTag, command);

            } catch (Exception e) {
                debug.sendWarning(DebugType.COMMAND, "Error while loading command from class " + clazz.getName());
                e.printStackTrace();
            }
        }

        f.setAccessible(false);
    }
}