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

package com.github.kpgtb.ktools.util.item;

import com.google.gson.Gson;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ItemBuilder - API Class to create a {@link ItemStack} with just one line of Code
 * Legacy version 1.8 - 1.13
 * @version 1.8
 * @author Acquized
 * @contributor Kev575
 * @contributor KPG-TB
 */
public class LegacyItemBuilder {

    private Material material;
    private MaterialData data;
    private int amount = 1;
    private int damage = 0;
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private String displayName;
    private List<String> lore = new ArrayList<>();
    private List<ItemFlag> flags = new ArrayList<>();


    private boolean unsafeStackSize = false;

    /** Initalizes the ItemBuilder with {@link Material} */
    public LegacyItemBuilder(Material material) {
        if(material == null) material = Material.AIR;
        this.material = material;
        this.data = new MaterialData(material);
    }

    /** Initalizes the ItemBuilder with {@link MaterialData} */
    public LegacyItemBuilder(MaterialData matData) {
        if(matData == null) material = Material.AIR;
        this.material = matData.getItemType();
        this.data = matData;
    }

    /** Initalizes the ItemBuilder with {@link Material} and Amount */
    public LegacyItemBuilder(Material material, int amount) {
        if(material == null) material = Material.AIR;
        if(((amount > material.getMaxStackSize()) || (amount <= 0)) && (!unsafeStackSize)) amount = 1;
        this.amount = amount;
        this.material = material;
        this.data = new MaterialData(material);
    }

    /** Initalizes the ItemBuilder with {@link Material}, Amount and Displayname */
    public LegacyItemBuilder(Material material, int amount, String displayname) {
        if(material == null) material = Material.AIR;
        Validate.notNull(displayname, "The Displayname is null.");
        this.material = material;
        if(((amount > material.getMaxStackSize()) || (amount <= 0)) && (!unsafeStackSize)) amount = 1;
        this.amount = amount;
        this.data = new MaterialData(material);
        this.displayName = displayname;
    }

    /** Initalizes the ItemBuilder with {@link Material} and Displayname */
    public LegacyItemBuilder(Material material, String displayname) {
        if(material == null) material = Material.AIR;
        Validate.notNull(displayname, "The Displayname is null.");
        this.material = material;
        this.data = new MaterialData(material);
        this.displayName = displayname;
    }

    /** Initalizes the ItemBuilder with a {@link ItemStack} */
    public LegacyItemBuilder(ItemStack item) {
        Validate.notNull(item, "The Item is null.");
        this.material = item.getType();
        this.data = item.getData();
        this.amount = item.getAmount();
        this.enchantments = item.getEnchantments();
        this.damage = item.getDurability();
        if(item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            this.displayName = meta.getDisplayName();
            this.lore = meta.getLore();
            for (ItemFlag f : meta.getItemFlags()) {
                flags.add(f);
            }
        }
    }

    /** Initalizes the ItemBuilder with a {@link FileConfiguration} ItemStack in Path */
    public LegacyItemBuilder(FileConfiguration cfg, String path) {
        this(cfg.getItemStack(path));
    }

    /**
     * Initalizes the ItemBuilder with an already existing
     * @deprecated Use the already initalized {@code ItemBuilder} Instance to improve performance
     */
    @Deprecated
    public LegacyItemBuilder(LegacyItemBuilder builder) {
        Validate.notNull(builder, "The ItemBuilder is null.");
        this.material = builder.material;
        this.amount = builder.amount;
        this.damage = builder.damage;
        this.enchantments = builder.enchantments;
        this.displayName = builder.displayName;
        this.lore = builder.lore;
        this.flags = builder.flags;
        this.data = builder.data;
    }

    /**
     * Sets the Amount of the ItemStack
     * @param amount Amount for the ItemStack
     */
    public LegacyItemBuilder amount(int amount) {
        if(((amount > material.getMaxStackSize()) || (amount <= 0)) && (!unsafeStackSize)) amount = 1;
        this.amount = amount;
        return this;
    }

    /**
     * Sets the data of the ItemStack
     * @param matData Data for the ItemStack
     */
    public LegacyItemBuilder data(MaterialData matData) {
        this.material = matData.getItemType();
        this.data = matData;
        return this;
    }

    /**
     * Sets the Damage of the ItemStack
     * @param damage Damage for the ItemStack
     * @deprecated Use {@code ItemBuilder#durability}
     */
    @Deprecated
    public LegacyItemBuilder damage(short damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Sets the Durability (Damage) of the ItemStack
     * @param damage Damage for the ItemStack
     */
    public LegacyItemBuilder durability(short damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Sets the {@link Material} of the ItemStack
     * @param material Material for the ItemStack
     */
    public LegacyItemBuilder material(Material material) {
        Validate.notNull(material, "The Material is null.");
        this.material = material;
        this.data = new MaterialData(material);
        return this;
    }

    /**
     * Adds a {@link Enchantment} to the ItemStack
     * @param enchant Enchantment for the ItemStack
     * @param level Level of the Enchantment
     */
    public LegacyItemBuilder enchant(Enchantment enchant, int level) {
        Validate.notNull(enchant, "The Enchantment is null.");
        enchantments.put(enchant, level);
        return this;
    }

    /**
     * Adds a list of {@link Enchantment} to the ItemStack
     * @param enchantments Map containing Enchantment and Level for the ItemStack
     */
    public LegacyItemBuilder enchant(Map<Enchantment, Integer> enchantments) {
        Validate.notNull(enchantments, "The Enchantments are null.");
        this.enchantments = enchantments;
        return this;
    }

    /**
     * Sets the Displayname of the ItemStack
     * @param displayname Displayname for the ItemStack
     */
    public LegacyItemBuilder displayname(String displayname) {
        Validate.notNull(displayname, "The Displayname is null.");
        this.displayName = displayname;
        return this;
    }

    /**
     * Adds a Line to the Lore of the ItemStack
     * @param line Line of the Lore for the ItemStack
     */
    public LegacyItemBuilder lore(String line) {
        Validate.notNull(line, "The Line is null.");
        lore.add(line);
        return this;
    }

    /**
     * Sets the Lore of the ItemStack
     * @param lore List containing String as Lines for the ItemStack Lore
     */
    public LegacyItemBuilder lore(List<String> lore) {
        Validate.notNull(lore, "The Lores are null.");
        this.lore = lore;
        return this;
    }

    /**
     * Adds one or more Lines to the Lore of the ItemStack
     * @param lines One or more Strings for the ItemStack Lore
     * @deprecated Use {@code ItemBuilder#lore}
     */
    @Deprecated
    public LegacyItemBuilder lores(String... lines) {
        Validate.notNull(lines, "The Lines are null.");
        for (String line : lines) {
            lore(line);
        }
        return this;
    }

    /**
     * Adds one or more Lines to the Lore of the ItemStack
     * @param lines One or more Strings for the ItemStack Lore
     */
    public LegacyItemBuilder lore(String... lines) {
        Validate.notNull(lines, "The Lines are null.");
        for (String line : lines) {
            lore(line);
        }
        return this;
    }

    /**
     * Adds a String at a specified position in the Lore of the ItemStack
     * @param line Line of the Lore for the ItemStack
     * @param index Position in the Lore for the ItemStack
     */
    public LegacyItemBuilder lore(String line, int index) {
        Validate.notNull(line, "The Line is null.");
        lore.set(index, line);
        return this;
    }

    /**
     * Adds a {@link ItemFlag} to the ItemStack
     * @param flag ItemFlag for the ItemStack
     */
    public LegacyItemBuilder flag(ItemFlag flag) {
        Validate.notNull(flag, "The Flag is null.");
        flags.add(flag);
        return this;
    }

    /**
     * Adds more than one {@link ItemFlag} to the ItemStack
     * @param flags List containing all ItemFlags
     */
    public LegacyItemBuilder flag(List<ItemFlag> flags) {
        Validate.notNull(flags, "The Flags are null.");
        this.flags = flags;
        return this;
    }

    /** Makes the ItemStack Glow like it had a Enchantment */
    public LegacyItemBuilder glow() {
        enchant(material != Material.BOW ? Enchantment.ARROW_INFINITE : Enchantment.LUCK, 10);
        flag(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    /**
     * Allows / Disallows Stack Sizes under 1 and above 64
     * @param allow Determinates if it should be allowed or not
     */
    public LegacyItemBuilder unsafeStackSize(boolean allow) {
        this.unsafeStackSize = allow;
        return this;
    }

    /** Toggles allowment of stack sizes under 1 and above 64*/
    public LegacyItemBuilder toggleUnsafeStackSize() {
        unsafeStackSize(!unsafeStackSize);
        return this;
    }

    /** Returns the Displayname */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the Amount */
    public int getAmount() {
        return amount;
    }

    /** Returns all Enchantments */
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    /**
     * Returns the Damage
     */
    public int getDamage() {
        return damage;
    }

    /** Returns the Durability */
    public int getDurability() {
        return damage;
    }

    /** Returns the Lores */
    public List<String> getLores() {
        return lore;
    }

    /** Returns all ItemFlags */
    public List<ItemFlag> getFlags() {
        return flags;
    }

    /** Returns the Material */
    public Material getMaterial() {
        return material;
    }

    /**
     * Returns all Lores
     */
    public List<String> getLore() {
        return lore;
    }

    public MaterialData getData() {
        return data;
    }

    /**
     * Converts the Item to a ConfigStack and writes it to path
     * @param cfg Configuration File to which it should be writed
     * @param path Path to which the ConfigStack should be writed
     */
    public LegacyItemBuilder toConfig(FileConfiguration cfg, String path) {
        cfg.set(path, build());
        return this;
    }

    /**
     * Converts back the ConfigStack to a ItemBuilder
     * @param cfg Configuration File from which it should be read
     * @param path Path from which the ConfigStack should be read
     */
    public LegacyItemBuilder fromConfig(FileConfiguration cfg, String path) {
        return new LegacyItemBuilder(cfg, path);
    }

    /**
     * Converts the Item to a ConfigStack and writes it to path
     * @param cfg Configuration File to which it should be writed
     * @param path Path to which the ConfigStack should be writed
     * @param builder Which ItemBuilder should be writed
     */
    public static void toConfig(FileConfiguration cfg, String path, LegacyItemBuilder builder) {
        cfg.set(path, builder.build());
    }

    /**
     * Converts the ItemBuilder to a JsonItemBuilder
     * @return The ItemBuilder as JSON String
     */
    public String toJson() {
        return new Gson().toJson(this);
    }

    /**
     * Converts the ItemBuilder to a JsonItemBuilder
     * @param builder Which ItemBuilder should be converted
     * @return The ItemBuilder as JSON String
     */
    public static String toJson(LegacyItemBuilder builder) {
        return new Gson().toJson(builder);
    }

    /**
     * Converts the JsonItemBuilder back to a ItemBuilder
     * @param json Which JsonItemBuilder should be converted
     */
    public static LegacyItemBuilder fromJson(String json) {
        return new Gson().fromJson(json, LegacyItemBuilder.class);
    }

    /**
     * Applies the currently ItemBuilder to the JSONItemBuilder
     * @param json Already existing JsonItemBuilder
     * @param overwrite Should the JsonItemBuilder used now
     */
    public LegacyItemBuilder applyJson(String json, boolean overwrite) {
        LegacyItemBuilder b = new Gson().fromJson(json, LegacyItemBuilder.class);
        if(overwrite)
            return b;
        if(b.displayName != null)
            displayName = b.displayName;
        if(b.material != null)
            material = b.material;
        if(b.data != null)
            data = b.data;
        if(b.lore != null)
            lore = b.lore;
        if(b.enchantments != null)
            enchantments = b.enchantments;
        if(b.flags != null)
            flags = b.flags;
        damage = b.damage;
        amount = b.amount;
        return this;
    }

    /** Converts the ItemBuilder to a {@link ItemStack} */
    public ItemStack build() {
        if(material == null) {
            material = Material.AIR;
        }
        if(data == null) {
            data = new MaterialData(material);
        }
        ItemStack item = new ItemStack(material, amount);
        item.setType(material);
        item.setAmount(amount);
        item.setData(data);
        item.setDurability((short) damage);
        if(enchantments.size() > 0) {
            item.addUnsafeEnchantments(enchantments);
        }
        ItemMeta meta = item.getItemMeta();
        if(displayName != null) {
            meta.setDisplayName(displayName);
        }
        if(lore.size() > 0) {
            meta.setLore(lore);
        }
        if(flags.size() > 0) {
            for (ItemFlag f : flags) {
                meta.addItemFlags(f);
            }
        }
        item.setItemMeta(meta);
        return item;
    }
}