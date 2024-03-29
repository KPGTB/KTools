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

import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.manager.gui.action.ClickAction;
import com.github.kpgtb.ktools.manager.gui.action.ClickLocation;
import com.github.kpgtb.ktools.manager.gui.action.CloseAction;
import com.github.kpgtb.ktools.manager.gui.action.DragAction;
import com.github.kpgtb.ktools.manager.gui.container.GuiContainer;
import com.github.kpgtb.ktools.manager.gui.item.GuiItem;
import com.github.kpgtb.ktools.manager.gui.item.GuiItemLocation;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * KGui handles gui making process in plugin
 */
public abstract class KGui implements Listener {
    private final String name;
    private final int rows;
    private final ArrayList<GuiContainer> containers;

    private ClickAction globalClickAction;
    private DragAction globalDragAction;
    private CloseAction closeAction;

    private final Inventory bukkitInventory;
    private boolean updateItems;

    private final DebugManager debug;
    private final JavaPlugin plugin;

    /**
     * Constructor of KGui
     * @param name Name of GUI
     * @param rows Number of rows in GUI (1-6)
     * @param tools Instance of ToolsObjectWrapper
     */
    public KGui(String name, int rows, ToolsObjectWrapper tools) {
        this.name = name;
        this.rows = rows;
        this.containers = new ArrayList<>();
        this.debug = tools.getDebugManager();
        this.updateItems = false;
        this.plugin = tools.getPlugin();

        if(rows > 6 || rows < 1) {
            this.debug.sendWarning(DebugType.GUI, "Gui rows must be a value between 1 and 6");
            this.bukkitInventory = null;
            return;
        }

        this.bukkitInventory = Bukkit.createInventory(null, (rows * 9), name);
        Bukkit.getPluginManager().registerEvents(this,tools.getPlugin());
    }

    /**
     * Block global click and drag
     * @since 1.3.0
     */
    public void blockClick() {
        this.setGlobalClickAction((e,loc) -> e.setCancelled(true));
        this.setGlobalDragAction(e -> e.setCancelled(true));
    }

    public String getName() {
        return name;
    }

    public int getRows() {
        return rows;
    }

    public DebugManager getDebug() {
        return debug;
    }

    public ArrayList<GuiContainer> getContainers() {
        return containers;
    }

    /**
     * Add container to GUI and auto update this GUI
     * @param container Container that is a part of this GUI
     */
    public void addContainer(GuiContainer container) {
        if(container.getGui() != this) {
            this.debug.sendWarning(DebugType.GUI, "Container isn't a part of this GUI!");
            return;
        }
        this.containers.add(container);
        this.update();
    }

    /**
     * Remove container from GUI
     * @param container Container that is a part of this GUI
     */
    public void removeContainer(GuiContainer container) {
        this.containers.remove(container);
        this.update();
    }

    /**
     * Reset containers
     */
    public void resetContainers() {
        this.containers.clear();
        this.update();
    }

    /**
     * Prepare items in gui
     */
    public abstract void prepareGui();

    /**
     * Open GUI to player
     * @param player Player that should have open GUI
     */
    public void open(Player player) {
        prepareGui();
        player.openInventory(bukkitInventory);
    }

    /**
     * Get container that is in gui
     * @param slot Slot where is container
     * @return Container from gui or null
     */
    public @Nullable  GuiContainer getContainerAt(int slot) {
        GuiContainer container = null;

        int y = Math.floorDiv(slot, 9);
        int x = Math.floorMod(slot, 9);

        for(GuiContainer c : containers) {
            if(x >= c.getX() && x <= ((c.getX() - 1) + c.getWidth())) {
                if(y >= c.getY() && y <= ((c.getY() - 1) + c.getHeight())) {
                    container = c;
                    break;
                }
            }
        }

        return container;
    }

    /**
     * Update items in GUI
     */
    public void update() {
        this.containers.forEach(container -> {
            container.getItems().forEach((location, item) -> {
                if(item == null) {
                    return;
                }
                this.bukkitInventory.setItem(container.getGuiLocFromContainerLoc(location), item.getItemStack());
            });
        });
    }

    public Inventory getBukkitInventory() {
        return bukkitInventory;
    }

    /**
     * Experimental feature
     * Check if items are updated when changed
     * @return true if items in container are updated after change
     * @since 2.0.0
     */
    public boolean isUpdateItems() {
        return updateItems;
    }

    /**
     * Experimental feature
     * Update items in containers after player will change it
     * @param updateItems If items should be updated
     * @since 2.0.0
     */
    public void setUpdateItems(boolean updateItems) {
        this.updateItems = updateItems;
    }

    /**
     * Get action that will be invoking, when someone clicks in this gui
     * @return ClickAction interface
     */
    public ClickAction getGlobalClickAction() {
        return globalClickAction;
    }

    /**
     * Set action that will be invoking, when someone clicks in this gui
     * @param globalClickAction ClickAction interface
     */
    public void setGlobalClickAction(ClickAction globalClickAction) {
        this.globalClickAction = globalClickAction;
    }

    /**
     * Get action that will be invoking, when someone drags items in this gui
     * @return DragAction interface
     */
    public DragAction getGlobalDragAction() {
        return globalDragAction;
    }

    /**
     * Set action that will be invoking, when someone drags items in this gui
     * @param globalDragAction DragAction interface
     */
    public void setGlobalDragAction(DragAction globalDragAction) {
        this.globalDragAction = globalDragAction;
    }

    /**
     * Get action that will be invoking, when someone close this gui
     * @return CloseAction interface
     */
    public CloseAction getCloseAction() {
        return closeAction;
    }

    /**
     * Set action that will be invoking, when someone close this gui
     * @param closeAction CloseAction interface
     */
    public void setCloseAction(CloseAction closeAction) {
        this.closeAction = closeAction;
    }

    @EventHandler
    public void onGlobalClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        Inventory clickedInv = event.getClickedInventory();
        if(!inv.equals(this.bukkitInventory)) {
            return;
        }

        if(this.getGlobalClickAction() != null) {
            ClickLocation clickLocation = clickedInv == null ? ClickLocation.OUTSIDE : clickedInv.equals(this.bukkitInventory) ? ClickLocation.TOP : ClickLocation.BOTTOM;
            this.getGlobalClickAction().run(event,clickLocation);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();

        if(inv != this.bukkitInventory) {
            return;
        }

        int slot = event.getSlot();
        GuiContainer container = this.getContainerAt(slot);

        if(container == null) {
            return;
        }

        GuiItem item = container.getItem(container.getContainerLocFromGuiLoc(slot));

        if(item == null) {
            return;
        }

        if(item.getClickAction() != null) {
            item.getClickAction().run(event, ClickLocation.TOP);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onUpdate(InventoryClickEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(!this.updateItems) {
            return;
        }

        Inventory inv = event.getInventory();
        if(!inv.equals(this.bukkitInventory)) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < inv.getContents().length; i++) {
                    ItemStack realIS = inv.getItem(i);
                    GuiContainer container = getContainerAt(i);
                    if(container == null) {
                        continue;
                    }
                    GuiItemLocation loc = container.getContainerLocFromGuiLoc(i);
                    GuiItem guiItem = container.getItem(loc);

                    if(guiItem == null) {
                        if(realIS != null && !realIS.getType().equals(Material.AIR)) {
                            container.setItem(loc.getX(), loc.getY(), new GuiItem(realIS));
                        }
                        continue;
                    }

                    if(realIS == null || realIS.getType().equals(Material.AIR)) {
                        container.removeItem(loc.getX(),loc.getY());
                        continue;
                    }

                    if(guiItem.getItemStack().isSimilar(realIS)) {
                        continue;
                    }

                    guiItem.setItemStack(realIS);
                }
            }
        }.runTaskLater(plugin,3);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();

        if(inv != this.bukkitInventory) {
            return;
        }

        if(this.getGlobalDragAction() != null) {
            this.getGlobalDragAction().run(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();

        if(inv != this.bukkitInventory) {
            return;
        }

        if(this.getCloseAction() != null) {
            this.getCloseAction().run(event);
        }
    }
}
