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

package com.github.kpgtb.ktools.manager.data.adapter;

import com.github.kpgtb.ktools.util.item.ItemUtil;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class ItemStackAdapter extends TypeAdapter<ItemStack> {
    @Override
    public void write(JsonWriter out, ItemStack value) throws IOException {
        out.beginObject();
        out.name("item").value(ItemUtil.serializeItem(value));
        out.endObject();
    }

    @SneakyThrows
    @Override
    public ItemStack read(JsonReader in) throws IOException {
        in.beginObject();
        ItemStack result = new ItemStack(Material.AIR);
        if(in.hasNext()) {
            in.nextName();
            String b64 = in.nextString();
            result = ItemUtil.deserializeItem(b64);
        }
        in.endObject();
        return result;
    }
}
