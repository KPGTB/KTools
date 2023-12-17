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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;

import java.lang.reflect.Type;

public class GsonAdapterManager {
    private static final GsonAdapterManager INSTANCE = new GsonAdapterManager();
    private final GsonBuilder gsonBuilder;

    private GsonAdapterManager() {
        gsonBuilder = new GsonBuilder();
    }

    public GsonAdapterManager registerAdapter(Type clazz, TypeAdapter<?> adapter) {
        gsonBuilder.registerTypeAdapter(clazz, adapter);
        return INSTANCE;
    }

    public Gson getGson() {
        return gsonBuilder.create();
    }

    public static GsonAdapterManager getInstance() {
        return INSTANCE;
    }
}
