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

package com.github.kpgtb.ktools.manager.gui.item.common;

import com.github.kpgtb.ktools.manager.gui.container.PagedGuiContainer;
import com.github.kpgtb.ktools.manager.gui.item.GuiItem;
import com.github.kpgtb.ktools.manager.language.LanguageLevel;
import com.github.kpgtb.ktools.util.item.ItemBuilder;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import org.bukkit.Material;

/**
 * Common used GuiItem - Close Inventory
 * @deprecated Use CommonGuiItem
 */
@Deprecated
public class CloseItem {
    /**
     * Get this item
     * @param wrapper Instance of ToolsObjectWrapper
     * @return Item
     */
    public static GuiItem get(ToolsObjectWrapper wrapper) {
        Material material = Material.BARRIER;
        try {
            material = Material.valueOf(wrapper.getPlugin().getConfig().getString("gui.closeItem").toUpperCase());
        } catch (Exception e) {}

        GuiItem result = new GuiItem(
          new ItemBuilder(material)
              .displayname(wrapper.getLanguageManager().getSingleString(LanguageLevel.GLOBAL, "closeItem"))
              .build()
        );
        result.setClickAction((e,place) -> e.getWhoClicked().closeInventory());
        return result;
    }
}
