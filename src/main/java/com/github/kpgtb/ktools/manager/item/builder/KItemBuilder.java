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

package com.github.kpgtb.ktools.manager.item.builder;

import com.github.kpgtb.ktools.manager.item.KItem;
import com.github.kpgtb.ktools.manager.item.builder.action.ItemBuilderAction;
import com.github.kpgtb.ktools.manager.item.builder.action.ItemBuilderBoolAction;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

/**
 * Builder of Kitem.
 */
public class KItemBuilder {
    private final ToolsObjectWrapper wrapper;
    private final String pluginTag;
    private final String itemName;
    private final ItemStack itemStack;

    private boolean dropBlock;

    private ItemBuilderAction<PlayerInteractEvent> onUseAction;
    private ItemBuilderBoolAction<InventoryClickEvent> onClickAction;
    private ItemBuilderAction<PlayerDropItemEvent> onDropAction;
    private ItemBuilderAction<PlayerDeathEvent> onDeathAction;
    private ItemBuilderAction<PlayerItemBreakEvent> onBreakAction;
    private ItemBuilderAction<PlayerItemConsumeEvent> onConsumeAction;
    private ItemBuilderBoolAction<PlayerItemHeldEvent> onHeldAction;
    private ItemBuilderAction<EntityPickupItemEvent> onPickupAction;
    private ItemBuilderAction<InventoryDragEvent> onDragAction;
    private ItemBuilderBoolAction<EntityDamageByEntityEvent> onDamageAction;
    private ItemBuilderAction<PlayerRespawnEvent> onRespawnAction;
    private ItemBuilderBoolAction<PlayerSwapHandItemsEvent> onSwapAction;

    public KItemBuilder(ToolsObjectWrapper wrapper, String pluginTag, String itemName, ItemStack itemStack) {
        this.wrapper = wrapper;
        this.pluginTag = pluginTag;
        this.itemName = itemName;
        this.itemStack = itemStack;
        this.dropBlock = false;
    }

    public boolean isDropBlocked() {
        return dropBlock;
    }

    public void setDropBlock(boolean dropBlock) {
        this.dropBlock = dropBlock;
    }

    public String getPluginTag() {
        return pluginTag;
    }

    public String getItemName() {
        return itemName;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemBuilderBoolAction<PlayerItemHeldEvent> getOnHeldAction() {
        return onHeldAction;
    }

    public void setOnHeldAction(ItemBuilderBoolAction<PlayerItemHeldEvent> onHeldAction) {
        this.onHeldAction = onHeldAction;
    }

    public ItemBuilderAction<PlayerInteractEvent> getOnUseAction() {
        return onUseAction;
    }

    public void setOnUseAction(ItemBuilderAction<PlayerInteractEvent> onUseAction) {
        this.onUseAction = onUseAction;
    }

    public ItemBuilderBoolAction<InventoryClickEvent> getOnClickAction() {
        return onClickAction;
    }

    public void setOnClickAction(ItemBuilderBoolAction<InventoryClickEvent> onClickAction) {
        this.onClickAction = onClickAction;
    }

    public ItemBuilderAction<PlayerDropItemEvent> getOnDropAction() {
        return onDropAction;
    }

    public void setOnDropAction(ItemBuilderAction<PlayerDropItemEvent> onDropAction) {
        this.onDropAction = onDropAction;
    }

    public ItemBuilderAction<PlayerDeathEvent> getOnDeathAction() {
        return onDeathAction;
    }

    public void setOnDeathAction(ItemBuilderAction<PlayerDeathEvent> onDeathAction) {
        this.onDeathAction = onDeathAction;
    }

    public ItemBuilderAction<PlayerItemBreakEvent> getOnBreakAction() {
        return onBreakAction;
    }

    public void setOnBreakAction(ItemBuilderAction<PlayerItemBreakEvent> onBreakAction) {
        this.onBreakAction = onBreakAction;
    }

    public ItemBuilderAction<PlayerItemConsumeEvent> getOnConsumeAction() {
        return onConsumeAction;
    }

    public void setOnConsumeAction(ItemBuilderAction<PlayerItemConsumeEvent> onConsumeAction) {
        this.onConsumeAction = onConsumeAction;
    }

    public ItemBuilderAction<EntityPickupItemEvent> getOnPickupAction() {
        return onPickupAction;
    }

    public void setOnPickupAction(ItemBuilderAction<EntityPickupItemEvent> onPickupAction) {
        this.onPickupAction = onPickupAction;
    }

    public ItemBuilderAction<InventoryDragEvent> getOnDragAction() {
        return onDragAction;
    }

    public void setOnDragAction(ItemBuilderAction<InventoryDragEvent> onDragAction) {
        this.onDragAction = onDragAction;
    }

    public ItemBuilderBoolAction<EntityDamageByEntityEvent> getOnDamageAction() {
        return onDamageAction;
    }

    public void setOnDamageAction(ItemBuilderBoolAction<EntityDamageByEntityEvent> onDamageAction) {
        this.onDamageAction = onDamageAction;
    }

    public ItemBuilderAction<PlayerRespawnEvent> getOnRespawnAction() {
        return onRespawnAction;
    }

    public void setOnRespawnAction(ItemBuilderAction<PlayerRespawnEvent> onRespawnAction) {
        this.onRespawnAction = onRespawnAction;
    }

    public ItemBuilderBoolAction<PlayerSwapHandItemsEvent> getOnSwapAction() {
        return onSwapAction;
    }

    public void setOnSwapAction(ItemBuilderBoolAction<PlayerSwapHandItemsEvent> onSwapAction) {
        this.onSwapAction = onSwapAction;
    }

    /**
     * Register custom item
     * @return Kitem instance
     */
    public KItem register() {
        KItem item = new KItem(wrapper, pluginTag + ":" + itemName) {
            @Override
            public ItemStack getItem() {
                return itemStack;
            }

            @Override
            public void onUse(PlayerInteractEvent event) {
                if(onUseAction != null) {
                    onUseAction.onEvent(event);
                }
            }

            @Override
            public void onClick(InventoryClickEvent event, boolean cursor) {
                if(onClickAction != null) {
                    onClickAction.onEvent(event,cursor);
                }
            }

            @Override
            public void onDrop(PlayerDropItemEvent event) {
                if(onDropAction != null) {
                    onDropAction.onEvent(event);
                }
            }

            @Override
            public void onDeath(PlayerDeathEvent event) {
                if(onDeathAction != null) {
                    onDeathAction.onEvent(event);
                }
            }

            @Override
            public void onBreak(PlayerItemBreakEvent event) {
                if(onBreakAction != null) {
                    onBreakAction.onEvent(event);
                }
            }

            @Override
            public void onConsume(PlayerItemConsumeEvent event) {
                if(onConsumeAction != null) {
                    onConsumeAction.onEvent(event);
                }
            }

            @Override
            public void onHeld(PlayerItemHeldEvent event, boolean old) {
                if(onHeldAction != null) {
                    onHeldAction.onEvent(event, old);
                }
            }

            @Override
            public void onPickup(EntityPickupItemEvent event) {
                if(onPickupAction != null) {
                    onPickupAction.onEvent(event);
                }
            }

            @Override
            public void onDrag(InventoryDragEvent event) {
                if(onDragAction != null) {
                    onDragAction.onEvent(event);
                }
            }

            @Override
            public void onDamage(EntityDamageByEntityEvent event, boolean off) {
                if(onDamageAction != null) {
                    onDamageAction.onEvent(event, off);
                }
            }

            @Override
            public void onRespawn(PlayerRespawnEvent event) {
                if(onRespawnAction != null) {
                    onRespawnAction.onEvent(event);
                }
            }

            @Override
            public void onSwap(PlayerSwapHandItemsEvent event, boolean toOff) {
                if(onSwapAction != null) {
                    onSwapAction.onEvent(event, toOff);
                }
            }
        };
        item.setDropBlock(dropBlock);
        wrapper.getItemManager().registerItem(wrapper,item);
        return item;
    }
}
