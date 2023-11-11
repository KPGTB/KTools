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

import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.util.file.ReflectionUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.LruObjectCache;
import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.jdbc.db.MysqlDatabaseType;
import com.j256.ormlite.jdbc.db.SqliteDatabaseType;
import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Data manager handles connecting with database in plugin. This manager use OrmLite!
 */
public class DataManager {
    private @Nullable JdbcPooledConnectionSource connectionSource;
    private final DebugManager debug;
    private final JavaPlugin plugin;

    private final HashMap<Class<?>, Dao<?, ?>> daosMap;

    /**
     * Constructor of DataManager. It also handles connection to database
     *
     * @param debug      Instance of DebugManager
     * @param config     Plugin config
     * @param dataFolder Plugin data folder
     * @param plugin     Instance of plugin
     */
    public DataManager(DebugManager debug, FileConfiguration config, File dataFolder, JavaPlugin plugin) {
        this.debug = debug;
        this.plugin = plugin;
        this.daosMap = new HashMap<>();

        this.debug.sendInfo(DebugType.DATA, "Connecting to database...");

        switch (config.getString("data.type")) {
            case "MYSQL":
                String host = config.getString("data.mysql.host");
                int port = config.getInt("data.mysql.port");
                String dbName = config.getString("data.mysql.database");
                String username = config.getString("data.mysql.username");
                String password = config.getString("data.mysql.password");

                String url = "jdbc:mysql://"+host+":"+port+"/" + dbName;
                try {
                    this.connectionSource = new JdbcPooledConnectionSource(url, username,password, new MysqlDatabaseType());
                } catch (SQLException e) {
                    this.connectionSource = null;
                    this.debug.sendWarning(DebugType.DATA, "Error while connection to MySQL");
                    e.printStackTrace();
                    return;
                }
                break;
            case "SQLITE":
                if(!dataFolder.exists()) {
                    dataFolder.mkdirs();
                }
                File dbFile = new File(dataFolder, "database.db");
                if(!dbFile.exists()) {
                    try {
                        dbFile.createNewFile();
                    } catch (IOException e) {
                        this.connectionSource = null;
                        this.debug.sendWarning(DebugType.DATA, "Error while creating SQLite file!");
                        e.printStackTrace();
                        return;
                    }
                }
                String sqliteURL = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                try {
                    this.connectionSource = new JdbcPooledConnectionSource(sqliteURL, new SqliteDatabaseType());
                } catch (SQLException e) {
                    this.connectionSource = null;
                    this.debug.sendWarning(DebugType.DATA, "Error while connection to SQLite");
                    e.printStackTrace();
                    return;
                }
                break;
            default:
                this.connectionSource = null;
                this.debug.sendWarning(DebugType.DATA, "Wrong type of database. Correct: MYSQL SQLITE");
                return;
        }

        if(!debug.isEnabled(DebugType.DATA)) {
            LoggerFactory.setLogBackendFactory(LogBackendType.NULL);
        }
        this.debug.sendInfo(DebugType.DATA, "Connected to database");
    }

    /**
     * Register all OrmLite persisters from specified package
     * @param packageName Package where are stored all persisters
     * @param jarFile JAR file of this plugin
     */
    public void registerPersisters(String packageName, File jarFile) {
        if(this.connectionSource == null) {
            this.debug.sendWarning(DebugType.DATA, "There isn't any connection source!");
            return;
        }
        for(Class<?> clazz : ReflectionUtil.getAllClassesInPackage(jarFile, packageName)) {
            if(!DataPersister.class.isAssignableFrom(clazz)) {
                continue;
            }
            this.debug.sendInfo(DebugType.DATA, "Loading " + clazz.getSimpleName());
            try {
                DataPersister persister = (DataPersister) clazz.getDeclaredConstructor().newInstance();
                DataPersisterManager.registerDataPersisters(persister);
                this.debug.sendInfo(DebugType.DATA, "Loaded " + clazz.getSimpleName());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                e.printStackTrace();
                this.debug.sendWarning(DebugType.DATA,"Error while loading persister!");
            }
        }
    }

    /**
     * Register all OrmLite tables from specified package
     * @param packageName Package where are stored all tables
     * @param jarFile JAR file of this plugin
     */
    public void registerTables(String packageName, File jarFile) {
        if(this.connectionSource == null) {
            this.debug.sendWarning(DebugType.DATA, "There isn't any connection source!");
            return;
        }
        for(Class<?> clazz : ReflectionUtil.getAllClassesInPackage(jarFile, packageName)) {
            if(clazz.getDeclaredAnnotation(DatabaseTable.class) == null) {
                continue;
            }
            this.debug.sendInfo(DebugType.DATA, "Loading " + clazz.getSimpleName());
            Field idField = null;
            for(Field f : clazz.getDeclaredFields()) {
                DatabaseField databaseField = f.getDeclaredAnnotation(DatabaseField.class);
                if(databaseField == null) {
                    continue;
                }
                if(!databaseField.id() && !databaseField.generatedId() && databaseField.generatedIdSequence().isEmpty()) {
                    continue;
                }
                this.debug.sendInfo(DebugType.DATA, "Loaded " + f.getName() + " as ID");
                idField = f;
                break;
            }
            if(idField == null) {
                this.debug.sendWarning(DebugType.DATA, "This class don't have ID! Cancelling!");
                return;
            }

            try {
                TableUtils.createTableIfNotExists(this.connectionSource, clazz);
            } catch (SQLException e) {
                this.debug.sendWarning(DebugType.DATA,"Error while creating table!");
                e.printStackTrace();
                continue;
            }

            Dao<?,?> dao;
            try {
                 dao = DaoManager.createDao(this.connectionSource, clazz);
                if(plugin.getConfig().getBoolean("data.cache.enabled")) {
                    int capacity = plugin.getConfig().getInt("data.cache.capacity");
                    if(capacity == 0) {
                        dao.setObjectCache(true);
                    } else {
                        dao.setObjectCache(new LruObjectCache(capacity));
                    }
                }
            } catch (SQLException e) {
                this.debug.sendWarning(DebugType.DATA,"Error while creating DAO!");
                e.printStackTrace();
                continue;
            }
            this.daosMap.put(clazz, dao);
            this.debug.sendInfo(DebugType.DATA, "Loaded " + clazz.getSimpleName());
        }
    }

    /**
     * Get DAO instance of table
     * @param daoSource Class with table
     * @param idType Class that represents ID
     * @return DAO of this table or null
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T,Z> Dao<T,Z> getDao(Class<T> daoSource, Class<Z> idType) {
        if(connectionSource == null) {
            return null;
        }

        Dao<?,?> dao = daosMap.get(daoSource);
        if(dao == null) {
            return null;
        }

        try {
            return (Dao<T, Z>) dao;
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Close connection with database
     */
    public void close() {
        if(this.connectionSource == null) {
            return;
        }
        try {
            this.connectionSource.close();
        } catch (Exception ignored) {

        }
    }

    @Nullable
    public JdbcPooledConnectionSource getConnectionSource() {
        return connectionSource;
    }
}
