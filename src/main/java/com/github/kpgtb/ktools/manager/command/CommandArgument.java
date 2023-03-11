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

public class CommandArgument {
    private final String name;
    private final Class<?> clazz;
    private final Class<? extends IFilter<?>>[] orFilters;
    private final Class<? extends IFilter<?>>[] andFilters;

    public CommandArgument(String name, Class<?> clazz, Class<? extends IFilter<?>>[] orFilters, Class<? extends IFilter<?>>[] andFilters) {
        this.name = name;
        this.clazz = clazz;
        this.orFilters = orFilters;
        this.andFilters = andFilters;
    }

    public String getName() {
        return name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Class<? extends IFilter<?>>[] getOrFilters() {
        return orFilters;
    }

    public Class<? extends IFilter<?>>[] getAndFilters() {
        return andFilters;
    }
}
