package io.coconut.ferrum.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.coconut.ferrum.Launcher.GAME_PATH;

public class Create {
    public static void createDirs() {
        try {
            Files.createDirectories(Paths.get(GAME_PATH + "/versions"));
            Files.createDirectories(Paths.get(GAME_PATH + "/libraries"));
            Files.createDirectories(Paths.get(GAME_PATH + "/assets/indexes"));
            Files.createDirectories(Paths.get(GAME_PATH + "/assets/objects"));
            Files.createDirectories(Paths.get(GAME_PATH + "/natives"));
        } catch (IOException e) {
            System.err.println("An error occurred while trying to create directories");
            e.printStackTrace();
        }
    }
}
