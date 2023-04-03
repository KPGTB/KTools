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

package com.github.kpgtb.ktools.manager.data.persister;

import com.github.kpgtb.ktools.util.item.ItemUtil;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.SQLException;

public class ItemStackPersister extends StringType {
    private static final ItemStackPersister SINGLETON = new ItemStackPersister();

    public ItemStackPersister() {
        super(SqlType.STRING, new Class[]{ItemStack.class});
    }

    public static ItemStackPersister getSingleton() {
        return SINGLETON;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        try {
            return ItemUtil.serializeItem((ItemStack) javaObject);
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        try {
            return ItemUtil.deserializeItem((String) sqlArg);
        } catch (IOException | ClassNotFoundException e) {
            return new ItemStack(Material.AIR);
        }
    }
}
