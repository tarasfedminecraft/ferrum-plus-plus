<img width="597" height="598" alt="image" src="https://github.com/user-attachments/assets/a5cfcb1e-d022-48f5-9c1c-bc698a4b9ea8" />



**Ferrum++** is an improved fork of the **Ferrum** library — a simple Java library for creating a Minecraft launcher.

The project is based on the original Ferrum, with the goal of further development, optimization, and future feature expansion.

---

## Features

- Simple API for launching Minecraft
- Minecraft version selection support
- Configurable minimum and maximum RAM
- Easy integration into Java projects
- Solid base for future extensions (authentication, assets, UI, etc.)

---

## Usage

```java
import io.coconut.ferrum.*;

public class Main {
    public static void main(String[] args) throws Exception {
        /*
        Arguments:
        1: Username
        2: Minecraft Version
        3: Min RAM (GB)
        4: Max RAM (GB)
        */
        Launcher launcher = new Launcher("Player", "1.12.2", 1, 2);
        launcher.launch(); // Launches the game
    }
}
````

---

## Building

```bash
git clone https://github.com/YOUR-USERNAME/ferrum-plus-plus.git
cd ferrum-plus-plus
mvn clean package
```

After building, the `.jar` file will be located in the **target** directory with a name similar to:

```
Ferrum++-1.0.1-ALPHA-jar-with-dependencies.jar
```

⚠️ **Important:** make sure to use the JAR file that contains `jar-with-dependencies` in its name.

---

## Original Project

This project is a fork of:

* **Ferrum** — [https://github.com/PlusMarden17/ferrum](https://github.com/PlusMarden17/ferrum)

All credit for the original work goes to the original author ❤️

---

## License

This project is distributed under the same license as the original Ferrum.
