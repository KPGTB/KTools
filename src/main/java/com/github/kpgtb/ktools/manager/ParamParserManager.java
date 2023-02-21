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

package com.github.kpgtb.ktools.manager;

import com.github.kpgtb.ktools.manager.command.IParamParser;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ParamParserManager {
    private final HashMap<Class<?>, IParamParser<?>> parsers;

    public ParamParserManager() {
        this.parsers = new HashMap<>();
    }

    public <T> void registerParser(Class<T> clazz, IParamParser<T> parser) {
        parsers.put(clazz,parser);
    }

    public void unregisterParser(Class<?> clazz) {
        parsers.remove(clazz);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> IParamParser<T> getParser(Class<T> clazz) {
        return (IParamParser<T>) parsers.get(clazz);
    }
}
