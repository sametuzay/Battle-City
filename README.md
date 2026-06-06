# Battle City (Tank 1990) - Java Remake

A retro-style Battle City remake implemented in Java.

## Features
- **Power-ups & Shields:** Features 6 iconic power-ups (Shield, Freeze, Shovel, Star, Grenade, and 1UP) along with spawn protection shields.
- **Map Editor:** Built-in Map Editor to design, save, and load custom battlefields.
- **High Scores:** Score tracking system that saves names and points locally, displaying the top 10 players.
- **Vanilla Java & No External Libraries:** Developed using pure Java SDK, adhering strictly to Thread-based game loops and Anonymous Inner Classes instead of Swing Timers or Lambdas.

## Controls
- **W, A, S, D or Arrow Keys:** Move player tank (Up, Left, Down, Right)
- **SPACE:** Fire bullet
- **P:** Pause / Resume the game

## Requirements
- Java Development Kit (JDK) 8 or higher.

## How to Run
1. Compile the source code:
   ```bash
   javac -d bin src/*.java
   ```
2. Run the game:
   ```bash
   java -cp bin Main
   ```
   *(Ensure you run the command from the root project directory so that map text files and resource images are correctly resolved).*