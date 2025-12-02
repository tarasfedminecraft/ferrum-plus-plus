package io.coconut.ferrum;

import io.coconut.ferrum.utils.Checker;
import io.coconut.ferrum.utils.Formatter;

public class Launcher {
    public static final String VERSION_LIST = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    public static final String GAME_PATH = System.getProperty("user.home") + "/.minecraft";

    private String username;
    private String version;
    private String min;
    private String max;

    public Launcher(String username, String version, String min, String max) {
        setUsername(username);
        setVersion(version);
        this.min = min;
        this.max = max;
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
        if (minV == 0 || maxV == 0 || maxV <= 0) {
                throw new IllegalArgumentException("Error: Invalid memory parameters! Parameters cannot be zero and max cannot be less or equals to min");
        }
        this.min = Formatter.formatMin(minV);
        this.max = Formatter.formatMax(maxV);
    }

    public String getUsername() {
        return username;
    }

    public String getVersion() {
        return version;
    }

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }
}
