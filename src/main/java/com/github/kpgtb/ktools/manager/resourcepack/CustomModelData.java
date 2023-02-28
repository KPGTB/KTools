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

package com.github.kpgtb.ktools.manager.resourcepack;

import org.bukkit.Material;

import java.io.File;

/**
 * Object that contains information about custom model data
 */
public class CustomModelData {
    private final File imageFile;
    private final Material material;
    private final int model;

    public CustomModelData(File imageFile, Material material, int model) {
        this.imageFile = imageFile;
        this.material = material;
        this.model = model;
    }

    public File getImageFile() {
        return imageFile;
    }

    public Material getMaterial() {
        return material;
    }

    public int getModel() {
        return model;
    }
}
