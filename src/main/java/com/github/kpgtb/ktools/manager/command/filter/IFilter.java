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

package com.github.kpgtb.ktools.manager.command.filter;

import com.github.kpgtb.ktools.util.ToolsObjectWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Filter to command's arguments. It can be used by annotation {@link com.github.kpgtb.ktools.manager.command.annotation.Filter}
 */
public interface IFilter<T> {
    /**
     * Filter method
     * @param obj Object that must be filtered
     * @param wrapper Instance of ToolsObjectWrapper
     * @param sender Command sender
     * @return true if object pass the test
     */
    boolean filter(T obj, ToolsObjectWrapper wrapper, CommandSender sender);

    /**
     * Component message that should be sent when filter is not passed
     * @param obj Object that must be filtered
     * @param wrapper Instance of ToolsObjectWrapper
     * @return Component that can be sent
     */
    List<Component> notPassMessage(T obj, ToolsObjectWrapper wrapper);

    /**
     * Weight of the filter
     * @return It declares which filter has the most priority to be sent on chat
     */
    int weight();
}
