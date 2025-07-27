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

import com.github.kpgtb.ktools.manager.cache.CacheManager;
import com.github.kpgtb.ktools.manager.debug.DebugManager;
import com.github.kpgtb.ktools.manager.debug.DebugType;
import com.github.kpgtb.ktools.manager.resourcepack.uploader.*;
import com.github.kpgtb.ktools.util.ui.FontWidth;
import com.google.gson.*;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ResourceManager handles process of creating resourcepack with custom chars and custom models.
 */
public class ResourcePackManager {
    private final JavaPlugin plugin;
    private final DebugManager debug;
    private final CacheManager cache;

    private File texturesFolder;
    private boolean required;
    private boolean spaces;

    private final ArrayList<CustomChar> customChars;
    private final ArrayList<CustomModelData> customModels;
    private final ArrayList<CustomFile> customFiles;
    private final ArrayList<String> plugins;

    private IUploader uploader;

    /**
     * Constructor of ResourcepackManager
     * @param plugin Instance of plugin
     * @param debug Instance of DebugManager
     * @param cache Instance of CacheManager
     */
    public ResourcePackManager(JavaPlugin plugin, DebugManager debug, CacheManager cache) {
        this.plugin = plugin;
        this.debug = debug;
        this.cache = cache;
        this.required = false;
        this.spaces = false;
        this.uploader = null;

        this.customChars = new ArrayList<>();
        this.customModels = new ArrayList<>();
        this.customFiles = new ArrayList<>();
        this.plugins = new ArrayList<>();
    }

    /**
     * Register NegativeSpaces Resource Pack
     * @since 2.1.0
     */
    public void registerSpaces() {
        this.spaces = true;
    }

    /**
     * Check if NegativeSpaces are registered
     * @since 2.1.0
     */
    public boolean areSpacesRegistered() {
        return this.spaces;
    }

    /**
     * Check if resourcepack is required by other plugins and if is enabled in config
     * @return true if is required and enabled
     */
    public boolean isEnabled() {
        return required && plugin.getConfig().getBoolean("resourcepack");
    }

    /**
     * Mark resourcepack manager as required
     * @param required boolean
     */
    public void setRequired(boolean required) {
        this.required = required;
        if(required) {
            File dataFolder = plugin.getDataFolder();
            dataFolder.mkdirs();
            this.texturesFolder = new File(dataFolder, "textures");
            this.texturesFolder.mkdirs();
        }
    }

    /**
     * Register custom character to resourcepack
     * @param pluginName Name of plugin (and folder that will contain texture)
     * @param character Character represented as String
     * @param imageName Name of image
     * @param image InputStream with image
     * @param height Height of char
     * @param ascent Ascent of char
     * @param width Width of char
     */
    public void registerCustomChar(String pluginName, String character, String imageName, InputStream image, int height, int ascent, double width) {
        registerCustomChar(pluginName, character, imageName, image, height, ascent, width,false);
    }

    /**
     * Register custom model data to resourcepack
     * @param pluginName Name of plugin (and folder that will contain texture)
     * @param model Custom model data
     * @param imageName Name of image
     * @param image InputStream with image
     * @param material Material that will have custom model data
     */
    public void registerCustomModelData(String pluginName, int model, String imageName, InputStream image, Material material) {
        registerCustomModelData(pluginName, model, imageName, image, material,false);
    }

    /**
     * Register custom file to resourcepack
     * @param pluginName Name of plugin (and folder that will contain texture)
     * @param destination Path to file
     * @param fileName Name of file
     * @param file InputStream with file
     */
    public void registerCustomFile(String pluginName, String destination, String fileName, InputStream file) {
        registerCustomFile(pluginName,destination,fileName,file,false);
    }

    /**
     * Register custom character to resourcepack
     * @param pluginName Name of plugin (and folder that will contain texture)
     * @param character Character represented as String
     * @param imageName Name of image
     * @param image InputStream with image
     * @param height Height of char
     * @param ascent Ascent of char
     * @param width Width of char
     * @param alwaysReplace True if texture should be always replaced
     */
    public void registerCustomChar(String pluginName, String character, String imageName, InputStream image, int height, int ascent, double width, boolean alwaysReplace) {
        File imageFile = this.saveFile(image,imageName,pluginName,alwaysReplace);
        if(imageFile == null) {
            return;
        }
        CustomChar customChar = new CustomChar(pluginName,imageFile,character,height,ascent, alwaysReplace);
        this.customChars.add(customChar);
        FontWidth.registerCustomChar(character.charAt(0), width);
    }

    /**
     * Register custom model data to resourcepack
     * @param pluginName Name of plugin (and folder that will contain texture)
     * @param model Custom model data
     * @param imageName Name of image
     * @param image InputStream with image
     * @param material Material that will have custom model data
     * @param alwaysReplace True if texture should be always replaced
     */
    public void registerCustomModelData(String pluginName, int model, String imageName, InputStream image, Material material, boolean alwaysReplace) {
        File imageFile = this.saveFile(image,imageName,pluginName,alwaysReplace);
        if(imageFile == null) {
            return;
        }
        CustomModelData customModelData = new CustomModelData(imageFile,material,model, alwaysReplace);
        this.customModels.add(customModelData);
    }

    /**
     * Register custom file to resourcepack
     * @param pluginName Name of plugin (and folder that will contain texture)
     * @param destination Path to file
     * @param fileName Name of file
     * @param file InputStream with file
     * @param alwaysReplace True if texture should be always replaced
     */
    public void registerCustomFile(String pluginName, String destination, String fileName, InputStream file, boolean alwaysReplace) {
        File cFile = this.saveFile(file, fileName, pluginName,alwaysReplace);
        if(cFile == null) {
            return;
        }
        CustomFile customFile= new CustomFile(cFile, destination, alwaysReplace);
        this.customFiles.add(customFile);
    }

    /**
     * Register plugin in resourcepack manager to check updates
     * @param pluginName Name of plugin
     * @param version Version of plugin
     */
    public void registerPlugin(String pluginName, String version) {
        this.plugins.add(pluginName+"["+version+"]");
    }

    /**
     * Check if plugin is registered
     * @param pluginName Name of plugin
     * @param version Version of plugin
     * @return true if is registered
     */
    public boolean isPluginRegistered(String pluginName, String version) {
        return this.plugins.contains(pluginName+"["+version+"]");
    }

    /**
     * Check plugins that require resourcepack manager
     * @return String with plugins and versions
     */
    public String getPluginsString() {
        Collections.sort(this.plugins);
        StringBuilder builder = new StringBuilder();
        this.plugins.forEach(s -> {
            builder.append(s)
                    .append("|");
        });
        return builder.toString();
    }

    /**
     * Check if resourcepack has the latest version
     * @return true if is latest
     */
    private boolean isResourcepackLatest() {
        String urlString = cache.getServerData("ktools", "resourcepackUrl", String.class);
        if(urlString == null || urlString.isEmpty()) {
            return false;
        }
        String versionTag = cache.getServerData("ktools", "resourcepackTag", String.class);
        if(versionTag == null || versionTag.isEmpty()) {
            return false;
        }
        if(!versionTag.equalsIgnoreCase(getPluginsString())) {
            return false;
        }
        if(uploader == null) {
            return false;
        }
        return uploader.testFile(urlString);
    }

    /**
     * Generate resourcepack
     */
    public void prepareResourcepack(boolean force) {
        if(this.uploader == null) {
            List<IUploader> uploaders = new ArrayList<>();
            if(plugin.getConfig().getBoolean("resourcePackSelfHost.enabled")) {
                uploaders.add(new SelfUploader(plugin));
            }

            uploaders.add(new KpgUploader());
            uploaders.add(new TransferShUploader());
            uploaders.add(new OshiAtUploader());

            for (IUploader uploader : uploaders) {
                if(uploader.test()) {
                    debug.sendInfo(DebugType.RESOURCEPACK, "Selected uploader -> " + uploader.getClass().getSimpleName());
                    this.uploader = uploader;
                    break;
                }

                debug.sendWarning(DebugType.RESOURCEPACK, "Uploader not works -> " + uploader.getClass().getSimpleName());
            }
        }

        if(!force && (isResourcepackLatest() || !isEnabled())) {
            return;
        }

        long millis = System.currentTimeMillis();
        this.debug.sendInfo(DebugType.RESOURCEPACK, "Preparing resourcepack...");

        try {
            File tempFolder = new File(plugin.getDataFolder(), "temp");
            if (tempFolder.exists()) {
                tempFolder.delete();
            }
            tempFolder.mkdirs();

            // pack.mcmeta
            String packPath = tempFolder.getAbsolutePath() + File.separator + "pack.mcmeta";
            JsonObject packObj = new JsonObject();

            JsonObject packObj2 = new JsonObject();
            packObj2.addProperty("pack_format", 4);
            JsonArray supportedArray = new JsonArray();
            supportedArray.add(4);
            supportedArray.add(81);
            packObj2.add("supported_formats", supportedArray);
            packObj2.addProperty("description", "Auto generated resourcepack that is a part of Ktools. It also support NegativeSpaceFont by AmberW");

            JsonObject ov1 = new JsonObject();
            JsonArray ov1Arr = new JsonArray();
            ov1Arr.add(25);
            ov1Arr.add(42);
            ov1.add("formats", ov1Arr);
            ov1.addProperty("directory", "psns_25_42");

            JsonObject ov2 = new JsonObject();
            JsonArray ov2Arr = new JsonArray();
            ov2Arr.add(16);
            ov2Arr.add(24);
            ov2.add("formats", ov2Arr);
            ov2.addProperty("directory", "psns_16_24");

            JsonArray entriesArr = new JsonArray();
            entriesArr.add(ov1);
            entriesArr.add(ov2);

            JsonObject overlays = new JsonObject();
            overlays.add("entries", entriesArr);

            packObj.add("pack", packObj2);
            packObj.add("overlays", overlays);

            FileWriter packWriter = new FileWriter(packPath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(packObj, packWriter);
            packWriter.close();

            this.customFiles.forEach(customFile -> {
                try {
                    File folder = new File(tempFolder, customFile.getDestination());
                    folder.mkdirs();
                    File file = new File(folder, customFile.getFile().getName());
                    file.createNewFile();
                    Files.copy(customFile.getFile().toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            /*File shaderFolder = new File(tempFolder, "assets\\minecraft\\shaders\\core\\");
            shaderFolder.mkdirs();
            plugin.saveResource("txt/rendertype_text.vsh", true);
            File savedShader = new File(plugin.getDataFolder(), "txt/rendertype_text.vsh");
            File shaderInFolder = new File(shaderFolder, "rendertype_text.vsh");
            Files.move(savedShader.toPath(), shaderInFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);*/
            
            File itemFolder = new File(tempFolder, "assets"+ File.separator +"minecraft"+ File.separator +"textures"+ File.separator +"item" + File.separator);
            itemFolder.mkdirs();
            File modelsFolder = new File(tempFolder, "assets"+ File.separator +"minecraft"+ File.separator +"models"+ File.separator +"item" + File.separator);
            modelsFolder.mkdirs();
            HashMap<Material, ArrayList<Integer>> modelsToJson = new HashMap<>();

            this.customModels.forEach(model -> {
                try {
                    File materialFolder = new File(itemFolder, model.getMaterial().name().toLowerCase());
                    materialFolder.mkdirs();
                    File itemTexture = new File(materialFolder, model.getModel() + ".png");
                    itemTexture.createNewFile();
                    Files.copy(model.getImageFile().toPath(), itemTexture.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    if(!modelsToJson.containsKey(model.getMaterial())) {
                        modelsToJson.put(model.getMaterial(), new ArrayList<>());
                    }
                    ArrayList<Integer> models = modelsToJson.get(model.getMaterial());
                    models.add(model.getModel());
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            });
            modelsToJson.forEach((mat, models) -> {
                Collections.sort(models);
                try {
                    String modelPath = modelsFolder.getAbsolutePath() + File.separator + mat.name().toLowerCase() + ".json";
                    JsonObject modelObj = new JsonObject();

                    modelObj.addProperty("parent", "item/handheld");

                    JsonObject texturesObj = new JsonObject();
                    texturesObj.addProperty("layer0", "item/" + mat.name().toLowerCase());
                    modelObj.add("textures", texturesObj);

                    JsonArray overridesArr = new JsonArray();
                    models.forEach(model -> {
                        JsonObject overrideObj = new JsonObject();
                        JsonObject predicateObj = new JsonObject();
                        predicateObj.addProperty("custom_model_data", model);
                        overrideObj.add("predicate", predicateObj);
                        overrideObj.addProperty("model", "item/" + mat.name().toLowerCase() + "/" + model);
                        overridesArr.add(overrideObj);
                    });
                    modelObj.add("overrides", overridesArr);

                    FileWriter modelWriter = new FileWriter(modelPath);
                    Gson modelGson = new GsonBuilder().setPrettyPrinting().create();
                    modelGson.toJson(modelObj, modelWriter);
                    modelWriter.close();

                    File matFolder = new File(modelsFolder, mat.name().toLowerCase()+File.separator);
                    matFolder.mkdirs();
                    models.forEach(model -> {
                        try {
                            String modelJsonPath = matFolder.getAbsolutePath() + File.separator + model + ".json";
                            JsonObject modelJsonObj = new JsonObject();

                            modelJsonObj.addProperty("parent", "item/handheld");

                            JsonObject modelTexturesObj = new JsonObject();
                            modelTexturesObj.addProperty("layer0", "item/" + mat.name().toLowerCase() + "/" + model);
                            modelJsonObj.add("textures", modelTexturesObj);

                            FileWriter modelJsonWriter = new FileWriter(modelJsonPath);
                            Gson modelJsonGson = new GsonBuilder().setPrettyPrinting().create();
                            modelJsonGson.toJson(modelJsonObj, modelJsonWriter);
                            modelJsonWriter.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            });

            File assetsFolder = new File(tempFolder, "assets" + File.separator);
            assetsFolder.mkdirs();
            File fontFolder = new File(tempFolder, "assets"+File.separator+"minecraft"+File.separator+"font"+File.separator);
            fontFolder.mkdirs();

            String fontPath = fontFolder.getAbsolutePath() + File.separator + "default.json";
            JsonObject fontObj = new JsonObject();
            JsonArray fontArr = new JsonArray();

            this.customChars.forEach(customChar -> {
                try {
                    File pluginFolder = new File(assetsFolder, customChar.getPluginName().toLowerCase() + File.separator + "textures");
                    pluginFolder.mkdirs();
                    File textureFile = new File(pluginFolder, customChar.getImageFile().getName().toLowerCase());
                    textureFile.createNewFile();
                    Files.copy(customChar.getImageFile().toPath(), textureFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    JsonObject charObj = new JsonObject();
                    charObj.addProperty("type", "bitmap");
                    charObj.addProperty("file", customChar.getPluginName().toLowerCase() + ":" + textureFile.getName());
                    charObj.addProperty("ascent", customChar.getAscent());
                    charObj.addProperty("height", customChar.getHeight());
                    JsonArray charsArr = new JsonArray();
                    charsArr.add(new JsonPrimitive(customChar.getCharacter()).getAsString());
                    charObj.add("chars", charsArr);
                    fontArr.add(charObj);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            });

            fontObj.add("providers", fontArr);

            try (OutputStreamWriter file = new OutputStreamWriter(new FileOutputStream(fontPath), StandardCharsets.UTF_8)) {
                new Gson().toJson(fontObj, file);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            File zip = this.folderToZip(tempFolder, "resourcepack.zip");
            String link = this.uploadFile(zip);
            if(link == null) {
                return;
            }
            this.cache.setServerData("ktools", "resourcepackUrl", link);
            this.cache.setServerData("ktools", "resourcepackTag", getPluginsString());

            deleteFolder(tempFolder);
            zip.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        this.debug.sendInfo(DebugType.RESOURCEPACK, "Prepared resourcepack in " + (System.currentTimeMillis() - millis) + "ms.");
    }


    private File folderToZip(File folder, String name) {
        try {
            File zipFile = new File(plugin.getDataFolder(), name);
            zipFile.createNewFile();

            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            addFolderToZip("", folder, zos);

            zos.close();
            fos.close();

            return zipFile;
        } catch (Exception e) {
            return null;
        }
    }
    private void addFolderToZip(String parentPath, File folder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                String path = parentPath + file.getName() + "/";
                ZipEntry zipEntry = new ZipEntry(path);
                zos.putNextEntry(zipEntry);
                addFolderToZip(path, file, zos);
                zos.closeEntry();
            } else {
                ZipEntry zipEntry = new ZipEntry(parentPath + file.getName());
                zos.putNextEntry(zipEntry);

                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }

                fis.close();
                zos.closeEntry();
            }
        }
    }

    private String uploadFile(File fileToUpload) {
        if(uploader == null) {
            debug.sendWarning(DebugType.RESOURCEPACK, "None of uploaders works!", true);
            return "";
        }
        return uploader.uploadFile(fileToUpload);
    }

    private File saveFile(InputStream stream, String fileName, String pluginName, boolean alawysRepalce) {
        File folder = new File(this.texturesFolder, pluginName);
        if(!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder, fileName);
        if(file.exists()) {
            if(!alawysRepalce) {
                return file;
            }
            file.delete();
        }
        try {
            file.createNewFile();
            OutputStream outputStream = Files.newOutputStream(file.toPath());

            // Read data from the InputStream and write it to the OutputStream until the end of the stream is reached
            byte[] buffer = new byte[1024];
            int length;
            while ((length = stream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Close both the InputStream and OutputStream to release any resources they are using
            stream.close();
            outputStream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void deleteFolder(File folder) {
        if(folder == null) {
            return;
        }
        if (folder.isDirectory()) {
            // recursively delete all files and subfolders
            for (File file : folder.listFiles()) {
                deleteFolder(file);
            }
        }

        // delete the folder itself
        folder.delete();
    }
}
