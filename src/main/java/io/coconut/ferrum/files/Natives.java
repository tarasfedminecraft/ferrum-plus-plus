package io.coconut.ferrum.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Natives {
    public static String getOS() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return "natives-windows";
        } else if (os.contains("nux")) {
            return "natives-linux";
        } else if (os.contains("mac")) {
            return "natives-macos";
        }

        return null;
    }

    public static void extractNatives(String jarPath, String dir) throws Exception {
        Files.createDirectories(Paths.get(dir));

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(jarPath))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];

            while((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                if (name.startsWith("META-INF/") || name.contains(".git") || name.contains(".sha1")) {
                    continue;
                }

                if (name.endsWith(".dll") || name.endsWith(".so") || name.endsWith(".dylib") || name.endsWith(".jnilib")) {
                    File output = new File(dir, new File(name).getName());

                    try (FileOutputStream fos = new FileOutputStream(output)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }

                zis.closeEntry();
            }
        }
    }
}
