package io.coconut.ferrum.files;

import io.coconut.ferrum.Launcher;
import io.coconut.ferrum.utils.Checker;
import io.coconut.ferrum.utils.Fetcher;
import org.json.*;

import javax.lang.model.type.DeclaredType;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Download {

    public static String findURL(String version) {
        try {
            JSONObject manifest = Fetcher.fetchJSON(Launcher.VERSION_LIST);
            JSONArray versions = manifest.getJSONArray("versions");
            for (int i = 0; i < versions.length(); i++) {
                JSONObject v = versions.getJSONObject(i);
                if (v.getString("id").equals(version)) {
                    return v.getString("url");
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("An error occurred while trying to find URL: " +  e.getMessage());
            return null;
        }
    }

    public static JSONObject downloadJSON(String urlV) throws Exception {
        return Fetcher.fetchJSON(urlV);
    }

    public static void downloadFile(String urlV, String dest) throws Exception {
        File file = new File(dest);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        URL url = new URL(urlV);
        try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
             FileOutputStream fos = new FileOutputStream(dest)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }


    public static String downloadLibs(JSONObject vInfo, String version) throws Exception {
        JSONArray libraries = vInfo.getJSONArray("libraries");
        StringBuilder classpath = new StringBuilder();
        String nativesDir = Launcher.GAME_PATH + "/natives/" + version;

        for (int i = 0; i < libraries.length(); i++) {
            JSONObject lib = libraries.getJSONObject(i);
            if (lib.has("rules")) {
                if (!Checker.isByRules(lib.getJSONArray("rules"))) {
                    continue;
                }
            }

            if (lib.has("downloads")) {
                JSONObject downloads = lib.getJSONObject("downloads");
                if (downloads.has("artifact")) {
                    JSONObject artifact = downloads.getJSONObject("artifact");
                    String libPath = Launcher.GAME_PATH + "/libraries/" + artifact.getString("path");

                    if (!Files.exists(Paths.get(libPath))) {
                        downloadFile(artifact.getString("url"), libPath);
                    }
                    classpath.append(libPath).append(File.pathSeparator);
                }

                if (downloads.has("classifiers")) {
                    JSONObject classifiers = downloads.getJSONObject("classifiers");
                    String nativeKey = Natives.getOS(); // natives-windows, etc.
                    if (nativeKey != null && classifiers.has(nativeKey)) {
                        JSONObject nativeLib = classifiers.getJSONObject(nativeKey);
                        String nativePath = Launcher.GAME_PATH + "/libraries/" + nativeLib.getString("path");

                        if (!Files.exists(Paths.get(nativePath))) {
                            downloadFile(nativeLib.getString("url"), nativePath);
                        }

                        Natives.extractNatives(nativePath, nativesDir);
                    }
                }
            }
        }
        return classpath.toString();
    }

    public static void downloadAssets(JSONObject vInfo) throws Exception {
        JSONObject assetIndex = vInfo.getJSONObject("assetIndex");
        String indexId = assetIndex.getString("id");
        String indexUrl = assetIndex.getString("url");
        String indexPath = Launcher.GAME_PATH + "/assets/indexes/" + indexId + ".json";
        if (!Files.exists(Paths.get(indexPath))) {
            downloadFile(indexUrl, indexPath);
        }

        JSONObject indexContent = new JSONObject(new String(Files.readAllBytes(Paths.get(indexPath))));
        JSONObject objects = indexContent.getJSONObject("objects");

        for (String key : objects.keySet()) {
            String hash = objects.getJSONObject(key).getString("hash");
            String prefix = hash.substring(0, 2);
            String path = Launcher.GAME_PATH + "/assets/objects/" + prefix + "/" + hash;

            if (!Files.exists(Paths.get(path))) {
                String url = "https://resources.download.minecraft.net/" + prefix + "/" + hash;
                try {
                    downloadFile(url, path);
                } catch (Exception e) {
                    System.err.println("Found and skipped invalid asset...");
                }
            }
        }
    }
}