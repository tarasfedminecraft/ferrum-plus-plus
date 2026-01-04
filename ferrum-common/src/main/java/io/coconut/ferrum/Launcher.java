
package io.coconut.ferrum;

import io.coconut.ferrum.files.Create;
import io.coconut.ferrum.files.Download;
import io.coconut.ferrum.utils.Command;
import io.coconut.ferrum.utils.Formatter;
import org.json.JSONObject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class Launcher {
    public static final String VERSION_LIST = "https:
    public static final String GAME_PATH = System.getProperty("user.home") + File.separator + ".Fminecraft";

    private String username;
    private String version;
    private String min = Formatter.formatMin(1);
    private String max = Formatter.formatMax(2);
    private String javaPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + (System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java");

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Error: Username cannot be null or empty!");
        }
        this.username = username;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public String getJavaPath() {
        return javaPath;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setMemory(int minV, int maxV) {
        if (minV <= 0 || maxV <= 0 || maxV < minV) {
            this.min = Formatter.formatMin(1);
            this.max = Formatter.formatMax(2);
        } else {
            this.min = Formatter.formatMin(minV);
            this.max = Formatter.formatMax(maxV);
        }
    }

    public String getUsername() { return username; }
    public String getVersion() { return version; }
    public String getMin() { return min; }
    public String getMax() { return max; }

    public void launch() throws Exception {
        System.setProperty("http.maxConnections", "50");

        if (this.username == null || this.username.isEmpty()) {
            throw new Exception("Username is not set!");
        }
        if (this.version == null || this.version.isEmpty()) {
            throw new Exception("Version is not set!");
        }

        Create.createDirs(this.getVersion());

        String versionURL = Download.findURL(this.version);
        if (versionURL == null) {
            throw new Exception("Version not found!");
        }
        JSONObject versionInfo = Download.downloadJSON(versionURL);

        String versionDir = GAME_PATH + "/versions/" + version;
        String jarPath = versionDir + "/" + version + ".jar";
        JSONObject clientDownload = versionInfo.getJSONObject("downloads").getJSONObject("client");
        String clientURL = clientDownload.getString("url");
        long expectedSize = clientDownload.optLong("size", -1);

        final String currentVersion = this.version;
        
        CompletableFuture<Void> jarFuture = CompletableFuture.runAsync(() -> {
            try {
                File jarFile = new File(jarPath);
                boolean needsDownload = !jarFile.exists() || jarFile.length() == 0;
                
                if (!needsDownload && expectedSize > 0 && jarFile.length() != expectedSize) {
                    System.out.println("Client jar size mismatch (found " + jarFile.length() + ", expected " + expectedSize + "). Redownloading...");
                    needsDownload = true;
                }

                if (needsDownload) {
                    System.out.println("Downloading client jar...");
                    Download.downloadFileWithRetry(clientURL, jarPath, true);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to download client jar", e);
            }
        });

        CompletableFuture<String> libsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                String cp = Download.downloadLibs(versionInfo, currentVersion);
                System.out.println("Libraries: Done!");
                return cp;
            } catch (Exception e) {
                throw new RuntimeException("Failed to download libraries", e);
            }
        });

        CompletableFuture<Void> assetsFuture = CompletableFuture.runAsync(() -> {
            try {
                if (versionInfo.has("assetIndex")) {
                    Download.downloadAssets(versionInfo);
                    System.out.println("Assets: Done!");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to download assets", e);
            }
        });

        CompletableFuture<String> logFuture = CompletableFuture.supplyAsync(() -> {
            try {
                String path = Download.downloadLogging(versionInfo);
                if (path != null) System.out.println("Logging config: Done!");
                return path;
            } catch (Exception e) {
                throw new RuntimeException("Failed to download logging config", e);
            }
        });

        CompletableFuture.allOf(jarFuture, libsFuture, assetsFuture, logFuture).join();

        String libsCp = libsFuture.join();
        String classpath;
        if (libsCp.isEmpty()) {
            classpath = jarPath;
        } else {
            classpath = libsCp + jarPath;
        }
        String logConfigPath = logFuture.join();

        String assetIndexID = "";
        if (versionInfo.has("assetIndex")) {
            assetIndexID = versionInfo.getJSONObject("assetIndex").getString("id");
        }

        Command.buildCommand(this, classpath, versionInfo, assetIndexID, logConfigPath);
    }
}
