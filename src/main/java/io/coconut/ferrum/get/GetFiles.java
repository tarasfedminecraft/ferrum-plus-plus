package io.coconut.ferrum.get;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;

import org.json.*;

public class GetFiles {
    private static final String MINECRAFT_DIR = System.getProperty("user.home") + "/.minecraft";
    private static final String VERSIONS_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    private static void createDirectories() throws IOException {
        Files.createDirectories(Paths.get(MINECRAFT_DIR + "/versions"));
        Files.createDirectories(Paths.get(MINECRAFT_DIR + "/libraries"));
        Files.createDirectories(Paths.get(MINECRAFT_DIR + "/assets/indexes"));
        Files.createDirectories(Paths.get(MINECRAFT_DIR + "/assets/objects"));
        Files.createDirectories(Paths.get(MINECRAFT_DIR + "/natives"));
    }

    private static JSONObject downloadJSON(String urlString) throws Exception {
        URL url = new URL(urlString);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder json = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            json.append(line);
        }
        reader.close();

        return new JSONObject(json.toString());
    }

    private static void downloadFile(String urlString, String destination) throws Exception {
        Files.createDirectories(Paths.get(destination).getParent());

        URL url = new URL(urlString);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(destination);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }
}
