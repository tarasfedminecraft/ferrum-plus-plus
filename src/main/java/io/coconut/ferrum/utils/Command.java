package io.coconut.ferrum.utils;

import io.coconut.ferrum.Launcher;
import org.json.JSONObject;

public class Command {
    public static void buildCommand(Launcher launcher, String classpath, JSONObject versionInfo, String assetIndexId) {
        try {
            String mainClass = versionInfo.getString("mainClass");
            String nativesDir = Launcher.GAME_PATH + "/natives/" + launcher.getVersion();

            ProcessBuilder pb = new ProcessBuilder(
                    "java",
                    launcher.getMin(),
                    launcher.getMax(),
                    "-Djava.library.path=" + nativesDir,
                    "-cp", classpath,
                    mainClass,
                    "--username", launcher.getUsername(),
                    "--version", launcher.getVersion(),
                    "--gameDir", Launcher.GAME_PATH,
                    "--assetsDir", Launcher.GAME_PATH + "/assets",
                    "--assetIndex", assetIndexId,
                    "--uuid", "00000000-0000-0000-0000-000000000000",
                    "--accessToken", "0",
                    "--userType", "legacy",
                    "--versionType", "release"
            );

            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            System.err.println("An error occurred while trying to launch the game: " + e.getMessage());
        }
    }
}
