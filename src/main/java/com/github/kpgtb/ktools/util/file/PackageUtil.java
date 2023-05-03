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

package com.github.kpgtb.ktools.util.file;

/**
 * Package util helps to provide package name to methods of API
 */
public class PackageUtil {
    private final String mainPackage;
    private final String tag;

    /**
     * Constructor of util
     * @param mainPackage Name of main package (where is main class)
     * @param tag Tag of plugin
     */
    public PackageUtil(String mainPackage, String tag) {
        this.mainPackage = mainPackage;
        this.tag = tag.toLowerCase();
    }

    /**
     * Get sub package
     * @param sub Name of sub package
     * @return full path to sub package
     */
    public String get(String sub) {
        return String.format(mainPackage+".%s", sub);
    }

    /**
     * Get tag of plugin
     * @return tag of plugin
     * @since 1.6.0
     */
    public String tag() {
        return tag;
    }
}
