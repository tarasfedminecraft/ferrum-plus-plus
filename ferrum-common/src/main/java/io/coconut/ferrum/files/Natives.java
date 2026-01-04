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

    public static synchronized void extractNatives(String jarPath, String dir) throws Exception {
        Files.createDirectories(Paths.get(dir));
        boolean is64 = System.getProperty("sun.arch.data.model").equals("64");

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(jarPath))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];

            while((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                if (name.startsWith("META-INF/") || name.contains(".git") || name.contains(".sha1")) {
                    continue;
                }

                if (name.endsWith(".dll") || name.endsWith(".so") || name.endsWith(".dylib") || name.endsWith(".jnilib")) {
                    String fileName = new File(name).getName();
                    File output = new File(dir, fileName);

                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    byte[] content = baos.toByteArray();

                    boolean is64Lib = fileName.contains("64") || fileName.contains("x64") || fileName.contains("amd64");

                    if (is64) {
                        if (is64Lib) {
                            
                            Files.write(output.toPath(), content);
                            
                            String baseName = fileName.replaceAll("(64|x64|amd64)\\.", ".");
                            if (!baseName.equals(fileName)) {
                                Files.write(new File(dir, baseName).toPath(), content);
                            }
                        } else {
                            
                            
                            
                            if (!output.exists()) {
                                Files.write(output.toPath(), content);
                            }
                        }
                    } else {
                        
                        if (!is64Lib) {
                            Files.write(output.toPath(), content);
                        }
                    }
                }

                zis.closeEntry();
            }
        }
    }
}
