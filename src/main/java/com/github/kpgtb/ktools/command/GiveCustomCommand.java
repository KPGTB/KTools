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

package com.github.kpgtb.ktools.command;

import com.github.kpgtb.ktools.manager.command.KCommand;
import com.github.kpgtb.ktools.manager.command.annotation.Description;
import com.github.kpgtb.ktools.manager.item.Kitem;
import com.github.kpgtb.ktools.manager.language.LanguageLevel;
import com.github.kpgtb.ktools.util.ItemUtil;
import com.github.kpgtb.ktools.util.ToolsObjectWrapper;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Description(text = "Give custom items from plugins that supports Ktools")
public class GiveCustomCommand extends KCommand {
    private final ToolsObjectWrapper wrapper;

    public GiveCustomCommand(ToolsObjectWrapper toolsObjectWrapper, String groupPath) {
        super(toolsObjectWrapper, groupPath);
        this.wrapper = toolsObjectWrapper;
    }

    @Description(text = "Give custom item from plugins that supports Ktools")
    public void item(Player player, Player target, Kitem item, int amount) {
        ItemStack result = item.getItem();
        result.setAmount(amount);

        String name = item.getFullItemTag();
        if(result.getItemMeta().hasDisplayName()) {
            name = result.getItemMeta().getDisplayName();
        }

        ItemUtil.giveItemToPlayer(target, result);

        wrapper.getLanguageManager().getComponent(
            LanguageLevel.PLUGIN,
            "givenCustomItem",
            Placeholder.unparsed("item", name),
            Placeholder.unparsed("amount", amount+""),
            Placeholder.unparsed("target", target.getName())
        ).forEach(msg -> wrapper.getAdventure().player(player).sendMessage(msg));

        wrapper.getLanguageManager().getComponent(
                LanguageLevel.PLUGIN,
                "getCustomItem",
                Placeholder.unparsed("item", name),
                Placeholder.unparsed("amount", amount+""),
                Placeholder.unparsed("player", player.getName())
        ).forEach(msg -> wrapper.getAdventure().player(target).sendMessage(msg));
    }

}
