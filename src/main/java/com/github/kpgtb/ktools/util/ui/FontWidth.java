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

package com.github.kpgtb.ktools.util.ui;

import com.google.gson.JsonElement;

import java.util.HashMap;

/**
 * Util with width of characters
 */
public class FontWidth {
    public static final HashMap<Character, Integer> customWidths = new HashMap<>();
    private static JsonElement spacesJson;

    /**
     * Init widths
     * @param spaces File with spaces from NegativeSpaces represented as JsonElement
     */
    public static void initWidth(JsonElement spaces) {
        customWidths.put(' ', 4);
        customWidths.put('f', 5);
        customWidths.put('t', 4);
        customWidths.put('i', 2);
        customWidths.put('k', 5);
        customWidths.put('l', 3);
        customWidths.put('I', 4);
        customWidths.put('\'', 2);
        customWidths.put('.', 2);
        customWidths.put(',', 2);
        customWidths.put(';', 2);
        customWidths.put(':', 2);
        customWidths.put('[', 4);
        customWidths.put(']', 4);
        customWidths.put('{', 4);
        customWidths.put('}', 4);
        customWidths.put('*', 4);
        customWidths.put('!', 2);
        customWidths.put('"', 4);
        customWidths.put('(', 4);
        customWidths.put(')', 4);
        customWidths.put('|', 2);
        customWidths.put('`', 3);
        customWidths.put('<', 5);
        customWidths.put('>', 5);
        customWidths.put('@',7 );
        customWidths.put('~', 7);

        spacesJson = spaces;
    }

    /**
     * Register custom character width
     * @param character Character
     * @param width Width
     */
    public static void registerCustomChar(Character character, int width) {
        customWidths.put(character, width);
    }

    /**
     * Get width of character
     * @param character Character
     * @return Width of character
     */
    public static Integer getWidth(Character character) {
        return customWidths.getOrDefault(character, 6);
    }

    /**
     * Get string with negative spaces
     * @param spaces Spaces number
     * @return String with spaces chars
     */
    public static String getSpaces(int spaces) {

        if(spaces > 1024 || spaces < -1024) {
            StringBuilder builder = new StringBuilder();
            int full = Math.floorDiv(spaces, 1024);
            int other = spaces % 1024;

            if(full > 0) {
                for (int i = 0; i < full; i++) {
                    builder.append(spacesJson.getAsJsonObject().get("space.1024").getAsString().replace("%s", ""));
                }
            } else {
                for (int i = 0; i < Math.abs(full); i++) {
                    builder.append(spacesJson.getAsJsonObject().get("space.-1024").getAsString().replace("%s", ""));
                }
            }
            builder.append(spacesJson.getAsJsonObject().get("space."+other).getAsString().replace("%s", ""));

            return builder.toString();
        }

        return spacesJson.getAsJsonObject().get("space."+spaces).getAsString().replace("%s", "");
    }
}
