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

package com.github.kpgtb.ktools.util;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Util to get classes from package
 */
public class ReflectionUtil {
    /**
     * This method returns all classes in specified package
     * @param jarFile JarFile in which is this plugin
     * @param packageName Name of package that have this classes
     * @return Set of classes in package
     */
    public static Set<Class<?>> getAllClassesInPackage(File jarFile, String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        try {
            JarFile file = new JarFile(jarFile);
            for (Enumeration<JarEntry> entry = file.entries(); entry.hasMoreElements();) {
                JarEntry jarEntry = entry.nextElement();
                String name = jarEntry.getName().replace("/", ".");
                if(name.startsWith(packageName) && name.endsWith(".class"))
                    classes.add(Class.forName(name.substring(0, name.length() - 6)));
            }
            file.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * This method returns all classes that extends specified class in specified package
     * @param jarfile JarFile in which is this plugin
     * @param packageName Name of package that have this classes
     * @param abstractClass Class that need to be extended
     * @return Set of classes in package
     */
    public static Set<Class<?>> getAllClassesInPackage(File jarfile, String packageName, Class<?> abstractClass) {
        Set<Class<?>> classes = getAllClassesInPackage(jarfile, packageName);
        Set<Class<?>> result = new HashSet<>();

        classes.forEach(clazz -> {
            if(clazz.getSuperclass().equals(abstractClass) ) {
                result.add(clazz);
            }
        });

        return result;
    }
}
