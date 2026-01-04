package io.coconut.ferrum.utils;

import io.coconut.ferrum.Launcher;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Command {
    public static void buildCommand(Launcher launcher, String classpath, JSONObject versionInfo, String assetIndexId, String logConfigPath) {
        try {
            List<String> command = new ArrayList<>();

            
            command.add(launcher.getJavaPath());

            
            command.add(launcher.getMin());
            command.add(launcher.getMax());
            command.add("-XX:+UnlockExperimentalVMOptions");
            command.add("-XX:+UseG1GC");
            command.add("-XX:G1NewSizePercent=20");
            command.add("-XX:G1ReservePercent=20");
            command.add("-XX:MaxGCPauseMillis=50");
            command.add("-XX:G1HeapRegionSize=32M");
            command.add("-Djava.net.preferIPv4Stack=true");

            
            int javaVersion = getJavaVersion();
            if (javaVersion > 8) {
                
                String[] packages = {
                    "java.base/java.lang", "java.base/java.util", "java.base/java.io",
                    "java.base/java.net", "java.base/java.nio", "java.base/java.util.concurrent",
                    "java.base/sun.nio.ch", "java.desktop/sun.awt", "java.desktop/sun.java2d",
                    "java.desktop/sun.awt.X11", "java.desktop/sun.awt.shell", "java.desktop/sun.awt.im",
                    "java.desktop/javax.swing", "java.desktop/javax.swing.tree",
                    "java.desktop/javax.swing.table", "java.desktop/javax.swing.text",
                    "java.desktop/javax.swing.plaf.basic"
                };
                for (String pkg : packages) {
                    command.add("--add-opens");
                    command.add(pkg + "=ALL-UNNAMED");
                }

                if (javaVersion >= 18) {
                    command.add("-Djava.security.manager=allow");
                }
                
                
                command.add("-Djava.util.Arrays.useLegacyMergeSort=true");
            }

            
            command.add("-Dsun.stdout.encoding=UTF-8");
            command.add("-Dsun.stderr.encoding=UTF-8");
            command.add("-Djava.awt.headless=false");

            
            if (logConfigPath != null && versionInfo.has("logging")) {
                JSONObject logging = versionInfo.getJSONObject("logging").getJSONObject("client");
                if (logging.has("argument")) {
                    String logArg = logging.getString("argument");
                    command.add(logArg.replace("${path}", logConfigPath));
                }
            }

            
            String nativesPath = Launcher.GAME_PATH + File.separator + "natives" + File.separator + launcher.getVersion();
            String javaHomeLib = System.getProperty("java.home") + File.separator + "lib";

            
            String libraryPath = nativesPath + File.pathSeparator + javaHomeLib;

            
            String[] commonSubs = {"amd64", "i386", "server"};
            for (String sub : commonSubs) {
                File subDir = new File(javaHomeLib, sub);
                if (subDir.exists()) {
                    libraryPath += File.pathSeparator + subDir.getAbsolutePath();
                }
            }

            String existingJavaLibPath = System.getProperty("java.library.path");
            if (existingJavaLibPath != null && !existingJavaLibPath.isEmpty()) {
                libraryPath += File.pathSeparator + existingJavaLibPath;
            }

            command.add("-Djava.library.path=" + libraryPath);
            command.add("-Dorg.lwjgl.librarypath=" + nativesPath);
            command.add("-Dnet.java.games.input.librarypath=" + nativesPath);

            boolean cpAdded = false;
            if (versionInfo.has("arguments") && versionInfo.getJSONObject("arguments").has("jvm")) {
                JSONArray jvmArgs = versionInfo.getJSONObject("arguments").getJSONArray("jvm");
                for (int i = 0; i < jvmArgs.length(); i++) {
                    Object arg = jvmArgs.get(i);
                    if (arg instanceof String) {
                        String s = (String) arg;
                        if (s.equals("-cp") || s.equals("-classpath")) cpAdded = true;
                        command.add(replacePlaceholders(s, launcher, classpath, versionInfo, assetIndexId));
                    } else if (arg instanceof JSONObject) {
                        JSONObject obj = (JSONObject) arg;
                        if (obj.has("rules") && obj.has("value")) {
                            if (Checker.isByRules(obj.getJSONArray("rules"))) {
                                Object value = obj.get("value");
                                if (value instanceof String) {
                                    String s = (String) value;
                                    if (s.equals("-cp") || s.equals("-classpath")) cpAdded = true;
                                    command.add(replacePlaceholders(s, launcher, classpath, versionInfo, assetIndexId));
                                } else if (value instanceof JSONArray) {
                                    JSONArray values = (JSONArray) value;
                                    for (int j = 0; j < values.length(); j++) {
                                        String s = values.getString(j);
                                        if (s.equals("-cp") || s.equals("-classpath")) cpAdded = true;
                                        command.add(replacePlaceholders(s, launcher, classpath, versionInfo, assetIndexId));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!cpAdded) {
                command.add("-cp");
                command.add(classpath);
            }

            
            command.add(versionInfo.getString("mainClass"));

            
            if (versionInfo.has("arguments") && versionInfo.getJSONObject("arguments").has("game")) {
                JSONArray gameArgs = versionInfo.getJSONObject("arguments").getJSONArray("game");
                for (int i = 0; i < gameArgs.length(); i++) {
                    Object arg = gameArgs.get(i);
                    if (arg instanceof String) {
                        command.add(replacePlaceholders((String) arg, launcher, classpath, versionInfo, assetIndexId));
                    } else if (arg instanceof JSONObject) {
                        JSONObject obj = (JSONObject) arg;
                        if (obj.has("rules") && obj.has("value")) {
                            if (Checker.isByRules(obj.getJSONArray("rules"))) {
                                Object value = obj.get("value");
                                if (value instanceof String) {
                                    command.add(replacePlaceholders((String) value, launcher, classpath, versionInfo, assetIndexId));
                                } else if (value instanceof JSONArray) {
                                    JSONArray values = (JSONArray) value;
                                    for (int j = 0; j < values.length(); j++) {
                                        command.add(replacePlaceholders(values.getString(j), launcher, classpath, versionInfo, assetIndexId));
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (versionInfo.has("minecraftArguments")) {
                String legacyArgs = versionInfo.getString("minecraftArguments");
                for (String arg : legacyArgs.split(" ")) {
                    command.add(replacePlaceholders(arg, launcher, classpath, versionInfo, assetIndexId));
                }
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();

            
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("linux") || os.contains("mac")) {
                String envVar = os.contains("linux") ? "LD_LIBRARY_PATH" : "DYLD_LIBRARY_PATH";
                String existing = System.getenv(envVar);

                
                String newPath = libraryPath;
                if (existing != null && !existing.isEmpty()) {
                    newPath = newPath + File.pathSeparator + existing;
                }
                pb.environment().put(envVar, newPath);
            }
            
            int requiredJava = getRequiredJavaVersion(versionInfo);
            int currentJava = getJavaVersion();
            if (currentJava < requiredJava) {
                System.out.println("[WARNING] This version of Minecraft recommends Java " + requiredJava + ", but you are running Java " + currentJava + ". If it fails, please install a newer JDK.");
            }

            System.out.println("Launching Minecraft " + launcher.getVersion() + "...");
            System.out.println("Full command: " + String.join(" ", command));
            Process process = pb.start();
            process.waitFor();
            System.out.println("Minecraft process finished.");
        } catch (Exception e) {
            System.err.println("An error occurred while trying to launch the game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static int getRequiredJavaVersion(JSONObject versionInfo) {
        if (versionInfo.has("javaVersion")) {
            return versionInfo.getJSONObject("javaVersion").optInt("majorVersion", 8);
        }
        return 8;
    }

    private static int getJavaVersion() {
        String version = System.getProperty("java.specification.version");
        if (version.startsWith("1.")) {
            return Integer.parseInt(version.substring(2));
        }
        return Integer.parseInt(version);
    }

    private static String replacePlaceholders(String arg, Launcher launcher, String classpath, JSONObject versionInfo, String assetIndexId) {
        String assetsRoot = Launcher.GAME_PATH + "/assets";
        
        if (assetIndexId.equals("legacy") || assetIndexId.startsWith("1.7") || assetIndexId.startsWith("1.6")) {
            assetsRoot = Launcher.GAME_PATH + "/resources";
        }

        return arg.replace("${auth_player_name}", launcher.getUsername())
                .replace("${version_name}", launcher.getVersion())
                .replace("${game_directory}", Launcher.GAME_PATH)
                .replace("${assets_root}", assetsRoot)
                .replace("${game_assets}", assetsRoot)
                .replace("${assets_index_name}", assetIndexId)
                .replace("${auth_uuid}", "00000000-0000-0000-0000-000000000000")
                .replace("${auth_access_token}", "0")
                .replace("${auth_session}", "0")
                .replace("${user_properties}", "{}")
                .replace("${user_type}", "mojang")
                .replace("${version_type}", versionInfo.optString("type", "release"))
                .replace("${classpath}", classpath)
                .replace("${natives_directory}", Launcher.GAME_PATH + "/natives/" + launcher.getVersion())
                .replace("${launcher_name}", "ferrum-plus-plus")
                .replace("${launcher_version}", "1.1.0")
                .replace("${library_directory}", Launcher.GAME_PATH + "/libraries")
                .replace("${classpath_separator}", File.pathSeparator)
                .replace("${clientid}", "0")
                .replace("${auth_xuid}", "0")
                .replace("${primary_executable_path}", launcher.getJavaPath())
                .replace("${original_logfilename}", "client.log");
    }
}
