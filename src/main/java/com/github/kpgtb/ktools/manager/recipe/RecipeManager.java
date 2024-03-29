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

import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.util.file.ReflectionUtil;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.PluginManager;

import java.io.File;

/**
 * RecipeManager handles all recipes in plugin
 */
public class RecipeManager {
    private final ToolsObjectWrapper wrapper;
    private final File jarFile;
    private final String pluginTag;

    private final DebugManager debug;

    /**
     * Constructor of manager
     * @param wrapper ToolsObjectWrapper or object that extends it.
     * @param jarFile JAR file of plugin
     * @param pluginTag Tag of plugin
     */
    public RecipeManager(ToolsObjectWrapper wrapper, File jarFile, String pluginTag) {
        this.wrapper = wrapper;
        this.jarFile = jarFile;
        this.pluginTag = pluginTag.toLowerCase();

        this.debug = wrapper.getDebugManager();
    }

    /**
     * Register all recipes from package
     * @param recipesPackage Package with recipes
     */
    public void registerRecipes(String recipesPackage) {
        PluginManager pluginManager = Bukkit.getPluginManager();

        for(Class<?> clazz : ReflectionUtil.getAllClassesInPackage(jarFile,recipesPackage, KRecipe.class)) {
            try {

                debug.sendInfo(DebugType.RECIPE, "Registering recipe " + clazz.getSimpleName() + "...");

                String recipeName = clazz.getSimpleName()
                        .toLowerCase();
                NamespacedKey recipeKey = new NamespacedKey(pluginTag,recipeName);

                debug.sendInfo(DebugType.RECIPE, "Recipe namespace key: " + recipeKey.getNamespace() + ":" + recipeKey.getKey());

                KRecipe recipe = (KRecipe) clazz.getDeclaredConstructor(NamespacedKey.class, ToolsObjectWrapper.class)
                        .newInstance(recipeKey, wrapper);

                Recipe bukkitRecipe = recipe.getRecipe();
                if(bukkitRecipe == null) {
                    debug.sendWarning(DebugType.RECIPE, "Recipe is null! Cancelling!");
                    continue;
                }
                recipe.register(bukkitRecipe);
                pluginManager.registerEvents(recipe, wrapper.getPlugin());
                if(recipe.autoDiscover()) {
                    debug.sendInfo(DebugType.RECIPE, "Auto discover enabled");
                }

                debug.sendInfo(DebugType.RECIPE, "Registered recipe " + clazz.getSimpleName());

            } catch (Exception e) {
                debug.sendWarning(DebugType.RECIPE, "Error while loading recipe from class " + clazz.getName());
                e.printStackTrace();
            }
        }
    }
}
