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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;

public class LocationAdapter extends TypeAdapter<Location> {
    @Override
    public void write(JsonWriter out, Location value) throws IOException {
        out.beginObject();
        out.name("world").value(value.getWorld().getName());
        out.name("x").value(value.getX());
        out.name("y").value(value.getY());
        out.name("z").value(value.getZ());
        out.name("pitch").value(value.getPitch());
        out.name("yaw").value(value.getYaw());
        out.endObject();
    }

    @Override
    public Location read(JsonReader in) throws IOException {
        in.beginObject();
        World world = null;
        double x = 0.0D;
        double y = 0.0D;
        double z = 0.0D;
        float pitch = 0.0F;
        float yaw = 0.0F;

        while(in.hasNext()) {
            switch (in.nextName()) {
                case "world":
                    world = Bukkit.getWorld(in.nextString());
                    break;
                case "x":
                    x = in.nextDouble();
                    break;
                case "y":
                    y = in.nextDouble();
                    break;
                case "z":
                    z = in.nextDouble();
                    break;
                case "pitch":
                    pitch = (float) in.nextDouble();
                    break;
                case "yaw":
                    yaw = (float) in.nextDouble();
                    break;
            }
        }
        in.endObject();
        return new Location(world,x,y,z,yaw,pitch);
    }
}
