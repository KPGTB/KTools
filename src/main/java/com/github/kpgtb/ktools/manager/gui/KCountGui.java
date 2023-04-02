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

package com.github.kpgtb.ktools.manager.gui;

import com.github.kpgtb.ktools.manager.gui.container.GuiContainer;
import com.github.kpgtb.ktools.manager.gui.item.GuiItem;
import com.github.kpgtb.ktools.manager.gui.write.ICountResponse;
import com.github.kpgtb.ktools.manager.language.LanguageLevel;
import com.github.kpgtb.ktools.util.item.ItemBuilder;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Count GUI is a gui with response where you can count sth
 */
public class KCountGui {
    private final ToolsObjectWrapper wrapper;
    private final KGui lastGui;
    private final ICountResponse response;
    private final Player player;

    private final double defaultValue;
    private final double min;
    private final double max;
    private final boolean decimals;

    private final Material countMat;

    private double value;
    private boolean responsed;

    public KCountGui(ToolsObjectWrapper wrapper, KGui lastGui, ICountResponse response, Player player, double defaultValue, double min, double max, boolean decimals, Material countMat) {
        this.wrapper = wrapper;
        this.lastGui = lastGui;
        this.response = response;
        this.player = player;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.decimals = decimals;
        this.countMat = countMat;

        this.value = defaultValue;
        this.responsed = false;
    }

    /**
     * Open GUI to player
     */
    public void open() {
        int rows = this.decimals ? 3 : 1;
        KGui gui = new KGui(
                wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countGuiName"),
                rows,
                wrapper
        );

        gui.blockClick();
        gui.setCloseAction(e -> {
            if(!this.responsed) {
                this.response.response(defaultValue);
                if(this.lastGui != null) {
                    this.lastGui.open((Player) e.getPlayer());
                }
            }
        });

        GuiContainer container = new GuiContainer(gui, 0,0,9,rows);

        changeValue(gui,container,0.0);

        GuiItem plus1 = new GuiItem(
                new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                        .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countPlus", Placeholder.parsed("value", 1.0D + "")))
                        .build()
        );
        plus1.setClickAction(e -> changeValue(gui, container,1.0));

        GuiItem plus10 = new GuiItem(
                new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                        .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countPlus", Placeholder.parsed("value", 10.0D + "")))
                        .build()
        );
        plus10.setClickAction(e -> changeValue(gui, container,10.0));

        GuiItem plus100 = new GuiItem(
                new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                        .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countPlus", Placeholder.parsed("value", 100.0D + "")))
                        .build()
        );
        plus100.setClickAction(e -> changeValue(gui, container,100.0));

        GuiItem minus1 = new GuiItem(
                new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                        .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countMinus", Placeholder.parsed("value", 1.0D + "")))
                        .build()
        );
        minus1.setClickAction(e -> changeValue(gui, container,-1.0));

        GuiItem minus10 = new GuiItem(
                new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                        .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countMinus", Placeholder.parsed("value", 10.0D + "")))
                        .build()
        );
        minus10.setClickAction(e -> changeValue(gui, container,-10.0));

        GuiItem minus100 = new GuiItem(
                new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                        .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countMinus", Placeholder.parsed("value", 100.0D + "")))
                        .build()
        );
        minus100.setClickAction(e -> changeValue(gui, container,-100.0));

        if(decimals) {
            GuiItem plusD01 = new GuiItem(
                    new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                            .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countPlus", Placeholder.parsed("value", 0.01D + "")))
                            .build()
            );
            plusD01.setClickAction(e -> changeValue(gui, container,0.01));

            GuiItem plusD1 = new GuiItem(
                    new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                            .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countPlus", Placeholder.parsed("value", 0.1D + "")))
                            .build()
            );
            plusD1.setClickAction(e -> changeValue(gui, container,0.1));

            GuiItem plusD5 = new GuiItem(
                    new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                            .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countPlus", Placeholder.parsed("value", 0.5D + "")))
                            .build()
            );
            plusD5.setClickAction(e -> changeValue(gui, container,0.5));

            GuiItem minusD01 = new GuiItem(
                    new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                            .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countMinus", Placeholder.parsed("value", 0.01D + "")))
                            .build()
            );
            minusD01.setClickAction(e -> changeValue(gui, container,-0.01));

            GuiItem minusD1 = new GuiItem(
                    new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                            .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countMinus", Placeholder.parsed("value", 0.1D + "")))
                            .build()
            );
            minusD1.setClickAction(e -> changeValue(gui, container,-0.1));

            GuiItem minusD5 = new GuiItem(
                    new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                            .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countMinus", Placeholder.parsed("value", 0.5D + "")))
                            .build()
            );
            minusD5.setClickAction(e -> changeValue(gui, container,-0.5));

            container.setItem(1,2,minus100);
            container.setItem(1,1,minus10);
            container.setItem(1,0,minus1);

            container.setItem(2,2,minusD5);
            container.setItem(2,1,minusD1);
            container.setItem(2,0,minusD01);

            container.setItem(6,2,plusD5);
            container.setItem(6,1,plusD1);
            container.setItem(6,0,plusD01);

            container.setItem(7,0,plus1);
            container.setItem(7,1,plus10);
            container.setItem(7,2,plus100);

        } else {
            container.setItem(0,0,minus100);
            container.setItem(1,0,minus10);
            container.setItem(2,0,minus1);

            container.setItem(6,0,plus1);
            container.setItem(7,0,plus10);
            container.setItem(8,0,plus100);
        }

        gui.addContainer(container);

        gui.open(player);
    }

    private void changeValue(KGui gui, GuiContainer container, double addValue) {
        double newValue = this.value + addValue;

        if(newValue < min) {
            return;
        }
        if(newValue > max) {
            return;
        }

        this.value = newValue;

        GuiItem countItem = new GuiItem(
                new ItemBuilder(this.countMat)
                        .displayname(
                                wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countInfo", Placeholder.parsed("value", this.value+""))
                        )
                        .lore(
                                wrapper.getLanguageManager().getString(LanguageLevel.GLOBAL, "countInfoLore")
                        )
                        .build()
        );
        countItem.setClickAction(e -> {
            this.responsed = true;
            this.response.response(this.value);
            if(this.lastGui != null) {
                this.lastGui.open((Player) e.getWhoClicked());
            }
        });

        if(decimals) {
            container.setItem(4,1,countItem);
        } else {
            container.setItem(4,0,countItem);
        }

        gui.update();
    }
}
