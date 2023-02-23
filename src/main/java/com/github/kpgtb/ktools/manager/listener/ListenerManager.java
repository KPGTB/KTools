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

package com.github.kpgtb.ktools.manager.listener;

import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.manager.listener.Klistener;
import com.github.kpgtb.ktools.util.ReflectionUtil;
import com.github.kpgtb.ktools.util.ToolsObjectWrapper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.io.File;

/**
 * ListenerManager handles all listeners in plugin
 */
public class ListenerManager {
    private final ToolsObjectWrapper toolsObjectWrapper;
    private final File jarFile;
    private final DebugManager debug;

    /**
     * Constructor of manager
     * @param toolsObjectWrapper ToolsObjectWrapper or object that extends it.
     * @param jarFile JAR file of plugin
     */
    public ListenerManager(ToolsObjectWrapper toolsObjectWrapper, File jarFile) {
        this.toolsObjectWrapper = toolsObjectWrapper;
        this.jarFile = jarFile;

        this.debug = toolsObjectWrapper.getDebugManager();
    }

    /**
     * Register all listeners from package
     * @param listenersPackage Package with listeners
     */
    public void registerListeners(String listenersPackage) {
        PluginManager pluginManager = Bukkit.getPluginManager();

        for(Class<?> clazz : ReflectionUtil.getAllClassesInPackage(jarFile,listenersPackage, Klistener.class)) {
            try {

                Klistener listener = (Klistener) clazz.getDeclaredConstructor(ToolsObjectWrapper.class)
                        .newInstance(toolsObjectWrapper);
                pluginManager.registerEvents(listener, toolsObjectWrapper.getPlugin());
                debug.sendInfo(DebugType.LISTENER, "Registered listener " + clazz.getSimpleName());

            } catch (Exception e) {
                debug.sendWarning(DebugType.LISTENER, "Error while loading listener from class " + clazz.getName());
                e.printStackTrace();
            }
        }
    }
}
