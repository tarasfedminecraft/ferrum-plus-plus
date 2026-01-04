package io.coconut.ferrum.v25;

import io.coconut.ferrum.Launcher;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Ferrum for Java 25 era Minecraft (Future)...");
        try {
            Launcher launcher = new Launcher();
            launcher.setUsername("FerrumUser25");
            launcher.setVersion("1.21.1"); 
            launcher.setMemory(4, 8);
            launcher.launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
