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
import com.github.kpgtb.ktools.manager.gui.item.CommonGuiItem;
import com.github.kpgtb.ktools.manager.gui.item.GuiItem;
import com.github.kpgtb.ktools.manager.gui.item.GuiItemLocation;
import com.github.kpgtb.ktools.manager.gui.write.ICountResponse;
import com.github.kpgtb.ktools.manager.language.LanguageLevel;
import com.github.kpgtb.ktools.util.item.ItemBuilder;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

/**
 * Count GUI is a gui with response where you can count sth
 */
public class KCountGui extends KGui{
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
        super(
                wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "countGuiName"),
                3,
                wrapper
        );

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


    @Override
    public void prepareGui() {
        resetContainers();
        blockClick();
        setCloseAction(e -> {
            if(!this.responsed) {
                this.response.response(defaultValue);
                if(this.lastGui != null) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            lastGui.open((Player) e.getPlayer());
                        }
                    }.runTaskLater(wrapper.getPlugin(), 3);
                }
            }
        });

        GuiContainer container = new GuiContainer(this, 0,0,9,3);

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
        countItem.setClickAction((e,place) -> {
            this.responsed = true;
            this.response.response(this.value);
            if(this.lastGui != null) {
                this.lastGui.open((Player) e.getWhoClicked());
            }
        });
        container.setItem(4,1,countItem);

        HashMap<GuiItemLocation, Double> changeItems = new HashMap<>();

        changeItems.put(decimals ? new GuiItemLocation(1,0) : new GuiItemLocation(2,1), -1.0);
        changeItems.put(decimals ? new GuiItemLocation(7,0) : new GuiItemLocation(6,1), 1.0);
        changeItems.put(new GuiItemLocation(1, 1), -10.0);
        changeItems.put(new GuiItemLocation(7, 1), 10.0);
        changeItems.put(decimals ? new GuiItemLocation(1,2) : new GuiItemLocation(0,1), -100.0);
        changeItems.put(decimals ? new GuiItemLocation(7,2) : new GuiItemLocation(8,1), 100.0);

        if(decimals) {
            changeItems.put(new GuiItemLocation(2,0), -0.01);
            changeItems.put(new GuiItemLocation(6,0), 0.01);
            changeItems.put(new GuiItemLocation(2,1), -0.1);
            changeItems.put(new GuiItemLocation(6,1), 0.1);
            changeItems.put(new GuiItemLocation(2,2), -0.5);
            changeItems.put(new GuiItemLocation(6,2), 0.5);
        }

        changeItems.forEach((location, value) -> {
            Material material = value >= 0 ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
            String langCode = value >= 0 ? "countPlus" : "countMinus";
            TagResolver valuePlaceholder = Placeholder.unparsed("value", String.valueOf(Math.abs(value)));

            GuiItem item = new GuiItem(
                new ItemBuilder(material)
                    .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, langCode, valuePlaceholder))
            );
            item.setClickAction((e,place) -> changeValue(value));
            container.setItem(location.getX(),location.getY(),item);
        });

        container.setItem(4,0, CommonGuiItem.getCloseItem(wrapper));
        container.setItem(4,2, CommonGuiItem.getCloseItem(wrapper));

        addContainer(container);
    }

    private void changeValue(double addValue) {
        double newValue = Math.round((this.value + addValue) * 100.0) / 100.0;

        if(newValue < min) {
            return;
        }
        if(newValue > max) {
            return;
        }

        this.value = newValue;
        prepareGui();
    }
}
