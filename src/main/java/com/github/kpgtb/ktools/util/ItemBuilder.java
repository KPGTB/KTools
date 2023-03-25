/* Copyright 2016 Acquized
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.kpgtb.ktools.util;

import com.google.gson.Gson;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.util.*;

/**
 * ItemBuilder - API Class to create a {@link org.bukkit.inventory.ItemStack} with just one line of Code
 * @version 1.8
 * @author Acquized
 * @contributor Kev575
 * @contributor KPG-TB
 */
public class ItemBuilder {

    private Material material;
    private int amount = 1;
    private int damage = 0;
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private String displayName;
    private int model = 0;
    private List<String> lore = new ArrayList<>();
    private List<ItemFlag> flags = new ArrayList<>();

    private boolean unbreakable = false;
    private OfflinePlayer owner;


    private boolean unsafeStackSize = false;

    /** Initalizes the ItemBuilder with {@link org.bukkit.Material} */
    public ItemBuilder(Material material) {
        if(material == null) material = Material.AIR;
        this.material = material;
    }

    /** Initalizes the ItemBuilder with {@link org.bukkit.Material} and Amount */
    public ItemBuilder(Material material, int amount) {
        if(material == null) material = Material.AIR;
        if(((amount > material.getMaxStackSize()) || (amount <= 0)) && (!unsafeStackSize)) amount = 1;
        this.amount = amount;
        this.material = material;
    }

    /** Initalizes the ItemBuilder with {@link org.bukkit.Material}, Amount and Displayname */
    public ItemBuilder(Material material, int amount, String displayname) {
        if(material == null) material = Material.AIR;
        Validate.notNull(displayname, "The Displayname is null.");
        this.material = material;
        if(((amount > material.getMaxStackSize()) || (amount <= 0)) && (!unsafeStackSize)) amount = 1;
        this.amount = amount;
        this.displayName = displayname;
    }

    /** Initalizes the ItemBuilder with {@link org.bukkit.Material} and Displayname */
    public ItemBuilder(Material material, String displayname) {
        if(material == null) material = Material.AIR;
        Validate.notNull(displayname, "The Displayname is null.");
        this.material = material;
        this.displayName = displayname;
    }

    /** Initalizes the ItemBuilder with a {@link org.bukkit.inventory.ItemStack} */
    public ItemBuilder(ItemStack item) {
        Validate.notNull(item, "The Item is null.");
        this.material = item.getType();
        this.amount = item.getAmount();
        this.enchantments = item.getEnchantments();
        if(item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            this.displayName = meta.getDisplayName();
            this.model = meta.getCustomModelData();
            this.lore = meta.getLore();
            this.unbreakable = meta.isUnbreakable();
            for (ItemFlag f : meta.getItemFlags()) {
                flags.add(f);
            }
            this.damage = 0;
            if(meta instanceof Damageable) {
                this.damage = ((Damageable) meta).getDamage();
            }
            if(this.material.equals(Material.PLAYER_HEAD)) {
                this.owner = ((SkullMeta)meta).getOwningPlayer();
            }
        }
    }

    /** Initalizes the ItemBuilder with a {@link org.bukkit.configuration.file.FileConfiguration} ItemStack in Path */
    public ItemBuilder(FileConfiguration cfg, String path) {
        this(cfg.getItemStack(path));
    }

    /**
     * Initalizes the ItemBuilder with an already existing
     * @deprecated Use the already initalized {@code ItemBuilder} Instance to improve performance
     */
    @Deprecated
    public ItemBuilder(ItemBuilder builder) {
        Validate.notNull(builder, "The ItemBuilder is null.");
        this.material = builder.material;
        this.amount = builder.amount;
        this.damage = builder.damage;
        this.enchantments = builder.enchantments;
        this.displayName = builder.displayName;
        this.lore = builder.lore;
        this.flags = builder.flags;
        this.model = builder.model;
        this.unbreakable = builder.unbreakable;
        this.owner = builder.owner;
    }

    /**
     * Sets the Amount of the ItemStack
     * @param amount Amount for the ItemStack
     */
    public ItemBuilder amount(int amount) {
        if(((amount > material.getMaxStackSize()) || (amount <= 0)) && (!unsafeStackSize)) amount = 1;
        this.amount = amount;
        return this;
    }

    /**
     * Sets custom model data of the ItemStack
     * @param model Custom model data value
     */
    public ItemBuilder model(int model) {
        this.model = model;
        return this;
    }

    /**
     * Sets the Damage of the ItemStack
     * @param damage Damage for the ItemStack
     * @deprecated Use {@code ItemBuilder#durability}
     */
    @Deprecated
    public ItemBuilder damage(short damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Sets the Durability (Damage) of the ItemStack
     * @param damage Damage for the ItemStack
     */
    public ItemBuilder durability(short damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Sets the {@link org.bukkit.Material} of the ItemStack
     * @param material Material for the ItemStack
     */
    public ItemBuilder material(Material material) {
        Validate.notNull(material, "The Material is null.");
        this.material = material;
        return this;
    }

    /**
     * Adds a {@link org.bukkit.enchantments.Enchantment} to the ItemStack
     * @param enchant Enchantment for the ItemStack
     * @param level Level of the Enchantment
     */
    public ItemBuilder enchant(Enchantment enchant, int level) {
        Validate.notNull(enchant, "The Enchantment is null.");
        enchantments.put(enchant, level);
        return this;
    }

    /**
     * Adds a list of {@link org.bukkit.enchantments.Enchantment} to the ItemStack
     * @param enchantments Map containing Enchantment and Level for the ItemStack
     */
    public ItemBuilder enchant(Map<Enchantment, Integer> enchantments) {
        Validate.notNull(enchantments, "The Enchantments are null.");
        this.enchantments = enchantments;
        return this;
    }

    /**
     * Sets the Displayname of the ItemStack
     * @param displayname Displayname for the ItemStack
     */
    public ItemBuilder displayname(String displayname) {
        Validate.notNull(displayname, "The Displayname is null.");
        this.displayName = displayname;
        return this;
    }

    /**
     * Adds a Line to the Lore of the ItemStack
     * @param line Line of the Lore for the ItemStack
     */
    public ItemBuilder lore(String line) {
        Validate.notNull(line, "The Line is null.");
        lore.add(line);
        return this;
    }

    /**
     * Sets the Lore of the ItemStack
     * @param lore List containing String as Lines for the ItemStack Lore
     */
    public ItemBuilder lore(List<String> lore) {
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
    public ItemBuilder lores(String... lines) {
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
    public ItemBuilder lore(String... lines) {
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
    public ItemBuilder lore(String line, int index) {
        Validate.notNull(line, "The Line is null.");
        lore.set(index, line);
        return this;
    }

    /**
     * Adds a {@link org.bukkit.inventory.ItemFlag} to the ItemStack
     * @param flag ItemFlag for the ItemStack
     */
    public ItemBuilder flag(ItemFlag flag) {
        Validate.notNull(flag, "The Flag is null.");
        flags.add(flag);
        return this;
    }

    /**
     * Adds more than one {@link org.bukkit.inventory.ItemFlag} to the ItemStack
     * @param flags List containing all ItemFlags
     */
    public ItemBuilder flag(List<ItemFlag> flags) {
        Validate.notNull(flags, "The Flags are null.");
        this.flags = flags;
        return this;
    }

    /**
     * Makes or removes the Unbreakable Flag from the ItemStack
     * @param unbreakable If it should be unbreakable
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    /** Makes the ItemStack Glow like it had a Enchantment */
    public ItemBuilder glow() {
        enchant(material != Material.BOW ? Enchantment.ARROW_INFINITE : Enchantment.LUCK, 10);
        flag(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    /**
     * Sets the Skin for the Skull
     * @param user Username of the Skull
     */
    public ItemBuilder owner(String user) {
        Validate.notNull(user, "The Username is null.");
        if(material == Material.PLAYER_HEAD) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(user);
            this.owner = op;
        }
        return this;
    }

    /**
     * Allows / Disallows Stack Sizes under 1 and above 64
     * @param allow Determinates if it should be allowed or not
     */
    public ItemBuilder unsafeStackSize(boolean allow) {
        this.unsafeStackSize = allow;
        return this;
    }

    /** Toggles allowment of stack sizes under 1 and above 64*/
    public ItemBuilder toggleUnsafeStackSize() {
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

    /**
     * Converts the Item to a ConfigStack and writes it to path
     * @param cfg Configuration File to which it should be writed
     * @param path Path to which the ConfigStack should be writed
     */
    public ItemBuilder toConfig(FileConfiguration cfg, String path) {
        cfg.set(path, build());
        return this;
    }

    /**
     * Converts back the ConfigStack to a ItemBuilder
     * @param cfg Configuration File from which it should be read
     * @param path Path from which the ConfigStack should be read
     */
    public ItemBuilder fromConfig(FileConfiguration cfg, String path) {
        return new ItemBuilder(cfg, path);
    }

    /**
     * Converts the Item to a ConfigStack and writes it to path
     * @param cfg Configuration File to which it should be writed
     * @param path Path to which the ConfigStack should be writed
     * @param builder Which ItemBuilder should be writed
     */
    public static void toConfig(FileConfiguration cfg, String path, ItemBuilder builder) {
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
    public static String toJson(ItemBuilder builder) {
        return new Gson().toJson(builder);
    }

    /**
     * Converts the JsonItemBuilder back to a ItemBuilder
     * @param json Which JsonItemBuilder should be converted
     */
    public static ItemBuilder fromJson(String json) {
        return new Gson().fromJson(json, ItemBuilder.class);
    }

    /**
     * Applies the currently ItemBuilder to the JSONItemBuilder
     * @param json Already existing JsonItemBuilder
     * @param overwrite Should the JsonItemBuilder used now
     */
    public ItemBuilder applyJson(String json, boolean overwrite) {
        ItemBuilder b = new Gson().fromJson(json, ItemBuilder.class);
        if(overwrite)
            return b;
        if(b.displayName != null)
            displayName = b.displayName;
        if(b.material != null)
            material = b.material;
        if(b.lore != null)
            lore = b.lore;
        if(b.enchantments != null)
            enchantments = b.enchantments;
        if(b.flags != null)
            flags = b.flags;
        damage = b.damage;
        amount = b.amount;
        model = b.model;
        owner = b.owner;
        unbreakable = b.unbreakable;
        return this;
    }

    /** Converts the ItemBuilder to a {@link org.bukkit.inventory.ItemStack} */
    public ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        item.setType(material);
        item.setAmount(amount);
        ItemMeta meta = item.getItemMeta();
        if(meta instanceof Damageable) {
            ((Damageable) meta).setDamage(damage);
        }
        meta.setUnbreakable(unbreakable);
        if(enchantments.size() > 0) {
            item.addUnsafeEnchantments(enchantments);
        }
        if(displayName != null) {
            meta.setDisplayName(displayName);
        }
        meta.setCustomModelData(model);
        if(lore.size() > 0) {
            meta.setLore(lore);
        }
        if(flags.size() > 0) {
            for (ItemFlag f : flags) {
                meta.addItemFlags(f);
            }
        }
        if(material.equals(Material.PLAYER_HEAD)) {
            SkullMeta skullMeta = (SkullMeta) meta;
            skullMeta.setOwningPlayer(owner);
            item.setItemMeta(skullMeta);
        } else {
            item.setItemMeta(meta);
        }
        return item;
    }
}