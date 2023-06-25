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

package com.github.kpgtb.ktools.manager.ui.bar;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Icons in bar
 */
public class BarIcons {
    private final double from;
    private final double to;

    private final JavaPlugin plugin;
    private final String fullImage;
    private final String halfImage;
    private final String emptyImage;

    private Map<Integer,String> fullChar;
    private Map<Integer,String> halfChar;
    private Map<Integer,String> emptyChar;

    private final int iconsHeight;
    private final int iconsWidth;

    public BarIcons(double from, double to, JavaPlugin plugin, String fullImage, String halfImage, String emptyImage, int iconsHeight, int iconsWidth) {
        this.from = from;
        this.to = to;
        this.plugin = plugin;
        this.fullImage = fullImage;
        this.halfImage = halfImage;
        this.emptyImage = emptyImage;
        this.iconsHeight = iconsHeight;
        this.iconsWidth = iconsWidth;

        this.fullChar = new HashMap<>();
        this.halfChar = new HashMap<>();
        this.emptyChar = new HashMap<>();
    }

    public Map<Integer, String> getFullChar() {
        return fullChar;
    }

    public void setFullChar(Map<Integer, String> fullChar) {
        this.fullChar = fullChar;
    }

    public Map<Integer, String> getHalfChar() {
        return halfChar;
    }

    public void setHalfChar(Map<Integer, String> halfChar) {
        this.halfChar = halfChar;
    }

    public Map<Integer, String> getEmptyChar() {
        return emptyChar;
    }

    public void setEmptyChar(Map<Integer, String> emptyChar) {
        this.emptyChar = emptyChar;
    }

    public int getIconsHeight() {
        return iconsHeight;
    }

    public int getIconsWidth() {
        return iconsWidth;
    }

    public double getFrom() {
        return from;
    }

    public double getTo() {
        return to;
    }

    public InputStream getFullImage() {
        return this.plugin.getResource(this.fullImage);
    }

    public InputStream getHalfImage() {
        return this.plugin.getResource(this.halfImage);
    }

    public InputStream getEmptyImage() {
        return this.plugin.getResource(this.emptyImage);
    }
}
