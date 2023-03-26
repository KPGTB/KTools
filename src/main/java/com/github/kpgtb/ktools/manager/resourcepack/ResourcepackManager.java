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
import com.github.kpgtb.ktools.util.FontWidth;
import com.google.gson.*;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ResourceManager handles process of creating resourcepack with custom chars and custom models.
 */
public class ResourcepackManager {
    private final JavaPlugin plugin;
    private final DebugManager debug;
    private final CacheManager cache;

    private File texturesFolder;
    private boolean required;

    private final ArrayList<CustomChar> customChars;
    private final ArrayList<CustomModelData> customModels;
    private final ArrayList<String> plugins;

    /**
     * Constructor of ResourcepackManager
     * @param plugin Instance of plugin
     * @param debug Instance of DebugManager
     * @param cache Instance of CacheManager
     */
    public ResourcepackManager(JavaPlugin plugin, DebugManager debug, CacheManager cache) {
        this.plugin = plugin;
        this.debug = debug;
        this.cache = cache;
        this.required = false;

        this.customChars = new ArrayList<>();
        this.customModels = new ArrayList<>();
        this.plugins = new ArrayList<>();
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
    public void registerCustomChar(String pluginName, String character, String imageName, InputStream image, int height, int ascent, int width) {
        if(!isEnabled()) {
            return;
        }
        File imageFile = this.saveImage(image,imageName,pluginName);
        if(imageFile == null) {
            return;
        }
        CustomChar customChar = new CustomChar(pluginName,imageFile,character,height,ascent);
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
     */
    public void registerCustomModelData(String pluginName, int model, String imageName, InputStream image, Material material) {
        if(!isEnabled()) {
            return;
        }
        File imageFile = this.saveImage(image,imageName,pluginName);
        if(imageFile == null) {
            return;
        }
        CustomModelData customModelData = new CustomModelData(imageFile,material,model);
        this.customModels.add(customModelData);
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
     * Check plugins that require resourcepack manager
     * @return String with plugins and versions
     */
    private String getPluginsString() {
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
        return urlExists(urlString);
    }

    /**
     * Generate resourcepack
     */
    public void prepareResourcepack() {
        if(isResourcepackLatest() || !isEnabled()) {
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
            packObj2.addProperty("description", "Auto generated resourcepack that is a part of Ktools. It also support NegativeSpaceFont by AmberW");

            packObj.add("pack", packObj2);

            FileWriter packWriter = new FileWriter(packPath);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(packObj, packWriter);
            packWriter.close();

            File itemFolder = new File(tempFolder, "assets\\minecraft\\textures\\item\\");
            itemFolder.mkdirs();
            File modelsFolder = new File(tempFolder, "assets\\minecraft\\models\\item\\");
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
                        predicateObj.addProperty("model", "item/" + mat.name().toLowerCase() + "/" + model);
                        overrideObj.add("predicate", predicateObj);
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

            File assetsFolder = new File(tempFolder, "assets\\");
            assetsFolder.mkdirs();
            File fontFolder = new File(tempFolder, "assets\\minecraft\\font\\");
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

    // Private utils

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
        try {
            String boundary = Long.toHexString(System.currentTimeMillis());

            HttpURLConnection connection = (HttpURLConnection) new URL("https://transfer.sh/").openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream output = connection.getOutputStream();
            FileInputStream inputStream = new FileInputStream(fileToUpload);

            output.write(("--" + boundary + "\r\n").getBytes());
            output.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileToUpload.getName() + "\"\r\n").getBytes());
            output.write(("Content-Type: " + URLConnection.guessContentTypeFromName(fileToUpload.getName()) + "\r\n").getBytes());
            output.write("\r\n".getBytes());

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            inputStream.close();

            output.write(("\r\n--" + boundary + "--\r\n").getBytes());

            int status = connection.getResponseCode();

            if (status == HttpURLConnection.HTTP_OK) {

                InputStream input = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String response = reader.readLine();

                response = response.replace("transfer.sh/", "transfer.sh/get/");
                input.close();
                return response;

            } else {
                throw new IOException("Server returned HTTP response code: " + status);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private boolean urlExists(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false;
        }
    }
    private File saveImage(InputStream stream, String imageName, String pluginName) {
        File folder = new File(this.texturesFolder, pluginName);
        if(!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder, imageName + ".png");
        if(file.exists()) {
            return file;
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
