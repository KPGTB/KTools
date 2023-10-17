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
import com.github.kpgtb.ktools.util.file.ReflectionUtil;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.io.File;

/**
 * ListenerManager handles all listeners in plugin
 */
public class ListenerManager {
    private final ToolsObjectWrapper wrapper;
    private final File jarFile;
    private final DebugManager debug;

    /**
     * Constructor of manager
     * @param wrapper ToolsObjectWrapper or object that extends it.
     * @param jarFile JAR file of plugin
     */
    public ListenerManager(ToolsObjectWrapper wrapper, File jarFile) {
        this.wrapper = wrapper;
        this.jarFile = jarFile;

        this.debug = wrapper.getDebugManager();
    }

    /**
     * Register all listeners from package
     * @param listenersPackage Package with listeners
     */
    public void registerListeners(String listenersPackage) {
        PluginManager pluginManager = Bukkit.getPluginManager();

        for(Class<?> clazz : ReflectionUtil.getAllClassesInPackage(jarFile,listenersPackage, KListener.class)) {
            try {

                KListener listener = (KListener) clazz.getDeclaredConstructor(ToolsObjectWrapper.class)
                        .newInstance(wrapper);
                pluginManager.registerEvents(listener, wrapper.getPlugin());
                debug.sendInfo(DebugType.LISTENER, "Registered listener " + clazz.getSimpleName());

            } catch (Exception e) {
                debug.sendWarning(DebugType.LISTENER, "Error while loading listener from class " + clazz.getName());
                e.printStackTrace();
            }
        }
    }
}
