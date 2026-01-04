package io.coconut.ferrum.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Fetcher {
    public static JSONObject fetchJSON(String urlStr) throws IOException, JSONException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("User-Agent", "Ferrum/1.0");
        
        try (InputStream is = new BufferedInputStream(conn.getInputStream())) {
            return new JSONObject(new JSONTokener(new InputStreamReader(is, StandardCharsets.UTF_8)));
        } finally {
            conn.disconnect();
        }
    }
}
