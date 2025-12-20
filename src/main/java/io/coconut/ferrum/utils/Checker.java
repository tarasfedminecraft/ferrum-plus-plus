package io.coconut.ferrum.utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class Checker {
    public static boolean isVersionValid(String versionStr) {
        String r = "\\d+\\.\\d+\\.\\d+";
        return versionStr.matches(r);
    }

    public static boolean isByRules(JSONArray rules) {
        String os = System.getProperty("os.name").toLowerCase();

        for (int i = 0; i < rules.length(); i++) {
            JSONObject rule = rules.getJSONObject(i);
            String action = rule.getString("action");

            if (rule.has("os")) {
                JSONObject osRule = rule.getJSONObject("os");
                String osName = osRule.optString("name", "");

                boolean matches = false;
                if (osName.equals("windows") && os.contains("win")) matches = true;
                if (osName.equals("linux") && os.contains("nux")) matches = true;
                if (osName.equals("osx") && os.contains("mac")) matches = true;

                if (action.equals("allow") && !matches) return false;
                if (action.equals("disallow") && matches) return false;
            }
        }

        return true;
    }
}
