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

import com.github.kpgtb.ktools.manager.command.filter.IFilter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Object that stores all information about sub command
 */
public class Subcommand {
    private final String name;
    private final String description;
    private final ArrayList<String> permissions;
    private final boolean playerRequired;
    private final Class<? extends IFilter<?>>[] senderOrFilters;
    private final Class<? extends IFilter<?>>[] senderAndFilters;
    private final LinkedHashMap<String, CommandArgument> argsType;
    private final Method method;
    private final boolean endless;

    public Subcommand(String name, String description, ArrayList<String> permissions, boolean playerRequired, Class<? extends IFilter<?>>[] senderOrFilters, Class<? extends IFilter<?>>[] senderAndFilters, LinkedHashMap<String, CommandArgument> argsType, Method method, boolean endless) {
        this.name = name;
        this.description = description;
        this.permissions = permissions;
        this.playerRequired = playerRequired;
        this.senderOrFilters = senderOrFilters;
        this.senderAndFilters = senderAndFilters;
        this.argsType = argsType;
        this.method = method;
        this.endless = endless;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getPermissions() {
        return permissions;
    }

    public boolean isPlayerRequired() {
        return playerRequired;
    }

    public LinkedHashMap<String, CommandArgument> getArgsType() {
        return argsType;
    }

    public Method getMethod() {
        return method;
    }

    public boolean isEndless() {
        return endless;
    }

    public Class<? extends IFilter<?>>[] getSenderOrFilters() {
        return senderOrFilters;
    }

    public Class<? extends IFilter<?>>[] getSenderAndFilters() {
        return senderAndFilters;
    }
}
