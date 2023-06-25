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

package com.github.kpgtb.ktools.manager.ui.bar;

import com.github.kpgtb.ktools.manager.resourcepack.ResourcePackManager;
import com.github.kpgtb.ktools.manager.ui.Alignment;
import com.github.kpgtb.ktools.manager.ui.BaseUiObject;
import com.github.kpgtb.ktools.manager.ui.UiManager;
import com.github.kpgtb.ktools.manager.ui.bar.event.BarValueChangeEvent;
import com.github.kpgtb.ktools.util.ui.NoShadow;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manager that handles ui bars above food
 * @since 2.0.0
 */
public class BarManager {
    private final Map<String, KBar> bars;
    private final Map<UUID, Map<KBar,BaseUiObject>> uiObjects;
    private ToolsObjectWrapper wrapper;
    private final Map<String, String> plugins;

    private int nextChar;

    public BarManager() {

        this.bars = new HashMap<>();
        this.uiObjects = new HashMap<>();
        this.plugins = new HashMap<>();

        this.nextChar = 45056;
    }

    public void setWrapper(ToolsObjectWrapper wrapper) {
        this.wrapper = wrapper;
    }

    /**
     * Register plugin. It helps with updating resource pack
     * @param name Name of plugin
     * @param version Version of plugin
     */
    public void registerPlugin(String name, String version) {
        this.plugins.put(name,version);
    }

    /**
     * Register custom bar
     * @param bar Custom bar
     */
    public void registerBar(KBar bar) {
        this.bars.put(bar.getName(),bar);
    }

    /**
     * Unregister custom bar
     * @param bar Custom bar
     */
    public void unRegisterBar(KBar bar) {
        this.bars.remove(bar.getName());
    }

    /**
     * Prepare resource pack and ui
     */
    public void prepareBars() {
        ResourcePackManager resourcePack = wrapper.getResourcepackManager();
        if(resourcePack == null) {
            throw new RuntimeException("ResourcePackManager is not supported! ResourcePack requirements: Minecraft Version 1.14+");
        }
        if(!resourcePack.isEnabled()) {
            resourcePack.setRequired(true);
        }
        this.plugins.forEach((name, version) -> {
            if(!resourcePack.isPluginRegistered(name,version)) {
                resourcePack.registerPlugin(name,version);
            }
        });
        UiManager uiManager = wrapper.getUiManager();
        if(uiManager == null) {
            throw new RuntimeException("UiManager is not supported! UI requirements: ProtocolLib & Minecraft Version 1.14+");
        }
         if(!uiManager.isRequired()) {
             uiManager.setRequired(true);
         }

         List<KBar> barsList = new ArrayList<>(this.bars.values());
         barsList.sort(Comparator.comparingInt(KBar::getUniqueID));
         int possiblePlaces = barsList.size() + 1;

         barsList.forEach(bar -> {
             bar.getIcons().forEach(icon -> {
                 for (int i = 0; i < possiblePlaces; i++) {
                     String fullChar = String.format("%04x", this.nextChar);
                     icon.getFullChar().put(i,fullChar);
                     this.nextChar++;
                     String halfChar = String.format("%04x", this.nextChar);
                     icon.getFullChar().put(i,halfChar);
                     this.nextChar++;
                     String emptyChar = String.format("%04x", this.nextChar);
                     icon.getFullChar().put(i,emptyChar);
                     this.nextChar++;

                     int ascent = -16 + (i* icon.getIconsHeight());

                     resourcePack.registerCustomChar(wrapper.getTag(), fullChar, bar.getName()+"_full.png", icon.getFullImage(),icon.getIconsHeight(),ascent,icon.getIconsWidth());
                     resourcePack.registerCustomChar(wrapper.getTag(), halfChar, bar.getName()+"_half.png", icon.getHalfImage(),icon.getIconsHeight(),ascent,icon.getIconsWidth());
                     resourcePack.registerCustomChar(wrapper.getTag(), emptyChar, bar.getName()+"_empty.png", icon.getEmptyImage(),icon.getIconsHeight(),ascent,icon.getIconsWidth());
                 }
             });
         });
    }

    /**
     * Show bar to player
     * @param bar Custom bar
     * @param player Player
     */
    public void showBar(KBar bar, Player player) {
        UUID uuid = player.getUniqueId();
        if(isBarShowed(bar,player)) {
            return;
        }
        BaseUiObject uiObj = new BaseUiObject("", Alignment.LEFT, 15);
        wrapper.getUiManager().addUI(uuid,uiObj);
        this.uiObjects.get(uuid).put(bar,uiObj);
        updateBars(player);
    }

    /**
     * Hide bar from player & update UI
     * @param bar Custom bar
     * @param player Player
     */
    public void hideBar(KBar bar, Player player) {
        this.hideBar(bar,player,false);
    }

    /**
     * Hide bar from player
     * @param bar Custom bar
     * @param player Player
     * @param all If plugin hides every bar
     */
    private void hideBar(KBar bar, Player player, boolean all) {
        UUID uuid = player.getUniqueId();
        if(!isBarShowed(bar,player)) {
            return;
        }
        BaseUiObject uiObj = this.uiObjects.get(uuid).get(bar);
        wrapper.getUiManager().removeUI(uuid,uiObj);
        this.uiObjects.get(uuid).remove(bar);
        if(!all) {
            updateBars(player);
        }
    }

    /**
     * Hide all bars from player
     * @param player Player
     */
    public void hideAllBars(Player player) {
        UUID uuid = player.getUniqueId();
        if(!this.uiObjects.containsKey(uuid)) {
            this.uiObjects.put(uuid, new HashMap<>());
        }
        this.uiObjects.get(uuid).keySet().forEach(bar -> hideBar(bar,player, true));
    }

    /**
     * Check if bar is showed
     * @param bar Custom bar
     * @param player Player
     * @return true if is showed
     */
    public boolean isBarShowed(KBar bar, Player player) {
        UUID uuid = player.getUniqueId();
        if(!this.uiObjects.containsKey(uuid)) {
            this.uiObjects.put(uuid, new HashMap<>());
        }
        return this.uiObjects.get(uuid).containsKey(bar);
    }

    /**
     * Update bars
     * @param player Player
     */
    public void updateBars(Player player) {
        this.bars.values().forEach(bar -> this.updateBar(bar,player));
    }

    /**
     * Update bar
     * @param bar Custom bar
     * @param player Player
     */
    public void updateBar(KBar bar, Player player) {
        if(!isBarShowed(bar,player)) {
            return;
        }
        BaseUiObject uiObj = this.uiObjects.get(player.getUniqueId()).get(bar);
        GameMode mode = player.getGameMode();

        if((mode.equals(GameMode.CREATIVE) && bar.isHideInCreative()) ||
                (mode.equals(GameMode.SPECTATOR) && bar.isHideInSpectator())) {
            uiObj.update("");
            return;
        }

        int barPlace = 0;
        if(player.getRemainingAir() < player.getMaximumAir()) {
            barPlace++;
        }

        for (KBar b : this.uiObjects.get(player.getUniqueId()).keySet()) {
            if(b.equals(bar)) {
                break;
            }
            barPlace++;
        }

        double value = this.getValue(bar,player);

        double fullIcon = bar.getMax() / 10.0;
        int fullIconsInUI = (int) Math.floor(value / fullIcon);
        boolean hasHalfIconInUI = value % fullIcon > 0;
        int emptyIconsInUI = 10 - fullIconsInUI;
        if(hasHalfIconInUI) {
            emptyIconsInUI -= 1;
        }

        String spaceChar = "\uF802";

        BarIcons icons = bar.getIconsFor(value);
        String fullIconChar = icons.getFullChar().get(barPlace) + spaceChar;
        String halfIconChar = icons.getHalfChar().get(barPlace) + spaceChar;
        String emptyIconChar = icons.getEmptyChar().get(barPlace) + spaceChar;

        StringBuilder ui = new StringBuilder();
        for(int i = 0; i < emptyIconsInUI; i++) {
            ui.append(emptyIconChar);
        }

        if(hasHalfIconInUI) {
            ui.append(halfIconChar);
        }

        for(int i = 0; i < fullIconsInUI; i++) {
            ui.append(fullIconChar);
        }

        uiObj.update(NoShadow.disableShadow(ui.toString(), wrapper.getLanguageManager()));
    }

    /**
     * Set value of bar
     * @param bar Custom bar
     * @param player Player
     * @param value New value
     */
    public void setValue(KBar bar, Player player, double value) {
        if(value > bar.getMax()) {
            value = bar.getMax();
        }
        if(value < 0.0) {
            value = 0.0;
        }
        BarValueChangeEvent event = new BarValueChangeEvent(player,bar,getValue(bar,player),value);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) {
            return;
        }
        bar.getSaveMethod().set(wrapper,bar,player,value);
        updateBar(bar,player);
    }

    /**
     * Get value of bar
     * @param bar Custom bar
     * @param player Player
     * @return value of bar
     */
    public double getValue(KBar bar, Player player) {
        return bar.getSaveMethod().get(wrapper,bar,player);
    }

    /**
     * Get all bars
     * @return all bars
     */
    public Map<String, KBar> getBars() {
        return bars;
    }

    /**
     * Get bar
     * @param name Name of bar
     * @return bar or null
     */
    @Nullable
    public KBar getBar(String name) {
        return bars.get(name);
    }
}
