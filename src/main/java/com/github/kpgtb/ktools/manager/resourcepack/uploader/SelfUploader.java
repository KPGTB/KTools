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

package com.github.kpgtb.ktools.manager.resourcepack.uploader;

import com.github.kpgtb.ktools.util.url.UrlUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class SelfUploader implements IUploader{
    private final String url;
    private final String testUrl;
    private final JavaPlugin plugin;

    public SelfUploader(JavaPlugin plugin) {
        this.plugin = plugin;
        String host = plugin.getConfig().getString("resourcePackSelfHost.host");
        int port = plugin.getConfig().getInt("resourcePackSelfHost.port");
        if(plugin.getConfig().getBoolean("resourcePackSelfHost.usePortInUrl")) {
            host += ":" + port;
        }
        this.url = host + "/resourcepack.zip";
        this.testUrl = host + "/test";
    }

    @Override
    public String uploadFile(File fileToUpload)  {
        File folder = new File(plugin.getDataFolder(), "http");
        folder.mkdirs();
        File newPath = new File(folder, "resourcepack.zip");
        try {
            Files.copy(fileToUpload.toPath(), newPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.url;
    }

    @Override
    public boolean test() {
        return testFile(this.testUrl);
    }

    @Override
    public boolean testFile(String url) {
        return UrlUtil.urlExists(url);
    }
}
