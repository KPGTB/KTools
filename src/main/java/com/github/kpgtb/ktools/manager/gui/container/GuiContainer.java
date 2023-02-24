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

package com.github.kpgtb.ktools.manager.gui.container;

import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.manager.gui.KGui;
import com.github.kpgtb.ktools.manager.gui.item.GuiItem;
import com.github.kpgtb.ktools.manager.gui.item.GuiItemLocation;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Gui container contains specific box of items from GUI
 */
public class GuiContainer {
    private final DebugManager debug;

    private final @Nullable KGui gui;
    private final @Nullable PagedGuiContainer pagedGuiContainer;

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private HashMap<GuiItemLocation, GuiItem> items;

    /**
     * Constructor for container that is part of GUI
     * @param debug Instance of DebugManager
     * @param gui Instance of KGui
     * @param x X position in KGui (0-8)
     * @param y Y position in KGui (0-<KGui rows - 1>)
     * @param width Width of container (1-9)
     * @param height Height of container (1-<KGui rows>)
     */
    public GuiContainer(DebugManager debug, @NotNull KGui gui, int x, int y, int width, int height) {
        this.debug = debug;
        this.gui = gui;
        this.pagedGuiContainer = null;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.items = new HashMap<>();

        if((x-1) + width >= 9) {
            this.debug.sendWarning(DebugType.GUI, "Container is too wide!");
            return;
        }
        if(x < 0 || y < 0 || width < 1 || height < 0) {
            this.debug.sendWarning(DebugType.GUI, "Container is too small!");
            return;
        }
        if((y-1) + height >= gui.getRows()) {
            this.debug.sendWarning(DebugType.GUI, "Container is too high!");
            return;
        }

        this.fill(
                new GuiItem(
                        new ItemStack(Material.AIR)
                )
        );
    }

    /**
     * Constructor for container that is part of PagedGuiContainer
     * @param debug Instance of DebugManager
     * @param pagedGuiContainer Instance of PagedGuiContainer
     */
    public GuiContainer(DebugManager debug, @NotNull  PagedGuiContainer pagedGuiContainer) {
        this.debug = debug;
        this.gui = null;
        this.pagedGuiContainer = pagedGuiContainer;
        this.x = pagedGuiContainer.getX();
        this.y = pagedGuiContainer.getY();
        this.width = pagedGuiContainer.getWidth();
        this.height = pagedGuiContainer.getHeight();
        this.items = new HashMap<>();

        this.fill(
            new GuiItem(
                    new ItemStack(Material.AIR)
            )
        );
    }

    public @Nullable KGui getGui() {
        return gui;
    }
    public @Nullable PagedGuiContainer getPagedGuiContainer() {
        return pagedGuiContainer;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

    public HashMap<GuiItemLocation, GuiItem> getItems() {
        return items;
    }
    public void setItems(HashMap<GuiItemLocation, GuiItem> items) {
        this.items = items;
    }

    /**
     * Set item in container
     * @param x X position in container (0-<width-1>)
     * @param y Y position in container (0-<height-1>)
     * @param item Instance of GuiItem
     */
    public void setItem(int x, int y, GuiItem item) {
        if(x < 0 || x >= width) {
            this.debug.sendWarning(DebugType.GUI, "X of item is in illegal position!");
            return;
        }
        if(y < 0 || y >= height) {
            this.debug.sendWarning(DebugType.GUI, "Y of item is in illegal position!");
            return;
        }
        this.items.put(new GuiItemLocation(x,y), item);
    }

    /**
     * Remove item from container
     * @param x X position in container (0-<width-1>)
     * @param y Y position in container (0-<height-1>)
     */
    public void removeItem(int x, int y) {
        this.items.remove(new GuiItemLocation(x,y));
    }

    /**
     * Get item from container
     * @param loc Instance of GuiItemLocation
     * @return Item from container or null
     */
    @Nullable
    public GuiItem getItem(GuiItemLocation loc) {
        return this.items.get(loc);
    }

    /**
     * Get item from container
     * @param x X position in container (0-<width-1>)
     * @param y Y position in container (0-<height-1>)
     * @return Item from container or null
     */
    @Nullable
    public GuiItem getItem(int x, int y) {
        return this.getItem(new GuiItemLocation(x,y));
    }

    /**
     * Fill all slots in container with items
     * @param item Instance of GuiItem
     */
    public void fill(GuiItem item) {
        for(int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                this.setItem(x,y,item);
            }
        }
    }

    /**
     * Fill all empty slots in container with items
     * @param item Instance of GuiItem
     */
    public void fillEmptySlots(GuiItem item) {
        for(int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(getItem(x,y) != null && !getItem(x,y).getItemStack().getType().equals(Material.AIR))  {
                    continue;
                }
                this.setItem(x,y,item);
            }
        }
    }

    /**
     * Calculate slot in gui from position in container
     * @param x X position in container (0-<width-1>)
     * @param y Y position in container (0-<height-1>)
     * @return Slot in GUI
     */
    public int getGuiLocFromContainerLoc(int x, int y) {
        int slot = 0;
        slot += ((this.y + y) * 9);
        slot += (this.x + x);
        return slot;
    }

    /**
     * Calculate slot in gui from position in container
     * @param loc Instance of GuiItemLocation
     * @return Slot in GUI
     */
    public int getGuiLocFromContainerLoc(GuiItemLocation loc) {
        return this.getGuiLocFromContainerLoc(loc.getX(), loc.getY());
    }

    /**
     * Calculate location in container from position in gui
     * @param slot Slot in GUI
     * @return Instance of GuiItemLocation
     */
    public GuiItemLocation getContainerLocFromGuiLoc(int slot) {
        int globalY = Math.floorDiv(slot, 9);
        int globalX = Math.floorMod(slot, 9);

        int containerX = globalX - this.x;
        int containerY = globalY - this.y;

        return new GuiItemLocation(containerX,containerY);
    }
}
