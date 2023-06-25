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

import com.github.kpgtb.ktools.manager.command.filter.FilterWrapper;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Info about command
 */
public class CommandInfo {
    private final CommandPath path;
    private final String description;
    private final List<String> permissions;

    private final boolean playerRequired;
    private final FilterWrapper sourceFilters;

    private final List<CommandArg> args;

    private final Object methodInvoker;
    private final Method method;
    private final boolean endless;
    private final boolean hidden;

    public CommandInfo(CommandPath path, String description, List<String> permissions, boolean playerRequired, FilterWrapper sourceFilters, List<CommandArg> args, Object methodInvoker, Method method, boolean endless, boolean hidden) {
        this.path = path;
        this.description = description;
        this.permissions = permissions;
        this.playerRequired = playerRequired;
        this.sourceFilters = sourceFilters;
        this.args = args;
        this.methodInvoker = methodInvoker;
        this.method = method;
        this.endless = endless;
        this.hidden = hidden;
    }

    public Object getMethodInvoker() {
        return methodInvoker;
    }

    public CommandPath getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public boolean isPlayerRequired() {
        return playerRequired;
    }

    public FilterWrapper getSourceFilters() {
        return sourceFilters;
    }

    public List<CommandArg> getArgs() {
        return args;
    }

    public Method getMethod() {
        return method;
    }

    public boolean isEndless() {
        return endless;
    }

    public boolean isHidden() {
        return hidden;
    }
}
