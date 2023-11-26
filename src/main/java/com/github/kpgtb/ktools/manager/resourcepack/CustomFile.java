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

import lombok.Getter;

import java.io.File;

/**
 * Object that contains information about custom resourcepack files
 */
@Getter
public class CustomFile {
    private final File file;
    private final String destination;
    private final boolean alwaysReplace;

    public CustomFile(File file, String destination, boolean alwaysReplace) {
        this.file = file;
        this.destination = destination;
        this.alwaysReplace = alwaysReplace;
    }
}
