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

/**
 * Interface with update checkers
 */
public interface IUpdater {
    /**
     * Check if there are some updates of this plugin
     * @param version Version of plugin
     * @return true if there are some updates
     */
    boolean hasUpdate(double version);

    /**
     * Check download link to update
     * @return Download link to update
     */
    String getDownloadLink();
}
