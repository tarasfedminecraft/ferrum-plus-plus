package io.coconut.ferrum.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class Checker {
    public static boolean isVersionValid(String versionStr) {
        
        return versionStr != null && versionStr.matches("[a-zA-Z0-9._-]+");
    }

    public static boolean isByRules(JSONArray rules) {
        String os = System.getProperty("os.name").toLowerCase();

        boolean allow = false;

        for (int i = 0; i < rules.length(); i++) {
            JSONObject rule = rules.getJSONObject(i);
            String action = rule.getString("action");

            boolean matches = true;

            if (rule.has("os")) {
                JSONObject osRule = rule.getJSONObject("os");
                String osName = osRule.optString("name", "");
                String osVersion = osRule.optString("version", "");
                String osArch = osRule.optString("arch", "");

                if (!osName.isEmpty()) {
                    if (osName.equals("windows") && !os.contains("win")) matches = false;
                    else if (osName.equals("linux") && !os.contains("nux")) matches = false;
                    else if ((osName.equals("osx") || osName.equals("mac")) && !os.contains("mac")) matches = false;
                }

                if (matches && !osVersion.isEmpty()) {
                    if (!System.getProperty("os.version").matches(osVersion)) matches = false;
                }

                if (matches && !osArch.isEmpty()) {
                    if (!System.getProperty("os.arch").equals(osArch)) matches = false;
                }
            }

            if (rule.has("features")) {
                
                
                matches = false;
            }

            if (action.equals("allow")) {
                if (matches) allow = true;
            } else if (action.equals("disallow")) {
                if (matches) allow = false;
            }
        }

        return allow;
    }
}
