package io.coconut.ferrum.utils;

public class Formatter {
    public static String formatMin(int min) {
        return "-Xms" + min + "G";
    }

    public static String formatMax(int max) {
        return "-Xmx" + max + "G";
    }
}
