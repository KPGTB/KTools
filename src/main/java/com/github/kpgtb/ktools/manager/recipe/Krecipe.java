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

package com.github.kpgtb.ktools.manager.recipe;

import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * Abstract class that handles process of preparing recipe
 */
public abstract class Krecipe implements Listener {
    private final NamespacedKey recipeKey;
    private Recipe recipe;
    private boolean isRegistered;

    /**
     * Constructor of recipe.
     * @param recipeKey Key of recipe
     * @param toolsObjectWrapper ToolsObjectWrapper or object that extends it.
     */
    public Krecipe(NamespacedKey recipeKey, ToolsObjectWrapper toolsObjectWrapper) {
        this.recipeKey = recipeKey;

        this.recipe = null;
        this.isRegistered = false;
    }

    /**
     * Creates recipe
     * @return Created recipe
     */
    public abstract Recipe getRecipe();

    /**
     * Auto discover recipe when player joins
     * @return true if recipe should be discovered when player joins, or false if not
     */
    public abstract boolean autoDiscover();

    /**
     * Register recipe in bukkit
     * @param recipe Recipe instance
     */
    public void register(@NotNull Recipe recipe) {
        if(this.recipe != null || this.isRegistered) {
            return;
        }

        Iterator<Recipe> it = Bukkit.recipeIterator();
        while(it.hasNext()) {
            Recipe r = it.next();
            if(r.equals(recipe)) {
                it.remove();
            }
            if(r instanceof ShapedRecipe || r instanceof ShapelessRecipe ||
            r instanceof CookingRecipe || r instanceof StonecuttingRecipe) {
                try {
                    NamespacedKey rKey = (NamespacedKey) r.getClass().getField("getKey").get(r);
                    if(rKey.equals(this.recipeKey)) {
                        it.remove();
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    continue;
                }
            }
        }

        this.recipe = recipe;
        Bukkit.addRecipe(recipe);
        this.isRegistered = true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!isRegistered || !autoDiscover()) {
            return;
        }
        player.undiscoverRecipe(recipeKey);
        player.discoverRecipe(recipeKey);
    }

}
