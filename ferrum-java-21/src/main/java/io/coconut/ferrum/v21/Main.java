package io.coconut.ferrum.v21;

import io.coconut.ferrum.Launcher;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Ferrum for Java 21 era Minecraft...");
        try {
            Launcher launcher = new Launcher();
            launcher.setUsername("FerrumUser21");
            launcher.setVersion("1.21.1"); 
            launcher.setMemory(2, 4);
            launcher.launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
