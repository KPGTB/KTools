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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.SQLException;

public class LocationPersister extends StringType {
    private static final LocationPersister SINGLETON = new LocationPersister();

    protected LocationPersister() {
        super(SqlType.STRING, new Class[]{Location.class});
    }

    public static LocationPersister getSingleton() {
        return SINGLETON;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        Location loc = (Location) javaObject;
        String worldName = loc.getWorld().getName();
        double x= loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        float pitch = loc.getPitch();
        float yaw = loc.getYaw();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("world", worldName);
        jsonObject.addProperty("x", x);
        jsonObject.addProperty("y", y);
        jsonObject.addProperty("z", z);
        jsonObject.addProperty("pitch", pitch);
        jsonObject.addProperty("yaw", yaw);

        return new Gson().toJson(jsonObject);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        JsonObject jsonObject = new JsonParser().parse((String) sqlArg).getAsJsonObject();

        Location result = new Location(
                Bukkit.getWorld(jsonObject.get("world").getAsString()),
                jsonObject.get("x").getAsDouble(),
                jsonObject.get("y").getAsDouble(),
                jsonObject.get("z").getAsDouble()
        );
        result.setPitch(jsonObject.get("pitch").getAsFloat());
        result.setYaw(jsonObject.get("yaw").getAsFloat());

        return result;
    }
}
