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

package com.github.kpgtb.ktools.manager.ui;

import com.github.kpgtb.ktools.KTools;
import com.github.kpgtb.ktools.util.ui.FontWidth;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This object contains information about UI objects
 */
public class BaseUiObject {
    private Component component;
    private ToolsObjectWrapper wrapper;
    private final Alignment alignment;
    private final int offset;

    @Getter
    private Component componentToShow;


    public BaseUiObject(Component component, Alignment alignment, int offset, ToolsObjectWrapper wrapper) {
        this.component = component;
        this.alignment = alignment;
        this.offset = offset;
        this.wrapper =wrapper;

        build();
    }

    private void build() {
        Integer[] pixels = getLeftAndRightPixels();

        this.componentToShow = Component.text(FontWidth.getSpaces(pixels[0])).append(this.component).append(Component.text(FontWidth.getSpaces(pixels[1])));
    }

    /**
     * Update component
     * @param component
     */
    public void update(Component component) {
        this.component = component;
        build();
    }

    // Private utils

    private String fixString(String text) {
        String fixed = text;

        Pattern colorPattern = Pattern.compile("§[xa-fA-F0-9]");
        Matcher colorMatcher = colorPattern.matcher(text);
        while (colorMatcher.find()) {
            fixed = fixed.replace(colorMatcher.group(), "");
        }
        return fixed;
    }

    private Integer[] getLeftAndRightPixels() {
        double width = 0;

        for(Character character : fixString(
            this.wrapper.getLanguageManager().convertComponentToString(this.component)
        ).toCharArray()) {
            width += FontWidth.getWidth(character);
        }

        Integer[] pixels = new Integer[2];

        switch (alignment) {
            case LEFT:
                pixels[0] = offset;
                pixels[1] = (int) (-offset-Math.round(width));
                break;
            case RIGHT:
                pixels[0] = (int) (offset-Math.round(width));
                pixels[1] = -offset;
                break;
            case CENTER:
                pixels[0] = (int) (offset-Math.round(width)/2);
                pixels[1] = (int) (-pixels[0]-Math.round(width));
                break;
        }

        return pixels;
    }
}
