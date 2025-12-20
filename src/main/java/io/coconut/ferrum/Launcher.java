package io.coconut.ferrum;

import io.coconut.ferrum.files.Create;
import io.coconut.ferrum.files.Download;
import io.coconut.ferrum.utils.Checker;
import io.coconut.ferrum.utils.Command;
import io.coconut.ferrum.utils.Formatter;
import org.json.JSONObject;

import io.coconut.ferrum.files.Create;
import io.coconut.ferrum.files.Download;
import io.coconut.ferrum.utils.Checker;
import io.coconut.ferrum.utils.Command;
import io.coconut.ferrum.utils.Formatter;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Launcher {
    public static final String VERSION_LIST = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    public static final String GAME_PATH = System.getProperty("user.home") + "/.Fminecraft";

    private String username;
    private String version;
    private String min;
    private String max;


    public Launcher(String username, String version, int minMem, int maxMem) throws Exception {
        setUsername(username);
        setVersion(version);
        setMemory(minMem, maxMem);
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Error: Username cannot be null or empty!");
        }
        this.username = username;
    }

    public void setVersion(String version) {
        if (!Checker.isVersionValid(version)) {
            System.err.println("Error: Invalid version type format! Correct: `X.Y.X`");
        }
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
        Create.createDirs(this.getVersion());

        String versionURL = Download.findURL(this.version);
        JSONObject versionInfo = Download.downloadJSON(versionURL);

        String versionDir = GAME_PATH + "/versions/" + version;
        String jarPath = versionDir + "/" + version + ".jar";
        String clientURL = versionInfo.getJSONObject("downloads").getJSONObject("client").getString("url");

        if (!Files.exists(Paths.get(jarPath))) {
            System.out.println("Downloading client jar...");
            Download.downloadFile(clientURL, jarPath);
        }

        String classpath = Download.downloadLibs(versionInfo, this.version);
        classpath += File.pathSeparator + jarPath;

        String assetIndexID = versionInfo.getJSONObject("assetIndex").getString("id");
        Download.downloadAssets(versionInfo);

        Command.buildCommand(this, classpath, versionInfo, assetIndexID);
    }
}
