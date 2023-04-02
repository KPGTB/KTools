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

package com.github.kpgtb.ktools.manager.command.parser.spigot;

import com.github.kpgtb.ktools.manager.command.parser.IParamParser;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class WorldParser implements IParamParser<World> {
    @Override
    public World convert(String param, ToolsObjectWrapper wrapper) {
        return Bukkit.getWorld(param);
    }

    @Override
    public boolean canConvert(String param, ToolsObjectWrapper wrapper) {
        return convert(param, wrapper) != null;
    }

    @Override
    public @NotNull List<String> complete(String arg, CommandSender sender, ToolsObjectWrapper wrapper) {
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(s -> s.startsWith(arg))
                .limit(30)
                .collect(Collectors.toList());
    }
}
