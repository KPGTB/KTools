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

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.UuidType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.UUID;

public class OfflinePlayerPersister extends UuidType {
    private static final OfflinePlayerPersister SINGLETON = new OfflinePlayerPersister();

    public OfflinePlayerPersister() {
        super(SqlType.UUID, new Class[]{OfflinePlayer.class});
    }

    public static OfflinePlayerPersister getSingleton() {
        return SINGLETON;
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        return Bukkit.getOfflinePlayer((UUID) sqlArg);
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object obj) {
        return ((OfflinePlayer) obj).getUniqueId();
    }
}
