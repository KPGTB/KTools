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

package com.github.kpgtb.ktools.manager.resourcepack;

import lombok.Getter;

import java.io.File;

/**
 * Object that contains information about custom character
 */
@Getter
public class CustomChar {
    private final String pluginName;
    private final File imageFile;
    private final String character;
    private final int height;
    private final int ascent;
    private final boolean alwaysReplace;

    public CustomChar(String pluginName, File imageFile, String character, int height, int ascent, boolean alwaysReplace) {
        this.pluginName = pluginName;
        this.imageFile = imageFile;
        this.character = character;
        this.height = height;
        this.ascent = ascent;
        this.alwaysReplace = alwaysReplace;
    }
}
