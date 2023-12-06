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

package com.github.kpgtb.ktools.manager.data;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.jdbc.db.SqliteDatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Dao Implementation that resolves problem of remove getGeneratedKeys() form SQLite-JDBC
 * Use it as daoClass in @DatabaseTable
 */
@ApiStatus.Experimental
public class FixedDaoImpl<T,ID> extends BaseDaoImpl<T,ID> {

    public FixedDaoImpl(Class<T> dataClass) throws SQLException {
        super(dataClass);
    }

    public FixedDaoImpl(ConnectionSource connectionSource, Class<T> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public FixedDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    @Override
    public int create(T data) throws SQLException {
        FieldType idField = this.tableInfo.getIdField();

        try {
            if (idField != null && idField.isGeneratedId()) {
                if (!idField.isAllowGeneratedIdInsert()) {
                    Field field = getField(idField.getClass(), "fieldConfig");
                    field.setAccessible(true);
                    DatabaseFieldConfig fieldConfig = (DatabaseFieldConfig) field.get(idField);
                    fieldConfig.setAllowGeneratedIdInsert(true);
                    field.setAccessible(false);
                }

                int id = 1;
                T last = queryBuilder()
                        .orderBy(idField.getColumnName(), false)
                        .queryForFirst();

                if (last != null) {
                    id = idField.getFieldValueIfNotDefault(last);
                    id++;
                }
                idField.assignIdValue(connectionSource, data, id, super.getObjectCache());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.create(data);
    }


    private Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            } else {
                return getField(superClass, fieldName);
            }
        }
    }


}
