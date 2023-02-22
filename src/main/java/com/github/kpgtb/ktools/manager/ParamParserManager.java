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

/**
 * Manager of ParamParsers that are used in commands
 */
public class ParamParserManager {
    private final DebugManager debug;

    /**
     * Map with all parsers
     */
    private final HashMap<Class<?>, IParamParser<?>> parsers;

    /**
     * Constructor of manager
     * @param debug Instance of DebugManager
     */
    public ParamParserManager(DebugManager debug) {
        this.debug = debug;
        this.parsers = new HashMap<>();
    }

    /**
     * Register ParamParser
     * @param clazz Class that is parsed
     * @param parser Parser instance
     */
    @SuppressWarnings("unchecked")
    public <T> void registerParser(Class<T> clazz, IParamParser<T> parser) {
        debug.sendInfo(DebugType.PARSER, "Registered " + clazz.getSimpleName() + " as " + clazz.getSimpleName());
        parsers.put(clazz,parser);
        if (!clazz.isPrimitive()) {
            Class<?> primitiveClazz = clazz.equals(Boolean.class) ? Boolean.TYPE :
                    clazz.equals(Character.class) ? Character.TYPE :
                    clazz.equals(Byte.class) ? Byte.TYPE :
                    clazz.equals(Short.class) ? Short.TYPE :
                    clazz.equals(Integer.class) ? Integer.TYPE :
                    clazz.equals(Long.class) ? Long.TYPE :
                    clazz.equals(Float.class) ? Float.TYPE :
                    clazz.equals(Double.class) ? Double.TYPE :
                    null;
            if (primitiveClazz != null) {
                registerParser((Class<T>) primitiveClazz, parser);
                debug.sendInfo(DebugType.PARSER, "Registered " + clazz.getSimpleName() + " as " + primitiveClazz.getSimpleName());
            } else {
                debug.sendInfo(DebugType.PARSER, "This class don't have primitive representation");
            }
        }
    }

    /**
     * Register all ParamParsers from specified package
     * @param parsersPackage Package where parsers are stored
     * @param jarFile File with that plugin
     */
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
            } catch (InstantiationException | IllegalAccessException e) {
                debug.sendWarning(DebugType.PARSER, "Error while registering " + clazz.getSimpleName());
                e.printStackTrace();
            }
        }
    }

    /**
     * Unregister ParamParser
     * @param clazz Class that should be unregistered
     */
    public void unregisterParser(Class<?> clazz) {
        this.debug.sendInfo(DebugType.PARSER, "Unregistered " + clazz.getSimpleName());
        parsers.remove(clazz);
    }

    /**
     * Get parser of specified class
     * @param clazz Class that have parser
     * @return IParamParser or null if there isn't any parsers of this class
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> IParamParser<T> getParser(Class<T> clazz) {
        return (IParamParser<T>) parsers.get(clazz);
    }

    /**
     * Check if string can be covert to class
     * @param s String that you want to convert
     * @param expected Class that is expected
     * @return true if you can convert, or false if you can't
     */
    public <T> boolean canConvert(String s, Class<T> expected) {
        IParamParser<T> parser = getParser(expected);
        if(parser == null) {
            return false;
        }
        return parser.canConvert(s);
    }

    /**
     * Convert string to class
     * @param s String that you want to convert
     * @param expected Class that is expected
     * @return Class that is converted from string
     */
    public <T> T convert(String s, Class<T> expected) {
        IParamParser<T> parser = getParser(expected);
        if(!canConvert(s, expected) || parser == null) {
            throw new IllegalArgumentException("You try convert string to class that you can't convert");
        }
        return parser.convert(s);
    }

    /**
     * Get a list to tab completer
     * @param s Command argument
     * @param sender CommandSender
     * @param expected Class that is expected
     */
    public <T> List<String> complete(String s, CommandSender sender, Class<T> expected) {
        IParamParser<T> parser = getParser(expected);
        if(parser == null) {
            throw new IllegalArgumentException("You try convert string to class that you can't convert");
        }
        return parser.complete(s,sender);
    }
}
