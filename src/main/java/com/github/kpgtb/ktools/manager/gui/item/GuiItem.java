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

package com.github.kpgtb.ktools.manager.gui.item;

import com.github.kpgtb.ktools.manager.gui.action.ClickAction;
import com.github.kpgtb.ktools.util.item.ItemBuilder;
import org.bukkit.inventory.ItemStack;

/**
 * GuiItem represents item in KGui
 */
public class GuiItem {
    private ItemStack itemStack;
    private ClickAction clickAction;

    /**
     * Constructor of GuiItem
     * @param itemStack ItemStack instance
     */
    public GuiItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Constructor of GuiItem
     * @param builder ItemBuilder instance
     * @since 1.5.0
     */
    public GuiItem(ItemBuilder builder) {
        itemStack = builder.build();
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * @since 1.5.0
     * @param builder ItemBuilder
     */
    public void setItemBuilder(ItemBuilder builder) {this.itemStack = builder.build();}

    /**
     * @since 1.5.0
     * @return ItemBuilder
     */
    public ItemBuilder getItemBuilder() {return new ItemBuilder(itemStack);}

    /**
     * Get action that will be invoking, when someone clicks this item in gui
     * @return ClickAction interface
     */
    public ClickAction getClickAction() {
        return clickAction;
    }

    /**
     * Set action that will be invoking, when someone clicks this item in gui
     * @param clickAction ClickAction interface
     */
    public void setClickAction(ClickAction clickAction) {
        this.clickAction = clickAction;
    }
}
