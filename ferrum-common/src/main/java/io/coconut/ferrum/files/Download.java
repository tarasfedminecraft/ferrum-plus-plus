package io.coconut.ferrum.files;

import io.coconut.ferrum.Launcher;
import io.coconut.ferrum.utils.Checker;
import io.coconut.ferrum.utils.Fetcher;
import org.json.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Download {

    private static final ExecutorService downloadExecutor = Executors.newFixedThreadPool(16);
    private static final int MAX_RETRIES = 5;

    public static String findURL(String version) {
        try {
            JSONObject manifest = downloadJSON(Launcher.VERSION_LIST);
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
        Exception lastException = null;
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return Fetcher.fetchJSON(urlV);
            } catch (Exception e) {
                lastException = e;
                if (i < MAX_RETRIES - 1) {
                    
                    long delay = (long) (Math.pow(2, i) * 1000) + (long) (Math.random() * 1000);
                    Thread.sleep(delay);
                }
            }
        }
        throw lastException;
    }

    public static void downloadFile(String urlV, String dest) throws Exception {
        downloadFile(urlV, dest, true);
    }

    public static void downloadFile(String urlV, String dest, boolean verbose) throws Exception {
        File file = new File(dest);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        File tempFile = new File(dest + ".tmp");
        long existingSize = 0;
        if (tempFile.exists()) {
            existingSize = tempFile.length();
        }
        
        URL url = new URL(urlV);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("User-Agent", "Ferrum/1.0");

        if (existingSize > 0) {
            conn.setRequestProperty("Range", "bytes=" + existingSize + "-");
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            throw new IOException("Server returned HTTP " + responseCode + " for " + urlV);
        }

        boolean append = false;
        if (existingSize > 0 && responseCode == HttpURLConnection.HTTP_PARTIAL) {
            append = true;
        } else {
            existingSize = 0;
        }

        try (InputStream is = new BufferedInputStream(conn.getInputStream())) {
            try (FileOutputStream fos = new FileOutputStream(tempFile, append)) {
                byte[] buffer = new byte[16384];
                int bytesRead;
                long totalRead = existingSize;
                long lastPrint = existingSize;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    if (verbose && (totalRead - lastPrint >= 1024 * 1024)) {
                        System.out.print(".");
                        lastPrint = totalRead;
                    }
                }
                if (verbose && totalRead > 1024 * 1024) System.out.println(" Done!");
            }
        } finally {
            conn.disconnect();
        }
        
        Files.move(tempFile.toPath(), Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void downloadFileWithRetry(String urlV, String dest, boolean verbose) throws Exception {
        Exception lastException = null;
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                downloadFile(urlV, dest, verbose);
                return;
            } catch (Exception e) {
                lastException = e;
                if (i < MAX_RETRIES - 1) {
                    String fileName = new File(dest).getName();
                    System.err.println("Retry " + (i + 1) + "/" + MAX_RETRIES + " for " + fileName + " (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
                    long delay = (long) (Math.pow(2, i) * 1000) + (long) (Math.random() * 1000);
                    Thread.sleep(delay);
                }
            }
        }
        throw lastException;
    }


    public static String getLibPath(String name) {
        String[] parts = name.split(":");
        String group = parts[0].replace(".", "/");
        String artifact = parts[1];
        String version = parts[2];
        String classifier = (parts.length > 3) ? "-" + parts[3] : "";
        return group + "/" + artifact + "/" + version + "/" + artifact + "-" + version + classifier + ".jar";
    }

    public static String downloadLibs(JSONObject vInfo, String version) throws Exception {
        JSONArray libraries = vInfo.getJSONArray("libraries");
        String[] cpParts = new String[libraries.length()];
        String nativesDir = Launcher.GAME_PATH + "/natives/" + version;

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < libraries.length(); i++) {
            final int index = i;
            JSONObject lib = libraries.getJSONObject(i);
            
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    if (lib.has("rules")) {
                        if (!Checker.isByRules(lib.getJSONArray("rules"))) {
                            return;
                        }
                    }

                    String name = lib.getString("name");
                    String libPath = "";

                    if (lib.has("downloads")) {
                        JSONObject downloads = lib.getJSONObject("downloads");
                        if (downloads.has("artifact")) {
                            JSONObject artifact = downloads.getJSONObject("artifact");
                            String url = artifact.getString("url");
                            libPath = Launcher.GAME_PATH + "/libraries/" + artifact.getString("path");
                            long expectedSize = artifact.optLong("size", -1);

                            File libFile = new File(libPath);
                            boolean needsDownload = !libFile.exists() || libFile.length() == 0;
                            if (!needsDownload && expectedSize > 0 && libFile.length() != expectedSize) {
                                needsDownload = true;
                            }

                            if (needsDownload) {
                                downloadFileWithRetry(url, libPath, false);
                            }
                            cpParts[index] = libPath;

                            if (name.contains("natives")) {
                                Natives.extractNatives(libPath, nativesDir);
                            }
                        }

                        if (downloads.has("classifiers")) {
                            JSONObject classifiers = downloads.getJSONObject("classifiers");
                            String nativeKey = Natives.getOS();
                            if (nativeKey != null && classifiers.has(nativeKey)) {
                                JSONObject nativeLib = classifiers.getJSONObject(nativeKey);
                                String url = nativeLib.getString("url");
                                String nativePath = Launcher.GAME_PATH + "/libraries/" + nativeLib.getString("path");
                                long expectedSize = nativeLib.optLong("size", -1);

                                File nFile = new File(nativePath);
                                boolean needsDownload = !nFile.exists() || nFile.length() == 0;
                                if (!needsDownload && expectedSize > 0 && nFile.length() != expectedSize) {
                                    needsDownload = true;
                                }

                                if (needsDownload) {
                                    downloadFileWithRetry(url, nativePath, false);
                                }

                                Natives.extractNatives(nativePath, nativesDir);
                            }
                        }
                    } else {
                        String relativePath = getLibPath(name);
                        libPath = Launcher.GAME_PATH + "/libraries/" + relativePath;
                        String url = lib.optString("url", "https:

                        File lFile = new File(libPath);
                        if (!lFile.exists() || lFile.length() == 0) {
                            downloadFileWithRetry(url, libPath, false);
                        }
                        cpParts[index] = libPath;

                        if (lib.has("natives")) {
                            JSONObject natives = lib.getJSONObject("natives");
                            String osName = System.getProperty("os.name").toLowerCase();
                            String osKey = osName.contains("win") ? "windows" : (osName.contains("mac") ? "osx" : "linux");
                            
                            if (natives.has(osKey)) {
                                String classifier = natives.getString(osKey).replace("${arch}", System.getProperty("sun.arch.data.model"));
                                String nativeName = name + ":" + classifier;
                                String nativeRelPath = getLibPath(nativeName);
                                String nativePath = Launcher.GAME_PATH + "/libraries/" + nativeRelPath;
                                String nativeUrl = lib.optString("url", "https:

                                if (!Files.exists(Paths.get(nativePath))) {
                                    downloadFileWithRetry(nativeUrl, nativePath, false);
                                }
                                Natives.extractNatives(nativePath, nativesDir);
                            }
                        }
                    }
                } catch (Exception e) {
                    String name = lib.optString("name", "unknown");
                    System.err.println("Error downloading library " + name + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    throw new RuntimeException("Failed to download library: " + name, e);
                }
            }, downloadExecutor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        StringBuilder classpath = new StringBuilder();
        for (String part : cpParts) {
            if (part != null) {
                classpath.append(part).append(File.pathSeparator);
            }
        }
        return classpath.toString();
    }

    public static void downloadAssets(JSONObject vInfo) throws Exception {
        if (!vInfo.has("assetIndex")) return;

        JSONObject assetIndex = vInfo.getJSONObject("assetIndex");
        String indexId = assetIndex.getString("id");
        String indexUrl = assetIndex.getString("url");
        String indexPath = Launcher.GAME_PATH + "/assets/indexes/" + indexId + ".json";
        
        if (!Files.exists(Paths.get(indexPath)) || new File(indexPath).length() == 0) {
            downloadFileWithRetry(indexUrl, indexPath, true);
        }

        JSONObject indexContent;
        try (InputStream is = new BufferedInputStream(new FileInputStream(indexPath))) {
            indexContent = new JSONObject(new JSONTokener(new InputStreamReader(is, "UTF-8")));
        }
        JSONObject objects = indexContent.getJSONObject("objects");

        boolean isVirtual = indexContent.optBoolean("virtual", false) || indexId.equals("legacy");

        int total = objects.keySet().size();
        AtomicInteger current = new AtomicInteger(0);
        System.out.println("Downloading assets (" + total + " files)...");

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String key : objects.keySet()) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    String hash = objects.getJSONObject(key).getString("hash");
                    long size = objects.getJSONObject(key).optLong("size", -1);
                    String prefix = hash.substring(0, 2);
                    String path = Launcher.GAME_PATH + "/assets/objects/" + prefix + "/" + hash;

                    File assetFile = new File(path);
                    boolean needsDownload = !assetFile.exists() || assetFile.length() == 0;
                    if (!needsDownload && size > 0 && assetFile.length() != size) {
                        needsDownload = true;
                    }

                    if (needsDownload) {
                        String url = "https:
                        try {
                            downloadFileWithRetry(url, path, false);
                        } catch (Exception e) {
                            System.err.println("Failed to download asset: " + key + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
                        }
                    }

                    if (isVirtual) {
                        String virtualPath = Launcher.GAME_PATH + "/assets/virtual/" + indexId + "/" + key;
                        if (indexId.equals("legacy")) {
                            virtualPath = Launcher.GAME_PATH + "/resources/" + key;
                        }
                        
                        File virtualFile = new File(virtualPath);
                        if (!virtualFile.exists()) {
                            virtualFile.getParentFile().mkdirs();
                            Files.copy(Paths.get(path), virtualFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }

                    int done = current.incrementAndGet();
                    if (done % 100 == 0 || done == total) {
                        System.out.println("Progress: " + done + "/" + total);
                    }
                } catch (Exception e) {
                    System.err.println("Unexpected error processing asset " + key + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }, downloadExecutor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        System.out.println("Assets download complete!");
    }

    public static String downloadLogging(JSONObject vInfo) throws Exception {
        if (vInfo.has("logging") && vInfo.getJSONObject("logging").has("client")) {
            JSONObject clientLogging = vInfo.getJSONObject("logging").getJSONObject("client");
            JSONObject file = clientLogging.getJSONObject("file");
            String logPath = Launcher.GAME_PATH + "/assets/log_configs/" + file.getString("id");

            File lFile = new File(logPath);
            if (!lFile.exists() || lFile.length() == 0) {
                downloadFileWithRetry(file.getString("url"), logPath, true);
            }
            return logPath;
        }
        return null;
    }
}