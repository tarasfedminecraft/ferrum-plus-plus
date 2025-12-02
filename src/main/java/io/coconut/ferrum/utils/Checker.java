package io.coconut.ferrum.utils;

public class Checker {
    public static boolean isVersionValid(String versionStr) {
        String r = "\\d+\\.\\d+\\.\\d+";
        return versionStr.matches(r);
    }
}
