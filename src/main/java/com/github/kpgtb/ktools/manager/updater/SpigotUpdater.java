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

package com.github.kpgtb.ktools.manager.updater;

import com.github.kpgtb.ktools.manager.updater.version.KVersion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Updater from spigotmc
 */
public class SpigotUpdater implements IUpdater {
    private final String resourceID;

    public SpigotUpdater(String resourceID) {
        this.resourceID = resourceID;
    }

    @Override
    public boolean hasUpdate(KVersion version) {
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource="+resourceID);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = bufferedReader.readLine();

            KVersion newVersion = new KVersion(line);
            return newVersion.isNewerThan(version);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getDownloadLink() {
        return "https://www.spigotmc.org/resources/"+resourceID;
    }
}
