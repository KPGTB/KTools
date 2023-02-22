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

package com.github.kpgtb.ktools.manager.command.parser.java;

import com.github.kpgtb.ktools.manager.command.IParamParser;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LongParser implements IParamParser<Long> {
    @Override
    public Long convert(String param) {
        return Long.parseLong(param);
    }

    @Override
    public boolean canConvert(String param) {
        try {
            convert(param);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public @NotNull List<String> complete(String arg, CommandSender sender) {
        return new ArrayList<>();
    }
}
