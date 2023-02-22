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
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.util.ReflectionUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;

public class ParamParserManager {
    private final DebugManager debug;

    private final HashMap<Class<?>, IParamParser<?>> parsers;

    public ParamParserManager(DebugManager debug) {
        this.debug = debug;
        this.parsers = new HashMap<>();
    }

    public <T> void registerParser(Class<T> clazz, IParamParser<T> parser) {
        parsers.put(clazz,parser);
    }

    @SuppressWarnings("unchecked")
    public <T> void registerParsers(String parsersPackage, File jarFile) {
        for(Class<?> clazz : ReflectionUtil.getAllClassesInPackage(jarFile,parsersPackage)) {
            if(!IParamParser.class.isAssignableFrom(clazz)) {
               continue;
            }
            ParameterizedType type = (ParameterizedType) clazz.getGenericInterfaces()[0];
            Class<T> typeArgClazz = (Class<T>) type.getActualTypeArguments()[0];
            try {
                IParamParser<T> parser = (IParamParser<T>) clazz.newInstance();
                registerParser(typeArgClazz, parser);
                debug.sendInfo(DebugType.PARSER, "Registered " + clazz.getSimpleName());
            } catch (InstantiationException | IllegalAccessException e) {
                debug.sendWarning(DebugType.PARSER, "Error while registering " + clazz.getSimpleName());
                e.printStackTrace();
            }
        }
    }

    public void unregisterParser(Class<?> clazz) {
        parsers.remove(clazz);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> IParamParser<T> getParser(Class<T> clazz) {
        return (IParamParser<T>) parsers.get(clazz);
    }

    public <T> boolean canConvert(String s, Class<T> expected) {
        IParamParser<T> parser = getParser(expected);
        if(parser == null) {
            return false;
        }
        return parser.canConvert(s);
    }

    public <T> T convert(String s, Class<T> expected) {
        IParamParser<T> parser = getParser(expected);
        if(!canConvert(s, expected) || parser == null) {
            throw new IllegalArgumentException("You try convert string to class that you can't convert");
        }
        return parser.convert(s);
    }

    public <T> List<String> complete(String s, CommandSender sender, Class<T> expected) {
        IParamParser<T> parser = getParser(expected);
        if(parser == null) {
            throw new IllegalArgumentException("You try convert string to class that you can't convert");
        }
        return parser.complete(s,sender);
    }
}
