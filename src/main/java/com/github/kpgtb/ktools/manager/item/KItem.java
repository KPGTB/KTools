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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Abstract class that handles process of preparing custom item
 * @since 1.3.0
 */
public abstract class KItem implements Listener {
    private final ToolsObjectWrapper wrapper;
    private final String fullItemTag;
    private final List<Player> deathPlayers;

    /**
     * Constructor of Kitem
     * @param wrapper Instance of ToolsObjectWrapper
     * @param fullItemTag Full item name (plugin:item)
     */
    public KItem(ToolsObjectWrapper wrapper, String fullItemTag) {
        this.wrapper = wrapper;
        this.fullItemTag = fullItemTag;
        this.deathPlayers = new ArrayList<>();
    }

    public final void generateItemInFile() {
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
    public final String getFullItemTag() {
        return fullItemTag;
    }

    /**
     * Get ItemStack of custom item
     * @return ItemStack of custom item
     */
    public abstract ItemStack getItem();

    /**
     * Check if ItemStack is similar to item from this object
     * @param is ItemStack to compare
     * @return true if items are similar
     */
    public boolean isSimilar(ItemStack is) {
        if(is == null || is.getType().equals(Material.AIR)) {
            return false;
        }
        return this.getItem().isSimilar(is);
    }

    public void onUse(PlayerInteractEvent event) {}
    public void onClick(InventoryClickEvent event, boolean cursor) {}
    public void onDrop(PlayerDropItemEvent event) {}
    public void onDeath(PlayerDeathEvent event) {}
    public void onBreak(PlayerItemBreakEvent event) {}
    public void onConsume(PlayerItemConsumeEvent event) {}
    public void onHeld(PlayerItemHeldEvent event, boolean old) {}
    public void onPickup(EntityPickupItemEvent event) {}
    public void onDrag(InventoryDragEvent event) {}
    public void onDamage(EntityDamageByEntityEvent event, boolean off) {}
    public void onRespawn(PlayerRespawnEvent event) {}
    public void onSwap(PlayerSwapHandItemsEvent event, boolean toOff) {}

    @EventHandler
    public final void onUseListener(PlayerInteractEvent event) {
        ItemStack is = event.getItem();

        if(is == null || is.getType().equals(Material.AIR)) {
            return;
        }

        if(this.isSimilar(is)) {
            this.onUse(event);
        }
    }

    @EventHandler
    public final void onClickListener(InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();

        if(clicked != null && !clicked.getType().equals(Material.AIR)) {
            if(this.isSimilar(clicked)) {
                this.onClick(event,false);
            }
        }

        ItemStack cursor = event.getCursor();
        if(cursor != null && !cursor.getType().equals(Material.AIR)) {
            if(this.isSimilar(cursor)) {
                this.onClick(event,true);
            }
        }
    }

    @EventHandler
    public final void onDropListener(PlayerDropItemEvent event) {
        ItemStack is = event.getItemDrop().getItemStack();;

        if(is == null || is.getType().equals(Material.AIR)) {
            return;
        }

        if(this.isSimilar(is)) {
            this.onDrop(event);
        }
    }

    @EventHandler
    public final void onDeathListener(PlayerDeathEvent event) {
        List<ItemStack> drops = event.getDrops();

        for (ItemStack is : drops) {
            if(is == null || is.getType().equals(Material.AIR)) {
                continue;
            }

            if(this.isSimilar(is)) {
                this.deathPlayers.add(event.getEntity());
                this.onDeath(event);
                break;
            }
        }
    }

    @EventHandler
    public final void onBreakListener(PlayerItemBreakEvent event) {
        ItemStack is = event.getBrokenItem();

        if(is == null || is.getType().equals(Material.AIR)) {
            return;
        }

        if(this.isSimilar(is)) {
            this.onBreak(event);
        }
    }

    @EventHandler
    public final void onConsumeListener(PlayerItemConsumeEvent event) {
        ItemStack is = event.getItem();

        if(is == null || is.getType().equals(Material.AIR)) {
            return;
        }

        if(this.isSimilar(is)) {
            this.onConsume(event);
        }
    }

    @EventHandler
    public final void onHeldListener(PlayerItemHeldEvent event) {
        PlayerInventory inv = event.getPlayer().getInventory();
        ItemStack newItem = inv.getItem(event.getNewSlot());

        if(newItem != null && !newItem.getType().equals(Material.AIR)) {
            if(this.isSimilar(newItem)) {
                this.onHeld(event,false);
            }
        }

        ItemStack oldItem = inv.getItem(event.getPreviousSlot());

        if(oldItem != null && !oldItem.getType().equals(Material.AIR)) {
            if(this.isSimilar(oldItem)) {
                this.onHeld(event,true);
            }
        }
    }

    @EventHandler
    public final void onPickupListener(EntityPickupItemEvent event) {
        ItemStack is = event.getItem().getItemStack();

        if(is == null || is.getType().equals(Material.AIR)) {
            return;
        }

        if(this.isSimilar(is)) {
            this.onPickup(event);
        }
    }

    @EventHandler
    public final void onDragListener(InventoryDragEvent event) {
        Collection<ItemStack> items = event.getNewItems().values();

        for (ItemStack is : items) {
            if(is == null || is.getType().equals(Material.AIR)) {
                continue;
            }

            if(this.isSimilar(is)) {
                this.onDrag(event);
                break;
            }
        }
    }

    @EventHandler
    public final void onDamageListener(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();
        PlayerInventory inv = player.getInventory();

        ItemStack main = inv.getItemInMainHand();
        ItemStack off = inv.getItemInOffHand();

        if(main != null && !main.getType().equals(Material.AIR)) {
            if(this.isSimilar(main)) {
                this.onDamage(event, false);
            }
        }

        if(off != null && !off.getType().equals(Material.AIR)) {
            if(this.isSimilar(off)) {
                this.onDamage(event, true);
            }
        }
    }

    @EventHandler
    public final void onRespawnListener(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if(this.deathPlayers.contains(player)) {
            this.deathPlayers.remove(player);
            this.onRespawn(event);
        }
    }

    @EventHandler
    public final void onSwapListener(PlayerSwapHandItemsEvent event) {
        ItemStack mainItem = event.getMainHandItem();

        if(mainItem != null && !mainItem.getType().equals(Material.AIR)) {
            if(this.isSimilar(mainItem)) {
                this.onSwap(event,false);
            }
        }

        ItemStack offItem = event.getOffHandItem();

        if(offItem != null && !offItem.getType().equals(Material.AIR)) {
            if(this.isSimilar(offItem)) {
                this.onSwap(event,true);
            }
        }
    }
}
