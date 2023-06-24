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

package com.github.kpgtb.ktools.manager.command;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CommandPath implements Cloneable {
    private String pathStr;
    private String[] path;

    public CommandPath() {
        this.pathStr = "";
        this.path = new String[0];
    }

    public CommandPath(String[] path) {
        this.path = path;
        this.generatePathStr();
    }

    public CommandPath(String pathStr) {
        this.pathStr = pathStr;
        this.path = pathStr.split(" ");
    }

    private void generatePathStr() {
        this.pathStr = String.join(" ", this.path);
    }

    public void add(String element) {
        if(element.isEmpty()) {
            return;
        }
        if(element.contains(" ")) {
            for (String el : element.split(" ")) {
                this.add(el);
            }
            return;
        }

        String[] newPath = new String[this.path.length+1];
        for (int i = 0; i < this.path.length; i++) {
            newPath[i] = this.path[i];
        }
        newPath[this.path.length] = element;

        this.path = newPath;
        this.generatePathStr();
    }

    public String getPermissionStr() {
        return String.join(".", path);
    }

    public String getPathStr() {
        return pathStr;
    }

    public String[] getPath() {
        return path;
    }

    @NotNull
    @Override
    public CommandPath clone() {
        try {
            return (CommandPath) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandPath that = (CommandPath) o;
        return Objects.equals(pathStr, that.pathStr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathStr);
    }
}
