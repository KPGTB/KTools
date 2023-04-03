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

package com.github.kpgtb.ktools.manager.item;

import com.github.kpgtb.ktools.util.item.ItemBuilder;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class that handles process of preparing custom item
 * @since 1.3.0
 */
public abstract class Kitem implements Listener {
    private final ToolsObjectWrapper wrapper;
    private final String fullItemTag;

    /**
     * Constructor of Kitem
     * @param wrapper Instance of ToolsObjectWrapper
     * @param fullItemTag Full item name (plugin:item)
     */
    public Kitem(ToolsObjectWrapper wrapper, String fullItemTag) {
        this.wrapper = wrapper;
        this.fullItemTag = fullItemTag;

        File itemsFile = wrapper.getItemManager().getItemsFile();
        YamlConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        String[] itemTag = fullItemTag.split(":");
        String plugin = itemTag[0];
        String itemName = itemTag[1];

        itemsConfig.set(plugin+"."+itemName+".tag", fullItemTag);

        ItemBuilder item = new ItemBuilder(getItem());

        itemsConfig.set(plugin+"."+itemName+".displayName", item.getDisplayName());
        itemsConfig.set(plugin+"."+itemName+".lore", item.getLore());

        itemsConfig.set(plugin+"."+itemName+".material", item.getMaterial().name());
        itemsConfig.set(plugin+"."+itemName+".model", item.getModel());
        itemsConfig.set(plugin+"."+itemName+".unbreakable", item.isUnbreakable());

        ArrayList<String> enchantments = new ArrayList<>();
        item.getEnchantments().forEach((ench, power) -> {
            enchantments.add(ench.getName() + " " + power);
        });
        itemsConfig.set(plugin+"."+itemName+".enchants", enchantments);

        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            return;
        }
    }

    /**
     * Get full item tag
     * @return full item name (plugin:item)
     */
    public String getFullItemTag() {
        return fullItemTag;
    }

    /**
     * Get ItemStack of custom item
     * @return ItemStack of custom item
     */
    public abstract ItemStack getItem();

    public void onUse(PlayerInteractEvent event) {}
    public void onClick(InventoryClickEvent event, boolean cursor) {}
    public void onDrop(PlayerDropItemEvent event) {}
    public void onDeath(PlayerDeathEvent event) {}
    public void onBreak(PlayerItemBreakEvent event) {}
    public void onConsume(PlayerItemConsumeEvent event) {}
    public void onHeld(PlayerItemHeldEvent event, boolean old) {}
    public void onPickup(EntityPickupItemEvent event) {}

    @EventHandler
    private void onUseListener(PlayerInteractEvent event) {
        ItemStack is = event.getItem();

        if(is == null || is.getType().equals(Material.AIR)) {
            return;
        }

        if(is.isSimilar(this.getItem())) {
            this.onUse(event);
        }
    }

    @EventHandler
    private void onClickListener(InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();

        if(clicked != null && !clicked.getType().equals(Material.AIR)) {
            if(clicked.isSimilar(this.getItem())) {
                this.onClick(event,false);
            }
        }

        ItemStack cursor = event.getCursor();
        if(cursor != null && !cursor.getType().equals(Material.AIR)) {
            if(cursor.isSimilar(this.getItem())) {
                this.onClick(event,true);
            }
        }
    }

    @EventHandler
    private void onDropListener(PlayerDropItemEvent event) {
        ItemStack is = event.getItemDrop().getItemStack();;

        if(is == null || is.getType().equals(Material.AIR)) {
            return;
        }

        if(is.isSimilar(this.getItem())) {
            this.onDrop(event);
        }
    }

    @EventHandler
    private void onDeathListener(PlayerDeathEvent event) {
        List<ItemStack> drops = event.getDrops();

        for (ItemStack is : drops) {
            if(is == null || is.getType().equals(Material.AIR)) {
                continue;
            }

            if(is.isSimilar(this.getItem())) {
                this.onDeath(event);
                break;
            }
        }
    }

    @EventHandler
    private void onBreakListener(PlayerItemBreakEvent event) {
        ItemStack is = event.getBrokenItem();

        if(is == null || is.getType().equals(Material.AIR)) {
            return;
        }

        if(is.isSimilar(this.getItem())) {
            this.onBreak(event);
        }
    }

    @EventHandler
    private void onConsumeListener(PlayerItemConsumeEvent event) {
        ItemStack is = event.getItem();

        if(is == null || is.getType().equals(Material.AIR)) {
            return;
        }

        if(is.isSimilar(this.getItem())) {
            this.onConsume(event);
        }
    }

    @EventHandler
    private void onHeldListener(PlayerItemHeldEvent event) {
        PlayerInventory inv = event.getPlayer().getInventory();
        ItemStack newItem = inv.getItem(event.getNewSlot());

        if(newItem != null && !newItem.getType().equals(Material.AIR)) {
            if(newItem.isSimilar(this.getItem())) {
                this.onHeld(event,false);
            }
        }

        ItemStack oldItem = inv.getItem(event.getPreviousSlot());

        if(oldItem != null && !oldItem.getType().equals(Material.AIR)) {
            if(oldItem.isSimilar(this.getItem())) {
                this.onHeld(event,true);
            }
        }
    }

    @EventHandler
    private void onPickupListener(EntityPickupItemEvent event) {
        ItemStack is = event.getItem().getItemStack();

        if(is == null || is.getType().equals(Material.AIR)) {
            return;
        }

        if(is.isSimilar(this.getItem())) {
            this.onPickup(event);
        }
    }
}
