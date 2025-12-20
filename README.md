<p align="center">
    <img width="100" height="100" alt="image" src="https://github.com/user-attachments/assets/5d487abd-e5c0-4cca-b17c-1fe35462820f" />
</p>

# Ferrum

Ferum - simple library that allows you to create a Minecraft launcher in Java

# Usage

```java
import io.coconut.ferrum.*;

public class Main {
  public static void main(String[] args) {
    /*
    Arguments:
    1: Username
    2: Version
    3: Min RAM
    4: Max RAM
    */
    Launcher launcher = new Launcher("Player", "1.12.2", 1, 2);
    launcher.launch(); // Method to launch the game
  }
}
```
