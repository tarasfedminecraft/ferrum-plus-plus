package io.coconut.ferrum.v8;

import io.coconut.ferrum.Launcher;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Ferrum for Java 8 era Minecraft...");
        try {
            Launcher launcher = new Launcher();
            launcher.setUsername("FerrumUser8");
            launcher.setVersion("1.12.2"); 
            launcher.setMemory(2, 4);
            launcher.launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
