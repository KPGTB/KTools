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

package com.github.kpgtb.ktools.util;

import com.google.gson.JsonElement;

import java.util.HashMap;

public class FontWidth {
    public static final HashMap<Character, Integer> customWidths = new HashMap<>();
    private static JsonElement spacesJson;

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

        customWidths.put('\uF801', -1);
        customWidths.put('\uF802', -2);
        customWidths.put('\uF803', -3);
        customWidths.put('\uF804', -4);
        customWidths.put('\uF805', -5);
        customWidths.put('\uF806', -6);
        customWidths.put('\uF807', -7);
        customWidths.put('\uF808', -8);
        customWidths.put('\uF809', -16);
        customWidths.put('\uF80A', -32);
        customWidths.put('\uF80B', -64);
        customWidths.put('\uF80C', -128);
        customWidths.put('\uF80D', -256);
        customWidths.put('\uF80E', -512);
        customWidths.put('\uF80F', -1024);

        customWidths.put('\uF821', 1);
        customWidths.put('\uF822', 2);
        customWidths.put('\uF823', 3);
        customWidths.put('\uF824', 4);
        customWidths.put('\uF825', 5);
        customWidths.put('\uF826', 6);
        customWidths.put('\uF827', 7);
        customWidths.put('\uF828', 8);
        customWidths.put('\uF829', 16);
        customWidths.put('\uF82A', 32);
        customWidths.put('\uF82B', 64);
        customWidths.put('\uF82C', 128);
        customWidths.put('\uF82D', 256);
        customWidths.put('\uF82E', 512);
        customWidths.put('\uF82F', 1024);

        spacesJson = spaces;
    }

    public static void registerCustomChar(Character character, int width) {
        customWidths.put(character, width);
    }

    public static Integer getWidth(Character character) {
        return customWidths.getOrDefault(character, 6);
    }

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
