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

package com.github.kpgtb.ktools.manager.command.parser;

import com.github.kpgtb.ktools.util.ToolsObjectWrapper;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface that is used to parse String to Class
 * @param <T> Class that should be parsed
 */
public interface IParamParser<T> {
    /**
     * Convert String to Class
     * @param param String that should be converted
     * @param wrapper Instance of ToolsObjectWrapper
     * @return Converted string
     */
    T convert(String param, ToolsObjectWrapper wrapper);

    /**
     * Check if String can be converted to Class
     * @param param String that should be checked
    * @param wrapper Instance of ToolsObjectWrapper
     * @return true if string can be converted or false if can't
     */
    boolean canConvert(String param, ToolsObjectWrapper wrapper);

    /**
     * Prepare list of Strings that can be used to tab completer
     * @param arg Argument that is written by player
     * @param sender CommandSender
     * @param wrapper Instance of ToolsObjectWrapper
     * @return List of strings to tab completer. It can be empty.
     */
    @NotNull
    List<String> complete(String arg, CommandSender sender, ToolsObjectWrapper wrapper);
}
