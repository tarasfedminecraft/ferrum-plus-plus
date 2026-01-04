package io.coconut.ferrum.v17;

import io.coconut.ferrum.Launcher;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Ferrum for Java 17 era Minecraft...");
        try {
            Launcher launcher = new Launcher();
            launcher.setUsername("FerrumUser17");
            launcher.setVersion("1.18.2"); 
            launcher.setMemory(2, 4);
            launcher.launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
