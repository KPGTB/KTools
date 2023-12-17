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

package com.github.kpgtb.ktools.util.file;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class CollectionUtil {
    public static final String NEXT_IDENTIFIER = ">";
    public static final String MAP_ENTRY_SPLIT = "___";

    public static String getObjectTypes(Object obj) {
        StringBuilder result = new StringBuilder();

        while(obj != null) {
            result.append(obj.getClass().getName());

            if(obj instanceof Collection) {
                Collection<?> collection = (Collection<?>) obj;
                if(collection.isEmpty()) {
                    break;
                }

                obj = collection.toArray()[0];
                result.append(NEXT_IDENTIFIER);
                continue;
            }

            if(obj instanceof Map) {
                Map<?,?> map = (Map<?, ?>) obj;
                if(map.isEmpty()) {
                    break;
                }

                String key = map.keySet().toArray()[0].getClass().getName();
                obj = map.values().toArray()[0];

                result.append(NEXT_IDENTIFIER)
                    .append(key)
                    .append(MAP_ENTRY_SPLIT);
                continue;
            }

            break;
        }

        return result.toString();
    }

    public static Type[] getTypesFromString(String s) throws ClassNotFoundException {
        return getTypesFromString(s.split(NEXT_IDENTIFIER), 0);
    }

    public static Type[] getTypesFromString(String[] elements, int idx) throws ClassNotFoundException {
        String[] mapElements = elements[idx].split(MAP_ENTRY_SPLIT, 2);
        boolean isMap = mapElements.length == 2;

        if(idx+1 == elements.length) {
            if(isMap) {
                return new Class[]{Class.forName(mapElements[0]), Class.forName(mapElements[1])};
            }
            return new Class[]{Class.forName(elements[idx])};
        }

        if(isMap) {
            return new Type[]{
                    Class.forName(mapElements[0]),
                    TypeToken.getParameterized(
                            Class.forName(mapElements[1]),
                            getTypesFromString(elements,++idx)
                    ).getType()
            };
        }

        return new Type[]{TypeToken.getParameterized(
                Class.forName(elements[idx]),
                getTypesFromString(elements,++idx)
        ).getType()};
    }
}
