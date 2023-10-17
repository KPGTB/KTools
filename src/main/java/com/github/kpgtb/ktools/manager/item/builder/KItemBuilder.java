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

import com.github.kpgtb.ktools.manager.item.KClickType;
import com.github.kpgtb.ktools.manager.item.KItem;
import com.github.kpgtb.ktools.manager.item.builder.action.ItemBuilderAction;
import com.github.kpgtb.ktools.manager.item.builder.action.ItemBuilderDualAction;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

/**
 * Builder of Kitem.
 */
@Getter
@Setter
public class KItemBuilder {
    private final ToolsObjectWrapper wrapper;
    private final String pluginTag;
    private final String itemName;
    private final ItemStack itemStack;

    private boolean dropBlock;

    private ItemBuilderAction<PlayerInteractEvent> onUseAction;
    private ItemBuilderDualAction<InventoryClickEvent, KClickType> onClickAction;
    private ItemBuilderAction<PlayerDropItemEvent> onDropAction;
    private ItemBuilderAction<PlayerDeathEvent> onDeathAction;
    private ItemBuilderAction<PlayerItemBreakEvent> onBreakAction;
    private ItemBuilderAction<PlayerItemConsumeEvent> onConsumeAction;
    private ItemBuilderDualAction<PlayerItemHeldEvent, Boolean> onHeldAction;
    private ItemBuilderAction<EntityPickupItemEvent> onPickupAction;
    private ItemBuilderAction<InventoryDragEvent> onDragAction;
    private ItemBuilderDualAction<EntityDamageByEntityEvent, Boolean> onDamageAction;
    private ItemBuilderAction<PlayerRespawnEvent> onRespawnAction;
    private ItemBuilderDualAction<PlayerSwapHandItemsEvent, Boolean> onSwapAction;
    private Predicate<ItemStack> isSimilarAction;

    public KItemBuilder(ToolsObjectWrapper wrapper, String pluginTag, String itemName, ItemStack itemStack) {
        this.wrapper = wrapper;
        this.pluginTag = pluginTag;
        this.itemName = itemName;
        this.itemStack = itemStack;
        this.dropBlock = false;
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
            public void onClick(InventoryClickEvent event, KClickType type) {
                if(onClickAction != null) {
                    onClickAction.onEvent(event,type);
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

            @Override
            public boolean isSimilar(ItemStack is) {
                if(isSimilarAction != null) {
                    return isSimilarAction.test(is);
                }
                return super.isSimilar(is);
            }
        };
        item.setDropBlock(dropBlock);
        wrapper.getItemManager().registerItem(wrapper,item);
        return item;
    }
}
