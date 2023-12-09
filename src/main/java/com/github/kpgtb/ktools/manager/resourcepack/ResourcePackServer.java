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

import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * ResourcePack Self Hosted Server
 * @since 2.3.0
 */
public class ResourcePackServer {
    private final JavaPlugin plugin;
    private HttpServer server;

    public ResourcePackServer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Start server
     * @throws IOException
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(plugin.getConfig().getInt("resourcePackSelfHost.port")), 0);
        server.createContext("/resourcepack.zip", httpExchange -> {
            File folder = new File(plugin.getDataFolder(), "http");
            folder.mkdirs();
            File file = new File(folder, "resourcepack.zip").getCanonicalFile();
            if(file.exists()) {
                httpExchange.sendResponseHeaders(200, 0);
                OutputStream os = httpExchange.getResponseBody();
                FileInputStream fs = new FileInputStream(file);
                final byte[] buffer = new byte[0x10000];
                int count = 0;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer,0,count);
                }
                fs.close();
                os.close();
                return;
            }

            String response = "404 (Not Found)\n";
            httpExchange.sendResponseHeaders(404, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.createContext("/test", httpExchange -> {
            String response = "200 (Ok)\n";
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.setExecutor(null);
        server.start();
    }

    /**
     * Stop server
     */
    public void stop() {
        if(server != null) {
            server.stop(0);
        }
    }
}
