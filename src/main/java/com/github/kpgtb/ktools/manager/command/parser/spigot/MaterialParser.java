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
import com.github.kpgtb.ktools.util.ToolsObjectWrapper;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MaterialParser implements IParamParser<Material> {
    @Override
    public Material convert(String param, ToolsObjectWrapper wrapper) {
        return Material.valueOf(param.toUpperCase());
    }

    @Override
    public boolean canConvert(String param, ToolsObjectWrapper wrapper) {
        try {
            convert(param,wrapper);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public @NotNull List<String> complete(String arg, CommandSender sender, ToolsObjectWrapper wrapper) {
        return Arrays.stream(Material.values())
                .map(Material::name)
                .filter(s -> s.startsWith(arg.toUpperCase()))
                .limit(30)
                .collect(Collectors.toList());
    }
}
