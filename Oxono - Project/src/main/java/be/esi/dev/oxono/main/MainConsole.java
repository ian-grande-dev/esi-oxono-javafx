package be.esi.dev.oxono.main;

import be.esi.dev.oxono.controller.console.ConsoleController;

/**
 * Starting point for the console version of the game.
 * Run this class to play the game in text mode.
 */
public class MainConsole {

    /**
     * Starts the console version of the OXONO game.
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // Create and start the console controller
        ConsoleController controller = new ConsoleController();
        controller.run();
    }
}