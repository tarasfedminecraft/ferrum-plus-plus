package io.coconut.ferrum.v6;

import io.coconut.ferrum.Launcher;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Ferrum for Java 6 era Minecraft...");
        try {
            Launcher launcher = new Launcher();
            launcher.setUsername("FerrumUser6");
            launcher.setVersion("b1.7.3"); 
            launcher.setMemory(1, 1);
            launcher.launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
