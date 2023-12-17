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
import java.util.ArrayList;
import java.util.List;

public class ListPersister extends LongStringType {
    private static final ListPersister SINGLETON = new ListPersister();
    private final String NONE_TAG = "NONE";

    public ListPersister() {
        super(SqlType.LONG_STRING, new Class[]{List.class});
    }

    public static ListPersister getSingleton() {
        return SINGLETON;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        List<?> list = (List<?>) javaObject;
        String clazz = NONE_TAG;

        if(!list.isEmpty()) {
            clazz = CollectionUtil.getObjectTypes(list.get(0));
        }
        String json = GsonAdapterManager.getInstance().getGson()
                .toJson(list);
        return clazz + " " + json;
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        String[] data = ((String) sqlArg).split(" ", 2);

        if(data.length < 2) {
            return new ArrayList<>();
        }
        if(data[0].equalsIgnoreCase(NONE_TAG)) {
            return new ArrayList<>();
        }

        try {
            Type valueType = CollectionUtil.getTypesFromString(data[0])[0];
            Type listType = TypeToken.getParameterized(List.class, valueType).getType();
            return GsonAdapterManager.getInstance().getGson()
                    .fromJson(data[1], listType);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
