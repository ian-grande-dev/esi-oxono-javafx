package be.esi.dev.oxono.command;

import java.util.Stack;

/**
 * Manages commands that can be undone and redone.
 * This class keeps track of actions so players can undo and redo them.
 */
public class CommandManager {
    // Stack to store commands that can be undone
    private final Stack<Command> undoStack;
    // Stack to store commands that can be redone
    private final Stack<Command> redoStack;

    /**
     * Creates a new command manager.
     */
    public CommandManager() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    /**
     * Adds a new command to the manager.
     * @param command the command to add
     */
    public void addCommand(Command command) {
        undoStack.push(command);
        // Clear redo stack when new command is added
        redoStack.clear();
    }

    /**
     * Checks if there are commands that can be undone.
     * @return true if undo is possible, false otherwise
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Checks if there are commands that can be redone.
     * @return true if redo is possible, false otherwise
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Undoes the last command.
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    /**
     * Redoes the last undone command.
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.redo();
            undoStack.push(command);
        }
    }
}
