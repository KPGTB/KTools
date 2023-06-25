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

import com.github.kpgtb.ktools.manager.ui.bar.save.IBarSaveMethod;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Custom bar object
 */
public class KBar {
    private final String name;

    private final IBarSaveMethod saveMethod;
    private final List<BarIcons> icons;
    private final int uniqueID;
    private final double max;
    private final double defaultValue;

    private final boolean defaultShow;
    private final boolean hideInCreative;
    private final boolean hideInSpectator;

    public KBar(String name, IBarSaveMethod saveMethod, List<BarIcons> icons, int uniqueID, double max, double defaultValue, boolean defaultShow, boolean hideInCreative, boolean hideInSpectator) {
        this.name = name.toLowerCase();
        this.saveMethod = saveMethod;
        this.icons = icons;
        this.uniqueID = uniqueID;
        this.max = max;
        this.defaultValue = defaultValue;
        this.defaultShow = defaultShow;
        this.hideInCreative = hideInCreative;
        this.hideInSpectator = hideInSpectator;
    }

    public String getName() {
        return name;
    }

    public IBarSaveMethod getSaveMethod() {
        return saveMethod;
    }

    public List<BarIcons> getIcons() {
        return icons;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public double getMax() {
        return max;
    }

    public double getDefaultValue() {
        return defaultValue;
    }

    public boolean isDefaultShow() {
        return defaultShow;
    }

    public boolean isHideInCreative() {
        return hideInCreative;
    }

    public boolean isHideInSpectator() {
        return hideInSpectator;
    }

    @Nullable
    public BarIcons getIconsFor(double value) {
        for (BarIcons icon : icons) {
            if(icon.getFrom() <= value && icon.getTo() >= value) {
                return icon;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KBar kBar = (KBar) o;
        return Objects.equals(uniqueID, kBar.uniqueID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueID);
    }
}
