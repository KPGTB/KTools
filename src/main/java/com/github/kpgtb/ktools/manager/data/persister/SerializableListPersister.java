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
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SerializableListPersister extends StringType {
    private static final SerializableListPersister SINGLETON = new SerializableListPersister();
    private final String FORMAT = "%s %s";
    private final String NONE_TAG = "NONE";

    public SerializableListPersister() {
        super(SqlType.STRING, new Class[]{List.class, ArrayList.class, LinkedList.class});
    }

    public static SerializableListPersister getSingleton() {
        return SINGLETON;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        List<?> list = (List<?>) javaObject;
        String listJson = new Gson().toJson(javaObject);
        String clazz = NONE_TAG;

        if(!list.isEmpty()) {
            clazz = list.get(0).getClass().getName();
        }
        return String.format(FORMAT,clazz,listJson);
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        String[] data = ((String) sqlArg).split(" ", 2);

        if(data.length < 2) {
            return new ArrayList<>();
        }

        if(data[0].equals(NONE_TAG)) {
            return new ArrayList<>();
        }

        try {
            Class<?> type = Class.forName(data[0]);
            return getList(data[1], type);
        } catch (ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    private <T> List<T> getList(String jsonArray, Class<T> clazz) {
        Type typeOfT = TypeToken.getParameterized(List.class, clazz).getType();
        return new Gson().fromJson(jsonArray, typeOfT);
    }
}
