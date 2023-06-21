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

package com.github.kpgtb.ktools.manager.command.parser.custom;

import com.github.kpgtb.ktools.manager.command.parser.IParamParser;
import com.github.kpgtb.ktools.manager.item.KItem;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class CustomItemParser implements IParamParser<KItem> {
    @Override
    public KItem convert(String param, ToolsObjectWrapper wrapper) {
        return wrapper.getItemManager().getCustomItems().get(param);
    }

    @Override
    public boolean canConvert(String param, ToolsObjectWrapper wrapper) {
        return wrapper.getItemManager().getCustomItems().containsKey(param);
    }

    @Override
    public @NotNull List<String> complete(String arg, CommandSender sender, ToolsObjectWrapper wrapper) {
        return wrapper.getItemManager().getCustomItems().keySet().stream()
                .filter(item -> item.startsWith(arg) || item.split(":",2)[1].startsWith(arg))
                .limit(30)
                .collect(Collectors.toList());
    }
}
