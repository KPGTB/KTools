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

package com.github.kpgtb.ktools.manager.data.persister.base;

import com.github.kpgtb.ktools.manager.data.GsonAdapterManager;
import com.github.kpgtb.ktools.util.file.CollectionUtil;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.LongStringType;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MapPersister extends LongStringType {
    private static final MapPersister SINGLETON = new MapPersister();
    private final String NONE_TAG = "NONE";

    public MapPersister() {
        super(SqlType.LONG_STRING, new Class[]{Map.class});
    }

    public static MapPersister getSingleton() {
        return SINGLETON;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        Map<?,?> map = (Map<?,?>) javaObject;
        String keyClazz = NONE_TAG;
        String valueClazz = NONE_TAG;

        if(!map.isEmpty()) {
            keyClazz = map.keySet().toArray()[0].getClass().getName();
            valueClazz = CollectionUtil.getObjectTypes(
                    map.values().toArray()[0]
            );
        }

        String mapJson = GsonAdapterManager.getInstance().getGson()
                .toJson(map);
        return keyClazz + " " + valueClazz + " " + mapJson;
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        String[] data = ((String) sqlArg).split(" ", 3);

        if(data.length < 3) {
            return new HashMap<>();
        }
        if(data[0].equalsIgnoreCase(NONE_TAG) || data[1].equalsIgnoreCase(NONE_TAG)) {
            return new HashMap<>();
        }

        try {
            Type keyType = Class.forName(data[0]);
            Type valueType = CollectionUtil.getTypesFromString(data[1])[0];
            Type mapType = TypeToken.getParameterized(Map.class, keyType, valueType).getType();
            return GsonAdapterManager.getInstance().getGson()
                    .fromJson(data[2], mapType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
}
