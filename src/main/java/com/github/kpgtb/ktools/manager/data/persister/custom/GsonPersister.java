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

package com.github.kpgtb.ktools.manager.data.persister.custom;

import com.github.kpgtb.ktools.manager.data.GsonAdapterManager;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.LongStringType;

import java.sql.SQLException;

public class GsonPersister extends LongStringType {
    private static final GsonPersister SINGLETON = new GsonPersister();
    private final String NONE_TAG = "NONE";

    public GsonPersister() {
        super(SqlType.LONG_STRING, new Class[]{});
    }

    public static GsonPersister getSingleton() {
        return SINGLETON;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        String clazz = NONE_TAG;
        if(javaObject != null) {
            clazz = javaObject.getClass().getName();
        }
        String json = GsonAdapterManager.getInstance().getGson()
                .toJson(javaObject);
        return clazz + " " + json;
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        String[] data = ((String) sqlArg).split(" ", 2);

        if(data.length < 2) {
            return null;
        }
        if(data[0].equalsIgnoreCase(NONE_TAG)) {
            return null;
        }

        try {
            return GsonAdapterManager.getInstance().getGson()
                    .fromJson(data[1], Class.forName(data[0]));
        } catch (Exception e) {
            return null;
        }
    }
}
