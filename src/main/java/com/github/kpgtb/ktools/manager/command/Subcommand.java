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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class Subcommand {
    private final String name;
    private final String description;
    private final ArrayList<String> permissions;
    private final boolean playerRequired;
    private final HashMap<String, Class<?>> argsType;
    private final Method method;

    public Subcommand(String name, String description, ArrayList<String> permissions, boolean playerRequired, HashMap<String, Class<?>> argsType, Method method) {
        this.name = name;
        this.description = description;
        this.permissions = permissions;
        this.playerRequired = playerRequired;
        this.argsType = argsType;
        this.method = method;
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

    public HashMap<String, Class<?>> getArgsType() {
        return argsType;
    }

    public Method getMethod() {
        return method;
    }
}
