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
import com.github.kpgtb.ktools.manager.command.parser.IParamParser;

public class CommandArg {
    private String name;
    private final Class<?> clazz;
    private final Class<? extends IParamParser<?>> customParser;
    private final FilterWrapper filters;

    public CommandArg(String name, Class<?> clazz, Class<? extends IParamParser<?>> customParser, FilterWrapper filters) {
        this.name = name;
        this.clazz = clazz;
        this.customParser = customParser;
        this.filters = filters;
    }

    public String getName() {
        return name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public FilterWrapper getFilters() {
        return filters;
    }

    public Class<? extends IParamParser<?>> getCustomParserClass() {
        return customParser;
    }

    public IParamParser<?> getCustomParser() {
        if(!hasCustomParser()) {
            return null;
        }
        try {
            return customParser.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasCustomParser() {
        return customParser != null;
    }

    public void setName(String name) {
        this.name = name;
    }
}
