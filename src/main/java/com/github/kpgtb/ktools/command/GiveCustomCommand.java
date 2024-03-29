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
import com.github.kpgtb.ktools.manager.item.KItem;
import com.github.kpgtb.ktools.manager.language.LanguageLevel;
import com.github.kpgtb.ktools.util.item.ItemUtil;
import com.github.kpgtb.ktools.util.wrapper.ToolsObjectWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Description("Give custom items from plugins that supports KTools")
public class GiveCustomCommand extends KCommand {
    private final ToolsObjectWrapper wrapper;

    public GiveCustomCommand(ToolsObjectWrapper wrapper, String groupPath) {
        super(wrapper, groupPath);
        this.wrapper = wrapper;
    }

    @Description("Give custom item from plugins that supports KTools")
    public void item(CommandSender sender, Player target, KItem item, int amount) {
        ItemStack result = item.getItem().clone();
        result.setAmount(amount);

        TextComponent name = Component.text(item.getFullItemTag());
        if(result.getItemMeta().hasDisplayName()) {
            name = wrapper.getLanguageManager().convertLegacyStringToComponent(
                    result.getItemMeta().getDisplayName()
            );
        }

        ItemUtil.giveItemToPlayer(target, result);

        wrapper.getLanguageManager().getComponent(
            LanguageLevel.GLOBAL,
            "givenCustomItem",
            Placeholder.component("item", name),
            Placeholder.unparsed("amount", String.valueOf(amount)),
            Placeholder.unparsed("target", target.getName())
        ).forEach(msg -> wrapper.getAdventure().sender(sender).sendMessage(msg));

        wrapper.getLanguageManager().getComponent(
                LanguageLevel.GLOBAL,
                "getCustomItem",
                Placeholder.component("item", name),
                Placeholder.unparsed("amount", String.valueOf(amount)),
                Placeholder.unparsed("player", sender.getName())
        ).forEach(msg -> wrapper.getAdventure().player(target).sendMessage(msg));
    }

}
